package com.happyfamily.apporganizer

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.happyfamily.apporganizer.data.AppRepository
import com.happyfamily.apporganizer.service.Bus
import com.happyfamily.apporganizer.ui.AppIcon
import com.happyfamily.apporganizer.ui.AppListScreen
import com.happyfamily.apporganizer.ui.AppState
import com.happyfamily.apporganizer.ui.C
import com.happyfamily.apporganizer.ui.Dlg
import com.happyfamily.apporganizer.ui.FolderEditScreen
import com.happyfamily.apporganizer.ui.HomeScreen
import com.happyfamily.apporganizer.ui.ListMode
import com.happyfamily.apporganizer.ui.LooseScreen
import com.happyfamily.apporganizer.ui.MoveSheetContent
import com.happyfamily.apporganizer.ui.RecommendScreen
import com.happyfamily.apporganizer.ui.Screen
import com.happyfamily.apporganizer.ui.SettingsScreen
import com.happyfamily.apporganizer.ui.Target
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var state: AppState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        state = AppState(applicationContext)
        handleIntent(intent)

        lifecycleScope.launch {
            Bus.events.collect { e ->
                state.onEvent(e)
                if (e is Bus.Event.DumpDone) shareText(e.text)
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                state.refreshAccess()
                val list = withContext(Dispatchers.Default) { AppRepository.loadApps(applicationContext) }
                state.setLoaded(list)
            }
        }
        lifecycleScope.launch { Bus.serviceOn.collect { state.refreshAccess() } }

        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = C.green, surface = Color.White, background = C.bgBottom)) {
                Root(state)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(i: Intent?) {
        if (i?.getStringExtra("open") == "new") {
            i.removeExtra("open")
            state.go(Screen.AppList(ListMode.NEW))
        }
    }

    fun openAccessibility() {
        runCatching { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
    }

    fun openAppInfo() {
        runCatching {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName")))
        }
    }

    fun askNotify() {
        if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7)
    }

    private fun shareText(text: String) {
        val body = if (text.length > 120_000) text.take(120_000) + "\n...(잘림)" else text
        val send = Intent(Intent.ACTION_SEND).setType("text/plain")
            .putExtra(Intent.EXTRA_SUBJECT, "앱정리 도우미 홈 화면 구조 기록")
            .putExtra(Intent.EXTRA_TEXT, body)
        runCatching { startActivity(Intent.createChooser(send, "기록 보내기")) }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Root(s: AppState) {
        // 뒤로가기: 열린 창 → 이전 화면 → (홈) 종료 확인
        BackHandler {
            when {
                s.dialog != null -> s.dialog = null
                s.sheetPkg != null -> s.sheetPkg = null
                s.stack.size > 1 -> s.back()
                else -> s.dialog = Dlg.Exit
            }
        }
        Box(Modifier.fillMaxSize()) {
            AnimatedContent(s.screen, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "screen") { sc ->
                when (sc) {
                    Screen.Home -> HomeScreen(s, ::openAccessibility)
                    Screen.Recommend -> RecommendScreen(s)
                    Screen.Loose -> LooseScreen(s)
                    is Screen.EditFolder -> FolderEditScreen(s, sc.id)
                    Screen.SettingsScreen -> SettingsScreen(s, ::openAccessibility, ::askNotify)
                    is Screen.AppList -> AppListScreen(s, sc.mode)
                }
            }
            s.busyText?.let { t ->
                Box(Modifier.fillMaxSize().background(Color(0x66000000)).clickable(enabled = true) {}, contentAlignment = Alignment.Center) {
                    Column(
                        Modifier.padding(32.dp).background(Color.White, RoundedCornerShape(24.dp)).padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = C.green)
                        Text(t, modifier = Modifier.padding(top = 16.dp), fontWeight = FontWeight.Bold, color = C.ink)
                        Text("잠시 ${s.surfaceName}으로 이동해요. 화면을 만지지 마세요.", fontSize = 13.sp, color = C.sub, modifier = Modifier.padding(top = 6.dp))
                        TextButton(onClick = { Bus.service?.stop(); s.busyText = null }) { Text("중지", color = C.red) }
                    }
                }
            }
        }

        // 앱 이동 시트: 직접 만든 패널 (끌어내리기 제스처와 목록 스크롤이 충돌하지 않도록)
        AnimatedVisibility(s.sheetPkg != null, enter = fadeIn(), exit = fadeOut()) {
            Box(Modifier.fillMaxSize().background(Color(0x66000000)).clickable(
                interactionSource = remember { MutableInteractionSource() }, indication = null
            ) { s.sheetPkg = null })
        }
        AnimatedVisibility(
            s.sheetPkg != null, modifier = Modifier.fillMaxSize(),
            enter = slideInVertically { it }, exit = slideOutVertically { it },
        ) {
            var last by remember { mutableStateOf<String?>(null) }
            s.sheetPkg?.let { last = it }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                Box(
                    Modifier.fillMaxWidth().fillMaxHeight(0.85f).wrapContentHeight(Alignment.Bottom)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(Color.White)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                        .navigationBarsPadding()
                ) {
                    last?.let { pkg -> MoveSheetContent(s, pkg) { s.sheetPkg = null } }
                }
            }
        }

        Dialogs(s)
    }

    @Composable
    private fun Dialogs(s: AppState) {
        val d = s.dialog ?: return
        val close = { s.dialog = null }
        when (d) {
            Dlg.Exit -> AlertDialog(
                onDismissRequest = close, title = { Text("앱을 종료하겠습니까?") },
                confirmButton = { TextButton({ close(); finish() }) { Text("예") } },
                dismissButton = { TextButton(close) { Text("아니오") } },
                containerColor = Color.White,
            )
            Dlg.NeedScan -> AlertDialog(
                onDismissRequest = close, title = { Text("먼저 한 번 분석이 필요해요") },
                text = { Text("${s.surfaceName}의 폴더 구성과 흩어진 앱을 한 번 읽어야 해요. 그 뒤로는 다시 읽지 않고 바로 정리할 수 있어요.") },
                confirmButton = { TextButton({ close(); if (s.accessOn) s.startRecommend() else s.dialog = Dlg.NeedAccess }) { Text("지금 분석") } },
                dismissButton = { TextButton(close) { Text("취소") } },
                containerColor = Color.White,
            )
            Dlg.NeedAccess -> AlertDialog(
                onDismissRequest = close, title = { Text("접근성 권한을 켜 주세요") },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Text("앱 아이콘을 대신 폴더에 옮기려면 접근성 권한이 필요해요.\n")
                        Text("1. 아래 '접근성 설정 열기'를 누르세요")
                        Text("2. 설치된 앱(또는 다운로드한 앱) → 앱정리 도우미")
                        Text("3. 스위치를 켜고 '허용'을 누르세요\n")
                        Text("스위치가 회색으로 눌리지 않으면: '앱 정보 열기' → 오른쪽 위 ⋮ → '제한된 설정 허용'을 누른 뒤 다시 켜 주세요.", fontSize = 13.sp, color = C.sub)
                    }
                },
                confirmButton = { TextButton({ close(); openAccessibility() }) { Text("접근성 설정 열기") } },
                dismissButton = { TextButton({ close(); openAppInfo() }) { Text("앱 정보 열기") } },
                containerColor = Color.White,
            )
            Dlg.ConfirmApply -> {
                val fs = s.plan?.folders?.filter { it.apps.size >= (if (it.existing) 1 else 2) }.orEmpty()
                val ex = fs.filter { it.existing }; val nw = fs.filter { !it.existing }
                AlertDialog(
                    onDismissRequest = close, title = { Text("${s.surfaceName}에 적용할까요?") },
                    text = {
                        Text((if (ex.isNotEmpty()) "기존 폴더 ${ex.size}개에 앱 ${ex.sumOf { it.apps.size }}개를 넣고\n" else "") +
                            (if (nw.isNotEmpty()) "새 폴더 ${nw.size}개(앱 ${nw.sumOf { it.apps.size }}개)를 만들어요.\n\n" else "\n") +
                            "• 정리하는 동안 휴대폰이 스스로 움직여요. 화면을 만지지 마세요.\n" +
                            "• 위쪽의 '중지'를 누르면 언제든 멈춰요.\n" +
                            "• 이미 폴더 안에 있는 앱과 제외한 앱은 건드리지 않아요.")
                    },
                    confirmButton = { TextButton({ close(); s.apply() }) { Text("정리 시작") } },
                    dismissButton = { TextButton(close) { Text("취소") } },
                    containerColor = Color.White,
                )
            }
            Dlg.ConfirmReRecommend -> AlertDialog(
                onDismissRequest = close, title = { Text("다시 추천할까요?") },
                text = { Text("지금까지 수정한 폴더 구성은 사라지고 새로 추천해요. 제외한 앱 설정은 유지돼요.") },
                confirmButton = { TextButton({ close(); s.startRecommend() }) { Text("다시 추천") } },
                dismissButton = { TextButton(close) { Text("취소") } },
                containerColor = Color.White,
            )
            is Dlg.Result -> AlertDialog(
                onDismissRequest = close,
                title = { Text(if (d.rec.appsMoved > 0) "정리를 마쳤어요" else "정리하지 못했어요") },
                text = {
                    Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                        Text("앱 ${d.rec.appsMoved}개를 폴더에 넣었어요." + if (d.rec.foldersMade > 0) " (새 폴더 ${d.rec.foldersMade}개)" else "", fontWeight = FontWeight.Bold)
                        if (d.rec.failed.isNotEmpty()) {
                            Text("\n확인이 필요한 항목", color = C.orange, fontWeight = FontWeight.Bold)
                            d.rec.failed.forEach { Text("• $it", fontSize = 14.sp) }
                        }
                        if (d.rec.appsMoved == 0) {
                            Text("\n설정 > '화면 구조 기록'을 만들어 보내주시면 이 휴대폰에 맞게 고칠 수 있어요.", fontSize = 13.sp, color = C.sub)
                        }
                    }
                },
                confirmButton = { TextButton(close) { Text("확인") } },
                containerColor = Color.White,
            )
            is Dlg.Message -> AlertDialog(
                onDismissRequest = close, title = { Text(d.title) }, text = { Text(d.body) },
                confirmButton = { TextButton(close) { Text("확인") } },
                containerColor = Color.White,
            )
            is Dlg.NewFolder -> {
                var name by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = close, title = { Text("새 폴더 만들기") },
                    text = { OutlinedTextField(name, { name = it.take(20) }, singleLine = true, placeholder = { Text("폴더 이름") }) },
                    confirmButton = {
                        TextButton({ if (name.isNotBlank()) { close(); s.addFolder(name, d.pkg) } }) { Text("만들기") }
                    },
                    dismissButton = { TextButton(close) { Text("취소") } },
                    containerColor = Color.White,
                )
            }
            Dlg.MinApps -> AlertDialog(
                onDismissRequest = close, title = { Text("폴더 최소 앱 수") },
                text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (2..5).forEach { n ->
                            val on = s.settings.minApps == n
                            Box(
                                Modifier.background(if (on) C.green else C.graySoft, RoundedCornerShape(14.dp))
                                    .clickable { s.saveSettings(s.settings.copy(minApps = n)); close() }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) { Text("${n}개", color = if (on) Color.White else C.ink, fontWeight = FontWeight.Bold) }
                        }
                    }
                },
                confirmButton = { TextButton(close) { Text("닫기") } },
                containerColor = Color.White,
            )
            is Dlg.DeleteFolder -> AlertDialog(
                onDismissRequest = close, title = { Text("폴더를 해체할까요?") },
                text = { Text("이 폴더의 앱은 미분류로 옮겨져요. 휴대폰 화면에는 아직 아무 변화가 없어요.") },
                confirmButton = { TextButton({ close(); s.deleteFolder(d.id); s.back() }) { Text("해체") } },
                dismissButton = { TextButton(close) { Text("취소") } },
                containerColor = Color.White,
            )
            is Dlg.AddApps -> {
                val cands = s.apps.filter { a -> a.pkg !in s.excluded && s.plan?.folders?.none { a.pkg in it.apps } != false }
                AlertDialog(
                    onDismissRequest = close, title = { Text("앱 추가") },
                    text = {
                        if (cands.isEmpty()) Text("추가할 수 있는 앱이 없어요. (다른 폴더의 앱은 그 앱을 눌러 옮겨 주세요)")
                        else LazyColumn(Modifier.heightIn(max = 420.dp)) {
                            items(cands, key = { it.pkg }) { a ->
                                Row(
                                    Modifier.fillMaxWidth().clickable { s.move(a.pkg, Target.ToFolder(d.folderId)) }.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppIcon(a.icon, a.label, 38.dp)
                                    Text(a.label, modifier = Modifier.padding(start = 12.dp).weight(1f), fontSize = 16.sp)
                                    Text("+", color = C.green, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    },
                    confirmButton = { TextButton(close) { Text("완료") } },
                    containerColor = Color.White,
                )
            }
        }
    }
}
