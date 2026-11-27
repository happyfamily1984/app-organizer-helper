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
        Cat("AI", 7,
            listOf("deepseek", "딥시크", "chatgpt", "챗gpt", "gemini", "제미나이", "claude", "클로드", "perplexity", "퍼플렉시티", "copilot", "코파일럿", "뤼튼", "wrtn", "grok", "그록", "klingai", "에이닷", "클로바x", "mistral"),
            listOf("deepseek", "openai", "chatgpt", "gemini", "anthropic", "claude", "perplexity", "copilot", "wrtn", "kling", "grok", "xai")),
        Cat("암호화폐", 10,
            listOf("업비트", "빗썸", "코인", "bitmex", "kucoin", "binance", "바이낸스", "bybit", "바이비트", "okx", "코빗", "크립토", "메타마스크", "비트코인", "이더리움"),
            listOf("upbit", "dunamu", "bithumb", "bitmex", "kucoin", "binance", "bybit", "okx", "coinone", "korbit", "crypto", "metamask", "coin", "bitcoin")),
        Cat("정부·인증", 11,
            listOf("pass", "정부24", "국세청", "홈택스", "손택스", "국민연금", "건강보험", "복지로", "안전신문고", "주정차", "민원", "kica", "signplus", "인증", "신분증", "사회서비스", "경기도", "서울시", "경찰", "병무청", "우체국", "공공"),
            listOf("gov", "mois", "hometax", "nps", "nhis", "bokjiro", "kica", "signplus", "ssis", "minwon", "epost")),
        Cat("부동산", 6,
            listOf("부동산", "아파트", "실거래", "경매", "직방", "다방", "호갱노노", "자이", "온비드", "래미안", "청약", "리치고", "빌라"),
            listOf("zigbang", "dabang", "hogangnono", "realestate", "onbid")),
        Cat("배달·음식", 5,
            listOf("배달", "배민", "쿠팡이츠", "요기요", "스타벅스", "맥도날드", "버거킹", "이디야", "투썸", "빽다방", "메가커피", "컴포즈", "맛집", "캐치테이블", "도미노", "피자", "치킨", "레시피", "만개의", "땡겨요", "배스킨", "파리바게뜨", "뚜레쥬르", "서브웨이", "커피", "치킨", "bhc", "bbq", "교촌", "네네", "푸라닭", "처갓집", "멕시카나", "굽네", "롯데잇츠", "맘스터치", "kfc", "subway", "배달"),
            listOf("baemin", "coupangeats", "yogiyo", "starbucks", "mcdonald", "burgerking", "ediya", "twosome", "megacoffee", "catchtable", "mangoplate", "domino", "pizza", "recipe", "subway", "baskin", "paris", "chicken", "kyochon", "lotteeatz", "kfc")),
        Cat("사진·영상", 4,
            listOf("갤러리", "카메라", "유튜브", "캡컷", "스노우", "넷플릭스", "티빙", "웨이브", "쿠팡플레이", "디즈니", "왓챠", "사진", "비디오", "영상", "블로", "b612", "푸디", "스냅시드", "라이트룸", "치지직", "숲", "아프리카", "라프텔", "틱톡 라이트", "포토", "편집"),
            listOf("gallery", "camera", "youtube", "capcut", "snow", "netflix", "tving", "wavve", "coupangplay", "disney", "watcha", "photo", "video", "vllo", "b612", "foodie", "snapseed", "lightroom", "picsart", "afreeca", "chzzk", "twitch", "laftel", "soop")),
        Cat("SNS·메신저", 1,
            listOf("카카오톡", "인스타그램", "페이스북", "밴드", "스레드", "텔레그램", "왓츠앱", "디스코드", "메신저", "틱톡", "스냅챗", "블라인드", "카카오스토리", "에브리타임", "트위터", "링크드인", "핀터레스트", "레딧", "위챗", "카페"),
            listOf("kakao.talk", "instagram", "facebook", "band", "twitter", "threads", "telegram", "naver.line", "whatsapp", "discord", "messenger", "tiktok", "musically", "snapchat", "blind", "kakao.story", "everytime", "linkedin", "pinterest", "reddit", "tencent.mm")),
        Cat("금융", 2,
            listOf("토스", "뱅크", "은행", "국민은행", "kb", "신한", "하나은행", "하나원큐", "하나카드", "우리은행", "우리won", "농협", "nh", "ibk", "기업", "새마을", "페이", "카드", "증권", "주식", "보험", "키움", "미래에셋", "sol", "쏠", "자산", "가계부", "뱅킹", "머니", "화재", "손해보험", "손보", "meritz", "db다이렉트"),
            listOf("toss", "bank", "kbstar", "kbank", "shinhan", "hana", "woori", "nonghyup", "ibk", "pay", "card", "stock", "insurance", "kiwoom", "miraeasset", "banksalad", "finance", "wallet", "securities")),
        Cat("쇼핑", 0,
            listOf("쿠팡", "11번가", "지마켓", "g마켓", "옥션", "무신사", "오늘의집", "당근", "ssg", "쓱", "롯데온", "29cm", "지그재그", "에이블리", "컬리", "알리익스프레스", "테무", "쇼핑", "마켓", "위메프", "티몬", "번개장터", "중고나라", "올리브영", "다이소", "이마트", "홈플러스", "쇼호스트", "홈쇼핑", "브랜디", "w컨셉", "크림", "아울렛", "쿠폰", "기프티콘"),
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
        Cat("메일", 12,
            listOf("메일", "gmail", "이메일", "outlook", "아웃룩"),
            listOf("mail", "gm", "email", "outlook")),
        Cat("업무·도구", 7,
            listOf("드라이브", "문서", "노션", "캘린더", "달력", "계산기", "메모", "노트", "오피스", "엑셀", "워드", "파워포인트", "한글", "한컴", "슬랙", "줌", "팀즈", "클로바", "스캐너", "파일", "시계", "알람", "날씨", "키보드", "크롬", "인터넷", "브라우저", "네이버", "다음", "구글", "번역기", "할 일", "투두", "pdf", "웨일", "사람인", "원티드", "잡코리아", "알바", "크몽", "숨고", "리멤버", "zoom", "naver", "프린트", "print"),
            listOf("drive", "docs", "notion", "calendar", "calculator", "memo", "note", "office", "excel", "word", "powerpoint", "hancom", "slack", "zoom", "teams", "scanner", "file", "clock", "alarm", "weather", "keyboard", "chrome", "browser", "daum", "todo", "pdf", "whale", "firefox", "opera")),
        Cat("게임", 10, listOf("게임", "game"), listOf("game", "games")),
        Cat("시스템·설정", 11, emptyList(), emptyList()),
    )

    val ETC = "기타"

    private fun catByName(n: String) = CATS.first { it.name == n }

    /** 앱 하나의 분류와 그 근거 */
    private fun labelHas(labelLower: String, labelNoSpace: String, key: String): Boolean {
        val k = key.lowercase()
        return if (k.length <= 3 && k.all { it in 'a'..'z' || it in '0'..'9' })
            Regex("(?<![a-z0-9])" + Regex.escape(k) + "(?![a-z0-9])").containsMatchIn(labelLower)
        else labelNoSpace.contains(k.replace(" ", ""))
    }

    private fun pkgHas(pkg: String, k: String): Boolean =
        if (k.contains('.')) pkg.contains(k)
        else pkg.split('.').any { seg -> if (k.length <= 3) seg == k else seg.contains(k) }

    /** 앱 하나의 분류와 그 근거 */
    fun classify(app: AppItem): Pair<String, String>? {
        val labelLower = app.label.lowercase()
        val label = labelLower.replace(" ", "")
        val pkg = app.pkg.lowercase()
        for (c in CATS) if (c.labelKeys.any { labelHas(labelLower, label, it) }) return c.name to "이름으로 추천"
        for (c in CATS) if (c.pkgKeys.any { pkgHas(pkg, it) }) return c.name to "패키지명으로 추천"
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

    // ───────── 기존 폴더에 맞춰 넣기 ─────────

    private val SYN = mapOf(
        "금융" to listOf("은행", "금융", "뱅크", "뱅킹", "증권", "주식", "결제", "페이", "포인트", "카드", "보험", "머니", "재테크"),
        "암호화폐" to listOf("암호화폐", "코인", "가상화폐", "크립토", "비트"),
        "쇼핑" to listOf("쇼핑", "구매", "마켓", "온라인", "쇼핑몰", "중고"),
        "배달·음식" to listOf("배달", "음식", "맛집", "외식", "카페", "치킨", "오프라인구매", "푸드"),
        "SNS·메신저" to listOf("sns", "메신저", "소셜", "채팅", "커뮤니티"),
        "사진·영상" to listOf("사진", "영상", "동영상", "엔터테인먼트", "ott", "방송", "엔터"),
        "음악·오디오" to listOf("음악", "오디오", "엔터테인먼트", "엔터"),
        "게임" to listOf("게임", "엔터테인먼트", "엔터"),
        "교통·지도" to listOf("네비", "내비", "지도", "교통", "여행", "티켓", "항공", "숙박", "주차"),
        "업무·도구" to listOf("업무", "도구", "유틸", "생산성", "툴", "도우미"),
        "메일" to listOf("메일", "이메일", "mail"),
        "AI" to listOf("ai", "인공지능"),
        "정부·인증" to listOf("인증", "정부", "공공", "민원"),
        "부동산" to listOf("부동산"),
        "건강·운동" to listOf("건강", "운동", "헬스", "병원"),
        "책·교육·뉴스" to listOf("책", "교육", "뉴스", "공부", "도서"),
        "시스템·설정" to listOf("samsung", "삼성", "google", "구글", "시스템", "설정"),
    )

    /** 폴더 이름이 가리키는 카테고리들 ("암호화폐,주식" -> 암호화폐, 금융) */
    fun categoriesOfFolderName(name: String): Set<String> {
        val tokens = name.lowercase().split(Regex("[,\\s·/&+]+")).filter { it.isNotBlank() }
        val out = HashSet<String>()
        for ((cat, syns) in SYN) for (t in tokens) for (w in syns) {
            val hit = if (w.length <= 2 && w.all { it in 'a'..'z' }) t == w else t == w || t.contains(w)
            if (hit) out += cat
        }
        return out
    }

    /** 같은 개발사로 볼 패키지 접두어 */
    fun developer(pkg: String): String {
        val p = pkg.lowercase()
        if (p.startsWith("com.samsung.") || p.startsWith("com.sec.")) return "samsung"
        if (p.startsWith("com.google.")) return "google"
        if (p.startsWith("com.microsoft.")) return "microsoft"
        val seg = p.split('.')
        if (seg.size < 2) return p
        // kr.co.회사 / jp.co.회사 / kr.go.기관 처럼 두 번째가 일반 단어면 세 번째까지
        val generic2 = setOf("co", "or", "go", "ne", "ac", "com", "re", "pe")
        return if (seg.size >= 3 && seg[1] in generic2) seg.take(3).joinToString(".") else seg[0] + "." + seg[1]
    }

    /** 같은 카테고리 안에서 더 나눔: 배달앱 vs 브랜드앱, 지도 vs 여행 */
    private fun subtype(app: AppItem, cat: String?): String? {
        val t = (app.label + " " + app.pkg).lowercase().replace(" ", "")
        return when (cat) {
            "배달·음식" -> if (listOf("배달", "배민", "이츠", "요기요", "땡겨요", "라이더", "커넥트", "rider", "delivery", "baemin", "yogiyo", "eats", "baedal")
                    .any { t.contains(it) } && !t.contains("잇츠")) "delivery" else "brand"
            "교통·지도" -> if (listOf("지도", "맵", "내비", "네비", "주차", "map", "navi", "tmap", "parking", "카카오t", "택시", "taxi")
                    .any { t.contains(it) }) "map" else "travel"
            else -> null
        }
    }

    private fun kind(app: AppItem): Pair<String?, String?> { val c = classify(app)?.first; return c to subtype(app, c) }

    /** 흩어진 앱 하나에 가장 잘 맞는 기존 폴더 (없으면 null) */
    fun bestExisting(app: AppItem, folders: List<ExistingFolder>, appMap: Map<String, AppItem>): ExistingFolder? {
        val k = kind(app)
        val c = k.first
        val dev = developer(app.pkg)
        var best: ExistingFolder? = null
        var bestScore = 0.0
        for (f in folders) {
            val contents = f.pkgs.mapNotNull { appMap[it] }
            var score = 0.0
            // 한 개발사 앱을 모은 폴더(Samsung, Google, 내앱 등)는 그 개발사 앱만
            val devFolder = contents.size >= 2 && contents.groupingBy { developer(it.pkg) }.eachCount().values.max() >= contents.size * 0.6
            val sameDev = contents.any { developer(it.pkg) == dev }
            if (devFolder && !sameDev) continue
            if (c != null && contents.isNotEmpty()) {
                val kinds = contents.map { kind(it) }
                score += 3.0 * kinds.count { it.first == c && (k.second == null || it.second == k.second) } / contents.size
                score += 1.0 * kinds.count { it.first == c && k.second != null && it.second != k.second } / contents.size
            }
            if (c != null && c in categoriesOfFolderName(f.name)) score += 2.0
            if (contents.isNotEmpty()) {
                val same = contents.count { developer(it.pkg) == dev }.toDouble() / contents.size
                score += if (same >= 0.6) 4.0 else if (same > 0) 1.5 else 0.0
            }
            if (score > bestScore) { bestScore = score; best = f }
        }
        val need = if (c == null) 1.5 else 2.0
        return if (bestScore >= need) best else null
    }

    /** 앱스 화면 분석 결과로 추천: 기존 폴더에 먼저 넣고, 남는 앱은 새 폴더 */
    fun recommendWithExisting(
        loose: List<AppItem>, existing: List<ExistingFolder>, appMap: Map<String, AppItem>,
        excluded: Set<String>, settings: Settings,
    ): Plan {
        val adds = LinkedHashMap<String, MutableList<String>>()
        existing.forEach { adds[it.name] = mutableListOf() }
        val rest = ArrayList<AppItem>()
        for (a in loose) {
            if (a.pkg in excluded) continue
            val f = bestExisting(a, existing, appMap)
            if (f != null) adds[f.name]!!.add(a.pkg) else rest += a
        }
        val fresh = recommend(rest, excluded, settings, true)
        val existingNames = existing.map { it.name }.toSet()
        val folders = ArrayList<FolderPlan>()
        existing.forEachIndexed { i, f ->
            folders += FolderPlan("e$i", f.name, colorForName(f.name), adds[f.name]!!.toList(), existing = true)
        }
        // 새 폴더 이름이 기존 폴더와 같으면 합치기
        for (nf in fresh.folders) {
            if (nf.name in existingNames) {
                val idx = folders.indexOfFirst { it.existing && it.name == nf.name }
                folders[idx] = folders[idx].copy(apps = folders[idx].apps + nf.apps)
            } else folders += nf.copy(id = "n" + nf.id)
        }
        return Plan(folders, fresh.unassigned, true, System.currentTimeMillis())
    }

    fun colorForName(name: String): Int = CATS.firstOrNull { it.name == name }?.color ?: (name.hashCode() and 0x7fffffff) % 13
}
