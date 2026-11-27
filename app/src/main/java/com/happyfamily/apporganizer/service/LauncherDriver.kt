package com.happyfamily.apporganizer.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction
import com.happyfamily.apporganizer.data.ExistingFolder
import com.happyfamily.apporganizer.data.ScanResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 홈 화면(런처)을 접근성으로 조작하는 엔진.
 *  - 아이콘 찾기: 화면에 보이는 노드의 글자/설명이 앱 이름과 같은 것
 *  - 폴더 만들기 1순위: 아이콘 길게 누르기 → "선택" → 여러 앱 체크 → "폴더 만들기" (One UI)
 *  - 2순위: 아이콘을 끌어서 다른 아이콘 위에 놓기 (같은 페이지 안에서만)
 */
class LauncherDriver(
    private val svc: AccessibilityService,
    private val launcherPkg: String?,
    private val factor: Float,
    /** true = 앱스 화면, false = 홈 화면 */
    private val appsScreen: Boolean,
    private val cancelled: () -> Boolean,
    private val log: (String) -> Unit,
) {
    class CancelledException : Exception()

    data class Icon(val node: AccessibilityNodeInfo, val name: String, val bounds: Rect, val isFolder: Boolean)

    private val screen: Rect = run {
        val wm = svc.getSystemService(WindowManager::class.java)
        if (Build.VERSION.SDK_INT >= 30) wm.maximumWindowMetrics.bounds
        else svc.resources.displayMetrics.let { Rect(0, 0, it.widthPixels, it.heightPixels) }
    }
    private val W get() = screen.width()
    private val H get() = screen.height()

    private val SELECT_WORDS = listOf("선택", "항목 선택", "여러 항목 선택", "Select", "Select items")
    private val ADD_APPS_WORDS = listOf("앱 추가", "앱추가", "Add apps", "Add app", "추가")
    private val DONE_WORDS = listOf("완료", "추가", "확인", "Done", "Add", "OK")
    private val SEARCH_WORDS = listOf("검색", "Search", "앱 검색", "Search apps")
    private val CREATE_FOLDER_WORDS = listOf("폴더 만들기", "폴더 생성", "폴더로 묶기", "Create folder", "Add to folder")

    // ───────── 기본 도구 ─────────

    suspend fun pause(ms: Long) {
        if (cancelled()) throw CancelledException()
        delay((ms * factor).toLong())
        if (cancelled()) throw CancelledException()
    }

    fun norm(s: String) = s.lowercase().replace(Regex("[\\s\\u200b\\u00a0]"), "")

    private fun roots(): List<AccessibilityNodeInfo> {
        val out = ArrayList<AccessibilityNodeInfo>()
        try {
            for (w in svc.windows) {
                val r = w.root ?: continue
                out += r
            }
        } catch (_: Exception) {}
        if (out.isEmpty()) svc.rootInActiveWindow?.let(out::add)
        return out
    }

    private fun launcherRoots(): List<AccessibilityNodeInfo> {
        val all = roots()
        val l = all.filter { launcherPkg == null || it.packageName?.toString() == launcherPkg }
        return l.ifEmpty { listOfNotNull(svc.rootInActiveWindow) }
    }

    private fun flatten(root: AccessibilityNodeInfo, limit: Int = 4000): List<AccessibilityNodeInfo> {
        val out = ArrayList<AccessibilityNodeInfo>()
        val q = ArrayDeque<AccessibilityNodeInfo>()
        q.add(root)
        while (q.isNotEmpty() && out.size < limit) {
            val n = q.removeFirst()
            out += n
            for (i in 0 until n.childCount) n.getChild(i)?.let(q::add)
        }
        return out
    }

    private fun AccessibilityNodeInfo.rect() = Rect().also { getBoundsInScreen(it) }

    private fun AccessibilityNodeInfo.name(): String? {
        val t = text?.toString()?.trim()
        if (!t.isNullOrEmpty()) return t
        val d = contentDescription?.toString()?.trim()
        return if (d.isNullOrEmpty()) null else d
    }

    private fun inDock(n: AccessibilityNodeInfo): Boolean {
        var p: AccessibilityNodeInfo? = n
        var depth = 0
        while (p != null && depth < 8) {
            val id = p.viewIdResourceName?.lowercase() ?: ""
            if (id.contains("hotseat") || id.contains("dock")) return true
            p = p.parent; depth++
        }
        return false
    }

    private fun looksFolder(n: AccessibilityNodeInfo, name: String): Boolean {
        val d = (n.contentDescription?.toString() ?: "").lowercase()
        val cls = n.className?.toString()?.lowercase() ?: ""
        val id = n.viewIdResourceName?.lowercase() ?: ""
        return d.contains("폴더") || d.contains("folder") || cls.contains("folder") || id.contains("folder") ||
            name.lowercase().startsWith("폴더:") || name.lowercase().startsWith("folder:")
    }

    /** 현재 홈 화면 페이지에 보이는 아이콘들 (독 제외) */
    fun visibleIcons(): List<Icon> {
        val out = ArrayList<Icon>()
        for (root in launcherRoots()) {
            for (n in flatten(root)) {
                if (!n.isVisibleToUser) continue
                val name = n.name() ?: continue
                val r = n.rect()
                if (r.width() < W * 0.07 || r.width() > W * 0.45) continue
                if (r.height() < H * 0.03 || r.height() > H * 0.25) continue
                if (r.centerY() > H * 0.90 || r.centerY() < H * 0.04) continue
                if (inDock(n)) continue
                out += Icon(n, name, r, looksFolder(n, name))
            }
        }
        // 같은 자리의 부모/자식 중복 제거
        val dedup = LinkedHashMap<String, Icon>()
        for (ic in out) {
            val key = norm(ic.name) + "@" + (ic.bounds.centerX() / (W / 12 + 1)) + "," + (ic.bounds.centerY() / (H / 24 + 1))
            val prev = dedup[key]
            if (prev == null || (ic.node.isClickable && !prev.node.isClickable)) dedup[key] = ic
        }
        return dedup.values.toList()
    }

    /** 아이콘 이름이 앱 이름과 같은지 ("카카오톡, 새 알림 3개" 같은 설명도 허용) */
    fun matches(iconName: String, label: String): Boolean {
        val a = norm(iconName); val b = norm(label)
        if (a.isEmpty() || b.isEmpty()) return false
        return a == b || a.startsWith("$b,") || a.startsWith("$b(") || a.startsWith("$b.")
    }

    private fun findByWords(words: List<String>): AccessibilityNodeInfo? {
        val nodes = roots().flatMap { flatten(it) }.filter { it.isVisibleToUser }
        for (w in words) {
            val nw = norm(w)
            nodes.firstOrNull { n -> n.name()?.let { norm(it) == nw } == true }?.let { return it }
        }
        for (w in words) {
            val nw = norm(w)
            nodes.firstOrNull { n -> n.name()?.let { val x = norm(it); x.length <= nw.length + 4 && x.contains(nw) } == true }?.let { return it }
        }
        return null
    }

    private fun findEditText(): AccessibilityNodeInfo? =
        roots().flatMap { flatten(it) }.firstOrNull {
            it.isVisibleToUser && (it.className?.toString()?.contains("EditText") == true || it.isEditable)
        }

    private suspend fun gesture(g: GestureDescription): Boolean = suspendCancellableCoroutine { c ->
        val ok = svc.dispatchGesture(g, object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(d: GestureDescription?) { if (c.isActive) c.resume(true) }
            override fun onCancelled(d: GestureDescription?) { if (c.isActive) c.resume(false) }
        }, null)
        if (!ok && c.isActive) c.resume(false)
    }

    private suspend fun tap(x: Float, y: Float, hold: Long = 60) {
        val p = Path().apply { moveTo(x, y); lineTo(x + 1, y + 1) }
        gesture(GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(p, 0, hold)).build())
    }

    private suspend fun swipe(x1: Float, y1: Float, x2: Float, y2: Float, dur: Long = 320) {
        val p = Path().apply { moveTo(x1, y1); lineTo(x2, y2) }
        gesture(GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(p, 0, dur)).build())
    }

    /** 길게 누른 채로 끌어서 다른 위치에 놓기 */
    private suspend fun drag(from: Rect, to: Rect): Boolean {
        val fx = from.exactCenterX(); val fy = from.exactCenterY()
        val tx = to.exactCenterX(); val ty = to.exactCenterY()
        val hold = GestureDescription.StrokeDescription(Path().apply { moveTo(fx, fy); lineTo(fx + 1, fy) }, 0, (900 * factor).toLong(), true)
        if (!gesture(GestureDescription.Builder().addStroke(hold).build())) return false
        val move = hold.continueStroke(Path().apply { moveTo(fx + 1, fy); lineTo(tx, ty) }, 0, (700 * factor).toLong(), true)
        if (!gesture(GestureDescription.Builder().addStroke(move).build())) return false
        val drop = move.continueStroke(Path().apply { moveTo(tx, ty); lineTo(tx + 1, ty + 1) }, 0, (900 * factor).toLong(), false)
        return gesture(GestureDescription.Builder().addStroke(drop).build())
    }

    private suspend fun click(n: AccessibilityNodeInfo): Boolean {
        var p: AccessibilityNodeInfo? = n
        repeat(5) {
            val x = p ?: return@repeat
            if (x.isClickable && x.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            p = x.parent
        }
        val r = n.rect()
        tap(r.exactCenterX(), r.exactCenterY())
        return true
    }

    private suspend fun longClick(n: AccessibilityNodeInfo): Boolean {
        var p: AccessibilityNodeInfo? = n
        repeat(4) {
            val x = p ?: return@repeat
            if (x.isLongClickable && x.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)) return true
            p = x.parent
        }
        val r = n.rect()
        tap(r.exactCenterX(), r.exactCenterY(), hold = 900)
        return true
    }

    // ───────── 홈 화면 페이지 이동 ─────────

    suspend fun goHome() {
        svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        pause(800)
        svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        pause(900)
    }

    private fun signature(): String = visibleIcons().joinToString("|") { norm(it.name) + ":" + it.bounds.left / 20 }

    private fun scroller(): AccessibilityNodeInfo? =
        launcherRoots().flatMap { flatten(it) }
            .filter { it.isScrollable && it.isVisibleToUser }
            .maxByOrNull { val r = it.rect(); r.width().toLong() * r.height() }

    /** 한 페이지 넘기기. 실제로 바뀌었으면 true */
    private suspend fun turnPage(forward: Boolean): Boolean {
        val before = signature()
        val sc = scroller()
        val act = if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        val ok = sc?.performAction(act) ?: false
        if (!ok) {
            val y = H * 0.45f
            if (forward) swipe(W * 0.85f, y, W * 0.15f, y) else swipe(W * 0.15f, y, W * 0.85f, y)
        }
        pause(750)
        val after = signature()
        if (after.isEmpty() && before.isNotEmpty()) {
            // 아이콘 없는 페이지(-1 페이지/피드)로 넘어감 → 되돌아오기
            val sc2 = scroller()
            val back = if (forward) AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD else AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            if (sc2?.performAction(back) != true) {
                val y = H * 0.45f
                if (forward) swipe(W * 0.15f, y, W * 0.85f, y) else swipe(W * 0.85f, y, W * 0.15f, y)
            }
            pause(700)
            return false
        }
        return after != before
    }

    class SurfaceException(msg: String) : Exception(msg)

    private fun isAppsScreen(): Boolean {
        for (n in launcherRoots().flatMap { flatten(it) }) {
            if (!n.isVisibleToUser) continue
            val id = n.viewIdResourceName?.lowercase() ?: ""
            if (id.contains("apps_") || id.contains("all_apps") || id.contains("apps_view") || id.contains("appsview")) return true
            val nm = n.name()?.let { norm(it) } ?: continue
            if (SEARCH_WORDS.any { norm(it) == nm } && n.rect().centerY() > H * 0.6) return true
        }
        return false
    }

    /** 앱스 화면(또는 홈 화면)을 열기 */
    suspend fun openSurface() {
        goHome()
        if (!appsScreen) return
        repeat(3) {
            if (isAppsScreen()) return
            swipe(W / 2f, H * 0.72f, W / 2f, H * 0.18f, 260)
            pause(1000)
        }
        if (!isAppsScreen()) {
            log("앱스 화면 확인 실패 - 그래도 진행")
        }
    }

    suspend fun goFirstPage() {
        openSurface()
        repeat(12) { if (!turnPage(false)) return }
    }

    private suspend fun goToPage(n: Int) {
        goFirstPage()
        repeat(n) { turnPage(true) }
    }

    private fun currentPkg(): String? = svc.rootInActiveWindow?.packageName?.toString()

    /** 폴더 안이 열렸는지 (폴더 이름 칸 또는 '앱 추가' 버튼) */
    private fun folderOpen(): Boolean = findEditText() != null || findByWords(ADD_APPS_WORDS.take(4)) != null

    private fun folderNameMatches(iconName: String, folderName: String): Boolean {
        val f = norm(folderName)
        if (norm(iconName) == f) return true
        return iconName.split(',', ':', '\n').any { norm(it) == f }
    }

    // ───────── 홈 화면 분석 ─────────

    suspend fun scan(labels: Map<String, String>, onStep: (String) -> Unit = {}): ScanResult {
        goFirstPage()
        var pages = 0
        val found = LinkedHashSet<String>()
        val folders = ArrayList<ExistingFolder>()
        val seenFolders = HashSet<String>()
        while (pages < 15) {
            pages++
            val icons = visibleIcons()
            val pageLoose = HashSet<String>()
            val folderIcons = ArrayList<Icon>()
            for (ic in icons) {
                val pkg = pkgFor(ic.name, labels, found)
                if (!ic.isFolder && pkg != null) { found += pkg; pageLoose += pkg }
                else if (ic.isFolder || pkg == null) folderIcons += ic
            }
            log("페이지 $pages: 흩어진 앱 ${found.size}개, 폴더 후보 ${folderIcons.size}개")
            // 폴더 안 앱 읽기 (어느 폴더에 넣을지 판단하는 데 사용)
            for (fi in folderIcons) {
                val fname = fi.name.split(',').first().removePrefix("폴더:").trim()
                if (!seenFolders.add(norm(fname))) continue
                onStep("폴더 읽는 중: $fname")
                val cur = visibleIcons().firstOrNull { norm(it.name) == norm(fi.name) } ?: continue
                click(cur.node)
                pause(900)
                if (currentPkg() != null && launcherPkg != null && currentPkg() != launcherPkg) {
                    // 폴더가 아니라 앱이 열림 → 돌아오기
                    log("$fname 은(는) 폴더가 아님")
                    goToPage(pages - 1)
                    continue
                }
                if (!folderOpen() && !fi.isFolder) { continue }
                val inside = LinkedHashSet<String>()
                for (ic in visibleIcons()) {
                    val pk = labels.entries.firstOrNull { (p, l) -> p !in pageLoose && matches(ic.name, l) }?.key
                    if (pk != null) inside += pk
                }
                folders += ExistingFolder(fname, inside.toList())
                svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                pause(700)
                if (folderOpen()) { svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK); pause(600) }
                // 원래 페이지가 안 보이면 다시 찾아가기
                if (visibleIcons().none { norm(it.name) == norm(fi.name) }) goToPage(pages - 1)
            }
            if (!turnPage(true)) break
        }
        goHome()
        return ScanResult(System.currentTimeMillis(), pages, found.size, folders.size, found.toList(), launcherPkg ?: "",
            folders, if (appsScreen) 0 else 1)
    }

    /** 기존 폴더를 찾아 열고 '앱 추가'로 앱을 넣기 */
    suspend fun addToFolder(folderName: String, apps: LinkedHashMap<String, String>, onStep: (String) -> Unit): FolderResult {
        goFirstPage()
        var opened = false
        for (page in 0 until 15) {
            val ic = visibleIcons().firstOrNull { folderNameMatches(it.name, folderName) }
            if (ic != null) {
                onStep("$folderName 폴더 여는 중")
                click(ic.node); pause(1000)
                opened = true
                break
            }
            if (!turnPage(true)) break
        }
        if (!opened) return FolderResult(0, apps.values.toList(), false, "앱 추가")
        val add = findByWords(ADD_APPS_WORDS)
        if (add == null) {
            log("'앱 추가' 버튼을 찾지 못함")
            svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK); pause(600)
            return FolderResult(0, apps.values.toList(), false, "앱 추가")
        }
        click(add); pause(1200)
        val remaining = LinkedHashMap(apps)
        var picked = 0
        // 1) 목록을 내리면서 체크
        var guard = 0
        while (remaining.isNotEmpty() && guard < 30) {
            guard++
            var clicked = true
            while (clicked) {
                clicked = false
                val nodes = roots().flatMap { flatten(it) }.filter { it.isVisibleToUser }
                for ((pkg, label) in remaining.entries.toList()) {
                    val n = nodes.firstOrNull { nd -> nd.name()?.let { matches(it, label) } == true } ?: continue
                    onStep("$label 체크")
                    if (!(n.isChecked)) click(n)
                    pause(350)
                    remaining.remove(pkg); picked++; clicked = true
                    break
                }
            }
            if (remaining.isEmpty()) break
            val before = roots().flatMap { flatten(it) }.mapNotNull { it.name() }.joinToString("|").hashCode()
            val sc = roots().flatMap { flatten(it) }.filter { it.isScrollable && it.isVisibleToUser }
                .maxByOrNull { val r = it.rect(); r.width().toLong() * r.height() }
            if (sc?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) != true) {
                swipe(W / 2f, H * 0.75f, W / 2f, H * 0.35f, 350)
            }
            pause(700)
            val after = roots().flatMap { flatten(it) }.mapNotNull { it.name() }.joinToString("|").hashCode()
            if (after == before) break
        }
        // 2) 못 찾은 앱은 검색칸으로 찾기
        if (remaining.isNotEmpty()) {
            val search = findEditText()
            if (search != null) {
                for ((pkg, label) in remaining.entries.toList()) {
                    onStep("$label 검색")
                    val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, label) }
                    findEditText()?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
                    pause(900)
                    val n = roots().flatMap { flatten(it) }.firstOrNull { nd ->
                        nd.isVisibleToUser && !nd.isEditable && nd.name()?.let { matches(it, label) } == true
                    }
                    if (n != null) { if (!n.isChecked) click(n); pause(400); remaining.remove(pkg); picked++ }
                }
                val clear = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "") }
                findEditText()?.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, clear)
                pause(500)
            }
        }
        if (picked == 0) {
            svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK); pause(600)
            svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK); pause(600)
            return FolderResult(0, apps.values.toList(), false, "앱 추가")
        }
        onStep("완료 누르는 중")
        val done = findByWords(DONE_WORDS)
        if (done != null) click(done) else log("'완료' 버튼을 찾지 못함")
        pause(1100)
        // 폴더 닫기
        if (folderOpen()) { svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK); pause(700) }
        return FolderResult(picked, remaining.values.toList(), done != null, "앱 추가")
    }

    private fun pkgFor(iconName: String, labels: Map<String, String>, taken: Set<String>): String? {
        val hits = labels.filter { (pkg, label) -> pkg !in taken && matches(iconName, label) }
        return hits.keys.firstOrNull()
    }

    // ───────── 폴더 만들기 ─────────

    data class FolderResult(val moved: Int, val missing: List<String>, val ok: Boolean, val method: String)

    /**
     * @param apps pkg -> label (순서대로)
     */
    suspend fun makeFolder(name: String, apps: LinkedHashMap<String, String>, onStep: (String) -> Unit): FolderResult {
        goFirstPage()
        val a = trySelectMode(name, apps, onStep)
        if (a != null) return a
        log("선택 모드를 찾지 못해 끌어다 놓기로 시도")
        return tryDrag(name, apps, onStep)
    }

    private suspend fun trySelectMode(name: String, apps: LinkedHashMap<String, String>, onStep: (String) -> Unit): FolderResult? {
        val remaining = LinkedHashMap(apps)
        val picked = ArrayList<String>()
        var started = false
        var page = 0
        while (page < 15) {
            var progressed = true
            while (progressed) {
                progressed = false
                val icons = visibleIcons().filter { !it.isFolder }
                val hit = icons.firstNotNullOfOrNull { ic ->
                    remaining.entries.firstOrNull { matches(ic.name, it.value) }?.let { ic to it.key }
                } ?: break
                val (ic, pkg) = hit
                if (!started) {
                    onStep("${remaining[pkg]} 길게 누르는 중")
                    longClick(ic.node)
                    pause(900)
                    val sel = findByWords(SELECT_WORDS)
                    if (sel == null) {
                        svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                        pause(500)
                        return null
                    }
                    click(sel)
                    pause(900)
                    started = true
                    // 길게 누른 앱이 자동으로 체크되지 않았다면 직접 체크
                    val again = visibleIcons().firstOrNull { matches(it.name, remaining[pkg]!!) }
                    if (again != null && again.node.isCheckable && !again.node.isChecked) {
                        click(again.node); pause(400)
                    }
                } else {
                    onStep("${remaining[pkg]} 선택")
                    click(ic.node)
                    pause(450)
                }
                picked += pkg
                remaining.remove(pkg)
                progressed = true
            }
            if (remaining.isEmpty()) break
            if (!turnPage(true)) break
            page++
        }
        if (!started) return FolderResult(0, apps.values.toList(), false, "선택")
        if (picked.size < 2) {
            svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK); pause(600)
            return FolderResult(0, apps.values.toList(), false, "선택")
        }
        onStep("폴더 만들기")
        val create = findByWords(CREATE_FOLDER_WORDS)
        if (create == null) {
            log("'폴더 만들기' 버튼을 찾지 못함")
            svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK); pause(600)
            return FolderResult(0, apps.values.toList(), false, "선택")
        }
        click(create)
        pause(1300)
        rename(name)
        return FolderResult(picked.size, remaining.values.toList(), true, "선택")
    }

    private suspend fun tryDrag(name: String, apps: LinkedHashMap<String, String>, onStep: (String) -> Unit): FolderResult {
        val remaining = LinkedHashMap(apps)
        var moved = 0
        var target: Rect? = null
        var page = 0
        while (page < 15 && remaining.isNotEmpty()) {
            val onPage = visibleIcons().filter { !it.isFolder }.mapNotNull { ic ->
                remaining.entries.firstOrNull { matches(ic.name, it.value) }?.let { ic to it.key }
            }
            if (onPage.size >= 2 || (target != null && onPage.isNotEmpty())) {
                // 대상(폴더가 될 자리)은 이 페이지의 첫 앱
                val first = onPage.first()
                val tgt = first.first.bounds
                for ((ic, pkg) in onPage.drop(1)) {
                    onStep("${remaining[pkg]} 옮기는 중")
                    val cur = visibleIcons().firstOrNull { matches(it.name, remaining[pkg]!!) }?.bounds ?: ic.bounds
                    drag(cur, tgt)
                    pause(1200)
                    if (findEditText() != null) { // 폴더가 열렸으면 닫기
                        svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK); pause(700)
                    }
                    moved++
                    remaining.remove(pkg)
                }
                remaining.remove(first.second); moved++
                target = tgt
                // 이름 붙이기: 폴더 열기
                click(visibleIcons().firstOrNull { it.bounds.contains(tgt.centerX(), tgt.centerY()) }?.node ?: first.first.node)
                pause(1000)
                rename(name)
                break // 다른 페이지 앱은 끌어서 옮길 수 없음
            }
            if (!turnPage(true)) break
            page++
        }
        return FolderResult(if (moved >= 2) moved else 0, remaining.values.toList(), moved >= 2, "끌어놓기")
    }

    private suspend fun rename(name: String) {
        var edit = findEditText()
        if (edit == null) {
            // 이름 칸을 누르면 편집이 시작되는 런처
            findByWords(listOf("폴더 이름", "이름 없는 폴더", "Folder name", "Unnamed folder", "폴더"))?.let { click(it); pause(700) }
            edit = findEditText()
        }
        if (edit != null) {
            edit.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            val args = Bundle().apply { putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, name) }
            edit.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            pause(500)
            if (Build.VERSION.SDK_INT >= 30) edit.performAction(AccessibilityAction.ACTION_IME_ENTER.id)
            pause(500)
        } else log("폴더 이름 칸을 찾지 못함")
        // 키보드/폴더 닫기
        repeat(2) {
            if (findEditText() != null) { svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK); pause(600) }
        }
        svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        pause(800)
    }

    // ───────── 문제 해결용: 화면 구조 저장 ─────────

    fun dumpScreen(title: String): String {
        val sb = StringBuilder()
        sb.append("===== ").append(title).append(" (화면 ${W}x$H) =====\n")
        for (root in roots()) {
            sb.append("--- window pkg=").append(root.packageName).append('\n')
            dumpNode(root, 0, sb)
        }
        return sb.toString()
    }

    private fun dumpNode(n: AccessibilityNodeInfo, d: Int, sb: StringBuilder) {
        if (d > 30 || sb.length > 400_000) return
        val r = n.rect()
        sb.append("  ".repeat(d))
            .append(n.className?.toString()?.substringAfterLast('.'))
            .append(" t=").append(n.text).append(" d=").append(n.contentDescription)
            .append(" id=").append(n.viewIdResourceName?.substringAfter(":id/"))
            .append(" ").append(r.toShortString())
            .append(" [")
            .append(if (n.isClickable) "c" else "").append(if (n.isLongClickable) "L" else "")
            .append(if (n.isScrollable) "s" else "").append(if (n.isCheckable) "k" else "")
            .append(if (n.isChecked) "K" else "").append(if (n.isVisibleToUser) "v" else "").append("]\n")
        for (i in 0 until n.childCount) n.getChild(i)?.let { dumpNode(it, d + 1, sb) }
    }

    suspend fun dumpFlow(): String {
        val sb = StringBuilder()
        openSurface()
        sb.append("앱스 화면 인식: ").append(isAppsScreen()).append("\n")
        sb.append(dumpScreen(if (appsScreen) "앱스 화면 첫 페이지" else "홈 첫 화면"))
        val icons = visibleIcons()
        sb.append("\n인식된 아이콘 ${icons.size}개: ").append(icons.joinToString { (if (it.isFolder) "[폴더]" else "") + it.name }).append("\n\n")
        val folder = icons.firstOrNull { it.isFolder }
        if (folder != null) {
            click(folder.node); pause(1000)
            sb.append(dumpScreen("폴더 열기: ${folder.name}"))
            val add = findByWords(ADD_APPS_WORDS)
            if (add != null) {
                click(add); pause(1200)
                sb.append(dumpScreen("폴더 앱 추가 화면"))
                svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK); pause(700)
            } else sb.append("\n('앱 추가' 버튼을 찾지 못함)\n")
            svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK); pause(700)
            openSurface()
        } else sb.append("\n(폴더로 인식된 아이콘 없음)\n")
        val first = visibleIcons().firstOrNull { !it.isFolder }
        if (first != null) {
            longClick(first.node); pause(1000)
            sb.append(dumpScreen("아이콘 길게 누름: ${first.name}"))
            val sel = findByWords(SELECT_WORDS)
            if (sel != null) {
                click(sel); pause(1000)
                sb.append(dumpScreen("선택 모드"))
            } else sb.append("\n('선택' 메뉴를 찾지 못함)\n")
            svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK); pause(600)
            svc.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK); pause(600)
        }
        goHome()
        return sb.toString()
    }
}
