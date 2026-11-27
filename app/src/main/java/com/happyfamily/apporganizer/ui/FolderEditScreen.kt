package com.happyfamily.apporganizer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.happyfamily.apporganizer.R

@Composable
fun FolderEditScreen(s: AppState, id: String) {
    val f = s.folder(id)
    if (f == null) { s.back(); return }
    var name by remember(id) { mutableStateOf(f.name) }
    val done = {
        if (name.isNotBlank() && name.trim() != f.name) s.folder(id)?.let { s.updateFolder(it.copy(name = name.trim())) }
        s.back()
    }
    ScreenBg {
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
            TopBar(
                title = { TitleText(name.ifBlank { f.name }) },
                image = if (f.name == "금융") R.drawable.hdr_wallet else null,
                onBack = { done() },
            ) {
                Text("완료", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = C.green,
                    modifier = Modifier.clip(RoundedCornerShape(50)).clickable { done() }.padding(10.dp))
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                if (f.name != "금융") {
                    // 폴더 색 표시
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, bottom = 10.dp)) {
                        IconTile(Icons.Filled.Folder, Color.White, C.folderColor(f.colorIndex), 34.dp)
                        RowGap(8)
                        Text("앱 ${f.apps.size}개 · 홈 화면에 이 이름으로 폴더가 만들어져요", fontSize = 13.sp, color = C.sub)
                    }
                }
                Card(pad = 18.dp) {
                    Text("폴더 이름", fontSize = 14.sp, color = C.sub)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BasicTextField(
                            value = name, onValueChange = { name = it.take(20) }, singleLine = true,
                            textStyle = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = C.ink),
                            modifier = Modifier.weight(1f).padding(vertical = 6.dp)
                        )
                        Box(Modifier.size(40.dp).clip(CircleShape).background(C.greenSoft), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Edit, null, tint = C.green, modifier = Modifier.size(20.dp))
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(2.dp).background(C.green))
                }
                Gap(14)
                ReorderList(s, id)
                Gap(14)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(C.greenSoft)
                            .clickable { s.dialog = Dlg.AddApps(id) }.padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Add, null, tint = C.green); RowGap(4)
                            Text("앱 추가", fontWeight = FontWeight.Bold, color = C.green)
                        }
                    }
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(C.orangeSoft)
                            .clickable { s.dialog = Dlg.DeleteFolder(id) }.padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("폴더 해체", fontWeight = FontWeight.Bold, color = C.red) }
                }
                Gap(10)
                Text("앱을 누르면 다른 폴더로 옮기거나 정리에서 뺄 수 있어요. 오른쪽 손잡이를 끌면 순서가 바뀌어요.",
                    fontSize = 13.sp, color = C.sub, modifier = Modifier.padding(horizontal = 4.dp))
                Gap(24)
            }
        }
    }
}

