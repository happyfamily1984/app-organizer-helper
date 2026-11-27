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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happyfamily.apporganizer.data.Classifier

@Composable
fun AppListScreen(s: AppState, mode: ListMode) {
    var q by remember { mutableStateOf("") }
    ScreenBg {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            TopBar(title = { TitleText(mode.title, size = 22) }, image = null, onBack = { s.back() }) {
                if (mode == ListMode.NEW && s.newApps.isNotEmpty()) {
                    Text("모두 확인", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = C.green,
                        modifier = Modifier.clip(RoundedCornerShape(50)).clickable { s.markAllKnown() }.padding(10.dp))
                }
            }
            val hint = when (mode) {
                ListMode.EXCLUDED -> "켜 둔 앱은 추천과 자동 정리에서 빠지고 홈 화면에 그대로 남아요."
                ListMode.UNASSIGNED -> "어느 폴더에도 들어가지 않은 앱이에요. 눌러서 폴더를 정해 주세요."
                ListMode.NEW -> "지난 정리 이후 설치된 앱이에요. 눌러서 넣을 폴더를 정해 주세요."
            }
            Text(hint, fontSize = 14.sp, color = C.sub, modifier = Modifier.padding(horizontal = 20.dp))
            Gap(10)
            if (mode == ListMode.EXCLUDED) {
                Row(
                    Modifier.padding(horizontal = 16.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(C.card).padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Search, null, tint = C.sub)
                    RowGap(8)
                    Box(Modifier.weight(1f)) {
                        if (q.isEmpty()) Text("앱 이름 검색", color = C.sub, fontSize = 16.sp)
                        BasicTextField(q, { q = it }, singleLine = true, textStyle = TextStyle(fontSize = 16.sp, color = C.ink), modifier = Modifier.fillMaxWidth())
                    }
                }
                Gap(10)
            }
            val list = when (mode) {
                ListMode.EXCLUDED -> s.apps.sortedBy { if (it.pkg in s.excluded) 0 else 1 }
                    .filter { q.isBlank() || it.label.contains(q.trim(), true) }
                ListMode.UNASSIGNED -> (s.plan?.unassigned ?: emptyList()).mapNotNull { s.appMap[it] }
                ListMode.NEW -> s.newApps
            }
            if (list.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("앱이 없어요", color = C.sub, fontSize = 16.sp) }
                return@Column
            }
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
                item { Gap(2) }
                items(list, key = { it.pkg }) { a ->
                    Card(Modifier.fillMaxWidth().padding(bottom = 8.dp), pad = 12.dp, radius = 18.dp) {
                        val folder = s.plan?.folders?.firstOrNull { a.pkg in it.apps }
                        ListRow(
                            { AppIcon(a.icon, a.label, 46.dp) },
                            a.label,
                            when {
                                a.pkg in s.excluded -> "정리에서 제외됨"
                                folder != null -> "폴더: ${folder.name}"
                                else -> Classifier.reason(a)
                            },
                            onClick = if (mode == ListMode.EXCLUDED) ({ s.setExcluded(a.pkg, a.pkg !in s.excluded) }) else ({ s.sheetPkg = a.pkg })
                        ) {
                            if (mode == ListMode.EXCLUDED) GreenSwitch(a.pkg in s.excluded) { s.setExcluded(a.pkg, it) }
                            else Text("›", fontSize = 22.sp, color = C.sub)
                        }
                    }
                }
            }
        }
    }
}
