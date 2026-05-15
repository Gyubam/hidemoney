# 숨은지원금

> 정부 지원금 추천 앱 / 1인 개발 / 0원 운영 / 디자인·UX 1순위
> 시작일: 2026-05-15

---

## 📍 다음 세션 시작 가이드 (집/다른 PC에서 이어할 때)

> 마지막 작업 시점: **2026-05-15 회사 PC**.
> 정책 데이터 파이프라인 2단계까지 완료, GitHub Pages 활성화 + 3단계 (GitHub Actions 크롤러)가 남음.
> **`CLAUDE.md`에 사용자 프로필·디자인 톤·협업 규칙 다 운반함** — Claude Code가 자동 로드.

### 🔧 0. 환경 셋업 (집 PC에서 첫 풀 받은 직후)

```bash
# 1) 프로젝트 폴더 결정 (어디든 OK, 회사와 동일할 필요 X)
cd C:\Users\<유저>\IdeaProjects   # 또는 원하는 위치
git clone https://github.com/Gyubam/hidemoney.git whatsapp
cd whatsapp

# 2) local.properties 생성 (gitignore라 repo에 없음)
#    Android SDK 경로 박기. Android Studio 설치되어 있으면 자동 — 없으면:
#    Windows 기본 SDK 경로: C:\Users\<유저>\AppData\Local\Android\Sdk
#    파일에 한 줄만:
#       sdk.dir=C:\\Users\\<유저>\\AppData\\Local\\Android\\Sdk

# 3) JDK 17 확인 (없으면 https://adoptium.net 에서 받기)
java -version       # 17.x.x 이어야 함

# 4) 첫 Gradle 동기화 (의존성 다운로드, 5~10분 걸림)
./gradlew.bat build

# 5) 폰 USB 연결 + 디버깅 허용
adb devices         # SM-A356N device 보이면 OK
```

**ADB 경로**: `C:\Users\<유저>\AppData\Local\Android\Sdk\platform-tools\adb.exe`. PATH 추가하든지 절대경로 사용.

### 🚀 1. 첫 빌드/실행 (5분)

```bash
./gradlew.bat installDebug   # 빌드 + 폰 설치 (incremental 후엔 20초)
```

폰에서 **숨은지원금** 아이콘 탭 → 온보딩 또는 홈 (이전 진행 상태 따라). 동작 확인 OK면 다음.

### 📦 2. 즉시 할 일: GitHub Pages 활성화 (5분)

`docs/policies.json`이 이미 repo에 들어가 있음 (이 푸시에 포함). GitHub Pages만 켜면 됨.

1. 브라우저: https://github.com/Gyubam/hidemoney/settings/pages
2. **Source**: Deploy from a branch
3. **Branch**: `main` / **Folder**: `/docs` → Save
4. 1~2분 후 접근 확인:
   ```
   https://gyubam.github.io/hidemoney/policies.json
   ```
   브라우저에 19개 정책 JSON 보이면 OK.
5. 앱 재실행:
   ```bash
   adb logcat -s policies-fetch
   ```
   → `"Refreshed from remote: 19"` 로그 나오면 클라이언트가 remote fetch 성공.

❗ Pages 활성화 안 해도 앱 자체는 정상 작동 (InMemory fallback).

### 🤖 3. 다음 라운드 (3단계): GitHub Actions 크롤러 + Gemini

목표: 매일 새벽 3시 자동 실행 → 정부24/복지로 크롤링 → Gemini Flash로 요약·태깅·ROI 점수 → `docs/policies.json` 자동 commit/push.

#### 💡 왜 서버 없이 0원으로 가능한가 (구조 원리)

**GitHub Actions = GitHub이 빌려주는 무료 서버**.
- 우리가 작성하는 건 `.github/workflows/crawl-policies.yml` (cron 명세 파일)
- GitHub이 자기네 Ubuntu VM(Azure 인프라)을 cron에 맞춰 자동으로 띄움 → 우리 스크립트 실행 → VM 폐기
- VM 관리·서버 운영·고정 IP 0, 비용 0, 사용자 손 안 대고 매일 자동

**무료 한도**:
| 항목 | 한도 | 우리 사용량 |
|---|---|---|
| Public repo (hidemoney) | **무제한** | 무한정 OK |
| Private repo | 월 2000분 | 하루 5분 × 30 = 150분 (어차피 한도 안) |

→ 우리 repo는 public이라 진짜로 0원, 한도 초과 불가.

**동작 흐름 (예시 YAML)**:
```yaml
# .github/workflows/crawl-policies.yml
name: 정책 자동 크롤링
on:
  schedule:
    - cron: '0 18 * * *'        # UTC 18시 = KST 03시
  workflow_dispatch:             # 수동 실행 버튼도 활성화
jobs:
  crawl:
    runs-on: ubuntu-latest       # GitHub이 매번 새 VM 띄워줌
    permissions:
      contents: write            # git push 권한
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with: { python-version: '3.11' }
      - run: pip install -r tools/requirements.txt
      - run: python tools/crawl.py            # 정부24/복지로 fetch + 파싱
      - run: python tools/summarize.py        # Gemini Flash로 요약·태깅·ROI
        env:
          GEMINI_API_KEY: ${{ secrets.GEMINI_API_KEY }}
      - name: Commit if changed
        run: |
          git config user.name  "github-actions[bot]"
          git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
          git add docs/policies.json
          git diff --staged --quiet || git commit -m "auto: 정책 데이터 갱신 $(date +%F)"
          git push
```

**push되면** → GitHub Pages 자동 재빌드 → 앱이 다음 fetch에서 최신 정책 받음.

**사용자 개입은 단 1회**: Gemini API key 발급 + GitHub Secret 등록. 그 다음부터 매일 자동.

→ 이게 PLAN 처음부터 "0원 운영" 가능했던 핵심 이유.

#### 🔐 비밀 정보 관리 (절대 규칙 — public repo니까 더 엄격히)

**API key·비밀번호·토큰은 절대 코드/JSON/`.env`에 박지 말 것.**
한 번 push되면 git history에 영원히 남음 (강제 삭제해도 캐시·fork에 남아있을 수 있음).

##### 1) GitHub Secrets 사용 (필수)
- 등록: https://github.com/Gyubam/hidemoney/settings/secrets/actions → `New repository secret`
- 등록할 키 목록 (3단계 진행 시):
  - `GEMINI_API_KEY` — Google AI Studio에서 발급
  - (추후) `FIREBASE_TOKEN` — Firebase Hosting deploy 시 (R4)
  - (추후) `PLAY_STORE_KEY` — Play Console 자동 배포 시 (R8)
- 워크플로우에서 참조 — 코드엔 절대 평문 X:
  ```yaml
  - run: python tools/summarize.py
    env:
      GEMINI_API_KEY: ${{ secrets.GEMINI_API_KEY }}
  ```
  Python 측은 `os.environ["GEMINI_API_KEY"]`로 읽기.

##### 2) Public repo에서도 Secret이 안전한 이유
- GitHub이 **AES-256으로 암호화** 저장 (콘솔에서도 다시 볼 수 없음, 수정만 가능)
- 워크플로우 로그에 자동 **마스킹** (실수로 `echo $GEMINI_API_KEY` 해도 `***`로 보임)
- **Fork PR에서는 Secret 노출 안 됨** (외부 기여자가 악성 PR로 키 빼낼 수 없음)
- repo 협업자 중 admin만 등록·수정 가능

##### 3) 로컬 개발 시 키 사용
- `.env` 파일 사용 + `.gitignore`에 추가 (이미 패턴 잡혀있음 — `*.local`/keystore 등)
- 또는 환경변수로 export:
  ```powershell
  $env:GEMINI_API_KEY = "..."   # PowerShell, 세션 동안만
  python tools/summarize.py
  ```
- `.env.example`만 push해서 다른 PC에서도 키 이름 확인 가능하게:
  ```
  GEMINI_API_KEY=
  FIREBASE_TOKEN=
  ```

##### 4) 이미 들어있는 안전 장치 (`.gitignore`)
- `keystore.properties` / `*.jks` / `*.keystore` (Android 서명 키)
- `google-services.json` (Firebase 설정)
- `local.properties` (Android SDK 경로 + 가능한 비밀)
- `.claude/` (Claude Code 로컬 작업 메타)

##### 5) 만약 실수로 키가 push 됐다면 (사고 대응)
1. **즉시 키 재발급/폐기** — git history 삭제로는 부족 (캐시·fork·archive에 남음)
2. Gemini: aistudio.google.com → 옛 키 Delete → 새 키 발급 → Secret 갱신
3. 모니터링: 사용량 폭증 없는지 콘솔에서 며칠 확인
4. 만약 도용 의심 → 콘솔에서 사용 통계 + IP 로그 확인

---

**미리 준비할 것 (사용자)**:
1. **Gemini API 키 발급** (무료, 일 100만 토큰):
   - https://aistudio.google.com/apikey
   - Get API key → 새 키 생성 → 복사
2. **GitHub Secret 등록**:
   - https://github.com/Gyubam/hidemoney/settings/secrets/actions
   - New repository secret → Name: `GEMINI_API_KEY` / Value: 복사한 키 → Add

**Claude가 할 것**:
- `.github/workflows/crawl-policies.yml` 작성
  - cron: `'0 18 * * *'` (UTC 18시 = KST 03시)
  - Python 3.11 / `requests` / `beautifulsoup4` / `google-generativeai`
  - 정부24·복지로 크롤링 (robots.txt 확인 + rate limit)
  - Gemini API로 요약·태깅·자격 조건 추출·ROI 계산
  - `docs/policies.json` 갱신 → git commit + push (변경 있을 때만)
- `tools/crawl.py` 신규 (크롤러)
- `tools/summarize.py` 신규 (Gemini 호출)
- `tools/schema.py` 신규 (Policy/EligibilityRule pydantic 검증)
- README에 수동 실행 방법 추가

**개발 흐름**:
1. 사용자가 API 키 준비 완료 알리면 Claude가 워크플로우 작성
2. 첫 실행은 로컬에서 (`python tools/crawl.py`) — 디버깅
3. 정상이면 GitHub Actions push → Pages 자동 갱신
4. 앱에서 remote fetch → 진짜 정책 데이터 보임

### 🔮 4. 그 다음 큰 갈래 (선택)

| 작업 | 사용자 개입 | 가치 |
|---|---|---|
| **Firebase 연동** (Auth + Firestore + FCM) | Firebase 콘솔에서 프로젝트 생성 + `google-services.json` 다운로드 | 사용자 클라우드 저장, 푸시 알림 |
| **출시 준비** (Phase 4) | Play Console 등록(25 USD) + 개인정보처리방침 호스팅 | 실제 출시 |
| **알림 스케줄링** (WorkManager) | 없음 | 마감 임박 D-3 자동 알림 |
| **즐겨찾기 목록 화면** | 없음 | 마이 "받을 예정 N건" 카드 탭 → 리스트 |

> 가장 자연스러운 순서: 3단계(데이터 파이프라인) → Firebase → 출시 준비.

---

## 📊 현재 상태 스냅샷 (2026-05-15 회사 PC 마지막 작업 시점)

