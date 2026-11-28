package com.happyfamily.apporganizer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
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
import com.happyfamily.apporganizer.data.Classifier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 폴더 밖에 흩어진 앱만 보여주고, 자동/직접으로 폴더를 정해 옮기는 화면 (화면을 다시 읽지 않음) */
@Composable
fun LooseScreen(s: AppState) {
    val loose = s.loosePkgs()
    val plan = s.plan
    val assigned = loose.count { p -> plan?.folders?.any { p in it.apps } == true }
    ScreenBg {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            TopBar(title = { TitleText("흩어진 앱 ", "${loose.size}개") }, image = R.drawable.hdr_folder, onBack = { s.back() })
            Box(Modifier.weight(1f)) {
                LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp)) {
                    item {
                        Segmented(listOf("자동 추천", "직접 고르기"), if (s.looseAuto) 0 else 1) { s.setLooseMode(it == 0) }
                        Gap(10)
                        val scan = s.validScan
                        Text(
                            (if (scan != null) "지난 분석 " + SimpleDateFormat("M월 d일 HH:mm", Locale.KOREA).format(Date(scan.time)) + " 기준이에요. " else "") +
                                "앱을 누르면 넣을 폴더를 바꿀 수 있어요.",
                            fontSize = 13.sp, color = C.sub, modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Gap(12)
                    }
                    if (loose.isEmpty()) item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("흩어진 앱이 없어요. 깔끔해요!", color = C.sub, fontSize = 16.sp)
                        }
                    }
                    items(loose, key = { it }) { pkg ->
                        val a = s.appMap[pkg]
                        val f = plan?.folders?.firstOrNull { pkg in it.apps }
                        Card(Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { s.sheetPkg = pkg }, pad = 12.dp, radius = 18.dp) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AppIcon(a?.icon, a?.label ?: pkg, 46.dp)
                                RowGap(14)
                                Column(Modifier.weight(1f)) {
                                    Text(a?.label ?: pkg, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = C.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        if (a != null && a.installedAt > (s.validScan?.time ?: Long.MAX_VALUE)) "새로 설치됨"
                                        else a?.let { Classifier.reason(it) } ?: "",
                                        fontSize = 13.sp, color = C.sub
                                    )
                                }
                                if (f != null) Row(
                                    Modifier.clip(RoundedCornerShape(50)).background(C.folderColor(f.colorIndex).copy(alpha = 0.14f))
                                        .padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(Modifier.size(8.dp).clip(CircleShape).background(C.folderColor(f.colorIndex)))
                                    RowGap(6)
                                    Text(f.name + if (!f.existing) " (새)" else "", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = C.ink,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(end = 2.dp))
                                    Text("›", fontSize = 17.sp, color = C.sub)
                                } else Box(
                                    Modifier.clip(RoundedCornerShape(50)).background(C.graySoft).padding(horizontal = 12.dp, vertical = 7.dp)
                                ) { Text("폴더 선택 ›", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = C.sub) }
                            }
                        }
                    }
                }
                Box(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, C.bgBottom, C.bgBottom)))
                        .navigationBarsPadding()
                        .padding(start = 18.dp, end = 18.dp, top = 24.dp, bottom = 14.dp)
                ) {
                    GradientButton(if (assigned > 0) "앱 ${assigned}개 옮기기" else "옮길 폴더를 골라 주세요", Icons.Filled.CleaningServices, enabled = assigned > 0) {
                        s.requestApply()
                    }
                }
            }
        }
    }
}
