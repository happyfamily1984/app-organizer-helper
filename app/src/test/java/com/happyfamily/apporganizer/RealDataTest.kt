package com.happyfamily.apporganizer

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.test.core.app.ApplicationProvider
import com.github.takahirom.roborazzi.captureRoboImage
import com.happyfamily.apporganizer.data.AppItem
import com.happyfamily.apporganizer.data.ExistingFolder
import com.happyfamily.apporganizer.data.ScanResult
import com.happyfamily.apporganizer.ui.AppState
import com.happyfamily.apporganizer.ui.C
import com.happyfamily.apporganizer.ui.RecommendScreen
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** 사용자 앱스 화면 캡처를 옮겨 적은 데이터로 추천 결과 확인 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w400dp-h1400dp-xxhdpi")
class RealDataTest {
    val folders = linkedMapOf(
        "Samsung" to listOf("com.samsung.android.oneconnect" to "SmartThings", "com.sec.android.app.voicenote" to "음성 녹음", "com.samsung.android.app.notes" to "Samsung Notes", "com.samsung.android.app.watchmanager" to "Galaxy Wearable", "com.sec.android.app.myfiles" to "내 파일"),
        "Google" to listOf("com.google.android.googlequicksearchbox" to "Google", "com.android.chrome" to "Chrome", "com.google.android.apps.maps" to "지도", "com.google.android.youtube" to "YouTube", "com.google.android.apps.docs" to "Drive", "com.google.android.apps.photos" to "포토", "com.google.android.calendar" to "Google 캘린더"),
        "Microsoft" to listOf("com.microsoft.copilot" to "Copilot", "com.microsoft.skydrive" to "OneDrive", "com.microsoft.office.officehubrow" to "Microsoft 365"),
        "안쓰는것" to listOf("com.sec.android.app.shealth" to "Samsung Health", "com.daum.android.news" to "다음 뉴스", "com.microsoft.office.word" to "Word", "com.google.android.apps.docs.editors.docs" to "Docs"),
        "은행" to listOf("com.kbstar.kbbank" to "KB국민은행", "com.kbcard.cxh.appcard" to "KB Pay", "nh.smart.nhallonepay" to "NH pay", "nh.smart.banking" to "NH스마트뱅킹", "com.nonghyup.nhallonebank" to "NH올원뱅크", "kvp.jjy.MispAndroid320" to "페이북", "com.kbankwith.smartbank" to "케이뱅크", "com.ibk.android.ionebank" to "i-ONE Bank"),
        "인증,정부" to listOf("kr.go.minwon.m" to "정부24", "kr.go.mobileid" to "모바일 신분증", "com.sktelecom.tauth" to "PASS", "kr.go.hometax" to "홈택스", "kr.go.ncs" to "국민비서"),
        "부동산,업무" to listOf("com.hogangnono" to "호갱노노", "kr.co.vito" to "비토", "com.gsenc.xi" to "자이앱", "kr.co.zigbang" to "직방"),
        "네비" to listOf("com.nhn.android.nmap" to "네이버 지도", "com.skt.tmap.ku" to "TMAP", "com.kakao.navi" to "카카오내비"),
        "인터넷구매" to listOf("com.elevenst" to "11번가", "com.coupang.mobile" to "쿠팡", "com.einnovation.temu" to "Temu", "com.ssg.serviceapp" to "SSG.COM"),
        "오프라인구매" to listOf("kr.co.burgerkinghybrid" to "버거킹", "com.starbucks.co" to "스타벅스", "com.mcdonalds.mobileapp" to "맥도날드", "com.momstouch" to "맘스터치", "com.kfc.korea" to "KFC", "com.emart.mobile" to "이마트"),
        "결제,포인트" to listOf("com.kakaopay.app" to "카카오페이", "com.spc.happypoint" to "해피포인트", "com.lottecard.lpoint" to "L.POINT"),
        "민한" to listOf("com.hiclass.kids" to "하이클래스", "com.kidsapp.cookie" to "쿠키런 킹덤"),
        "암호화폐,주식" to listOf("com.dunamu.exchange" to "업비트", "com.btckorea.bithumb" to "빗썸", "com.binance.dev" to "Binance", "com.bybit.app" to "Bybit", "com.okinc.okex.gp" to "OKX", "com.kiwoom.heromts" to "영웅문S#"),
        "배달" to listOf("com.coupang.mobile.eats" to "쿠팡이츠", "com.woowahan.bros" to "배민커넥트", "com.yogiyo.rider" to "요기요 라이더"),
        "티켓,여행" to listOf("kr.co.srt" to "SRT", "com.korail.talk" to "코레일톡", "com.airbnb.android" to "에어비앤비", "com.koreanair.passenger" to "대한항공", "com.interpark.tour" to "인터파크 투어"),
        "엔터테인먼트" to listOf("com.netflix.mediaclient" to "넷플릭스", "com.sooplive.live" to "SOOP", "com.supercell.clashroyale" to "클래시 로얄", "com.google.android.youtube.music" to "YouTube Music"),
        "메일" to listOf("com.daum.android.mail" to "Daum 메일", "com.samsung.android.email.provider" to "이메일", "com.google.android.gm" to "Gmail"),
        "SNS" to listOf("com.twitter.android" to "X", "com.kakao.talk" to "카카오톡", "com.discord" to "Discord", "jp.naver.line.android" to "LINE", "org.telegram.messenger" to "텔레그램"),
        "AI" to listOf("com.openai.chatgpt" to "ChatGPT", "com.google.android.apps.bard" to "Gemini", "com.anthropic.claude" to "Claude"),
        "내앱" to listOf("com.happyfamily.coinradar" to "코인 레이더", "com.happyfamily.realestate" to "부동산 알리미", "com.happyfamily.bible" to "성경 읽기"),
    )
    val loose = listOf(
        "com.samsung.android.calendar" to "캘린더", "com.sec.android.app.popupcalculator" to "계산기", "com.digitalchemy.currency" to "변환기", "com.bringnt.app" to "BRINGNT",
        "us.zoom.videomeetings" to "Zoom", "com.innopage.manse" to "만세력 천을귀인", "kr.weather.alimi" to "날씨알리미", "com.nhn.android.search" to "NAVER",
        "kr.co.holybible" to "성경", "com.cultsotry.yanolja.nativeapp" to "NOL(야놀자)", "com.yamaha.yconnect" to "Y-Connect", "kr.uscampus" to "어스캠퍼스",
        "com.teamblind.blind" to "blind", "com.naver.nplusstore" to "N+스토어", "com.vaunce" to "바운스", "kr.or.deliveryinsur" to "배달서비스공제",
        "kr.go.bokjiro" to "복지로", "com.br.baskin" to "배스킨라빈스",
        "com.bitmex" to "BitMEX", "com.kubi.kucoin" to "KuCoin", "com.gsshop" to "GS SHOP", "kr.modu.parking" to "모두의주차장",
        "com.mydoctor" to "나만의닥터", "com.baedalpan" to "배달판", "com.navercorp.game.android.community" to "치지직", "com.movl" to "MOVL",
        "com.samsung.android.app.routines" to "모드 및 루틴", "jp.co.canon.bsd.ad.pixmaprint" to "Canon PRINT", "kr.go.safetyreport" to "안전신문고", "com.welaaa" to "윌라", "com.sktelecom.pass2" to "PASS",
        "com.skt.skaf.A000Z00040" to "원스토어", "com.ebay.kr.auction" to "옥션", "com.ebay.kr.gmarket" to "G마켓", "com.towneers.www" to "당근",
        "kr.go.gg.gseek" to "경기도 지식(GSEEK)", "com.kyobo.ebook.library" to "교보도서관", "com.madang" to "경매마당", "com.saramin" to "사람인",
        "com.wanted.android.wanted" to "원티드", "com.cj.themarket" to "CJ더마켓", "com.gsenc.xihome" to "자이홈", "com.gsenc.xipass" to "자이패스",
        "kr.co.kamco.onbid" to "온비드", "com.bhc.order" to "bhc 치킨", "com.meritzfire" to "메리츠화재", "com.kica.signplus" to "KICASignPlus",
        "kr.go.gg.dental" to "덴티아이경기", "kr.go.nts.android" to "국세청 손택스",
        "kr.kwangdong.mall" to "광동상회", "com.dbins.direct" to "DB다이렉트", "com.prnd.heydealer" to "헤이딜러", "kr.go.parking.alarm" to "주정차단속통합알림",
        "kr.or.nps.mobile" to "내 곁에 국민연금", "com.spotify.music" to "Spotify", "com.mexicana.order" to "멕시카나치킨", "com.cheogajip" to "처갓집양념치킨",
        "com.kyochon.order" to "교촌치킨", "com.nene.order" to "네네치킨", "com.puradak" to "푸라닭 치킨", "com.jangsin.mall" to "장신몰",
        "com.shinhan.ddangyo" to "땡겨요", "com.polaris.secuone" to "Polaris SecuOne", "com.knocktown" to "노크타운", "net.cj.cjhv.gs.tving" to "TVING",
        "com.m33" to "삼삼엠투", "com.fanding" to "팬딩", "com.caloriecal" to "칼로리 캘린더",
        "kr.or.ssis.voucher" to "사회서비스 전자바우처", "com.google.android.apps.playconsole" to "Play Console", "com.bbq.order" to "BBQ", "com.muse" to "Muse",
        "com.instagram.android" to "Instagram", "com.instagram.barcelona" to "Threads", "com.deepseek.chat" to "DeepSeek", "com.subway.korea" to "Subway",
        "com.happyfamily.adventure" to "신창의 모험", "com.lotte.lotteeatz" to "롯데잇츠", "com.amaze.go" to "Amaze GO!", "com.kmong.kmong" to "크몽",
        "com.pickle" to "피클", "com.weareone" to "We Are One", "com.happyfamily.floatsticker" to "플로팅 앱 스티커", "com.happyfamily.twophone" to "투폰문자체커 개인용",
        "com.happyfamily.villa" to "빌라 실거래 알리미", "com.happyfamily.daehaengja" to "대행자 몰아보기",
        "com.happyfamily.coinwhale" to "코인 고래 레이더", "com.happyfamily.coinverify" to "코인 검증기", "com.happyfamily.jplayer" to "J플레이어", "com.happyfamily.keyring" to "키링",
        "com.happyfamily.multilogin" to "멀티로그인 개인용", "com.happyfamily.shortdrama" to "숏드라마 메이커", "com.vooy" to "Vooy", "com.kling.ai" to "KLINGAI",
        "com.happyfamily.anniversary" to "우리 가족 기념일", "com.happyfamily.files" to "파일 탐색기", "com.happyfamily.ridertoilet" to "라이더 화장실맵", "com.happyfamily.shortform" to "숏폼 메이커",
        "com.happyfamily.newsalarm" to "뉴스 알리미", "com.happyfamily.apporganizer" to "앱정리 도우미",
    )