### ✅ 완료된 것
- **5개 화면 풀 구현**: 홈 / 캘린더 / 이벤트 / 마이 / 온보딩 + 정책 상세 + 놓친 내역 시트 + 프로필 편집
- **하단 탭바 4탭** (홈/캘린더/이벤트/마이) + sealed Screen 기반 push 트랜지션 (AnimatedContent 280ms)
- **디자인 시스템 코드화**: Color/Type/Spacing/Shape/Theme + 토스 톤 디자인 토큰
- **앱 아이콘 + 스플래시**: 사용자 직접 디자인한 `appIcon2.png` (1254×1254 → 패딩 700×500), 외곽 `#0B7A5E`
- **데이터 모델**: Policy(+EligibilityRule) / LifeEvent / TimelineGroup / EventBundle / PolicyCalendarEvent / UserProfile / MissedGrant / DocumentRequirement / MySummary
- **샘플 데이터**: 19개 정책 + 6개 생애 이벤트 + 14개 캘린더 일정 + 3개 missed grants
- **자격 매칭 로직** (`PolicyMatching.kt`): 룰 기반 동적 isEligible 계산 — 필수(age/region) 부재 시 미충족, 선택(occupation/married/hasChildren) 부재 시 관대 통과
- **즐겨찾기**: SharedPreferences Set<String> + 정책 상세 ⭐ 토글 + 마이 카드 동적 카운트/금액
- **공유/intent**: ShareHelper (놓친돈 공유 / 친구 초대 / mailto 의견)
- **알림 권한**: Android 13+ POST_NOTIFICATIONS launcher + Toast 피드백
- **온보딩 영속화**: SharedPreferences `hs_prefs` (onboarded + age/region/occupation/married/has_children)
- **프로필 편집**: ProfileInputPage internal 재사용 (코드 중복 X)
- **데이터 파이프라인 1단계**: PolicyRepository 인터페이스 + InMemory 추상화, byId 캐시
- **데이터 파이프라인 2단계**: RemotePolicyRepository (Ktor) + CachedPolicyRepository (filesDir) + AppRoot background refresh
- **`docs/policies.json` (12.5KB, 19 정책)**: SampleData export 완료, push 대기 중

### ❌ 아직 안 한 것 (TODO)
- GitHub Pages 활성화 (사용자 측, 5분)
- GitHub Actions 크롤러 (3단계, 다음 라운드)
- Firebase 연동
- WorkManager 알림 스케줄링
- 즐겨찾기 목록 화면 (마이 → 받을 예정 카드 탭)
- 신청한 지원금 → 수령 확인 동작
- 출시 준비 (keystore, AAB, Play Console)
- 개인정보처리방침 호스팅

---

## 🗺️ 출시까지의 길

### 라운드별 추천 순서 (집에서 시작 후)

| 라운드 | 작업 | 사용자 개입 | 예상 |
|---|---|---|---|
| **R1** | GitHub Pages 활성화 + remote fetch 검증 | Settings → Pages 클릭 | 5분 |
| **R2** | GitHub Actions 크롤러 + Gemini Flash (3단계) | Gemini API key 발급 + GitHub Secret 등록 | 2~3 세션 |
| **R3** | 자동화 데이터 검증·튜닝 + WorkManager 알림 스케줄링 | 없음 | 1~2 세션 |
| **R4** | Firebase 연동 (Auth + Firestore + FCM) | Firebase 콘솔 + `google-services.json` | 2~3 세션 |
| **R5** | 폴리시 (즐겨찾기 목록 화면 / 수령 확인 토글 / 신청→받음 흐름) | 없음 | 1~2 세션 |
| **R6** | 출시 준비 (Phase 4) | keystore + Play Console 등록(25 USD) + 정책 호스팅 | 1~2 세션 |
| **R7** | 내부/베타 테스트 + 버그 수정 | Play Console closed beta | 지속 |
| **R8** | 정식 출시 | Play Console production | 1 세션 |

**총: 약 10~14 세션 후 정식 출시**. 사용자 개입은 콘솔 설정·결제 위주.

### 🔥 R4. Firebase 통합 절차

#### 1) Firebase 콘솔 (사용자)
1. https://console.firebase.google.com → "프로젝트 추가" → 이름 `숨은지원금` → **Google Analytics OFF** (Spark 무료 티어 유지)
2. Android 앱 추가 → 패키지명 `com.hiddensubsidy.app.debug` (디버그용 먼저). 나중에 release용 `com.hiddensubsidy.app` 별도 추가
3. SHA-1 지문 등록 (디버그용 — Google Sign-In 위해 필수):
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
4. `google-services.json` 다운로드 → 채팅에 첨부 또는 `app/` 폴더에 직접 둠
5. **콘솔에서 기능 활성화**:
   - Auth → Sign-in method → Google 활성화
   - Firestore → 데이터베이스 만들기 → 프로덕션 모드 → asia-northeast3(서울)
   - Cloud Messaging → 자동 활성화

#### 2) Gradle 설정 (Claude가 처리)
- 프로젝트 `build.gradle.kts`: `alias(libs.plugins.google.services) apply false`
- `app/build.gradle.kts`: plugin apply + Firebase BOM
- `libs.versions.toml`: `firebase-bom = "33.x.x"` + `google-services = "4.4.x"`

#### 3) 기능별 SDK + 코드 (Claude가 처리)
- **Auth (Google Sign-In)**: `firebase-auth-ktx` + `play-services-auth`
  - `AuthRepository` + `LoginScreen` + Splash에서 자동 로그인 체크
  - 로그아웃 → 온보딩 리셋
- **Firestore**: `firebase-firestore-ktx`
  - `users/{uid}` 문서: profile 필드 + favorites 배열
  - UserPrefs / FavoritesRepository → Firestore migration (오프라인 캐시 활용)
- **FCM**: `firebase-messaging-ktx`
  - `MessagingService` + 토픽 구독 (`weekly-deadline`, `region-seoul` 등)
  - 매주 일요일 큐레이션 푸시 (FCM topic + Cloud Function 또는 Actions cron)

#### 4) 보안 룰 (Firestore Rules)
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
    match /policies/{id} {
      allow read: if true;  // 공개 정책 데이터
      allow write: if false; // 크롤러는 GitHub Actions에서만
    }
  }
}
```

---

### 🚀 R6. Phase 4 출시 절차

#### 1) Keystore 생성 (사용자, JDK keytool)
```bash
# 프로젝트 root에서
keytool -genkey -v -keystore hidemoney-release.jks `
  -keyalg RSA -keysize 2048 -validity 10000 `
  -alias hidemoney
```
- 비밀번호 2개 입력 (keystore + alias) — **절대 잃어버리면 안 됨, 이후 업데이트 영구 불가**
- `.gitignore`에 이미 `*.jks` 포함됨
- `keystore.properties` 생성 (역시 gitignore됨):
  ```
  storePassword=...
  keyPassword=...
  keyAlias=hidemoney
  storeFile=../hidemoney-release.jks
  ```
- **백업**: 외장 SSD/Google Drive에 keystore + properties 둘 다 안전 보관

#### 2) 서명 빌드 설정 (Claude가 처리)
- `app/build.gradle.kts`: `signingConfigs { release { ... } }` + release buildType 적용
- ProGuard 룰 보강 (kotlinx-serialization, Ktor reflection class keep)
- `./gradlew bundleRelease` → `app/build/outputs/bundle/release/app-release.aab`

#### 3) Play Console 등록 (사용자, 25 USD 일회성)
1. https://play.google.com/console → 가입 + 25 USD 결제 (개발자 계정)
2. "앱 만들기" → 앱 이름 `숨은지원금` / 기본 언어 한국어 / 무료
3. **메인 스토어 등록정보 (Claude가 초안 제공)**:
   - 짧은 설명 (80자): "정부 지원금 추천 — 못 받은 돈부터 발견"
   - 자세한 설명: 차별화 3가지 + 0원 운영 + 토스 톤 가치 강조
   - **앱 아이콘 512×512** (현재 `appIcon2.png`에서 변환 — Claude 처리)
   - **피처 그래픽 1024×500** (Canva·Figma)
   - **스크린샷 최소 2장** (홈/캘린더/마이/온보딩 4~5장 권장) — 폰 캡처 또는 Compose Preview
4. **앱 콘텐츠 (필수)**:
   - **개인정보처리방침 URL** — `docs/privacy.md` 작성 후 GitHub Pages 호스팅 (Claude가 초안 작성)
   - 광고 X / 인앱결제 X (MVP)
   - 데이터 안전성: 사용자 데이터 수집 항목 명시 (프로필·즐겨찾기·기기 ID)
   - 만 13세 미만 대상 아님
5. **출시 단계**:
   - **내부 테스트** (사용자 본인 + 지인 1~2명) → 1~2주
   - **Closed beta** (지인 5~20명) → 2~4주, 피드백 수집
   - **Production**: AAB 업로드 → 심사 1~3일 → 출시

#### 4) 출시 후
- Play Console에서 통계·크래시·리뷰 모니터링
- Firebase Crashlytics 추가 검토 (Spark 무료)
- 첫 1000 다운로드까지 0원 유지 가능 (Spark 한도)
- 사용자 증가 시 Firestore 쿼터 모니터링 → 필요 시 Blaze 전환 검토

---

## 0. 한 줄 포지션

> "검색하는 앱"이 아니라 **"받게 만드는 앱"**
> 기존 보조금24·정책알리미는 100개 띄우고 끝. 우리는 매주 1개를 받게 만든다.

---

## 1. 핵심 차별화 (MVP에 들어갈 것 3개)

### ① "못 받은 돈" 후행 진단 ⭐️ 첫인상·바이럴 담당
- 사용자 과거 3년 상황 입력 → "당신은 2024년 청년월세지원 자격 있었는데 미신청, 약 240만원 손실"
- 캡처/공유 충동 유발하는 카피
- 기존 앱에 전혀 없는 기능

### ② "이번 주 1개" 큐레이션 ⭐️ 재방문 담당
- AI가 (예상 수령액 ÷ 신청 난이도)로 ROI 계산
- 매주 단 1개만 푸시
- 100개 보여주면 0개 신청한다 → 1개 보여주면 1개 신청한다

### ③ 생애 이벤트 트리거 ⭐️ 데이터 수집 담당
- "이사/퇴사/임신/창업/결혼" 한 번 누르면 묶음 안내
- 자연스럽게 사용자 정보 수집
- 어필리에이트 단가 가장 높은 구간 (보험·청약·이사)

### 2단계 (MVP 이후)
- 신청 가이드 AI 챗봇
- 사업자/프리랜서 특화 모드
- 동·구 단위 마이크로 지원금 크롤링
- 사용자 후기·난이도 평점 (UGC 모트)

---

## 2. 수익 모델

전부 **계약/제휴 없이** 가능:
1. **카드/증권/보험 어필리에이트** — 생애 이벤트 시점 (단가 3~10만원/건)
2. **프리미엄 IAP** — 가족 단위 진단, 상세 신청 가이드 (월 2,900원 가정)
3. **광고** — 마지막 옵션 (디자인 해침)

수익 우선순위: 어필리에이트 > IAP > 광고

---

## 3. 기술 스택 (전부 무료)

| 영역 | 도구 | 한도 |
|---|---|---|
| 프론트엔드 | **Kotlin + Jetpack Compose** (Android 전용) | Android-only 확정 + 디자인 1순위 + 시스템 통합 핵심 |
| 사용자 DB | Firebase Firestore | 1GB / 일 5만 읽기 |
| 로그인 | Firebase Auth (구글/애플) | 사실상 무제한 |
| 푸시 | FCM | 무제한 무료 |
| 정책 데이터 | Firebase Hosting에 정적 JSON | 360MB/일 전송 |
| 크롤러 | **GitHub Actions cron** | Public repo면 무제한 |
| AI | **Gemini API** 무료 티어 (Flash) | 일 100만 토큰, **크론 배치로 미리 계산** |
| 폰트 | Pretendard Variable | 무료 |
| 아이콘 | Lucide / Phosphor | 무료 |

### 데이터 흐름
```
[GitHub Actions, 매일 새벽 3시]
   ├→ 정부24 / 복지로 / 구청 사이트 크롤
   ├→ Gemini로 정책 요약·태깅·ROI 점수 (배치)
   ├→ policies.json 생성 (~10MB)
   └→ Firebase Hosting에 푸시

[앱 실행 시]
   ├→ policies.json 1회 다운로드 (CDN 캐시)
   ├→ 사용자 프로필은 Firestore (개인 ~50KB)
   ├→ 매칭 계산은 클라이언트(폰)에서
   └→ 푸시는 FCM 토픽 그룹으로 발송
