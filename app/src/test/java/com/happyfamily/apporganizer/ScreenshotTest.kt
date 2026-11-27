package com.happyfamily.apporganizer

import android.content.pm.ApplicationInfo
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import com.happyfamily.apporganizer.data.AppItem
import com.happyfamily.apporganizer.data.RunRecord
import com.happyfamily.apporganizer.data.ScanResult
import com.happyfamily.apporganizer.ui.AppState
import com.happyfamily.apporganizer.ui.C
import com.happyfamily.apporganizer.ui.FolderEditScreen
import com.happyfamily.apporganizer.ui.HomeScreen
import com.happyfamily.apporganizer.ui.MoveSheetContent
import com.happyfamily.apporganizer.ui.RecommendScreen
import com.happyfamily.apporganizer.ui.SettingsScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h866dp-xxhdpi")
class ScreenshotTest {
    private fun state(): AppState {
        val ctx = ApplicationProvider.getApplicationContext<android.app.Application>()
        val s = AppState(ctx)
        val names = listOf(
            "com.viva.toss" to "토스", "com.kbstar.kbbank" to "KB국민", "com.shinhan.sbanking" to "신한SOL", "com.kakaobank.channel" to "카카오뱅크",
            "com.samsung.android.spay" to "삼성페이", "com.dunamu.exchange" to "업비트", "com.coupang.mobile" to "쿠팡", "com.musinsa.store" to "무신사",
            "com.bucketplace" to "오늘의집", "com.towneers.www" to "당근", "com.kakao.talk" to "카카오톡", "com.instagram.android" to "인스타그램",
            "com.nhn.android.band" to "밴드", "com.sampleapp" to "배달의민족", "com.coupang.mobile.eats" to "쿠팡이츠", "com.fineapp.yogiyo" to "요기요",
            "com.google.android.youtube" to "유튜브", "com.lemon.lvoverseas" to "캡컷", "com.netflix.mediaclient" to "넷플릭스",
            "com.kakao.taxi" to "카카오T", "com.nhn.android.nmap" to "네이버지도", "com.lgt.tmoney" to "티머니",
            "com.google.android.gm" to "Gmail", "notion.id" to "노션", "com.sec.android.app.popupcalculator" to "계산기",
            "com.supercell.brawlstars" to "브롤스타즈", "com.devsisters.ck" to "쿠키런", "com.roblox.client" to "로블록스",
        )
        s.setLoaded(names.map { (p, l) -> AppItem(p, l, if (p.contains("supercell") || p.contains("roblox") || p.contains("devsisters")) ApplicationInfo.CATEGORY_GAME else -1, null, 0) })
        s.accessOn = true
        s.excluded = setOf("com.kakao.talk")
        s.lastScan = ScanResult(0, 5, 17, 3, emptyList(), "")
        s.lastRun = RunRecord(System.currentTimeMillis(), 8, 47, emptyList(), "")
        s.buildPlan(null)
        return s
    }

    private fun shot(name: String, content: @Composable () -> Unit) = captureRoboImage("../shots/$name.png") {
        MaterialTheme(colorScheme = lightColorScheme(primary = C.green)) { content() }
    }

    @Test fun home() { val s = state(); shot("1_home") { HomeScreen(s) {} } }
    @Test fun recommend() { val s = state(); shot("2_recommend") { RecommendScreen(s) } }
    @Test fun edit() {
        val s = state(); val f = s.plan!!.folders.first { it.name == "금융" }
        shot("3_edit") {
            Box(Modifier.fillMaxSize()) {
                FolderEditScreen(s, f.id)
                Box(Modifier.fillMaxSize().background(Color(0x55000000)))
                Column(Modifier.align(Alignment.BottomCenter).background(Color.White, RoundedCornerShape(topStart = 28f, topEnd = 28f))) {
                    MoveSheetContent(s, f.apps[4]) {}
                }
            }
        }
    }
    @Test fun settings() { val s = state(); shot("4_settings") { SettingsScreen(s, {}, {}) } }
}
