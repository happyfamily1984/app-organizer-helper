package com.happyfamily.apporganizer.service

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.happyfamily.apporganizer.MainActivity
import com.happyfamily.apporganizer.data.AppRepository
import com.happyfamily.apporganizer.data.FolderPlan
import com.happyfamily.apporganizer.data.RunRecord
import com.happyfamily.apporganizer.data.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File

class OrganizerService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var job: Job? = null
    @Volatile private var stopRequested = false
    private var overlay: View? = null
    private var overlayText: TextView? = null
    private var overlayBar: ProgressBar? = null

    val busy get() = job?.isActive == true

    override fun onServiceConnected() {
        super.onServiceConnected()
        Bus.service = this
        Bus.serviceOn.value = true
        val f = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED); addDataScheme("package")
        }
        ContextCompat.registerReceiver(this, pkgReceiver, f, ContextCompat.RECEIVER_EXPORTED)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onDestroy() {
        Bus.service = null
        Bus.serviceOn.value = false
        runCatching { unregisterReceiver(pkgReceiver) }
        hideOverlay()
        scope.cancel()
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Bus.service = null
        Bus.serviceOn.value = false
        return super.onUnbind(intent)
    }

    private fun driver(): LauncherDriver {
        val st = Store(this).settings
        return LauncherDriver(this, AppRepository.launcherPackage(this), st.delayFactor, { stopRequested }) {
            Log.d("Organizer", it)
        }
    }

    private fun labels(): Map<String, String> =
        AppRepository.loadApps(this).associate { it.pkg to it.label }

    // ───────── 작업 ─────────

    fun startScan() {
        if (busy) return
        stopRequested = false
        job = scope.launch {
            showOverlay("홈 화면 분석 중")
            try {
                val r = driver().scan(labels())
                Store(this@OrganizerService).lastScan = r
                Bus.events.tryEmit(Bus.Event.ScanDone(r))
            } catch (e: LauncherDriver.CancelledException) {
                Bus.events.tryEmit(Bus.Event.Failed("분석을 중지했어요"))
            } catch (e: Exception) {
                Bus.events.tryEmit(Bus.Event.Failed("분석 중 문제가 생겼어요: ${e.message}"))
            } finally {
                finishJob()
            }
        }
    }

    fun startApply(folders: List<FolderPlan>) {
        if (busy) return
        stopRequested = false
        job = scope.launch {
            showOverlay("홈 화면 정리 준비 중")
            val names = labels()
            var made = 0; var moved = 0
            val failed = ArrayList<String>()
            var method = ""
            try {
                val d = driver()
                folders.forEachIndexed { i, f ->
                    val apps = LinkedHashMap<String, String>()
                    f.apps.forEach { p -> names[p]?.let { apps[p] = it } }
                    if (apps.size < 2) { failed += "${f.name}: 앱이 2개 미만"; return@forEachIndexed }
                    updateOverlay("${f.name} 폴더 만드는 중 (${i + 1}/${folders.size})", i, folders.size)
                    val r = d.makeFolder(f.name, apps) { step ->
                        updateOverlay("${f.name} · $step (${i + 1}/${folders.size})", i, folders.size)
                    }
                    method = r.method
                    if (r.ok) { made++; moved += r.moved } else failed += "${f.name}: 폴더를 만들지 못함"
                    if (r.missing.isNotEmpty() && r.ok) failed += "${f.name}: 홈 화면에서 못 찾음 - ${r.missing.joinToString()}"
                }
                d.goHome()
                val rec = RunRecord(System.currentTimeMillis(), made, moved, failed, method)
                Store(this@OrganizerService).lastRun = rec
                Bus.events.tryEmit(Bus.Event.ApplyDone(rec))
            } catch (e: LauncherDriver.CancelledException) {
                val rec = RunRecord(System.currentTimeMillis(), made, moved, failed + "중간에 중지함", method)
                Store(this@OrganizerService).lastRun = rec
                Bus.events.tryEmit(Bus.Event.ApplyDone(rec))
            } catch (e: Exception) {
                Bus.events.tryEmit(Bus.Event.Failed("정리 중 문제가 생겼어요: ${e.message}"))
            } finally {
                finishJob()
            }
        }
    }

    fun startDump() {
        if (busy) return
        stopRequested = false
        job = scope.launch {
            showOverlay("홈 화면 구조 기록 중")
            try {
                val text = driver().dumpFlow()
                File(filesDir, "home_dump.txt").writeText(text)
                Bus.events.tryEmit(Bus.Event.DumpDone(text))
            } catch (e: Exception) {
                Bus.events.tryEmit(Bus.Event.Failed("기록 중 문제가 생겼어요: ${e.message}"))
            } finally {
                finishJob()
            }
        }
    }

    fun stop() { stopRequested = true }

    private fun finishJob() {
        hideOverlay()
        Bus.progress.value = null
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        )
    }

    // ───────── 진행 표시 (화면 위쪽에 작게) ─────────

    private fun dp(v: Int) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    private fun showOverlay(text: String) {
        Bus.progress.value = Bus.Progress(text, "", 0, 0)
        if (overlay != null) { updateOverlay(text, 0, 0); return }
        val wm = getSystemService(WindowManager::class.java)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(10), dp(10), dp(10))
            background = GradientDrawable().apply {
                cornerRadius = dp(18).toFloat(); setColor(Color.parseColor("#F21D2621"))
            }
        }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val tv = TextView(this).apply {
            setTextColor(Color.WHITE); textSize = 13f; maxLines = 2; this.text = text
        }
        val bar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            isIndeterminate = true
            progressTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#3FD28A"))
        }
        col.addView(tv)
        col.addView(bar, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(6)).apply { topMargin = dp(6) })
        val stop = TextView(this).apply {
            this.text = "중지"; setTextColor(Color.parseColor("#1D2621")); textSize = 14f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(16), dp(8), dp(16), dp(8))
            background = GradientDrawable().apply { cornerRadius = dp(14).toFloat(); setColor(Color.WHITE) }
            setOnClickListener { stop(); this.text = "중지 중…" }
        }
        box.addView(col, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        box.addView(stop, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginStart = dp(10) })
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
            x = 0; y = dp(4)
            horizontalMargin = 0.03f
            if (Build.VERSION.SDK_INT >= 28) layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        runCatching { wm.addView(box, lp) }
        overlay = box; overlayText = tv; overlayBar = bar
    }

    private fun updateOverlay(text: String, done: Int, total: Int) {
        Bus.progress.value = Bus.Progress(text, "", done, total)
        overlayText?.text = text
        overlayBar?.let {
            if (total > 0) { it.isIndeterminate = false; it.max = total; it.progress = done } else it.isIndeterminate = true
        }
    }

    private fun hideOverlay() {
        overlay?.let { v -> runCatching { getSystemService(WindowManager::class.java).removeView(v) } }
        overlay = null; overlayText = null; overlayBar = null
    }

    // ───────── 새 앱 설치 알림 ─────────

    private val pkgReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return
            if (!Store(ctx).settings.notifyNewApps) return
            val pkg = intent.data?.schemeSpecificPart ?: return
            val label = runCatching {
                ctx.packageManager.getApplicationLabel(ctx.packageManager.getApplicationInfo(pkg, 0)).toString()
            }.getOrDefault(pkg)
            notifyNew(label)
        }
    }

    private fun notifyNew(label: String) {
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(NotificationChannel("new_apps", "새 앱 설치 알림", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val pi = PendingIntent.getActivity(
            this, 1, Intent(this, MainActivity::class.java).putExtra("open", "new"),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val n = NotificationCompat.Builder(this, "new_apps")
            .setSmallIcon(android.R.drawable.ic_menu_sort_by_size)
            .setContentTitle("새 앱이 설치됐어요: $label")
            .setContentText("눌러서 어느 폴더에 넣을지 정리해 보세요")
            .setContentIntent(pi).setAutoCancel(true).build()
        runCatching { nm.notify(label.hashCode(), n) }
    }
}