```

**핵심**: 정책 데이터는 정적 JSON이라 Firestore 읽기 쿼터 안 깎아먹음 → 사용자 늘어도 무료 유지.

---

## 4. 디자인 시스템

### 참고 앱 (모방 우선순위)
1. **토스** — 정보 위계, 큰 숫자, 마이크로 인터랙션
2. **당근페이** — 친근한 톤, 둥근 카드
3. **Apple Wallet** — 카드 메타포
4. **Linear** — 다크모드, 키네틱 타이포

### 디자인 토큰
- **폰트**: Pretendard Variable (Bold, SemiBold, Medium, Regular)
- **컬러 팔레트**:
  - Base: 모노톤 6단계 (#0A0A0A → #FAFAFA)
  - Accent: 민트/그린 계열 (#00C896 또는 #1FCB8E) — "돈이 들어온다" 컨셉
  - Warning: #FF6B6B (못 받은 돈 강조용)
- **모서리**: 16px (카드), 20px (큰 컨테이너), 12px (버튼)
- **그림자**: `0 4px 24px rgba(0,0,0,0.06)`
- **다크모드**: **첫날부터** 동시 작업 (나중 추가 X)
- **여백**: 16px, 24px, 32px, 48px (4의 배수)

### 디자인 1순위 원칙 (절대 지킬 것)
1. **3탭 안에 답** — 첫 화면에 "받을 수 있는 돈 OO원" 카드 1개
2. **숫자가 주인공** — 금액 폰트 48~64pt, 나머지 14pt
3. **입력 점진적** — 첫 진입 시 나이·지역 2개만
4. **알림 주 1회** — 푸시 폭격 절대 금지
5. **로딩 = 스켈레톤 UI** — 스피너 X
6. **마이크로 인터랙션** — 햅틱, 스프링 애니메이션 적극

---

## 5. 화면 구조 (계획)

### MVP 화면
1. **온보딩** (3장) — 가치 제안 → 권한 요청 → 기본 정보
2. **홈** — "이번 주 받을 돈" 카드 1개 + "못 받은 돈 합계" 위젯
3. **상세** — 정책 1개의 모든 정보 + 신청하기 딥링크
4. **이벤트** — 생애 이벤트 선택 → 묶음 추천
5. **마이페이지** — 프로필 수정, 받은 내역 기록

### 화면별 진행 상황
- [x] 온보딩 (5.4)
- [x] 홈 (5.1)
- [x] 놓친 내역 시트 (5.2) + 공유 자산 (5.3)
- [x] 정책 상세 (5.5)
- [x] 이벤트 (5.6)
- [x] 마이페이지 (5.7)
- [x] 화면 흐름 다이어그램 (5.8)

**MVP 화면 와이어프레임 100% 완료** ✓

---

## 5.1 홈 화면 상세 설계

### 와이어프레임 (모바일 세로)

```
┌─────────────────────────────┐
│ ☀ 안녕하세요          ⚙   │  ← 16pt, 우측 작은 프로필
│                             │
│ ╭─────────────────────────╮ │
│ │                         │ │
│ │ 당신이 놓친 돈            │ │  ← 14pt, 60% opacity
│ │                         │ │
│ │  2,400,000원            │ │  ← 64pt, Bold, 액센트
│ │  ━━━━                   │ │     (카운트업 애니메이션)
│ │                         │ │
│ │ 지난 3년간 받을 수 있었던  │ │  ← 13pt
│ │ 지원금 12건              │ │
│ │                         │ │
│ │ 내역 자세히 보기  →       │ │  ← 카드 전체 탭 가능
│ ╰─────────────────────────╯ │
│                             │
│ 이번 주 받을 수 있어요         │  ← 섹션 라벨 14pt
│                             │
│ ╭─────────────────────────╮ │
│ │ 청년 월세 지원            │ │
│ │ 600,000원      D-12     │ │
│ │                         │ │
│ │ [신청 가이드 보기]         │ │
│ ╰─────────────────────────╯ │
│                             │
│ 곧 마감돼요                   │
│ ╭─────────────────────────╮ │
│ │ 출산장려금       D-3 🔴 │ │
│ │ 통신비 감면      D-7    │ │
│ │ 주거안정장학금    D-12   │ │
│ ╰─────────────────────────╯ │
│                             │
│ ┌──────┬──────┬──────┐     │  ← 하단 탭바
│ │ 홈   │ 이벤트│ 마이 │     │
│ └──────┴──────┴──────┘     │
└─────────────────────────────┘
```

### 정보 위계 (위 → 아래)
1. **임팩트 카드** (메인): "놓친 돈 합계" — 충격으로 시선 잡기
2. **액션 카드**: "이번 주 받을 돈" — 즉시 행동 유도
3. **마감 임박 리스트**: FOMO 자극

### 디자인 토큰 (홈 화면 한정)

| 요소 | 라이트 | 다크 |
|---|---|---|
| 배경 | #FAFAFA | #0A0A0A |
| 임팩트 카드 배경 | 그라데이션 (#E8FFF6 → #D0FFF0) | 그라데이션 (#0F2920 → #082018) |
| 임팩트 카드 숫자 | #00805C | #4FFFD0 |
| 액션 카드 배경 | #FFFFFF | #1A1A1A |
| 액션 카드 보더 | 1px #E8FFF6 | 1px #1A3028 |
| 본문 텍스트 | #0A0A0A | #FAFAFA |
| 보조 텍스트 | rgba(0,0,0,0.6) | rgba(255,255,255,0.6) |

### 타이포그래피
- 임팩트 숫자: **Pretendard Bold 64pt**, letter-spacing -0.04em
- 임팩트 라벨 ("당신이 놓친 돈"): Medium 14pt
- 카드 제목: SemiBold 18pt
- 카드 금액: Bold 24pt
- 본문/D-day: Medium 13pt
- 섹션 라벨: SemiBold 14pt, 60% opacity

### 인터랙션
- 앱 진입 → 메인 숫자 **0에서 카운트업** (1.2초, easeOutCubic)
- 메인 카드 탭 → "놓친 내역" 시트 슬라이드업 (모달)
- 액션 카드 탭 → 정책 상세 화면 (push 트랜지션)
- 풀 다운 → 새로고침 (햅틱 medium)
- 모든 탭 → 햅틱 light
- 카드 prefetch: 화면 진입 시 다음 화면 데이터 미리 로드

### 빈 상태 처리
- **온보딩 직후 (입력 0)**: 임팩트 카드 자리에 "**내가 받을 수 있는 지원금부터 찾아볼까요?**" + 큰 CTA
- **자격 있는 지원금 0건**: "현재 받을 수 있는 지원금이 없어요. 새 지원금이 등록되면 알려드릴게요" + 알림 설정 토글
- **로딩**: 스켈레톤 UI (회색 박스 shimmer 애니메이션)

### 다음 액션 (홈 화면 후속 작업)
- [ ] 임팩트 카드 카운트업 애니메이션 사양 확정
- [ ] 다크모드 컬러 시각 검증 (대비비 4.5:1 이상)
- [x] "놓친 내역" 시트 화면 설계 → 5.2
- [ ] 정책 상세 화면 설계
- [ ] Figma 시안 작업 (이 와이어프레임 기반)

---

## 5.2 "놓친 내역" 시트 (홈 메인 카드 탭 시)

> 이 화면이 **이 앱의 바이럴 엔진**. 캡처해서 공유하고 싶게 만드는 게 1순위 디자인 목표.

### 와이어프레임

```
┌─────────────────────────────┐
│         ━                   │  ← drag handle (시트)
│                          ✕  │
│                             │
│ 당신이 놓친 돈                │
│                             │
│   2,400,000원              │  ← 56pt Bold, 액센트 컬러
│                             │
│ 12건  ·  최근 3년            │  ← 14pt 60% opacity
│                             │
│ ╭─────────────────────────╮ │
│ │  📤  친구에게 공유하기   │ │  ← 시트 상단에도 공유 버튼
│ │  📱  카카오톡 / 인스타     │ │     (스크롤 안 해도 보임)
│ ╰─────────────────────────╯ │
│                             │
│ ─────────────────────       │
│ 2024년       1,800,000원   │  ← 연도 그룹 헤더 (sticky)
│ ─────────────────────       │
│                             │
│ ╭─────────────────────────╮ │
│ │ 청년 도약 계좌            │ │
│ │                         │ │
│ │  1,200,000원            │ │  ← 28pt Bold
│ │                         │ │
│ │ 2024.07 ~ 자격 충족       │ │
│ │ "5년간 최대 5천만원 모음" │ │
│ │                         │ │
│ │ 자세히 보기  ▾            │ │  ← 탭 시 확장
│ ╰─────────────────────────╯ │
│                             │
│ ╭─────────────────────────╮ │
│ │ 청년 월세 지원            │ │
│ │  600,000원              │ │
│ │ 2024.03 ~ 자격 충족       │ │
│ │ 자세히 보기  ▾            │ │
│ ╰─────────────────────────╯ │
│                             │
│ ─────────────────────       │
│ 2023년         600,000원   │
│ ─────────────────────       │
│ ╭─────────────────────────╮ │
│ │ ...                     │ │
│ ╰─────────────────────────╯ │
│                             │
│ ╭─────────────────────────╮ │
│ │ 🔔 올해는 놓치지 않을게요  │ │  ← 메인 CTA (sticky 하단)
│ ╰─────────────────────────╯ │
└─────────────────────────────┘
```

### 카드 확장 상태 (탭 시)

```
╭─────────────────────────╮
│ 청년 도약 계좌            │
│  1,200,000원            │
│ 2024.07 ~ 자격 충족       │
│                         │
│ [요약]                   │
│ 만 19~34세 청년이 5년간   │
│ 매월 70만원 납입 시 정부가 │
│ 매칭 지원하는 자산형성 상품 │
│                         │
│ [당시 자격]              │
│ ✓ 만 25세                │
│ ✓ 연소득 6천만원 이하    │
│ ✓ 가구소득 중위 180% 이하 │
│                         │
│ 신청 안 하신 이유? (선택)  │
│ ○ 몰랐어요                │
│ ○ 너무 어려워 보였어요    │
│ ○ 자격 없는 줄 알았어요    │
│ ○ 다른 이유               │
│                         │
│ [지금이라도 알아보기 →]   │  ← 일부 정책은 재신청 가능
│                         │
│ 접기  ▴                  │
╰─────────────────────────╯
```

### 인터랙션
- 시트는 바텀시트(드래그 핸들), 풀 화면 80% 높이
- 카드 탭 → 펼침/접힘 (스프링 애니메이션, 햅틱 light)
- 연도 헤더는 sticky (스크롤해도 상단 고정)
- 메인 CTA "🔔 올해는 놓치지 않을게요"는 시트 하단 sticky
- 공유 버튼 → 시스템 공유 시트 + 자동 생성 이미지 첨부

### 빈 상태 (놓친 돈 0원)
```
🎉
완벽해요!
받을 수 있는 건 다 받으셨네요

