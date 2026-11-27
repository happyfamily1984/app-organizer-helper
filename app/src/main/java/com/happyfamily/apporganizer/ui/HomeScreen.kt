package com.happyfamily.apporganizer.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyfamily.apporganizer.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(s: AppState, openAccess: () -> Unit) {
    ScreenBg {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 18.dp)
        ) {
            // 헤더
            Row(Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.hdr_broom), null, Modifier.size(56.dp))
                RowGap(12)
                Text("앱정리 ", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, color = C.ink)
                Text("도우미", fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, color = C.green)
                Icon(Icons.Filled.AutoAwesome, null, tint = Color(0xFFF7C325), modifier = Modifier.size(20.dp).offset(y = (-12).dp))
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(46.dp).clip(CircleShape).clickable { s.go(Screen.SettingsScreen) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Settings, "설정", tint = Color(0xFF27343A), modifier = Modifier.size(32.dp))
                }
            }

            // 상태 카드
            val scan = s.lastScan
            Card(pad = 20.dp) {
                Box(Modifier.fillMaxWidth()) {
                    Image(
                        painterResource(R.drawable.illust_phone), null,
                        Modifier.size(122.dp).align(Alignment.TopEnd).offset(x = 10.dp, y = (-4).dp)
                    )
                    Column {
                        if (s.accessOn) Chip("자동 정리 켜짐 · 접근성", C.green, C.greenSoft, dot = true, trailing = true, onClick = openAccess)
                        else Chip("자동 정리 꺼짐 · 켜기", C.orange, C.orangeSoft, dot = true, trailing = true, onClick = { s.dialog = Dlg.NeedAccess })
                        Gap(14)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(if (s.loading) "…" else "${s.apps.size}", fontSize = 46.sp, fontWeight = FontWeight.Black, color = C.ink)
                                Text("설치된 앱", fontSize = 14.sp, color = C.sub)
                            }
                            Box(Modifier.padding(horizontal = 18.dp).width(1.dp).height(64.dp).background(C.line))
                            Column(Modifier.clickable { s.scanOnly() }) {
                                Text(scan?.looseIcons?.toString() ?: "—", fontSize = 46.sp, fontWeight = FontWeight.Black, color = C.orange)
                                Text("폴더 밖에 흩어진 앱", fontSize = 14.sp, color = C.sub)
                            }
                        }
                    }
                }
                Gap(14)
                val total = s.apps.size.coerceAtLeast(1)
                val ratio = if (scan == null) 0f else (1f - scan.looseIcons.toFloat() / total).coerceIn(0f, 1f)
                Box(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFE6ECEA))) {
                    Box(
                        Modifier.fillMaxWidth(ratio).height(12.dp).clip(RoundedCornerShape(6.dp))
                            .background(Brush.horizontalGradient(listOf(Color(0xFF0B8F4F), Color(0xFF1DB866))))
                    )
                }
                Gap(10)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { s.scanOnly() }) {
                    Icon(Icons.Filled.Eco, null, tint = C.green, modifier = Modifier.size(20.dp))
                    RowGap(10)
                    if (scan == null) {
                        Text("눌러서 ${s.surfaceName}을 분석하면 정리율을 보여드려요", fontSize = 14.sp, color = C.sub)
                    } else {
                        Text((if (scan.surface == 0) "앱스 화면" else "홈 화면") + " ${scan.pages}페이지 · 정리된 비율 ", fontSize = 14.sp, color = C.ink)
                        Text("${(ratio * 100).toInt()}%", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = C.green)
                    }
                }
            }
            Gap(22)
            GradientButton("추천 폴더 만들기", Icons.Filled.CleaningServices, enabled = !s.loading) { s.startRecommend() }
            Gap(26)
            SectionTitle("내 정리 규칙") {
                Box(
                    Modifier.clip(RoundedCornerShape(50)).background(C.graySoft).clickable { s.go(Screen.SettingsScreen) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) { Text("더보기 ›", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = C.ink) }
            }
            Gap(10)
            Card(pad = 16.dp) {
                val ex = s.excluded.filter { it in s.appMap }
                ListRow(
                    { IconTile(Icons.Filled.Block, Color.White, Color(0xFF6E7F8C)) },
                    "정리에서 제외한 앱",
                    if (ex.isEmpty()) "없음" else ex.take(3).joinToString { s.label(it) } + if (ex.size > 3) " 외 ${ex.size - 3}개" else "",
                    onClick = { s.go(Screen.AppList(ListMode.EXCLUDED)) }
                ) { CountBadge(ex.size) }
                Divider()
                val run = s.lastRun
                ListRow(
                    { IconTile(Icons.Filled.Apps, Color.White, Color(0xFF11995A)) },
                    "지난 정리",
                    if (run == null) "아직 정리한 적이 없어요"
                    else SimpleDateFormat("M월 d일", Locale.KOREA).format(Date(run.time)) + " · 폴더 ${run.foldersMade}개 · 앱 ${run.appsMoved}개",
                    onClick = { run?.let { s.dialog = Dlg.Result(it) } }
                ) {
                    if (run != null) Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.clip(RoundedCornerShape(50)).background(C.greenSoft).padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text("결과 보기", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = C.green)
                        }
                        RowGap(4); Text("›", fontSize = 22.sp, color = C.sub)
                    }
                }
                Divider()
                val nw = s.newApps
                ListRow(
                    { IconTile(Icons.Filled.Add, Color.White, Color(0xFF2F7BF0)) },
                    "새로 설치된 앱",
                    if (nw.isEmpty()) "새 앱이 없어요" else nw.take(2).joinToString { it.label } + " · 아직 폴더 없음",
                    onClick = { s.go(Screen.AppList(ListMode.NEW)) }
                ) { CountBadge(nw.size) }
            }
            Gap(20)
            // 로봇 배너
            Box(
                Modifier.fillMaxWidth().height(118.dp).clip(RoundedCornerShape(24.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFFBDEFE0), Color(0xFFE4F7F3), Color(0xFFC3EEE6))))
            ) {
                Image(painterResource(R.drawable.robot), null, Modifier.height(118.dp).width(128.dp).align(Alignment.CenterStart))
                Column(Modifier.align(Alignment.CenterStart).padding(start = 128.dp, end = 10.dp)) {
                    Text("스마트한 정리로", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = C.ink)
                    Row {
                        Text("더 깔끔한 홈 화면", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = C.green)
                        Text("을 만들어보세요!", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = C.ink)
                    }
                }
            }
            Gap(24)
        }
    }
}