    @Test fun realRecommendation() {
        val ctx = ApplicationProvider.getApplicationContext<android.app.Application>()
        val s = AppState(ctx)
        val all = folders.values.flatten() + loose
        s.setLoaded(all.map { (p, l) -> AppItem(p, l, -1, null, 0) })
        s.excluded = emptySet()
        s.accessOn = true
        s.lastScan = ScanResult(0, 7, loose.size, folders.size, loose.map { it.first }, "",
            folders.map { (n, l) -> ExistingFolder(n, l.map { it.first }) }, 0)
        s.buildPlan(loose.map { it.first })
        val p = s.plan!!
        val sb = StringBuilder()
        p.folders.filter { it.apps.isNotEmpty() }.forEach { f ->
            sb.append(if (f.existing) "[기존] " else "[새로] ").append(f.name).append(": ")
                .append(f.apps.joinToString { s.label(it) }).append('\n')
        }
        sb.append("[미분류] ").append(p.unassigned.joinToString { s.label(it) }).append('\n')
        java.io.File("../shots/real_plan.txt").writeText(sb.toString())
        captureRoboImage("../shots/5_real_recommend.png") {
            MaterialTheme(colorScheme = lightColorScheme(primary = C.green)) { RecommendScreen(s) }
        }
    }
}