[알림 받기 → 새 지원금 등록되면 알려드릴게요]
```
긍정 톤으로 전환. 알림 설정 유도.

---

## 5.3 공유 자산 (바이럴 핵심)

### 자동 생성 이미지 (1080×1080, 카톡/인스타 공유용)

```
┌───────────────────────────┐
│  숨은지원금 ●●●●          │  ← 워터마크 (좌측 상단)
│                           │
│                           │
│      나는                  │
│                           │
│   2,400,000원            │  ← 80pt, 액센트
│                           │
│      놓쳤어요               │
│                           │
│                           │
│  너는 얼마나 놓쳤어?         │
│                           │
│                           │
│  ────────────────         │
│                           │
│  ▢ 숨은지원금              │  ← QR 코드 + 앱 이름
│  스토어에서 확인하기         │
│                           │
└───────────────────────────┘
```

- 배경: 라이트모드와 다크모드 둘 다 (사용자 설정 따라)
- 폰트: Pretendard Black 80pt (금액)
- QR: 앱 스토어 다이렉트 링크
- 워터마크: 작지만 분명하게 — 캡처 후에도 "어디서 받지?" 자연 검색 유도

### 공유 텍스트 (자동 채워짐)
> "나 정부 지원금 240만원이나 놓쳤대 ㅋㅋ 너도 한번 봐봐 [딥링크]"

캐주얼 톤이 카톡 공유에 더 잘 맞음. 사용자가 수정 가능.

### 공유 채널 우선순위
1. **카카오톡** (한국 1순위)
2. **인스타 스토리** (1080×1920 별도 이미지 필요 → 추가 작업)
3. 시스템 공유 시트 (그 외)

---

## 5.4 온보딩 (3장 + 정보 입력)

> 목표: **30초 안에 첫 임팩트 카드 보게 하기.** 회원가입은 끝까지 미룬다.

### 화면 1 — 후크
```
┌─────────────────────────────┐
│                             │
│                             │
│                             │
│   당신은 정부 지원금          │
│                             │
│   2,400,000원              │  ← 카운트업 애니메이션
│                             │
│   놓치고 있을지도 몰라요       │
│                             │
│   ━━━━                     │
│                             │
│   30초면 알 수 있어요          │
│                             │
│                             │
│                             │
│   [시작하기]                 │  ← 큰 버튼, 액센트 컬러
│                             │
│   • • •                     │  ← 페이지 인디케이터
└─────────────────────────────┘
```
- "당신은"으로 시작 → 즉시 개인화감
- 240만원은 **국민 평균 미수령액 추정치** (실제 통계 기반)
- 카운트업 애니메이션 = 첫 모션 임팩트

### 화면 2 — 작동 원리 (신뢰 빌드)
```
┌─────────────────────────────┐
│  ←                          │
│                             │
│  어떻게 찾아드리냐면요          │
│                             │
│  ╭─────────────────────╮    │
│  │ 1️⃣                   │   │
│  │ 정부24·복지로의       │    │
│  │ 모든 지원금을 매일      │    │
│  │ 자동으로 모아요         │    │
│  ╰─────────────────────╯    │
│                             │
│  ╭─────────────────────╮    │
│  │ 2️⃣                   │   │
│  │ 당신 상황에 맞는        │    │
│  │ 것만 골라드려요         │    │
│  ╰─────────────────────╯    │
│                             │
│  ╭─────────────────────╮    │
│  │ 3️⃣                   │   │
│  │ 신청 가이드까지         │    │
│  │ 친절하게 알려드려요      │    │
│  ╰─────────────────────╯    │
│                             │
│  [다음]                     │
│  ● ● •                      │
└─────────────────────────────┘
```

### 화면 3 — 최소 정보 입력 (회원가입 X)
```
┌─────────────────────────────┐
│  ←                          │
│                             │
│  딱 두 가지만 알려주세요         │
│                             │
│  나이                       │
│  ┌─────────────────────┐    │
│  │ 25세              ▾ │    │
│  └─────────────────────┘    │
│                             │
│  사는 지역                   │
│  ┌─────────────────────┐    │
│  │ 서울 마포구         ▾ │    │
│  └─────────────────────┘    │
│                             │
│  ─────                      │
│                             │
│  더 정확하게 찾고 싶다면? (선택) │
│  ☐ 직장인 / 학생 / 사업자     │
│  ☐ 결혼 여부                │
│  ☐ 자녀 여부                │
│  ☐ 소득 (월 단위)            │
│                             │
│  [내가 받을 지원금 보기]      │  ← 메인 CTA
│                             │
│  ● ● ●                      │
└─────────────────────────────┘
```
- **필수 2개, 선택 4개** — 진입 마찰 최소화
- 선택 항목 체크할수록 정확도 올라감 (홈 화면에 진행률 바로 노출)
- **회원가입은 안 함** — 나중에 "내역 백업" 같은 가치 제안 시점에만 권유

### 권한 요청 타이밍 (절대 첫 진입 시 X)
- **알림 권한**: 첫 매칭 결과 본 직후 → "신청 마감일 알려드릴까요?"
- **위치 권한**: 지역 선택 화면에서 "현재 위치로 자동 입력하시겠어요?" (옵션)

### 빈 결과 처리
첫 진단 결과 0건이면 → "지금 자격이 없어요. 하지만 OO일 후 나이 조건이 충족됩니다" 같은 미래 알림 약속.

---

## 5.5 정책 상세 화면

> 사용자가 실제로 신청까지 가는 마지막 화면. **신청 완주율**이 핵심 지표.

### 와이어프레임
```
┌─────────────────────────────┐
│  ←                  ⭐  📤 │  ← 즐겨찾기 / 공유
│                             │
│ [카테고리 칩] 청년·주거       │
│                             │
│ 청년 월세 지원                │  ← 28pt Bold
│                             │
│  600,000원                  │  ← 48pt 액센트
│  최대 12개월                  │
│                             │
│ ╭─────────────────────────╮ │
│ │ ✓ 자격 충족              │ │  ← 녹색 카드
│ │ 당신은 받을 수 있어요       │ │
│ ╰─────────────────────────╯ │
│                             │
│ ▸ 마감일                    │
│   2026-06-30 (D-12)         │
│                             │
│ ▸ 한 줄 요약                │
│   만 19~34세 청년 무주택자에게  │
│   월 최대 20만원 12개월 지원   │
│                             │
│ ▸ 자격 조건                  │
│   ✓ 만 19~34세              │
│   ✓ 무주택자                │
│   ✓ 가구 중위소득 60% 이하    │
│   ✓ 본인 소득 중위 100% 이하  │
│                             │
│ ▸ 필요 서류                  │
│   • 주민등록등본 [발급처 →]   │
│   • 임대차계약서             │
│   • 소득금액증명원 [발급처 →] │
│                             │
│ ▸ 신청 절차 (5단계)           │
│   1. 복지로 회원가입          │
│   2. 주거급여 메뉴 선택       │
│   ...                       │
│                             │
│ ▸ 다른 사람들 후기             │  ← 2단계, MVP에선 비움
│   "23일 만에 입금됐어요"      │
│                             │
│ ─────────────────────       │
│                             │
│ ╭─────────────────────────╮ │  ← sticky 하단
│ │ 복지로에서 신청하기 →     │ │
│ ╰─────────────────────────╯ │
└─────────────────────────────┘
```

### 핵심 디자인 결정
- **자격 충족 카드**가 가장 위 — "받을 수 있다"는 확신부터
- **금액·마감일·자격조건** 순서로 사용자 의사결정 정보 위계
- **필요 서류는 발급처 딥링크** — 정부24/복지로/홈택스 직링크
- **하단 sticky CTA**는 외부 신청 페이지 딥링크
- 신청 완료 체크 → 마이페이지 "신청 내역"에 자동 추가 (수령 추적)

### 인터랙션
- 신청 버튼 탭 → 외부 브라우저로 신청 페이지 → 돌아오면 자동으로 "신청하셨나요?" 시트 표시
- 즐겨찾기 ⭐ → "내가 받을 지원금" 보드에 핀
- 자격 미충족이면 카드를 빨간색이 아닌 **회색**으로 (좌절감 방지). "OO이 충족되면 자격 생겨요" 안내.

---

## 5.6 이벤트 화면 (생애 이벤트 트리거)

> 사용자가 인생 이벤트를 누르면 그 순간 받을 수 있는 지원금 묶음이 펼쳐짐.
> **수익 핵심**: 이사·결혼·출산·창업은 어필리에이트 단가가 가장 높은 구간.

### 와이어프레임
```
┌─────────────────────────────┐
│ 이벤트                       │
│                             │
│ 인생에 변화가 있을 때         │
│ 받을 수 있는 지원금이 있어요  │
│                             │
│ ╭───────────╮ ╭───────────╮ │
│ │   🏠      │ │    💼     │ │
│ │   이사    │ │   퇴사    │ │
│ │  18건 💰  │ │  12건 💰  │ │
│ ╰───────────╯ ╰───────────╯ │
│                             │
│ ╭───────────╮ ╭───────────╮ │
│ │   👶      │ │    💍     │ │
│ │   임신    │ │   결혼    │ │
│ │  24건 💰  │ │  9건 💰   │ │
│ ╰───────────╯ ╰───────────╯ │
│                             │
│ ╭───────────╮ ╭───────────╮ │
│ │   🚀      │ │    🎓     │ │
│ │   창업    │ │   취업    │ │
│ │  15건 💰  │ │  11건 💰  │ │
│ ╰───────────╯ ╰───────────╯ │
│                             │
└─────────────────────────────┘
```

### 이벤트 카드 탭 시 (예: "이사")
```
┌─────────────────────────────┐
│ ←  이사할 때 받는 지원금       │
│                             │
│ 최대 받을 수 있어요           │
│  4,800,000원                │  ← 큰 숫자
│                             │
│ 18건 · 정부+지자체            │
│                             │
│ ─────────────────────       │
│ 📍 이사 전 (3개월 안)         │
│ ─────────────────────       │
│ ╭─────────────────────────╮ │
│ │ 청년 전월세 보증금 대출    │ │
│ │ 최대 2억원 1.5%          │ │
│ ╰─────────────────────────╯ │
│ ╭─────────────────────────╮ │
│ │ 신혼부부 전세자금 대출    │ │
│ │ 최대 3억원 1.2%          │ │
│ ╰─────────────────────────╯ │
│                             │
│ ─────────────────────       │
│ 🏠 이사 직후 (1개월 안)       │
│ ─────────────────────       │
│ ╭─────────────────────────╮ │
│ │ 청년 월세 지원            │ │
│ │ 600,000원               │ │
│ ╰─────────────────────────╯ │
│ ╭─────────────────────────╮ │
│ │ 마포구 청년 정착지원금     │ │
│ │ 300,000원 (지자체)       │ │
│ ╰─────────────────────────╯ │
│                             │
│ ─────────────────────       │
│ 💡 이사 비용 줄이기 팁         │
│ ─────────────────────       │
│ ╭─────────────────────────╮ │
│ │ 이사 견적 비교 (광고)     │ │  ← 어필리에이트
│ │ 평균 30% 절감             │ │
│ ╰─────────────────────────╯ │
└─────────────────────────────┘
```

### 핵심 디자인 결정
- **시간순 그룹** (이사 전 → 직후 → 1년 안) — 사용자 행동 순서대로
- **금액 합계 헤더** — "최대 받을 수 있는 돈"으로 임팩트
- **어필리에이트 자연 노출** — "팁" 섹션에 슬쩍, 광고 라벨 명시
- 이벤트 선택 → 마이페이지 프로필에 자동 반영 (다음에 자동 매칭)

---

## 5.7 마이페이지

> 신청 추적 + 프로필 + 알림 설정. 단순할수록 좋음.

### 와이어프레임
```
┌─────────────────────────────┐
│ 마이                        │
│                             │
│ ╭─────────────────────────╮ │
│ │ 만 25세 · 서울 마포구    │ │
│ │ [프로필 더 채우기 →]     │ │  ← 정확도 강조
│ │ 정확도 60%  ▓▓▓▓░░░░    │ │
│ ╰─────────────────────────╯ │
│                             │
│ ─────────────────────       │
│ 내 지원금                    │
│ ─────────────────────       │
│                             │
│ ╭─────────────────────────╮ │
│ │ ⭐ 받을 예정 (3건)        │ │
│ │   1,500,000원            │ │
│ ╰─────────────────────────╯ │
│                             │
│ ╭─────────────────────────╮ │
│ │ 📝 신청한 지원금 (1건)    │ │
│ │   600,000원              │ │
│ │   [수령 확인 →]          │ │
│ ╰─────────────────────────╯ │
│                             │
│ ╭─────────────────────────╮ │
│ │ ✅ 받은 지원금 (5건)      │ │
│ │   2,300,000원 누적       │ │  ← 게이미피케이션
│ ╰─────────────────────────╯ │
│                             │
│ ─────────────────────       │
│ 설정                        │
│ ─────────────────────       │
│   🔔 알림 설정              │
│   👨‍👩‍👧 가족 진단 (프리미엄)  │
│   💌 친구 초대              │  ← 보상형 추천
│   📋 개인정보 처리방침       │
│   ✉️ 의견 보내기            │
└─────────────────────────────┘
```

### 핵심 디자인 결정
- **프로필 정확도 % 바** — 입력 항목 늘리고 싶게 만드는 디자인
- **누적 받은 금액** — 게이미피케이션, 자랑하고 싶게
- **신청 → 수령 추적** — 다른 앱에 없는 차별점, 데이터 모트
- **친구 초대** — 추천인 보상은 IAP 1개월 무료 등 (수익 부담 0)

---

## 5.8 화면 흐름 다이어그램

```
[온보딩 1] → [온보딩 2] → [온보딩 3·정보 입력]
                                ↓
                          [홈 화면]
                          ↙   ↓   ↘
              [놓친 내역 시트] [정책 상세] [이벤트]
                    ↓             ↓         ↓
                [공유]         [외부 신청]  [정책 상세]
                                ↓
                          [수령 확인 시트]
                                ↓
                          [마이페이지 자동 반영]
