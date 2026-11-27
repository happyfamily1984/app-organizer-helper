package com.happyfamily.apporganizer.data

import android.content.pm.ApplicationInfo

/**
 * 기기 안에서만 동작하는 앱 분류기.
 * 1) 앱 이름 / 패키지명 키워드  2) 안드로이드 앱 카테고리  3) 제조사 기본 앱  순서로 판단.
 */
object Classifier {

    data class Cat(val name: String, val color: Int, val labelKeys: List<String>, val pkgKeys: List<String>)

    // 순서가 중요: 앞의 카테고리가 먼저 판정됨 (쿠팡이츠 -> 배달, 쿠팡플레이 -> 영상, 쿠팡 -> 쇼핑)
    val CATS = listOf(
        Cat("배달·음식", 5,
            listOf("배달", "배민", "쿠팡이츠", "요기요", "스타벅스", "맥도날드", "버거킹", "이디야", "투썸", "빽다방", "메가커피", "컴포즈", "맛집", "캐치테이블", "도미노", "피자", "치킨", "레시피", "만개의", "땡겨요", "배스킨", "파리바게뜨", "뚜레쥬르", "서브웨이", "커피"),
            listOf("baemin", "coupangeats", "yogiyo", "starbucks", "mcdonald", "burgerking", "ediya", "twosome", "megacoffee", "catchtable", "mangoplate", "domino", "pizza", "recipe", "subway", "baskin", "paris")),
        Cat("사진·영상", 4,
            listOf("갤러리", "카메라", "유튜브", "캡컷", "스노우", "넷플릭스", "티빙", "웨이브", "쿠팡플레이", "디즈니", "왓챠", "사진", "비디오", "영상", "블로", "b612", "푸디", "스냅시드", "라이트룸", "치지직", "숲", "아프리카", "라프텔", "틱톡 라이트", "포토", "편집"),
            listOf("gallery", "camera", "youtube", "capcut", "snow", "netflix", "tving", "wavve", "coupangplay", "disney", "watcha", "photo", "video", "vllo", "b612", "foodie", "snapseed", "lightroom", "picsart", "afreeca", "chzzk", "twitch", "laftel", "soop")),
        Cat("SNS·메신저", 1,
            listOf("카카오톡", "인스타그램", "페이스북", "밴드", "스레드", "텔레그램", "왓츠앱", "디스코드", "메신저", "틱톡", "스냅챗", "블라인드", "카카오스토리", "에브리타임", "트위터", "링크드인", "핀터레스트", "레딧", "위챗", "카페"),
            listOf("kakao.talk", "instagram", "facebook", "band", "twitter", "threads", "telegram", "naver.line", "whatsapp", "discord", "messenger", "tiktok", "musically", "snapchat", "blind", "kakao.story", "everytime", "linkedin", "pinterest", "reddit", "tencent.mm")),
        Cat("금융", 2,
            listOf("토스", "뱅크", "은행", "국민", "kb", "신한", "하나", "우리", "농협", "nh", "ibk", "기업", "새마을", "우체국", "페이", "카드", "증권", "주식", "업비트", "빗썸", "코인", "보험", "키움", "미래에셋", "홈택스", "손택스", "연금", "sol", "쏠", "자산", "가계부", "뱅킹", "머니"),
            listOf("toss", "bank", "kbstar", "kbank", "shinhan", "hana", "woori", "nonghyup", "ibk", "pay", "card", "stock", "upbit", "bithumb", "insurance", "kiwoom", "miraeasset", "hometax", "banksalad", "finance", "wallet", "securities")),
        Cat("쇼핑", 0,
            listOf("쿠팡", "11번가", "지마켓", "g마켓", "옥션", "무신사", "오늘의집", "당근", "ssg", "쓱", "롯데온", "29cm", "지그재그", "에이블리", "컬리", "알리", "테무", "쇼핑", "마켓", "위메프", "티몬", "번개장터", "중고나라", "올리브영", "다이소", "이마트", "홈플러스", "쇼호스트", "홈쇼핑", "브랜디", "w컨셉", "크림", "아울렛", "쿠폰", "기프티콘"),
            listOf("coupang", "11st", "gmarket", "auction", "musinsa", "ohou", "daangn", "ssg", "lotteon", "29cm", "zigzag", "ably", "kurly", "aliexpress", "temu", "shopping", "wemakeprice", "tmon", "bunjang", "oliveyoung", "daiso", "emart", "homeplus", "kream", "shop", "mall")),
        Cat("음악·오디오", 9,
            listOf("멜론", "지니", "플로", "벅스", "스포티파이", "뮤직", "음악", "팟캐스트", "오디오", "녹음", "바이브", "라디오", "노래", "사운드"),
            listOf("melon", "genie", "flo", "bugs", "spotify", "music", "podcast", "audio", "recorder", "vibe", "radio", "sound")),
        Cat("교통·지도", 6,
            listOf("카카오t", "지도", "맵", "내비", "티맵", "티머니", "코레일", "srt", "지하철", "버스", "택시", "우버", "쏘카", "그린카", "항공", "대한항공", "아시아나", "야놀자", "여기어때", "에어비앤비", "호텔", "아고다", "트립", "주차", "하이패스", "따릉이", "여행", "킥보드", "카카오 t"),
            listOf("kakao.taxi", "map", "navi", "tmap", "tmoney", "korail", "srt", "subway", "busan", "taxi", "uber", "socar", "greencar", "airline", "koreanair", "yanolja", "goodchoice", "airbnb", "hotel", "agoda", "trip", "parking", "travel", "kickgoing", "kakaomobility")),
        Cat("건강·운동", 3,
            listOf("헬스", "건강", "운동", "걸음", "캐시워크", "병원", "약국", "똑닥", "굿닥", "나이키", "스트라바", "다이어트", "수면", "명상", "피트니스", "만보기", "체중", "식단"),
            listOf("health", "fit", "cashwalk", "ddocdoc", "goodoc", "nike", "strava", "diet", "sleep", "calm", "workout", "pedometer")),
        Cat("책·교육·뉴스", 8,
            listOf("교육", "학습", "영어", "듀오링고", "클래스", "강의", "사전", "파파고", "번역", "책", "리디", "밀리", "윌라", "웹툰", "시리즈", "카카오페이지", "뉴스", "신문", "도서관", "공부", "단어", "수학", "e학습터", "ebs", "소설"),
            listOf("edu", "duolingo", "class", "dictionary", "papago", "translate", "book", "ridi", "millie", "welaaa", "webtoon", "comic", "news", "library", "study", "ebs")),
        Cat("업무·도구", 7,
            listOf("gmail", "메일", "드라이브", "문서", "노션", "캘린더", "달력", "계산기", "메모", "노트", "오피스", "엑셀", "워드", "파워포인트", "한글", "한컴", "슬랙", "줌", "팀즈", "클로바", "스캐너", "파일", "시계", "알람", "날씨", "키보드", "크롬", "인터넷", "브라우저", "네이버", "다음", "구글", "번역기", "할 일", "투두", "pdf", "웨일", "쉘"),
            listOf("gm", "mail", "drive", "docs", "notion", "calendar", "calculator", "memo", "note", "office", "excel", "word", "powerpoint", "hancom", "slack", "zoom", "teams", "scanner", "file", "clock", "alarm", "weather", "keyboard", "chrome", "browser", "naver", "daum", "todo", "pdf", "whale", "firefox", "opera")),
        Cat("게임", 10, listOf("게임", "game"), listOf("game", "games")),
        Cat("시스템·설정", 11, emptyList(), emptyList()),
    )

