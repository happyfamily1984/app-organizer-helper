package com.happyfamily.apporganizer.ui

import android.content.ComponentName
import android.content.Context
import android.provider.Settings as SysSettings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.happyfamily.apporganizer.data.AppItem
import com.happyfamily.apporganizer.data.AppRepository
import com.happyfamily.apporganizer.data.Classifier
import com.happyfamily.apporganizer.data.FolderPlan
import com.happyfamily.apporganizer.data.Plan
import com.happyfamily.apporganizer.data.RunRecord
import com.happyfamily.apporganizer.data.Settings
import com.happyfamily.apporganizer.data.Store
import com.happyfamily.apporganizer.service.Bus
import com.happyfamily.apporganizer.service.OrganizerService

sealed class Screen {
    data object Home : Screen()
    data object Recommend : Screen()
    data class EditFolder(val id: String) : Screen()
    data object SettingsScreen : Screen()
    data class AppList(val mode: ListMode) : Screen()
}

enum class ListMode(val title: String) { EXCLUDED("정리에서 제외한 앱"), UNASSIGNED("미분류 앱"), NEW("새로 설치된 앱") }

sealed class Dlg {
    data object Exit : Dlg()
    data object NeedAccess : Dlg()
    data object ConfirmApply : Dlg()
    data object ConfirmReRecommend : Dlg()
    data class Result(val rec: RunRecord) : Dlg()
    data class Message(val title: String, val body: String) : Dlg()
    data class NewFolder(val pkg: String?) : Dlg()
    data object MinApps : Dlg()
    data class DeleteFolder(val id: String) : Dlg()
    data class AddApps(val folderId: String) : Dlg()
}

/** 이동 대상 */
sealed class Target {
    data class ToFolder(val id: String) : Target()
    data object Unassign : Target()
    data object Exclude : Target()
}

class AppState(private val ctx: Context) {
    val store = Store(ctx)

    var loading by mutableStateOf(true)
    var apps by mutableStateOf<List<AppItem>>(emptyList())
        private set
    var appMap by mutableStateOf<Map<String, AppItem>>(emptyMap())
        private set
    var excluded by mutableStateOf(store.excluded)
    var plan by mutableStateOf(store.plan)
    var settings by mutableStateOf(store.settings)
    var lastScan by mutableStateOf(store.lastScan)
    var lastRun by mutableStateOf(store.lastRun)
    var known by mutableStateOf(store.knownPkgs)
    var accessOn by mutableStateOf(false)

    val stack = mutableStateListOf<Screen>(Screen.Home)
    var dialog by mutableStateOf<Dlg?>(null)
    /** 이동 시트를 띄울 앱 */
    var sheetPkg by mutableStateOf<String?>(null)
    /** 분석이 끝나면 추천을 만들고 이동할지 */
    var recommendAfterScan = false
    var busyText by mutableStateOf<String?>(null)

    val screen get() = stack.last()
    fun go(s: Screen) { stack.add(s) }
    fun back() { if (stack.size > 1) stack.removeAt(stack.lastIndex) }

    fun setLoaded(list: List<AppItem>) {
        apps = list
        appMap = list.associateBy { it.pkg }
        if (!store.initialized) {
            excluded = AppRepository.defaultExcluded(ctx) + excluded
            store.excluded = excluded
            known = list.map { it.pkg }.toSet()
            store.knownPkgs = known
            store.initialized = true
        }
        // 지워진 앱 정리
        plan?.let { p ->
            val ok = appMap.keys
            val np = p.copy(
                folders = p.folders.map { f -> f.copy(apps = f.apps.filter { it in ok }) },
                unassigned = p.unassigned.filter { it in ok }
            )
            if (np != p) updatePlan(np)
        }
        loading = false
    }