```

---

---

## 6. 마일스톤

### Phase 1: 디자인 (1~2주 목표)
- [ ] 홈 화면 와이어프레임 (종이/Figma)
- [ ] 디자인 토큰 확정
- [ ] 핵심 5개 화면 Figma 시안
- [ ] 다크모드 동시 설계

### Phase 2: 데이터 (1주)
- [ ] 정부24 / 복지로 크롤러 프로토타입
- [ ] Gemini 프롬프트 설계 (정책 요약·태깅·ROI 계산)
- [ ] policies.json 스키마 확정
- [ ] GitHub Actions 크론 셋업

### Phase 3: 앱 개발 (3~4주)
- [x] ~~Android Studio 설치~~ → 불필요 (SDK·Gradle·JDK 이미 설치돼 있음, CLI로 빌드)
- [x] Android 프로젝트 셋업 (com.hiddensubsidy.app, minSdk 26, targetSdk 35)
- [x] 디자인 토큰 코드화 (Color/Type/Spacing/Shape/Theme.kt) — 라이트·다크 동시
- [x] 홈 화면 1차 구현 (Compose) — 임팩트 카드 + 액션 카드 + 마감 임박 리스트
- [x] **첫 디버그 APK 빌드 성공** (17.8MB, 20초 incremental)
- [ ] Firebase 연동 (Auth, Firestore, FCM)
- [ ] 매칭 로직 (클라이언트 사이드)
- [ ] 푸시 알림 (주 1회 토픽)
- [ ] 놓친 내역 시트 / 정책 상세 / 이벤트 / 마이페이지 / 온보딩

### Phase 4: 출시 (1주)
- [ ] 앱 아이콘, 스플래시
- [ ] 스토어 스크린샷 (디자인 마케팅 핵심)
- [ ] 개인정보처리방침 (정책 데이터 다루므로 필수)
- [ ] Play Store 등록

---

## 7. 결정 로그

| 날짜 | 결정 | 이유 |
|---|---|---|
| 2026-05-15 | Flutter 채택 → **번복** | (당시: 한 코드 iOS+Android, 모던 디자인 쉬움) |
| 2026-05-15 | **Kotlin + Jetpack Compose로 변경** | Android-only 확정 + 디자인 천장이 더 높음 + 시스템 통합 완벽 + APK 가벼움. iOS 가능성 없으면 Flutter의 핵심 장점 무효화. 사용자가 코딩 안 하므로 학습 곡선 무관. |
| 2026-05-15 | Firebase Spark + GitHub Actions 크롤러 | Cloud Functions 외부 호출 막혀서 우회 필요, 평생 0원 가능 |
| 2026-05-15 | 다크모드 첫날부터 | 나중에 추가하면 디자인 다 깨짐 |
| 2026-05-15 | 액센트 컬러: 민트/그린 | "돈이 들어온다" 시각 메타포 |
| 2026-05-15 | 앱 이름: **숨은지원금** | "숨었다" 후크 + 남녀노소 즉시 이해 + 검색 노출 유리. 후보 10개 중 톱픽 선정 |
| 2026-05-15 | 홈 화면 톤: **임팩트형 (못 받은 돈)** | 첫 화면에 "당신이 놓친 돈 OO원" 큰 숫자로 충격·바이럴 우선 |

---

## 8. 오픈 이슈 / 의사결정 대기

- [ ] iOS도 동시 출시 vs Android 먼저
- [ ] 회원가입 의무 vs 비회원 사용 가능 구간
- [ ] 정확한 어필리에이트 파트너 (트라이브 / 디비랩 등 비교 필요)

---

## 9. 진행 일지

### 2026-05-15
- 프로젝트 컨셉 확정 (정부 지원금 앱, 차별화 포인트 7개 중 3개를 MVP에 포함)
- 무료 기술 스택 설계 완료
- PLAN.md 작성
- **앱 이름 확정: 숨은지원금** (후보 10개 중 톱픽)
- **홈 화면 톤 결정: 임팩트형 (못 받은 돈 카드 메인)**
- **홈 화면 상세 설계 완료** (5.1 — 와이어프레임/디자인 토큰/인터랙션/빈 상태)
- **"놓친 내역" 시트 + 공유 자산 설계 완료** (5.2 / 5.3 — 바이럴 엔진)
- **온보딩 3장 + 정책 상세 + 이벤트 + 마이페이지 + 화면 흐름 설계 완료** (5.4~5.8)
- **MVP 화면 와이어프레임 전부 완료** — Phase 1의 와이어프레임 단계 ✅
- **기술 스택 변경: Flutter → Kotlin + Jetpack Compose** (Android-only 확정으로 디자인 천장이 더 높은 네이티브로 전환)
- **환경 점검**: SDK 34/35/36, Build-tools, Gradle 9.2, JDK 17 모두 이미 설치됨 → Android Studio 불필요
- **Android 프로젝트 스캐폴딩 완료** (settings/build.gradle.kts, libs.versions.toml, Manifest, 어댑티브 아이콘)
- **디자인 시스템 코드화 완료** (Color.kt 라이트·다크 토큰, Type.kt Pretendard 11단계, Spacing/Shape/Theme)
- **홈 화면 1차 구현 완료** — 임팩트 카드(카운트업 애니메이션 포함) + 액션 카드 + 마감 임박 리스트
- **첫 디버그 APK 빌드 성공** ✅ (17.8MB)
- **폰 설치·확인** (갤럭시 A35 5G): 동작 확인
- **사용자 피드백 1차**: 다크모드 자동 적용이 거슬림 + UI가 "토스처럼" 깔끔하지 않음
- **2차 디자인 개편 — 토스 톤 적용**:
  - 라이트 모드 강제 (시스템 다크 무시)
  - 토스 팔레트 채택 (배경 #F2F4F6, 카드 #FFFFFF, 텍스트 #191F28/#4E5968/#8B95A1)
  - 임팩트 숫자 72pt Black (압도적 위계)
  - 액션 카드 CTA를 큰 채워진 민트 버튼으로 (52dp)
- **3차 — 토스 스샷 6장 분석 후 패턴 보강**:
  - 배경을 살짝 푸른 톤 #F2F4F8 으로 (토스 시그니처)
  - **카드 안 헤더 + 일러스트 행 + footer 링크 패턴** 도입 (메인 변경점)
  - **이모지+컬러 버블 IconBubble** (토스 3D 일러스트 대체)
  - **회색 알약 PillAction** (D-day, 우측 미니 액션)
  - **CardFooterLink** (카드 안 "전체 보기 >" 미니 링크)
  - 카드 사이 여백 40→12dp로 줄임 (토스는 카드끼리 가까움)
  - 카테고리별 컬러 버블 (주거=Sky, 출산=Coral, 교육=Lemon...)
  - CTA 버튼 56dp 높이 + rounded 16dp (토스 시그니처)
- **다음**: 사용자 톤 피드백 → 놓친 내역 시트 구현 → 정책 상세 / 온보딩

### 2026-05-15 (이어서)
- **사용자 피드백 2차**: 임팩트 숫자가 너무 큼 → `displayLarge` 72sp → 60sp 축소 (letterSpacing -0.045 → -0.04)
- **놓친 내역 시트(5.2) 구현 완료** — `ui/missed/MissedSheet.kt`
  - `ModalBottomSheet` 92% 높이 + 토스 시그니처 드래그 핸들
  - 헤더: "당신이 놓친 돈" + `displayMedium`(48sp) 카운트업 + "건수 · 최근 3년"
  - 공유 카드 (📤 친구에게 공유하기) — 시트 상단 sticky 자리
  - **연도별 sticky 헤더** + 연도 총액 (LazyColumn `stickyHeader`)
  - 카드 탭 → `AnimatedVisibility`로 펼침 (요약 + 민트 액션 "지금이라도 알아보기")
  - 하단 sticky CTA "🔔 올해는 놓치지 않을게요" + 위쪽 그라데이션 페이드
  - 빈 상태 (놓친 돈 0) 긍정 톤 ("🎉 완벽해요!" + 알림 받기 CTA)
- `PrimaryCtaButton` 공유 컴포넌트로 분리 (`ui/components/PrimaryButton.kt`)
- MainActivity에 `AppRoot` 컨테이너 추가 — 홈 임팩트 카드 탭 시 시트 슬라이드업 연결
- **빌드/설치 확인** ✅ (incremental 18s, A35)
- **사용자 피드백**: 시트 톤 통과 ✓
- **Policy 모델 확장** — `period`, `eligibility`, `documents`, `procedure`, `applicationOrg`, `applicationUrl`, `isEligible` + `DocumentRequirement` 신규
- **SampleData 풀데이터** — 청년 월세 지원 / 출산장려금 / 통신비 감면(미충족) / 주거안정 장학금 4건 완성
- **정책 상세 화면(5.5) 구현 완료** — `ui/detail/PolicyDetailScreen.kt`
  - TopBar (← / ⭐ / 📤)
  - Hero: 카테고리 칩 + 28sp 제목 + 36sp 금액 + 보조 기간
  - **자격 충족 배지** (민트 / 회색 — 미충족 시 좌절감 방지)
  - **마감일 카드** (날짜 + D-day pill, D-3 이내면 적색)
  - 한 줄 요약 카드
  - 자격 조건 카드 (✓ 체크리스트)
  - 필요 서류 카드 (항목 + 발급처 PillAction → 외부 URL)
  - 신청 절차 카드 (1·2·3 민트 step 배지)
  - **하단 sticky CTA** ("복지로에서 신청하기") + 위쪽 페이드
- **MainActivity 네비게이션** — `AnimatedContent`로 push 트랜지션 (280ms, 새 화면이 우측에서 슬라이드인 + 이전 화면 1/5만큼 시차 슬라이드아웃) + `BackHandler` 처리
- **빌드/설치 확인** ✅ (23s, A35)
- **사용자 피드백**: 정책 상세 톤 통과 ✓
- **UserProfile 모델 신규** — age/region/occupation/married/hasChildren/incomeMonthly + Regions·Occupations 상수
- **온보딩 화면 3장(5.4) 구현 완료** — `ui/onboarding/OnboardingScreen.kt`
  - `HorizontalPager`(스와이프 비활성, CTA로만 진행) + 상단 sticky 점 인디케이터 (현재 페이지 알약 형태 18dp)
  - **Page 1 (후크)**: "당신은 정부 지원금 / 2,400,000원 / 놓치고 있을지도 몰라요" — 60sp 카운트업 1.4s + 민트 액센트 라인 + "30초면 알 수 있어요"
  - **Page 2 (작동 원리)**: 3개 컬러 버블 스텝 카드 (Sky·Mint·Lemon) — 매일 자동 수집 / 상황 매칭 / 신청 안내
  - **Page 3 (정보 입력)**: 나이 / 지역 필수 picker + 선택(직업·결혼·자녀) ToggleChip — `ModalBottomSheet` 기반 picker 3종, 필수 2개 입력 전 CTA 비활성
- **SharedPreferences 영속화** — `hs_prefs`에 `onboarded` 플래그 + 프로필 필드 저장
- **MainActivity 게이트** — `AnimatedContent`로 온보딩 → 홈 페이드 트랜지션 (320ms in / 200ms out)
- **`adb shell pm clear`로 초기화 후 빌드/설치 확인** ✅ (42s)
- **사용자 피드백**: 온보딩 톤 통과 ✓
- **LifeEvent 모델 + 6개 이벤트 데이터** — Move/Resign/Pregnancy/Marriage/Startup/Employment + `TimelineGroup`(시간순) + `EventBundle`(tagline/maxAmountLabel) + 19개 정책 풀 확장
- **카테고리 스타일 공용화** — `ui/theme/CategoryStyle.kt`로 `categoryEmoji`/`categoryBubble` 분리, HomeScreen private 제거
- **이벤트 리스트 화면(5.6)** 구현 — `LazyVerticalGrid` 2열, 각 카드: 컬러 버블(이벤트 전용 6컬러 매핑) + 라벨 + 건수
- **이벤트 상세 화면(5.6)** 구현 — Hero 카드(최대 받을 수 있어요 + maxAmountLabel + 이벤트 버블) + 시간순 그룹 헤더 + 정책 행 리스트 (탭 → 정책 상세)
- **BottomTabBar** 컴포넌트 신규 — 홈/이벤트/마이 3탭, 토스 톤(흰 배경, 선택 시 filled icon + G900)
- **MainActivity sealed Screen 재구조화** — `Screen.Tabs / PolicyDetail / EventDetail`, `AnimatedContent`로 push/pop 트랜지션, 탭 화면은 `TabsHost`에서 통합
- **MyScreen stub** — "곧 만나요" 캐주얼 placeholder (5.7에서 본구현 예정)
- **HomeScreen contentPadding 조정** — 탭바 안에 들어가므로 systemBars bottom 제거
- **빌드/설치 확인** ✅ (26s)
- **사용자 피드백**: 이벤트 톤 OK ✓ + **캘린더 탭 신규 요청** (사용자에게 해당하는 신청 시작/마감/발표/지급 일정을 달력에 표시) → 2번째 탭으로 배치 요청

### 2026-05-15 (캘린더 탭 추가)
- **PolicyCalendarEvent 모델 + 4종 일정 kind** (ApplicationOpen / Deadline / Announcement / Disbursement)
- **SampleData.calendarEvents** — 자격 충족 정책 5건의 2026-05·06 일정 14개 (오늘 2026-05-15 기준)
- **CalendarScreen 구현** — `ui/calendar/CalendarScreen.kt`
  - 월 헤더 (←/→ + "오늘로" 버튼)
  - 7×6 그리드 (일~토 헤더, 일요일 빨강 / 토요일 파랑 / 평일 기본)
  - 각 DayCell: 30dp 원형 배경 (선택=민트풀, 오늘=민트연한) + 아래 일정 dot 최대 3개
  - dot 색상: 신청 시작=민트 / 마감=적 / 발표·지급=회색 (절제된 3색)
  - 범례 바
  - 선택 날짜 섹션 헤더 + 일정 카드 리스트 (좌측 종류 라벨 + 정책 제목)
  - 빈 날 / 미선택 hint 카드
- **탭바 4탭으로 확장** — 홈 / **캘린더(2번째)** / 이벤트 / 마이 (CalendarMonth icon, 패딩 14dp로 좁힘)
- **MainActivity TabsHost** — CalendarScreen 연결, 카드 탭 → 정책 상세 push
- **빌드/설치 확인** ✅ (16s)
- **앱 아이콘 교체** — 사용자가 나노바나나로 직접 디자인한 `appIcon.png` (1254×1254, 둥근 카툰 한글 "숨은/지원금" + 봉투에서 나오는 ₩ 동전 + 반짝이) 채택
  - `mipmap-xxxhdpi/ic_launcher_foreground.png`로 복사
  - `mipmap-anydpi-v26/ic_launcher.xml`·`ic_launcher_round.xml` foreground를 `@mipmap/ic_launcher_foreground`로 교체, themed monochrome 항목 제거 (PNG는 monochrome 불가)
  - `drawable/ic_launcher_background.xml` 배경색 `#00C896` → `#3DDB9F` (PNG 내부 민트와 매칭, 마스크 외곽 노출 시 자연스럽게)
  - 빌드/설치 ✅ (19s)
