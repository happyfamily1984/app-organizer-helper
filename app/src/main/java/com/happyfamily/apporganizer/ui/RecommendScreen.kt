package com.happyfamily.apporganizer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyfamily.apporganizer.R
import com.happyfamily.apporganizer.data.FolderPlan

@Composable
fun RecommendScreen(s: AppState) {
    val plan = s.plan
    ScreenBg {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            TopBar(
                title = { TitleText("추천 폴더 ", "${s.activeFolders.size}개") },
                image = R.drawable.hdr_folder,
                onBack = { s.back() },
            ) {
                Row(
                    Modifier.clip(RoundedCornerShape(50)).clickable { s.dialog = Dlg.ConfirmReRecommend }.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("다시 추천", fontSize = 15.sp, color = C.sub)
                    RowGap(4)
                    Icon(Icons.Filled.Refresh, null, tint = C.ink, modifier = Modifier.size(22.dp))
                }
            }
            if (plan == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("추천을 만들어 주세요", color = C.sub) }
                return@Column
            }
            val shown = s.activeFolders
            val total = shown.sumOf { it.apps.size }
            val exCount = s.excluded.count { it in s.appMap }
            Box(Modifier.weight(1f)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(span = { GridItemSpan(2) }) {
                        Column {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Chip("${total}개 앱 정리", C.green, C.greenSoft)
                                Chip("제외 $exCount", C.orange, C.orangeSoft, onClick = { s.go(Screen.AppList(ListMode.EXCLUDED)) })
                                Chip("미분류 ${plan.unassigned.size}", C.sub, C.graySoft, onClick = { s.go(Screen.AppList(ListMode.UNASSIGNED)) })
                            }
                            if (!plan.fromHomeScan) {
                                Gap(10)
                                Text(
                                    if (s.accessOn) "설치된 모든 앱 기준 추천이에요. 설정에서 '흩어진 앱만 추천'을 켤 수 있어요."
                                    else "설치된 모든 앱 기준 추천이에요. 접근성을 켜면 ${s.surfaceName}에서 폴더 밖에 있는 앱만 골라, 기존 폴더에 먼저 넣도록 추천해요.",
                                    fontSize = 13.sp, color = C.sub
                                )
                            }
                        }
                    }
                    if (plan.fromHomeScan && shown.any { it.existing }) item(span = { GridItemSpan(2) }) {
                        Text("기존 폴더에 넣기", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = C.ink, modifier = Modifier.padding(top = 4.dp))
                    }
                    items(shown.filter { it.existing }, key = { it.id }) { f -> FolderCard(s, f) }
                    if (shown.any { it.existing } && shown.any { !it.existing }) item(span = { GridItemSpan(2) }) {
                        Text("새 폴더 만들기", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = C.ink, modifier = Modifier.padding(top = 8.dp))
                    }
                    items(shown.filter { !it.existing }, key = { it.id }) { f -> FolderCard(s, f) }
                    item {
                        Box(
                            Modifier.fillMaxWidth().heightIn(min = 150.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .border(2.dp, Color(0xFFBFD9D0), RoundedCornerShape(22.dp))
                                .clickable { s.dialog = Dlg.NewFolder(null) },
                            contentAlignment = Alignment.Center
                        ) { Text("+ 폴더 추가", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = C.green) }
                    }
                }
                Box(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, C.bgBottom, C.bgBottom)))
                        .navigationBarsPadding()
                        .padding(start = 18.dp, end = 18.dp, top = 24.dp, bottom = 14.dp)
                ) {
                    GradientButton("${s.surfaceName}에 적용하기", Icons.Filled.CleaningServices, enabled = shown.any { it.apps.size >= (if (it.existing) 1 else 2) }) {
                        s.requestApply()
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderCard(s: AppState, f: FolderPlan) {
    Card(Modifier.fillMaxWidth().clickable { s.go(Screen.EditFolder(f.id)) }, pad = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(C.folderColor(f.colorIndex)))
            RowGap(8)
            Text(f.name, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = C.ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            if (f.existing) Text("기존", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = C.green,
                modifier = Modifier.clip(RoundedCornerShape(50)).background(C.greenSoft).padding(horizontal = 8.dp, vertical = 2.dp))
            else Icon(Icons.Outlined.Sell, "편집", tint = Color(0xFF8C9792), modifier = Modifier.size(20.dp))
        }
        Gap(10)
        val shown = f.apps.take(6)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(80.dp)) {
            shown.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { p -> val a = s.appMap[p]; AppIcon(a?.icon, a?.label ?: "?", 36.dp) }
                }
            }
            if (shown.isEmpty()) Text("앱을 옮겨 넣으세요", fontSize = 13.sp, color = C.sub)
        }
        Gap(8)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Groups, null, tint = C.green, modifier = Modifier.size(18.dp))
            RowGap(6)
            Text(if (f.existing) "${f.apps.size}개 넣기" else "${f.apps.size}개" + if (f.apps.size > 6) " · +${f.apps.size - 6}" else "", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = C.ink)
            Spacer(Modifier.weight(1f))
            Text("›", fontSize = 22.sp, color = C.sub)
        }
        if (!f.existing && f.apps.size == 1) Text("앱 1개는 폴더를 만들 수 없어요", fontSize = 11.sp, color = C.orange)
    }
}
