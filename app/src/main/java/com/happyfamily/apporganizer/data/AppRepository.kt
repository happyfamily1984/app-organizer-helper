package com.happyfamily.apporganizer.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.provider.MediaStore
import android.provider.Settings as SysSettings
import android.telecom.TelecomManager
import android.provider.Telephony
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap

object AppRepository {

    /** 런처에 보이는 모든 앱 (이 앱 자신 제외) */
    fun loadApps(ctx: Context): List<AppItem> {
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val list = pm.queryIntentActivities(intent, 0)
        val seen = HashSet<String>()
        val out = ArrayList<AppItem>()
        for (ri in list) {
            val pkg = ri.activityInfo.packageName
            if (pkg == ctx.packageName || !seen.add(pkg)) continue
            val label = ri.loadLabel(pm).toString().trim()
            val ai = ri.activityInfo.applicationInfo
            val installed = try {
                pm.getPackageInfo(pkg, 0).firstInstallTime
            } catch (_: Exception) { 0L }
            out += AppItem(pkg, label, ai.category, iconOf(ri.loadIcon(pm)), installed)
        }
        return out.sortedBy { it.label }
    }

    private fun iconOf(d: android.graphics.drawable.Drawable?) = try {
        if (d == null) null else {
            val size = 96
            val bmp: Bitmap = createBitmap(size, size)
            val c = Canvas(bmp)
            d.setBounds(0, 0, size, size)
            d.draw(c)
            bmp.asImageBitmap()
        }
    } catch (_: Exception) { null }

    /** 처음 실행할 때 기본으로 정리에서 뺄 앱: 전화, 문자, 카메라 */
    fun defaultExcluded(ctx: Context): Set<String> {
        val out = HashSet<String>()
        try {
            val tm = ctx.getSystemService(TelecomManager::class.java)
            tm?.defaultDialerPackage?.let(out::add)
        } catch (_: Exception) {}
        try { Telephony.Sms.getDefaultSmsPackage(ctx)?.let(out::add) } catch (_: Exception) {}
        try {
            ctx.packageManager.resolveActivity(Intent(MediaStore.ACTION_IMAGE_CAPTURE), PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName?.takeIf { it != "android" }?.let(out::add)
        } catch (_: Exception) {}
        try {
            ctx.packageManager.resolveActivity(Intent(SysSettings.ACTION_SETTINGS), PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName?.let(out::add)
        } catch (_: Exception) {}
        return out
    }

    /** 현재 기본 홈 앱(런처) 패키지 */
    fun launcherPackage(ctx: Context): String? = try {
        val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        ctx.packageManager.resolveActivity(i, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
    } catch (_: Exception) { null }

    fun launcherName(pkg: String?): String = when {
        pkg == null -> "알 수 없음"
        pkg.startsWith("com.sec.android.app.launcher") -> "One UI 홈"
        pkg.contains("nexuslauncher") || pkg.contains("google.android.apps.nexus") -> "픽셀 런처"
        pkg.contains("launcher3") -> "기본 런처"
        else -> pkg.substringAfterLast('.')
    }
}