- **앱 아이콘 v2 교체 + 마스크 잘림 해결** — 사용자가 1차 아이콘이 폰에서 잘린다고 피드백
  - 어댑티브 마스크 안전 영역은 가운데 ~66% (108dp 중 72dp). 1차는 콘텐츠가 외곽까지 가서 잘림
  - 새 `appIcon2.png` (500×500) 받아서 PowerShell + System.Drawing으로 **900×900 캔버스 가운데에 500×500 패딩 배치** (콘텐츠 55.6% — 안전 영역 이내)
  - PNG 외곽 squircle 색 추출(`#0B7A5E`)해서 `ic_launcher_background.xml`도 매칭 → 마스크가 잘려도 외곽 색이 자연스럽게 이어짐
  - 빌드/설치 ✅ (18s)
  - 콘텐츠 더 키워달라는 피드백 → 캔버스 900 → 700, 콘텐츠 비율 55.6% → 71.4%로 재처리
- **스플래시(SplashScreen API) 아이콘·배경 교체** — 기존 vector 로고 + 라이트민트 배경
  - `drawable/ic_splash_logo.xml`을 `<bitmap android:src="@mipmap/ic_launcher_foreground" />` 로 변경 → 앱 아이콘 PNG 그대로 재사용
  - `values/colors.xml`·`values-night/colors.xml` 모두 `splash_background`를 `#0B7A5E`로 통일 (PNG 외곽 squircle 색과 매칭, 다크모드여도 동일하게)
  - 빌드/설치 ✅
- **사용자 피드백**: 앱 아이콘·스플래시 톤 OK ✓
- **마이페이지(5.7) 본구현** — `ui/my/MyScreen.kt` 풀 교체
  - `UserProfile.completeness` 확장 (필수 50% + 선택 50%), `summary` 헬퍼 (`만 25세 · 서울`)
  - `MySummary` 모델 + SampleData stub (받을 3건/150만, 신청 1건/60만, 받은 5건/230만)
  - `UserPrefs.load()` + `rememberUserProfile()` Composable — SharedPreferences에서 프로필 읽기
  - **프로필 카드**: 좌측 IconBubble + 요약 텍스트 + 정확도 %, **민트 ProgressBar 애니메이션** (700ms tween), "프로필 더 채우기" 민트 액션 박스
  - **내 지원금 3카드**: ⭐ 받을 예정 (Lemon 버블), 📝 신청한 (Sky 버블 + "수령 확인" 알약), **✅ 받은 지원금 (민트 풀톤 강조 카드 + 누적 금액 36sp)**
  - **설정 리스트 카드**: 5개 항목 (🔔 알림, 👨‍👩‍👧 가족진단[프리미엄 뱃지], 💌 친구초대, 📋 개인정보, ✉️ 의견) — 토스 리스트 스타일, 항목 사이 indent divider
- **빌드/설치 확인** ✅ (35s)
- **다음**: 마이 톤 피드백 → 정책 데이터 파이프라인 (GitHub Actions 크롤러) → Firebase 연동
- **GitHub 연동 + 초기 푸시 완료** ✅ — https://github.com/Gyubam/hidemoney.git (main 브랜치)
  - Android 표준 .gitignore (build/.gradle/.kotlin/local.properties/keystore/.claude/google-services.json 제외)
  - 첫 커밋: 5개 화면 + 디자인 시스템 + 앱 아이콘 / appIcon.png/appIcon2.png 원본 디자인 자산도 포함
  - 이후 PLAN.md 진행 일지 업데이트마다 커밋 → 푸시 패턴으로 운영 (단, **푸시는 사용자 명시 요청 시만**)

### 2026-05-15 (자격 매칭 로직)
- **EligibilityRule 모델 신규** — `minAge / maxAge / regions / requiresOccupation / requiresMarried / requiresChildren` 선언적 룰
- **PolicyMatching 헬퍼** (`data/PolicyMatching.kt`)
  - `EligibilityRule.matches(profile)` — 필수(age/region) 부재 시 false, 선택(occupation/married/hasChildren) 부재 시 관대 통과
  - `Policy.matchedWith(profile)` — copy(isEligible = rule.matches)
  - `List<Policy>.eligibleOnly(profile)` — 자격 충족만 필터링
- **19개 정책에 자격 룰 부여**
  - 청년 시리즈(월세지원/대출/도약계좌/내일채움/창업금): age 19~34 (창업학교/창업금은 39 상한)
  - 출산/육아 시리즈: requiresChildren=true
  - 결혼 시리즈(전세대출/디딤돌/희망타운): requiresMarried=true
  - 마포구 정착지원금: regions=["서울"] + age 19~34
  - 주거안정 장학금: age 18~27 + requiresOccupation=["학생"]
  - 국민취업지원제도: requiresOccupation=["구직 중"]
  - 통신비 감면: 룰 없이 isEligible=false 박음 (기초생활수급자/차상위 — UserProfile 필드 부재)
- **AppRoot에서 매칭 적용**
  - 홈 "이번 주 받을 수 있어요" → `eligibleOnly`로 필터
  - 홈 "곧 마감돼요" → `matchedWith`만 (정보성 — 미충족도 표시)
  - 캘린더 일정 → 자격 충족 정책 일정만 필터
  - 정책 상세 진입 시 → `matchedWith` 적용해서 동적 자격 배지
- **빌드/설치 확인** ✅ (18s)
- **다음**: 매칭 톤 피드백 → 공유/액션 stub 채우기 OR 정책 데이터 파이프라인 OR Firebase 연동

### 2026-05-15 (공유 / 알림 권한 / 외부 intent stub)
- **ShareHelper** (`util/ShareHelper.kt`) — 공유 카피 통합 헬퍼
  - `shareMissed(amount, count)` — "나 정부 지원금 N원이나 놓쳤대 ㅋㅋ" 바이럴 카피 + Play Store URL
  - `inviteFriends()` — 앱 추천
  - `sendFeedback()` — `mailto:sgb8154@gmail.com` (없으면 일반 공유로 fallback)
  - `openPrivacyPolicy()` — Play Store URL placeholder (Firebase Hosting 호스팅 후 교체)
- **NotificationPermission** (`util/NotificationPermission.kt`)
  - `rememberNotificationPermissionRequest { granted -> ... }` — Android 13+ POST_NOTIFICATIONS 시스템 다이얼로그
  - 13 미만은 자동 true (이미 manifest에 권한 선언, 시스템 자동 부여)
- **화면 연결**
  - 놓친 내역 시트: 📤 공유 카드 → `shareMissed`, 🔔 "올해는 놓치지 않을게요" → 알림 권한 요청 + 시트 닫힘
  - 마이: 🔔 알림 설정 → 권한 요청, 💌 친구 초대 → 공유, 📋 개인정보 → 정책 URL, ✉️ 의견 → mailto
  - 마이 프로필 더 채우기는 일단 Toast stub ("곧 만나요")
  - 권한 결과는 Toast로 피드백 ("🔔 알림이 켜졌어요" / "알림 권한이 거부됐어요")
- **빌드/설치 확인** ✅ (20s)
- **다음**: 공유 동작 검증 → 정책 데이터 파이프라인 OR Firebase 연동 OR 프로필 편집 화면

### 2026-05-15 (프로필 편집 화면)
- **UserPrefs.save(profile)** — null 필드는 remove()로 정리, 비-null은 put. 양방향 영속화
- **OnboardingScreen.ProfileInputPage 노출** — private → `internal`, `title`/`submitLabel` 파라미터화로 재사용 가능
  - 온보딩 기존 호출: default param ("딱 두 가지만 알려주세요" / "내가 받을 지원금 보기") 유지
  - 편집 화면 호출: "프로필 편집" / "저장하기"
- **ProfileEditScreen 신규** (`ui/profile/ProfileEditScreen.kt`) — `ProfileInputPage`를 그대로 재사용. 코드 중복 0
- **MainActivity 통합**
  - `Screen.ProfileEdit` sealed 추가 + `AnimatedContent` push 트랜지션 자동 적용
  - `profile`을 `mutableStateOf`로 보유 → 저장 시 `profile = newProfile` 트리거 → home/calendarEvents 재계산 (자격 매칭 즉시 반영)
  - `MyScreen`에 `profile` 파라미터 추가 (rememberUserProfile() 캐시 의존 제거)
  - 마이 "프로필 더 채우기" → `screen = Screen.ProfileEdit` 진입, 저장 후 자동으로 Tabs로 복귀
- **빌드/설치 확인** ✅ (25s)
- **다음**: 프로필 편집 동작 검증 → 정책 데이터 파이프라인 OR Firebase 연동

### 2026-05-15 (즐겨찾기)
- **FavoritesRepository** (`data/FavoritesRepository.kt`) — SharedPreferences `StringSet` 기반 `load/save/toggle`
- **정책 상세 ⭐ 토글** — `PolicyDetailScreen` 시그니처에 `isFavorite/onToggleFavorite` 추가
  - 채워진 별(`Icons.Rounded.Star`, 민트 컬러) / 빈 별(`StarBorder`, 텍스트 컬러) 전환
  - 공유 아이콘은 `ShareHelper.inviteFriends` 연결 (정책 상세에서 친구 추천)