    val ETC = "기타"

    private fun catByName(n: String) = CATS.first { it.name == n }

    /** 앱 하나의 분류와 그 근거 */
    fun classify(app: AppItem): Pair<String, String>? {
        val label = app.label.lowercase().replace(" ", "")
        val pkg = app.pkg.lowercase()
        for (c in CATS) {
            if (c.labelKeys.any { label.contains(it.replace(" ", "")) }) return c.name to "이름으로 추천"
            if (c.pkgKeys.any { k -> if (k.contains('.')) pkg.contains(k) else pkg.split('.').any { seg -> if (k.length <= 3) seg == k else seg.contains(k) } })
                return c.name to "패키지명으로 추천"
        }
        val byCat = when (app.category) {
            ApplicationInfo.CATEGORY_GAME -> "게임"
            ApplicationInfo.CATEGORY_AUDIO -> "음악·오디오"
            ApplicationInfo.CATEGORY_VIDEO, ApplicationInfo.CATEGORY_IMAGE -> "사진·영상"
            ApplicationInfo.CATEGORY_SOCIAL -> "SNS·메신저"
            ApplicationInfo.CATEGORY_NEWS -> "책·교육·뉴스"
            ApplicationInfo.CATEGORY_MAPS -> "교통·지도"
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> "업무·도구"
            else -> null
        }
        if (byCat != null) return byCat to "카테고리: $byCat"
        if (pkg.startsWith("com.samsung.") || pkg.startsWith("com.sec.") || pkg.startsWith("com.android.") ||
            pkg.startsWith("com.google.android.") || pkg.startsWith("com.skt.") || pkg.startsWith("com.kt.") ||
            pkg.startsWith("com.lguplus.") || pkg.startsWith("com.lge.")
        ) return "시스템·설정" to "기본 앱"
        return null
    }

    fun reason(app: AppItem): String = classify(app)?.second ?: "분류 안 됨"

    /** 전체 추천 만들기 */
    fun recommend(apps: List<AppItem>, excluded: Set<String>, settings: Settings, fromHome: Boolean): Plan {
        val groups = LinkedHashMap<String, MutableList<String>>()
        CATS.forEach { groups[it.name] = mutableListOf() }
        val unassigned = mutableListOf<String>()
        for (a in apps) {
            if (a.pkg in excluded) continue
            val c = classify(a)?.first
            if (c == null) unassigned += a.pkg else groups[c]!!.add(a.pkg)
        }
        val folders = mutableListOf<FolderPlan>()
        var n = 0
        for ((name, list) in groups) {
            if (list.isEmpty()) continue
            if (list.size < settings.minApps) { unassigned += list; continue }
            folders += FolderPlan("f${n++}", name, catByName(name).color, list.toList())
        }
        val rest = if (settings.etcFolder && unassigned.size >= 2) {
            folders += FolderPlan("f${n++}", ETC, 12, unassigned.toList()); emptyList()
        } else unassigned
        return Plan(folders, rest, fromHome, System.currentTimeMillis())
    }

    fun colorForName(name: String): Int = CATS.firstOrNull { it.name == name }?.color ?: (name.hashCode() and 0x7fffffff) % 13
}
