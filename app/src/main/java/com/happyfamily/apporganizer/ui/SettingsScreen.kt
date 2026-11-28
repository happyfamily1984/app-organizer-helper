package com.happyfamily.apporganizer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyfamily.apporganizer.R
import com.happyfamily.apporganizer.service.Bus

@Composable
fun SettingsScreen(s: AppState, openAccess: () -> Unit, askNotify: () -> Unit) {
    val st = s.settings
    ScreenBg {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            TopBar(title = { TitleText("설정", size = 28) }, image = R.drawable.hdr_gear, onBack = { s.back() })
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                SectionTitle("자동 정리")
                Gap(10)
                Card(pad = 16.dp) {
                    ListRow(
                        { IconTile(Icons.Filled.EditCalendar, C.green, C.greenSoft, 54.dp) },
                        "접근성 권한", "아이콘을 대신 옮겨요", onClick = openAccess
                    ) {
                        if (s.accessOn) Chip("켜짐", C.green, C.greenSoft, dot = true, trailing = true, onClick = openAccess)
                        else Chip("꺼짐", C.orange, C.orangeSoft, dot = true, trailing = true, onClick = openAccess)
                    }
                }
                Gap(12)
                Card(pad = 16.dp) {
                    ListRow({ IconTile(Icons.Filled.Apps, Color(0xFF7A5AF0), Color(0xFFEDE7FF), 54.dp) }, "정리할 화면", "앱스 화면은 위로 밀면 나오는 앱 목록이에요")
                    Segmented(listOf("앱스 화면", "홈 화면"), st.target) { s.saveSettings(st.copy(target = it)); s.lastScan = null; s.store.lastScan = null }
                }
                Gap(12)
                Card(pad = 16.dp) {
                    ListRow({ IconTile(Icons.Filled.Speed, C.green, C.greenSoft, 54.dp) }, "정리 속도", "느릴수록 실패가 적어요")
                    Segmented(listOf("빠름", "보통", "천천히"), st.speed) { s.saveSettings(st.copy(speed = it)) }
                }
                Gap(26)
                SectionTitle("정리 방식")
                Gap(10)
                Card(pad = 16.dp) {
                    ListRow(
                        { IconTile(Icons.Filled.VerifiedUser, Color(0xFF2F7BF0), Color(0xFFDDEBFF)) },
                        "흩어진 앱만 추천", "폴더 밖 앱만 기존 폴더에 넣거나 새로 묶기"
                    ) { GreenSwitch(st.homeOnly) { s.saveSettings(st.copy(homeOnly = it)) } }
                    Divider()
                    ListRow(
                        { IconTile(Icons.Filled.Inventory2, C.green, C.greenSoft) },
                        "미분류 앱은 '기타' 폴더로", "꺼두면 미분류 앱은 그대로 둬요"
                    ) { GreenSwitch(st.etcFolder) { s.saveSettings(st.copy(etcFolder = it)) } }
                    Divider()
                    ListRow(
                        { IconTile(Icons.Filled.Layers, C.orange, C.orangeSoft) },
                        "폴더 최소 앱 수", "이보다 적으면 폴더를 안 만듦",
                        onClick = { s.dialog = Dlg.MinApps }
                    ) {
                        Text("${st.minApps}개 ›", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = C.ink)
                    }
                }
                Gap(26)
                SectionTitle("기타")
                Gap(10)
                Card(pad = 16.dp) {
                    ListRow(
                        { IconTile(Icons.Filled.Schedule, Color(0xFF4F6475), Color(0xFFE6ECF1)) },
                        "새 앱 설치 시 알림", "접근성이 켜져 있을 때 알려드려요"
                    ) {
                        GreenSwitch(st.notifyNewApps) {
                            s.saveSettings(st.copy(notifyNewApps = it)); if (it) askNotify()
                        }
                    }
                    Divider()
                    ListRow(
                        { IconTile(Icons.Filled.Bolt, Color(0xFF2F7BF0), Color(0xFFDDEBFF)) },
                        "화면 구조 기록 (문제 해결용)",
                        "정리가 잘 안 될 때 기록을 만들어 보내주세요",
                        onClick = {
                            val svc = Bus.service
                            if (svc == null) s.dialog = Dlg.NeedAccess
                            else { s.busyText = "홈 화면 구조를 기록하고 있어요"; svc.startDump() }
                        }
                    ) { Text("›", fontSize = 22.sp, color = C.sub) }
                }
                Gap(18)
                Text(
                    "모든 정보는 이 휴대폰 안에만 저장되고, 어디로도 보내지 않아요.",
                    fontSize = 13.sp, color = C.sub, modifier = Modifier.padding(horizontal = 4.dp)
                )
                Gap(28)
            }
        }
    }
}