- **마이 "받을 예정" 동적화** — AppRoot에서 `favorites` state 보유, `mySummary` `remember(favorites)`로 재계산
  - savedCount = `favorites.size`
  - savedAmount = 즐겨찾기 정책들의 amount 합산
- 토글 시 Toast 피드백 ("받을 예정에 추가됐어요" / "받을 예정에서 빠졌어요")
- **빌드/설치 확인** ✅ (24s)
- **다음**: 즐겨찾기 동작 검증 → 정책 데이터 파이프라인 OR Firebase 연동

### 2026-05-15 (정책 데이터 파이프라인 1단계 — Repository 추상화)
- **PolicyRepository 인터페이스** (`data/PolicyRepository.kt`)
  - `suspend fun loadAll(): List<Policy>` / `suspend fun findById(id)`
  - InMemory 구현: SampleData wrapper
- **SampleData.allPolicies** private → `internal val`로 노출 (Repository 의존성)
- **AppRoot 리팩토링**
  - `Repository` + `allPolicies` state + `LaunchedEffect`로 비동기 로드 (초기값 = SampleData → 깜박임 없음)
  - `byId = remember(allPolicies) { allPolicies.associateBy { it.id } }` — O(1) lookup 캐시
  - 모든 `SampleData.findPolicy(id)` 직접 호출을 `byId[id]`로 교체
  - PolicyDetail / calendarEvents 필터 / mySummary 계산 / TabsHost CalendarScreen 콜백 모두 byId 사용
- **TabsHost에 byId 파라미터 전달** — CalendarScreen의 String → Policy lookup용
- **빌드/설치 확인** ✅ (29s)
- **다음 라운드 (2단계)**: RemotePolicyRepository (Ktor) + 로컬 캐시 (cacheDir) + 외부 호스팅 URL placeholder + 오프라인 fallback. 사용자가 호스팅 결정(GitHub Pages / Firebase Hosting / vercel free).
- **3단계**: GitHub Actions 크론 (정부24 크롤러 + Gemini Flash 요약 + policies.json 호스팅 push). 사용자 개입: Gemini API key 발급 + GitHub Actions secrets 설정.

### 2026-05-15 (정책 데이터 파이프라인 2단계 — Remote fetch + 캐시)
- **policies.json export 자동화**
  - `SampleData.exportPoliciesJson()` 추가 — `Json { prettyPrint; encodeDefaults=false; explicitNulls=false }`
  - 앱 첫 진입 시 `cacheDir/policies.json`에 자동 저장
  - `adb exec-out run-as ... cat ...` + PowerShell UTF-8 변환으로 `docs/policies.json` (12.5KB / 19 정책) 추출 완료
- **RemotePolicyRepository** (`data/RemotePolicyRepository.kt`)
  - Ktor `HttpClient(OkHttp) { install(ContentNegotiation) { json(...) } }`로 외부 JSON GET
  - URL: `https://gyubam.github.io/hidemoney/policies.json` (placeholder — 사용자가 GitHub Pages 활성화하면 동작)
- **CachedPolicyRepository** (`data/CachedPolicyRepository.kt`)
  - `filesDir/policies-cache.json`에 캐시
  - `loadAll()` — 캐시 우선 → 없으면 remote → 실패 시 fallback(InMemory/SampleData)
  - `refresh()` — remote 강제 fetch + 캐시 갱신
- **AppRoot 통합**
  - LaunchedEffect: `loadAll()` 즉시 응답 → background로 `refresh()` 시도 → 성공 시 `allPolicies` 갱신
  - 외부 fetch 실패해도 항상 InMemory fallback으로 정상 동작
- **빌드/설치 확인** ✅ (21s)
- **사용자 측 작업 (다음 단계 활성화 위해)**:
  1. `docs/policies.json` git add + commit + push
  2. GitHub Repo → Settings → Pages → Source: Deploy from branch / Branch: main / Folder: `/docs` → Save
  3. 1~2분 후 `https://gyubam.github.io/hidemoney/policies.json` 접근 가능
  4. 앱 재진입 시 logcat `policies-fetch`에서 "Refreshed from remote: 19" 확인
- **다음 라운드 (3단계)**: GitHub Actions 크론 워크플로우 — 정부24/복지로 크롤링 + Gemini Flash로 요약·태깅·ROI 점수 + `docs/policies.json` 자동 commit/push

### 2026-05-15 (R2 — GitHub Actions 빌드 파이프라인 + Gemini Flash 보강)

- **환경 셋업 (집 PC)**: 저장소 clone(`whatsapp/hidemoney/`), GitHub Pages 활성화 직후 `https://gyubam.github.io/hidemoney/policies.json` 200 OK / 19 정책 fetch 확인.
- **🔐 보안 사고 대응**: Gemini API key가 채팅에 평문 노출됨 → PLAN.md 사고대응 절차대로 **즉시 폐기 + 신규 발급 + GitHub Secret(`GEMINI_API_KEY`)에만 등록**. 새 키는 채팅에 보내지 않음.
- **robots.txt 사전 점검**:
  - `gov.kr` → `Disallow: /` 전역 금지 (보조금 검색 페이지 크롤링 불가)
  - `bokjiro.go.kr` → robots.txt 미제공 (서버 에러 HTML)
  - 결과: PLAN 가정 변경. **R2는 인프라+Gemini 검증까지만**, 실 데이터 소스(정부24 OpenAPI 또는 복지로)는 **R2.5로 분리**.
- **Python 도구 일식** (`tools/`):
  - `schema.py` — Pydantic Policy/EligibilityRule/DocumentRequirement (Kotlin 모델 미러, `extra="allow"`로 Gemini 신규 필드 안전 수용)
  - `summarize.py` — `GeminiClient` (지연 import) + `enrich_policy` 화이트리스트 머지(현재 `summary`만), 마크다운 펜스 관용 파싱, 실패 시 원본 반환(빌드 절대 안 깨짐)
  - `build_policies.py` — orchestrator (`load → enrich_local → enrich_llm → validate → write_if_changed`)
    - 결정론적 보강: `daysLeft` 재계산 + `difficultyScore`(문서·절차·자격 항목 가중 1~10) + `roiScore`(log10(amount)·15 − 난이도 페널티, 0~100)
    - `--enrich` 플래그로 LLM on/off, `--today YYYY-MM-DD`로 CI 재현성 확보
  - `requirements.txt` — `google-generativeai==0.8.3` / `pydantic==2.9.2` / `python-dateutil==2.9.0`
- **GitHub Actions 워크플로우** (`.github/workflows/crawl-policies.yml`):
  - cron `0 18 * * *` (UTC 18 = KST 03) + `workflow_dispatch` (수동 실행 입력 `enrich=true/false`)
  - `permissions: contents: write` + `concurrency.group: crawl-policies` (중복 실행 방지)
  - `actions/setup-python@v5` 3.11 + pip 캐시 + 의존성 install → `python build_policies.py --enrich`
  - 변경 있을 때만 `github-actions[bot]` 계정으로 commit + push (`docs/policies.json`만 add)
- **로컬 검증 결과** (`.venv` Python 3.12.10):
  - LLM 미사용 dry run (`--today 2026-05-15`) → 19/19 검증 통과, 신규 필드 정상 채움
  - 톱 ROI 후보: 출산장려금 77 / 통신비 감면 70 / 청년 월세 60 등
  - 기존 필드 1개도 안 깨짐, schema strict 검증 통과
- **`.gitignore` 보강**: `__pycache__/`, `.venv/`, `.env*`, `*.secret` 추가 (Python 도구·키 노출 방지)
- **클라 호환성 확인**: `RemotePolicyRepository`/`CachedPolicyRepository`/`MainActivity` 모두 `Json { ignoreUnknownKeys = true }` 설정돼 있어 `difficultyScore`/`roiScore` 추가해도 deserialization 절대 안 깨짐.
- **남은 사용자 액션 (R2 마무리)**:
  1. PLAN/도구/워크플로우/갱신된 `docs/policies.json` commit + push (Claude 명시 요청 대기)
  2. push 후 https://github.com/Gyubam/hidemoney/actions → **`정책 자동 빌드`** → **Run workflow** (`enrich=true`) → 약 1분 후 commit 자동 생성 확인 → Pages 재빌드 → 앱에서 `policies-fetch` 로그 새 데이터 확인
- **다음 라운드 (R2.5)**: 실제 데이터 소스 연결 옵션
  - (a) **공공데이터포털 `data.go.kr`** `보조금24 정부서비스 목록` OpenAPI 신청(무료, 자동승인 인증키 5분) — **합법·안정 톱픽**
  - (b) 복지로 사이트 직접 fetch (robots.txt 없으니 가능, 다만 SPA·세션 이슈 가능)
  - (c) 둘 다 — OpenAPI를 기준, 복지로는 카테고리 보강용
  - 결정 후 `tools/crawl.py` 추가하고 `build_policies.py`에 입력 단계 끼우면 됨

### 2026-05-15 (R2.5 — data.go.kr 공공서비스 API 연결)

- **API 활용신청 완료**: `행정안전부_대한민국 공공서비스(혜택) 정보` (data.go.kr) — **자동승인**, 일 호출 50만건, 활용기간 24개월, 비용 0
  - Base URL: `https://api.odcloud.kr/api`
  - 3 엔드포인트: `serviceList` (페이징 목록) / `serviceDetail` (단건 상세) / `supportConditions` (자격조건 구조화 JA0xxx 코드)
  - 인증: `serviceKey` 쿼리 파라미터 (Decoding 원본 키 사용, requests 자동 URL 인코딩)
- **🔐 신규 Secret**: `DATA_GO_KR_API_KEY` (Decoding 키) GitHub Secrets에 등록
- **Swagger 스펙 파악**: serviceList 21필드 / serviceDetail 20필드 / supportConditions 32+ JA코드 — 우리 Policy 모델과 1:1 매핑 가능
- **신규 Python 모듈**:
  - `tools/crawl.py` — `GovApiClient` (retry 3회 + per-call 0.3s sleep + Session keep-alive) + `iter_services` 페이징 제너레이터 + `RawPolicy` dataclass (list/detail/conditions 묶음) + `fetch_policies(limit=N)`
  - `tools/normalize.py` — `conditions_to_eligibility_rule` (JA0110/0111 → minAge/maxAge, JA0317~0320 → 학생, JA0326 → 직장인, JA0327 → 구직 중, JA0303 → requiresChildren — 정부 데이터 직매핑), `_llm_extract` (지원내용·구비서류·신청방법·지원대상 텍스트를 Gemini로 우리 스키마 추출), 화이트리스트 머지 + 카테고리 정합성 검증(OUR_CATEGORIES 6개만 허용)
- **`build_policies.py` 통합**:
  - 새 플래그: `--crawl` (정부 API에서 fresh fetch), `--limit N` (기본 30)
  - 흐름: `fetch_policies → normalize_all → enrich_local(daysLeft/difficulty/roi) → validate → write_if_changed`
  - crawl 모드는 normalize 단계에서 이미 LLM 사용 → summary 재정련(`enrich_policy`)은 스킵 (중복 호출 방지)
  - 결정론적 부분만으로도 valid Policy 생산 — LLM 실패해도 빌드 안 깨짐
- **`tools/requirements.txt`**: `requests==2.32.3` 추가
- **워크플로우 갱신** (`.github/workflows/crawl-policies.yml`):
  - `DATA_GO_KR_API_KEY` secret 주입 추가
  - `workflow_dispatch` 입력 3개: `crawl` (기본 true) / `enrich` (기본 true) / `limit` (기본 30)
  - cron 기본 동작: `--crawl --enrich --limit 30` (매일 새벽 3시 자동)
- **검증 전략**: 첫 push 후 **workflow_dispatch로 `limit=3` 짧게 트리거** → 빨강·초록 + 자동 commit 결과 보고 → 잘 되면 cron으로 풀가동
- **다음 라운드 (R3)**: WorkManager 알림 스케줄링 (마감 D-3 자동 푸시) OR Firebase 연동(Auth + Firestore + FCM) OR data.go.kr API 결과 품질 튜닝 (limit 늘리기, 카테고리 매핑 보강)