@Composable
private fun ReorderList(s: AppState, id: String) {
    val f = s.folder(id) ?: return
    val rowH = 76.dp
    val gap = 8.dp
    val stepPx = with(LocalDensity.current) { (rowH + gap).toPx() }
    var dragIdx by remember { mutableIntStateOf(-1) }
    var offset by remember { mutableFloatStateOf(0f) }
    Column(verticalArrangement = Arrangement.spacedBy(gap)) {
        f.apps.forEachIndexed { i, pkg -> key(pkg) {
            val a = s.appMap[pkg]
            val dragging = i == dragIdx
            Row(
                Modifier
                    .zIndex(if (dragging) 1f else 0f)
                    .graphicsLayer { translationY = if (dragging) offset else 0f; scaleX = if (dragging) 1.02f else 1f; scaleY = scaleX }
                    .fillMaxWidth().height(rowH)
                    .shadow(if (dragging) 12.dp else 6.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x2210A080), spotColor = Color(0x2210A080))
                    .clip(RoundedCornerShape(20.dp)).background(C.card)
                    .clickable { s.sheetPkg = pkg }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(a?.icon, a?.label ?: pkg, 50.dp)
                RowGap(16)
                Column(Modifier.weight(1f)) {
                    Text(a?.label ?: pkg, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = C.ink, maxLines = 1)
                    Text(a?.let { com.happyfamily.apporganizer.data.Classifier.reason(it) } ?: "", fontSize = 14.sp, color = C.sub)
                }
                Icon(
                    Icons.Filled.DragIndicator, "순서 바꾸기", tint = Color(0xFF9AA5A0),
                    modifier = Modifier.size(44.dp).padding(8.dp).pointerInput(pkg) {
                        detectDragGestures(
                            onDragStart = { dragIdx = s.folder(id)?.apps?.indexOf(pkg) ?: -1; offset = 0f },
                            onDragEnd = { dragIdx = -1; offset = 0f },
                            onDragCancel = { dragIdx = -1; offset = 0f },
                        ) { ch, d ->
                            ch.consume()
                            if (dragIdx < 0) return@detectDragGestures
                            offset += d.y
                            val cur = s.folder(id) ?: return@detectDragGestures
                            val list = cur.apps.toMutableList()
                            if (offset > stepPx / 2 && dragIdx < list.lastIndex) {
                                list.add(dragIdx + 1, list.removeAt(dragIdx)); dragIdx++; offset -= stepPx
                                s.updateFolder(cur.copy(apps = list))
                            } else if (offset < -stepPx / 2 && dragIdx > 0) {
                                list.add(dragIdx - 1, list.removeAt(dragIdx)); dragIdx--; offset += stepPx
                                s.updateFolder(cur.copy(apps = list))
                            }
                        }
                    }
                )
            }
        } }
        if (f.apps.isEmpty()) Text("폴더가 비어 있어요. '앱 추가'로 넣어주세요.", color = C.sub, modifier = Modifier.padding(8.dp))
    }
}

/** 앱 이동 시트 내용 */
@Composable
fun MoveSheetContent(s: AppState, pkg: String, close: () -> Unit) {
    val a = s.appMap[pkg]
    val cur = s.plan?.folders?.firstOrNull { pkg in it.apps }
    Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(a?.icon, a?.label ?: pkg, 56.dp)
            RowGap(16)
            Column {
                Text(a?.label ?: pkg, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = C.ink)
                Text("어디로 옮길까요?", fontSize = 15.sp, color = C.sub)
            }
        }
        Gap(12)
        s.plan?.folders?.filter { it.id != cur?.id }?.forEach { f ->
            SheetOption(f.name, C.folderColor(f.colorIndex)) { s.move(pkg, Target.ToFolder(f.id)); close() }
        }
        SheetOption("+ 새 폴더 만들기", Color(0xFF9DAAA9)) { close(); s.dialog = Dlg.NewFolder(pkg) }
        if (cur != null) SheetOption("미분류로 빼기", Color(0xFFB8C2BF)) { s.move(pkg, Target.Unassign); close() }
        Gap(6)
        Divider()
        Gap(10)
        if (pkg in s.excluded) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(C.greenSoft)
                    .clickable { s.setExcluded(pkg, false); close() }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) { Text("제외 해제 (다시 정리 대상으로)", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = C.green) }
        } else Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFFFFF1EC))
                .clickable { s.move(pkg, Target.Exclude); close() }.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(C.orangeSoft), contentAlignment = Alignment.Center) {
                Text("⊘", fontSize = 22.sp, color = C.red, fontWeight = FontWeight.Bold)
            }
            RowGap(14)
            Text("정리에서 제외 (홈에 그대로 두기)", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = C.red)
        }
    }
}

@Composable
private fun SheetOption(text: String, color: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onClick() }.padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(Icons.Filled.Folder, Color.White, color, 46.dp)
        RowGap(16)
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = C.ink)
    }
}

