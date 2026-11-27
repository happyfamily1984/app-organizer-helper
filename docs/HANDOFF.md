# 앱정리 도우미 — 작업 인수인계

새 세션에서 이어서 작업할 때 먼저 읽는 문서입니다.

## 앱 개요
- **개인용 앱이며 출시하지 않음.** 처음에는 출시를 검토했으나, 접근성 권한이 필요해 개인용으로 결정했다.
- 접근성 서비스가 휴대폰 화면을 직접 조작해서, 폴더 밖에 흩어진 앱을 폴더로 정리한다.
- **정리 대상의 기본값은 앱스 화면**(홈에서 위로 밀면 나오는 앱 목록)이다. 홈 화면은 설정에서 고를 수 있다.
- 사용자는 앱스 화면에 이미 직접 만든 폴더가 많다.
  - 예: Samsung, Google, Microsoft, 안쓰는것, 은행, 인증,정부, 부동산,업무, 네비, 인터넷구매, 오프라인구매, 결제,포인트, 민한, 암호화폐,주식, 배달, 티켓,여행, 엔터테인먼트, 메일, SNS, AI, 내앱
  - 그래서 **기존 폴더에 앱을 넣는 것이 우선**이고, 맞는 폴더가 없을 때만 새 폴더를 만든다.
- 사용자 폰: 갤럭시 One UI. 앱스 화면은 가로 페이지 7장이고, 아래쪽에 "검색" 막대가 있다.
- 디자인 시안과 앱 아이콘 원본: `docs/design/` (디자인 확정됨)

## 구조
- `data/Classifier.kt`: 기기 안에서만 동작하는 분류기.
  - 판단 근거: 앱 이름 키워드 → 패키지명 → 안드로이드 카테고리 → 제조사 기본 앱 순서.
  - `recommendWithExisting`: 기존 폴더 안 앱들과의 카테고리·하위유형 일치, 폴더 이름, 같은 개발사인지를 점수로 매겨 폴더를 고른다.
  - Samsung·Google·내앱처럼 한 개발사 앱을 모은 폴더에는 그 개발사 앱만 넣는다.
- `service/LauncherDriver.kt`: 접근성으로 화면을 조작하는 엔진.
  - 앱스 화면 열기: 홈에서 위로 스와이프
  - 페이지 넘기기: `ACTION_SCROLL`, 안 되면 스와이프
  - 기존 폴더에 넣기: 폴더 열기 → "앱 추가" → 앱 체크(목록을 내리며 찾고, 못 찾으면 검색칸) → "완료"
  - 새 폴더 만들기: 아이콘 길게 누르기 → "선택" → 여러 앱 체크 → "폴더 만들기" → 이름 입력. 안 되면 끌어다 놓기로 대체.
  - `dumpFlow`: 화면 구조를 기록한다. 앱의 설정 → "화면 구조 기록"에서 실행.
- `service/OrganizerService.kt`: 접근성 서비스 본체. 화면 위쪽 진행 표시와 중지 버튼, 새 앱 설치 알림을 맡는다.
- `ui/*`: Compose 화면 (홈, 추천, 폴더 편집, 설정, 앱 목록)
- `src/test/.../RealDataTest.kt`: 사용자 앱스 화면 캡처를 옮겨 적은 데이터로 추천 결과를 검증한다. 결과는 `shots/real_plan.txt`에 저장된다.
  - 패키지명은 추정한 값이다.

## 현재 상태 (2026-11-27)
- 테스트 APK v2를 사용자에게 전달했다. **실제 기기 테스트는 아직 안 됨.**
- 확인되지 않은 것 (One UI 실기기에서 확인 필요):
  - 앱스 화면 인식 (`isAppsScreen`)
  - 폴더 안 "앱 추가" 버튼 문구
  - "선택" / "폴더 만들기" 문구
  - 폴더 노드의 설명(desc)에 "폴더"가 들어가는지
- 문제가 생기면 사용자가 "화면 구조 기록" 텍스트를 보내줄 예정이다. 그 기록을 보고 단어 목록과 인식 조건을 고친다.

## 빌드 환경 (새 컨테이너에서)
```bash
mkdir -p /opt/android-sdk && cd /opt/android-sdk
curl -sSLo clt.zip https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip
unzip -q clt.zip && mkdir -p cmdline-tools/latest && mv cmdline-tools/{bin,lib,NOTICE.txt,source.properties} cmdline-tools/latest/
yes | cmdline-tools/latest/bin/sdkmanager --licenses
cmdline-tools/latest/bin/sdkmanager "platforms;android-36" "build-tools;36.0.0" "platform-tools"
cd <repo> && echo "sdk.dir=/opt/android-sdk" > local.properties
./gradlew assembleDebug                                              # APK
./gradlew :app:testDebugUnitTest -Proborazzi.test.record=true        # 화면 캡처 → shots/
```
- Maven Central이 429 오류를 내면, `settings.gradle.kts`에 들어 있는 구글 미러가 대신 쓰인다.
- Robolectric도 미러를 쓰도록 `app/build.gradle.kts`에 설정되어 있다.

## 주의
- 테스트 APK는 기본 디버그 키로 서명된다. 컨테이너가 바뀌면 키도 바뀌므로, 새 세션의 첫 APK는 **기존 앱을 삭제한 뒤 설치**해야 한다.
  - 저장된 추천·제외 설정은 이때 초기화된다.
- 출시용 서명 키(upload.jks)는 만든 적이 없다. 출시할 계획도 없다.