### 2026-05-15 (R2.6 — 데이터 품질 튜닝)

- **첫 실측 결과 (R2.5 검증)**: workflow_dispatch limit=30 → bot auto-commit `13c332f`. 인프라 100% 동작 확인. **하지만 두 가지 데이터 품질 문제 발견**:
  - **부처 편향**: 30개 중 27개가 해양수산부 (정부24 serviceList가 소관기관코드 순 정렬 → page=1 통째로 한 부처에 걸림)
  - **LLM 정규화 거의 실패**: amount=0 (30/30), category="" (30/30), eligibility/documents/procedure 빈 배열 (30/30). summary는 raw text("○ 근로장려세제 - ...") 그대로 복붙.
  - 단 `eligibilityRule`은 정확 (`{minAge:19,maxAge:35,...}` JA0xxx 직매핑 OK)
- **두 갈래 fix**:
  - **부처 편향 → 사용자구분 필터**: `crawl.py`/`build_policies.py`에 `user_type` 파라미터 추가, workflow_dispatch `user_type` input 신설 (기본값 `'개인'`)
  - **LLM 정규화 → 프롬프트 전면 재설계** (`normalize.py`):
    - 적극 추출 톤 ("원문에 있는 정보를 적극 추출")
    - **few-shot 예시** 1개 (청년 월세 지원 정규화 결과를 모델 안에 박음)
    - summary **재작성 의무** (raw 복붙 금지)
    - category **빈 문자열 금지** (6개 중 가장 가까운 거 무조건 선택)
    - amount 계산 가이드 ("월 N만원은 12개월 가정해 N*120000")
    - 카테고리 매핑 가이드 6개 카테고리별 키워드 명시
- **amount fallback (정규식)**: `guess_amount_from_text` 신규 — LLM이 amount=0 출력해도 `지원내용`/`서비스목적` 텍스트에서 정규식으로 가장 큰 금액 추정(`'\d+(억|만|천)?원'` 패턴). 스모크 테스트 통과:
  - '월 최대 20만원, 12개월' → 200,000 (LLM이 12개월 곱셈 처리하면 2,400,000)
  - '최대 2억원 대출' → 200,000,000
  - '연 1,200,000원' → 1,200,000
  - '비금전 지원' → 0
- **워크플로우 갱신**: bash arg array 패턴으로 변경(한국어 user_type 단어 split 안전), `shell: bash` 명시
- **사용자 액션 (R2.6 검증)**: push 후 workflow_dispatch → `crawl=true / enrich=true / limit=20 / user_type=개인` → 1~2분 → bot auto-commit 확인 + 결과 품질 점검 (부처 다양성·amount·category·summary 톤 4가지)
- **남은 위험**:
  - `user_type='개인'`이 정부24 API에서 valid 값인지 불확실 (추측). 실패 시 다른 값(`일반인`/`전체` 등) 시도.
  - LLM이 너무 적극 추론해서 환각 위험. 다만 검증 시 raw text와 비교해 평가 가능.

### 2026-05-15 (R2.7 — LLM 부담 분산 + 카테고리 결정론 매핑 + 정찰 도구)

- **R2.6 검증 결과 (`a9f8b44`)**: 4가지 점검 모두 미흡
  - 부처 다양성 ❌ (28/30 해양수산부, user_type='개인' 필터가 정부24에서 무력화)
  - amount 17% (정규식 fallback만 5건 잡음)
  - category 0% (LLM이 6 카테고리 빈 값으로 출력)
  - summary 정부 raw 문서체 그대로 (재작성 안 함)
- **진단**: 한 LLM 호출에 5필드 동시 추출이 부담 + 어업/수산업 정책 28개가 우리 6 카테고리에 진짜로 안 맞음 → 모델이 안전한 길(빈 값) 선택
- **R2.7 핵심 변경**:
  - **카테고리 결정론 매핑** (`normalize.py`) — 정부24 표준 `서비스분야` 11종(`주거-자립`/`임신-출산`/`고용-창업` 등) → 우리 6 카테고리(주거/출산/생활/교육/청년/창업) 직매핑 dict. LLM 완전 우회. 하이픈 표기 변형 대비 alias 추가.
  - **LLM 호출 2개로 분리**:
    - `_llm_summary` — summary + period + amount만 (단일 작업)
    - `_llm_items` — eligibility + documents + procedure만 (단일 작업)
    - 한 호출이 너무 많은 책임을 갖지 않도록 prompt도 짧고 명확하게. few-shot 예시는 각 prompt마다 1개씩.
  - **amount 정규식 적용 텍스트 확장** — `지원내용/서비스목적/지원대상/선정기준` 4필드(list_row+detail) 전부 통합해 정규식 시도. LLM amount=0 시 fallback.
  - 결과 머지 시 LLM amount > 정규식 amount일 때만 LLM 값으로 갱신 (큰 값 우선)
- **정찰 도구** (`tools/probe_fields.py` + `.github/workflows/probe-fields.yml`):
  - 별도 워크플로우 (수동 실행 전용, build 잡과 분리)
  - serviceList page=1 perPage=200 fetch → 사용자구분/서비스분야/지원유형/소관기관유형/소관기관명 unique 값 Counter 출력 + sample raw row 출력
  - 이거 한 번 돌리면 user_type='개인'이 valid한지, 서비스분야 실제 표기가 우리 dict와 일치하는지 즉시 확인 가능
- **사용자 액션 (R2.7 검증)**:
  1. push 후 `정부24 필드 정찰` 워크플로우 Run workflow (1분) → Actions 로그에서 unique 값 캡처
  2. 그 결과 보고 user_type 진짜 값/서비스분야 alias 보강
  3. 다시 `정책 자동 빌드` 워크플로우 Run workflow (limit=20, user_type=정찰결과값)
- **다음 라운드 (R2.8)**: 풀빌드 + 증분 갱신
  - `--full-build`: 정부24 전체 catalog 1회 fetch (~수천 건), LLM 없이 결정론 매핑만
  - cron 매일: `cond[수정일시::GTE]=어제` 필터로 변경분만 + LLM 정규화 백필
  - 2~3개월 후 완전 자동화된 풀 카탈로그 + LLM 정규화 100%

### 2026-05-15 (R2.7.5 — 정찰 결과 기반 즉시 fix)

- **정찰 결과 (사용자가 `정부24 필드 정찰` 워크플로우 실행)** — 핵심 정보 확보:
  - `totalCount`: **10,941**개 정책 (정부24 catalog 풀 규모)
  - `사용자구분` unique 값 (7종): `개인` 139, `개인||법인/시설/단체` 23, `법인/시설/단체` 21, `가구` 8 등
  - `서비스분야` unique 값 (10종, 정찰 200건 기준): `농림축산어업` 52, `보건·의료` 34, `행정·안전` 32, `보육·교육` 31, `생활안정` 15, `주거·자립` 11, `문화·환경` 7, `보호·돌봄` 7, `임신·출산` 6, `고용·창업` 5
  - `지원유형` top: `현금` 65, `현금(감면)` 17, `이용권` 12, `현금(융자)` 9, `현물` 9 (현금 계열이 약 50%)
  - `소관기관유형`: 중앙행정기관 199 / 공공기관 1 — **지자체 정책은 정부24 API에 없음**
  - `소관기관명` top: 보건복지부 68, 해양수산부 61, 교육부 20, 행안부 10
- **🔴 발견 1 — 카테고리 매핑 dict가 하이픈(-) vs 정부24 실제 표기 가운뎃점(·) 불일치**
  - `주거-자립` vs **`주거·자립`** → 모든 매핑 실패 → category 0%의 진범
  - `·` (U+00B7) ≠ `-` (U+002D) 완전 다른 문자
- **🔴 발견 2 — `cond[사용자구분::LIKE]=개인` 서버 필터 무력**
  - 정찰 200건 중 개인=139, 개인 복합=30. 필터 동작하면 169건 매치돼야 함.
  - 실제로 R2.6/R2.7 결과는 28/30 해양수산부 (필터 안 적용된 상태와 동일)
  - 추정 원인: 한국어 키 `cond[사용자구분::LIKE]`의 URL 인코딩이 정부24 서버 기대와 다름
- **R2.7.5 fix**:
  - `normalize.py` SERVICE_FIELD_TO_CATEGORY — **가운뎃점 정식 표기**로 변경 + 정찰에서 발견된 5개 분야 추가 매핑(`농림축산어업→창업`, `보건·의료/행정·안전/생활안정/문화·환경→생활`). 하이픈/공백 변형은 `_FIELD_ALIASES`에서 가운뎃점으로 정규화.
  - `crawl.py` `iter_services`에 `client_filter_user_type` 파라미터 추가 — 서버 필터 시도 + **응답 받은 후 클라이언트 측에서 `사용자구분` 필드 substring 매치로 한 번 더 필터링**. 서버 필터가 무력해도 100% 안정 동작.
  - `fetch_policies`는 두 필터 자동 동시 적용. 클라 필터로 많이 걸러질 수 있어 per_page를 100 이상으로 강제(부족 페이지 자동 추가 fetch).
- **남은 검증 필요**: LLM 호출 silent fail 가설 — Actions 로그에서 `LLM summary failed` WARNING 있는지 사용자가 확인해줘야. summary가 raw text 그대로인 게 LLM 호출 실패 때문인지, LLM이 raw 복사한 건지 아직 미확정.
- **R2.7.5 후 검증 예상**: category 100%, 부처 다양성 확보(개인 대상 정책만), amount 30% 이상 (정규식 + 정부24 catalog의 50%가 현금 계열이라 강한 신호).

### 2026-05-15 (R2.7.6 — Gemini 429 rate limit fix + 부처 필터 OR)

- **R2.7.5 검증 결과**: category 0%→**100%** ✅ (가운뎃점 fix 결정타!). 다만 두 가지 남음:
  - 부처 다양성 28/30 해양수산부 그대로 — `client_filter_user_type='개인'` substring 매치가 너무 엄격 → 보건복지부 가구 단위 정책 다 제외
  - LLM 0% — Actions 로그에 결정적 단서:
    ```
    429 You exceeded your current quota...
    * Quota exceeded for metric: ...input_token_count, limit: 0, model: gemini-2.0-flash
    * Quota exceeded for metric: ...requests, limit: 0
    Please retry in 49.816000791s.
    ```
- **진단 확정**: **Gemini 무료 티어 RPM(분당 요청) 한도 초과**. 정책 1개당 LLM 2회 호출(`_llm_summary` + `_llm_items`) × 30 = 60회를 throttling 없이 연속 호출 → 분당 ~15회 한도 초과 → 6번째 정책부터 429 fail. 첫 5개도 응답이 빈 dict였을 가능성(메시지 limit: 0 보면 신규 키가 quota 매우 낮은 상태일 수도).
- **R2.7.6 fix**:
  - `normalize.py` `_call_llm` — 429/quota 메시지 catch + `retry_delay` 정규식 파싱(`retry.*?(\d+)s`) + 그만큼 sleep + 최대 2회 재시도. 비-429 에러는 즉시 fail.
  - `normalize.py` `normalize_all` — 정책 사이 5초 sleep 추가(`throttle_sec=5.0`). 분당 약 12회 호출 → RPM 안전권. 30개 정책 ≈ 2.5분.
  - `crawl.py` `iter_services` `client_filter_user_type` — 콤마 분리 `'개인,가구'` 형태 입력 지원, OR 매치(any). 단일 값 substring 매치 + 콤마 분리 list 둘 다.
  - `.github/workflows/crawl-policies.yml` `user_type` input default를 `'개인,가구'`로 변경.
- **검증 후 예상**:
  - LLM 0% → 정상화 (RPM throttling으로 429 회피)
  - 부처 다양성 확보 (보건복지부 가구 단위 정책 포함)
  - 30개 정책 빌드 시간 ~2.5분
- **사용자 추가 옵션** (우리 fix 안 풀리면): https://aistudio.google.com/apikey → 새 키 클릭 → 사용량/한도 확인. 무료 한도 진짜 0이면 Google Cloud에서 결제 활성화(여전히 무료 한도 내 사용은 0원).

