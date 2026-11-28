package com.happyfamily.apporganizer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object C {
    val bgTop = Color(0xFFE3F7F3)
    val bgBottom = Color(0xFFF5FBFA)
    val blob = Color(0xFFCDF1E8)
    val card = Color(0xFFFFFFFF)
    val ink = Color(0xFF15201B)
    val sub = Color(0xFF6B7671)
    val line = Color(0xFFE7EEEB)
    val green = Color(0xFF0E9F6E)
    val greenSoft = Color(0xFFDDF5EB)
    val orange = Color(0xFFF7831E)
    val orangeSoft = Color(0xFFFDE7DA)
    val red = Color(0xFFE8452E)
    val graySoft = Color(0xFFE9EDEC)
    val gradA = Color(0xFF0C9E80)
    val gradB = Color(0xFF4BD467)

    val folder = listOf(
        Color(0xFFEF4444), Color(0xFFF59E0B), Color(0xFF16A34A), Color(0xFFEC4899),
        Color(0xFF3B82F6), Color(0xFFDB2777), Color(0xFF14B8A6), Color(0xFF8B5CF6),
        Color(0xFF0EA5E9), Color(0xFFF97316), Color(0xFFEAB308), Color(0xFF64748B), Color(0xFF94A3B8),
    )
    fun folderColor(i: Int) = folder[((i % folder.size) + folder.size) % folder.size]
}

/** 민트빛 배경 + 흐릿한 원 장식 */
@Composable
fun ScreenBg(content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(C.bgTop, C.bgBottom, C.bgBottom)))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(C.blob.copy(alpha = 0.55f), radius = size.width * 0.42f, center = Offset(size.width * 0.82f, size.height * 0.02f))
            drawCircle(C.blob.copy(alpha = 0.35f), radius = size.width * 0.35f, center = Offset(size.width * 1.02f, size.height * 0.16f))
            drawCircle(C.blob.copy(alpha = 0.35f), radius = size.width * 0.30f, center = Offset(size.width * 1.0f, size.height * 0.97f))
        }
        content()
    }
}

@Composable
fun Card(modifier: Modifier = Modifier, pad: Dp = 16.dp, radius: Dp = 22.dp, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .shadow(10.dp, RoundedCornerShape(radius), ambientColor = Color(0x2210A080), spotColor = Color(0x2210A080))
            .clip(RoundedCornerShape(radius))
            .background(C.card)
            .padding(pad),
        content = content
    )
}

@Composable
fun TopBar(title: @Composable RowScope.() -> Unit, image: Int?, onBack: (() -> Unit)?, trailing: @Composable RowScope.() -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Box(Modifier.size(44.dp).clip(CircleShape).clickable { onBack() }, contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBackIos, "뒤로", tint = C.ink, modifier = Modifier.size(20.dp).padding(start = 4.dp))
            }
        } else Spacer(Modifier.width(8.dp))
        if (image != null) {
            Image(painterResource(image), null, Modifier.size(52.dp))
            Spacer(Modifier.width(10.dp))
        }
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, content = title)
        trailing()
    }
}

@Composable
fun TitleText(text: String, accent: String? = null, size: Int = 24) {
    Text(text, fontSize = size.sp, fontWeight = FontWeight.ExtraBold, color = C.ink)
    if (accent != null) Text(accent, fontSize = size.sp, fontWeight = FontWeight.ExtraBold, color = C.green)
}

