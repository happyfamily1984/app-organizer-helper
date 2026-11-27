package com.happyfamily.apporganizer.data

import androidx.compose.ui.graphics.ImageBitmap

/** 설치된(실행 가능한) 앱 하나 */
data class AppItem(
    val pkg: String,
    val label: String,
    val category: Int,
    val icon: ImageBitmap?,
    val installedAt: Long,
)

/** 추천/편집 중인 폴더 */
data class FolderPlan(
    val id: String,
    val name: String,
    val colorIndex: Int,
    val apps: List<String>,
    /** 분류 근거 (pkg -> "카테고리: 금융" 등) 는 화면에서 다시 계산 */
)

data class Plan(
    val folders: List<FolderPlan>,
    val unassigned: List<String>,
    /** 홈 화면 분석 결과를 바탕으로 만든 추천인지 */
    val fromHomeScan: Boolean,
    val createdAt: Long,
)

data class ScanResult(
    val time: Long,
    val pages: Int,
    val looseIcons: Int,
    val folders: Int,
    /** 홈 화면(폴더 밖)에서 찾은 앱 패키지 */
    val homePkgs: List<String>,
    val launcher: String,
)

data class RunRecord(
    val time: Long,
    val foldersMade: Int,
    val appsMoved: Int,
    val failed: List<String>,
    val note: String,
)

data class Settings(
    /** 0=빠름 1=보통 2=천천히 */
    val speed: Int = 1,
    val minApps: Int = 3,
    val etcFolder: Boolean = false,
    val homeOnly: Boolean = true,
    val notifyNewApps: Boolean = false,
) {
    val delayFactor: Float get() = when (speed) { 0 -> 0.7f; 2 -> 1.8f; else -> 1f }
}