    fun refreshAccess() {
        val name = ComponentName(ctx, OrganizerService::class.java).flattenToString()
        val enabled = SysSettings.Secure.getString(ctx.contentResolver, SysSettings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: ""
        accessOn = Bus.service != null || enabled.split(':').any { it.equals(name, true) }
    }

    val newApps: List<AppItem> get() = apps.filter { it.pkg !in known }
    fun markAllKnown() { known = apps.map { it.pkg }.toSet(); store.knownPkgs = known }

    fun label(pkg: String) = appMap[pkg]?.label ?: pkg

    // ───────── 추천 ─────────

    val surfaceName get() = if (settings.target == 0) "앱스 화면" else "홈 화면"

    fun startRecommend() {
        val svc = Bus.service
        if (settings.homeOnly && svc != null) {
            recommendAfterScan = true
            busyText = "${surfaceName}을 분석하고 있어요"
            svc.startScan()
        } else buildPlan(null)
    }

    fun scanOnly() {
        val svc = Bus.service ?: run { dialog = Dlg.NeedAccess; return }
        recommendAfterScan = false
        busyText = "${surfaceName}을 분석하고 있어요"
        svc.startScan()
    }

    fun buildPlan(homePkgs: List<String>?) {
        val src = if (homePkgs != null) apps.filter { it.pkg in homePkgs.toSet() } else apps
        val scan = lastScan
        val p = if (homePkgs != null && scan != null && scan.folders.isNotEmpty())
            Classifier.recommendWithExisting(src, scan.folders, appMap, excluded, settings)
        else Classifier.recommend(src, excluded, settings, homePkgs != null)
        updatePlan(p)
        if (screen != Screen.Recommend) go(Screen.Recommend)
    }

    fun updatePlan(p: Plan?) { plan = p; store.plan = p }

    fun folder(id: String) = plan?.folders?.firstOrNull { it.id == id }

    fun updateFolder(f: FolderPlan) {
        val p = plan ?: return
        updatePlan(p.copy(folders = p.folders.map { if (it.id == f.id) f else it }))
    }

    private fun nextId(p: Plan) = "u" + (System.currentTimeMillis() % 1_000_000_000) + p.folders.size

    fun addFolder(name: String, pkg: String?): String? {
        val p = plan ?: Plan(emptyList(), emptyList(), false, System.currentTimeMillis())
        val id = nextId(p)
        val f = FolderPlan(id, name.trim(), Classifier.colorForName(name.trim()), emptyList())
        updatePlan(p.copy(folders = p.folders + f))
        if (pkg != null) move(pkg, Target.ToFolder(id))
        return id
    }

    fun deleteFolder(id: String) {
        val p = plan ?: return
        val f = p.folders.firstOrNull { it.id == id } ?: return
        updatePlan(p.copy(folders = p.folders.filter { it.id != id }, unassigned = p.unassigned + f.apps))
    }

    /** 실제로 적용될 폴더 (기존 폴더는 넣을 앱이 있을 때만) */
    val activeFolders get() = plan?.folders?.filter { !it.existing || it.apps.isNotEmpty() }.orEmpty()

    /** 앱 하나를 다른 폴더/미분류/제외로 옮김 */
    fun move(pkg: String, t: Target) {
        val p = plan ?: Plan(emptyList(), emptyList(), false, System.currentTimeMillis())
        var folders = p.folders.map { it.copy(apps = it.apps - pkg) }
        var un = p.unassigned - pkg
        when (t) {
            is Target.ToFolder -> folders = folders.map { if (it.id == t.id) it.copy(apps = it.apps + pkg) else it }
            Target.Unassign -> un = un + pkg
            Target.Exclude -> setExcluded(pkg, true, touchPlan = false)
        }
        updatePlan(p.copy(folders = folders, unassigned = un))
    }

    fun setExcluded(pkg: String, ex: Boolean, touchPlan: Boolean = true) {
        excluded = if (ex) excluded + pkg else excluded - pkg
        store.excluded = excluded
        if (!touchPlan) return
        val p = plan ?: return
        if (ex) updatePlan(p.copy(folders = p.folders.map { it.copy(apps = it.apps - pkg) }, unassigned = p.unassigned - pkg))
        else if (p.folders.none { pkg in it.apps } && pkg !in p.unassigned) updatePlan(p.copy(unassigned = p.unassigned + pkg))
    }

    fun saveSettings(s: Settings) { settings = s; store.settings = s }

    // ───────── 적용 ─────────

    fun requestApply() {
        if (Bus.service == null) { dialog = Dlg.NeedAccess; return }
        dialog = Dlg.ConfirmApply
    }

    fun apply() {
        val svc = Bus.service ?: run { dialog = Dlg.NeedAccess; return }
        val folders = plan?.folders?.filter { it.apps.size >= (if (it.existing) 1 else 2) } ?: return
        busyText = "${surfaceName}을 정리하고 있어요"
        svc.startApply(folders)
    }

    fun onEvent(e: Bus.Event) {
        busyText = null
        when (e) {
            is Bus.Event.ScanDone -> {
                lastScan = e.result
                if (recommendAfterScan) {
                    recommendAfterScan = false
                    if (e.result.homePkgs.isEmpty()) {
                        dialog = Dlg.Message(
                            "${surfaceName}에서 흩어진 앱을 찾지 못했어요",
                            "설치된 모든 앱을 기준으로 추천할게요.\n휴대폰에 따라 아이콘을 읽지 못할 수 있어요. 설정 > '화면 구조 기록'을 보내주시면 맞춰 고칠 수 있어요."
                        )
                        buildPlan(null)
                    } else buildPlan(e.result.homePkgs)
                }
            }
            is Bus.Event.ApplyDone -> {
                lastRun = e.record
                markAllKnown()
                dialog = Dlg.Result(e.record)
            }
            is Bus.Event.DumpDone -> {}
            is Bus.Event.Failed -> { recommendAfterScan = false; dialog = Dlg.Message("알림", e.message) }
        }
    }
}
