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
    /** 이미 앱스 화면에 있는 폴더 (앱만 추가) */
    val existing: Boolean = false,
)

/** 앱스 화면에 이미 있는 폴더와 그 안의 앱(읽은 만큼) */
data class ExistingFolder(val name: String, val pkgs: List<String>)

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
    val folderCount: Int,
    /** 폴더 밖에 흩어진 앱 패키지 */
    val homePkgs: List<String>,
    val launcher: String,
    val folders: List<ExistingFolder> = emptyList(),
    /** 0=앱스 화면 1=홈 화면 */
    val surface: Int = 0,
)

data class RunRecord(
    val time: Long,
    val foldersMade: Int,
    val appsMoved: Int,
    val failed: List<String>,
    val note: String,
    /** 이번 실행에서 실제로 옮긴 앱 (폴더 이름 to 패키지). 저장하지 않음 */
    val moved: List<Pair<String, String>> = emptyList(),
)

data class Settings(
    /** 0=빠름 1=보통 2=천천히 */
    val speed: Int = 1,
    val minApps: Int = 3,
    val etcFolder: Boolean = false,
    val homeOnly: Boolean = true,
    val notifyNewApps: Boolean = false,
    /** 정리할 화면 0=앱스 화면 1=홈 화면 */
    val target: Int = 0,
) {
    val delayFactor: Float get() = when (speed) { 0 -> 0.7f; 2 -> 1.8f; else -> 1f }
}