@Composable
fun Chip(text: String, fg: Color, bg: Color, dot: Boolean = false, onClick: (() -> Unit)? = null, trailing: Boolean = false) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (dot) { Box(Modifier.size(8.dp).clip(CircleShape).background(fg)); Spacer(Modifier.width(7.dp)) }
        Text(text, color = fg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        if (trailing) { Spacer(Modifier.width(4.dp)); Text("›", color = fg, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun GradientButton(text: String, icon: ImageVector?, modifier: Modifier = Modifier, height: Dp = 62.dp, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .shadow(14.dp, RoundedCornerShape(50), ambientColor = Color(0x5520B070), spotColor = Color(0x5520B070))
            .clip(RoundedCornerShape(50))
            .background(
                if (enabled) Brush.horizontalGradient(listOf(C.gradA, C.gradB))
                else Brush.horizontalGradient(listOf(Color(0xFFB9C4C0), Color(0xFFC9D3CF)))
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp)); Spacer(Modifier.width(14.dp)) }
            Text(text, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
        }
        Text("›", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp))
    }
}

@Composable
fun SectionTitle(text: String, trailing: @Composable () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(5.dp).height(20.dp).clip(RoundedCornerShape(3.dp)).background(C.green))
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = C.ink, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
fun IconTile(icon: ImageVector, tint: Color, bg: Color, size: Dp = 48.dp) {
    Box(Modifier.size(size).clip(RoundedCornerShape(size * 0.3f)).background(bg), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(size * 0.55f))
    }
}

@Composable
fun AppIcon(bmp: ImageBitmap?, label: String, size: Dp, colorIndex: Int = label.hashCode()) {
    if (bmp != null) {
        Image(bmp, label, Modifier.size(size).clip(RoundedCornerShape(size * 0.26f)))
    } else {
        Box(
            Modifier.size(size).clip(RoundedCornerShape(size * 0.28f)).background(C.folderColor(colorIndex)),
            contentAlignment = Alignment.Center
        ) { Text(label.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.42f).sp) }
    }
}

@Composable
fun ListRow(
    lead: @Composable () -> Unit,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        lead()
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = C.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) Text(subtitle, fontSize = 14.sp, color = C.sub, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        trailing()
    }
}

@Composable
fun Divider() = Box(Modifier.fillMaxWidth().height(1.dp).background(C.line))

@Composable
fun GreenSwitch(checked: Boolean, onChange: (Boolean) -> Unit) = Switch(
    checked = checked, onCheckedChange = onChange,
    colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White, checkedTrackColor = C.green, checkedBorderColor = C.green,
        uncheckedThumbColor = Color.White, uncheckedTrackColor = Color(0xFFD5DCD9), uncheckedBorderColor = Color(0xFFD5DCD9),
    )
)

@Composable
fun CountBadge(n: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(C.graySoft), contentAlignment = Alignment.Center) {
            Text("$n", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = C.ink)
        }
        Spacer(Modifier.width(6.dp))
        Text("›", fontSize = 22.sp, color = C.sub)
    }
}

@Composable
fun Gap(h: Int) = Spacer(Modifier.height(h.dp))

@Composable
fun RowGap(w: Int) = Spacer(Modifier.width(w.dp))

val Arrangement.gap12 get() = Arrangement.spacedBy(12.dp)

@Composable
fun Segmented(items: List<String>, sel: Int, onSel: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(Color(0xFFEDF1F3)).padding(4.dp)) {
        items.forEachIndexed { i, t ->
            val on = sel == i
            Box(
                Modifier.weight(1f)
                    .then(if (on) Modifier.shadow(8.dp, RoundedCornerShape(50), spotColor = Color(0x5520B070)) else Modifier)
                    .clip(RoundedCornerShape(50))
                    .background(if (on) Brush.horizontalGradient(listOf(C.gradA, C.gradB)) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)))
                    .clickable { onSel(i) }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(t, fontSize = 17.sp, fontWeight = if (on) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (on) Color.White else C.sub, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun OutlineButton(text: String, icon: ImageVector?, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.fillMaxWidth().height(54.dp)
            .clip(RoundedCornerShape(50))
            .background(C.card)
            .border(2.dp, C.green, RoundedCornerShape(50))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) { Icon(icon, null, tint = C.green, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(10.dp)) }
            Text(text, color = C.green, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
    }
}
