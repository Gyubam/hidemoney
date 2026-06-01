# 숨은지원금

> 정부 지원금 추천 앱 / 1인 개발 / 0원 운영 / 디자인·UX 1순위
> 시작일: 2026-05-15

---

## 📍 다음 세션 시작 가이드 (집/다른 PC에서 이어할 때)

> **마지막 작업 시점: 2026-06-01 — R13 출시(1.0.0) 후 첫 업데이트(1.0.1) 빌드 완료 🚀**
> 5/18 출시 → **정식 출시 완료**. 오늘 매칭 대개편 + 강제업데이트 기능 넣고 `versionCode 2 / 1.0.1` AAB 빌드함.
> 다음 세션: **Play Console에 1.0.1 AAB 업로드 + 출시** / GitHub Pages `app-config.json` 반영 확인 / 광고·가입 통계 모니터링.
> **`CLAUDE.md`에 사용자 프로필·디자인 톤·협업 규칙 다 운반함** — Claude Code가 자동 로드.

### 🚦 현재 상태 (2026-06-01 기준)

| 영역 | 상태 |
|---|---|
| **출시** | 1.0.0 정식 출시 완료. **1.0.1 AAB 빌드 완료** (`app-release.aab` 10.41 MB, versionCode 2) — 업로드 대기 |
| **앱 빌드** | release 서명 빌드 OK. 출시 노트: "내게 맞는 추천·우리 동네 맞춤·출생연도 입력·지역 정확도" |
| **Firebase** | Auth + Firestore + 두 패키지(debug/release) SHA-1 등록 완료 |
| **AdMob** | 실 ID 등록. 전면 광고: 상세 **3회**마다 + **외부 신청링크 클릭 시 매번** (쿨다운/진입보호 제거) |
| **강제 업데이트** | `docs/app-config.json`의 `minVersionCode`로 제어. 현재 `min=1`(강제 X). 올리면 그 아래 버전 차단 |
| **매칭 엔진** | strict→**soft + 개인화 랭킹(relevanceScore)**. 보편정책 노출 + roiScore 정렬. 시·군·구 정밀 매칭 |
| **Play Console** | 1.0.0 출시됨. 1.0.1 업로드 필요 |
| **GitHub Pages** | 랜딩 + privacy + account-deletion + policies.json + **app-config.json(신규, push 필요)** |
| **데이터 cron** | 매일 KST 03:00 detail 풀빌드. 정책 약 **9,939개** 축적 |
| **신규 정책 알림** | WorkManager 24h diff baseline 동작 |

### 🔐 절대 잃어버리면 안 되는 것

| 항목 | 위치 |
|---|---|
| **Release keystore** | `hidemoney-release.jks` (외장 백업 + Google Drive — 사용자가 직접 백업) |
| **keystore.properties** | 비밀번호 포함 (gitignore, 백업 필수) |
| **Keystore 비밀번호** | `PiC2wehlZ8Wy9p9lp2bU0aVC` (24자 영숫자 랜덤. 별도 비번 매니저 저장) |
| **SHA-1 (release)** | `EC:F0:4D:C7:03:E7:CE:F6:94:99:D5:08:D9:A6:B9:90:1F:32:67:6F` |
| **SHA-256 (release)** | `DC:26:60:CE:01:71:18:34:8A:7A:70:DE:9D:4F:53:B3:12:0F:D3:47:A7:D5:87:74:EE:BF:3F:C4:E7:DC:94:8F` |

> **Play 앱 서명 활성화 추천**: Play Console에서 처음 업로드 시 "Google에서 키 관리" 선택 시 키 분실 복구 가능. 우리는 첫 업로드라 이 옵션 사용 가능.

### 📅 다음 세션 할 일 (출시 후)

**1순위 — 심사 결과 확인**:
- https://play.google.com/console → 앱 → 출시 → 내부 테스트
- 상태: `검토 중` → `사용 가능` (1~3일) 또는 `거부됨` (이유 표시)
- 거부 시: 정책 위반 항목 fix + 재제출

**2순위 — 폴리시 보강**:
- **런처 아이콘 캐시 갱신 확인**: 폰에서 새 디자인 잘 보이는지. 안 보이면 백그라운드 색·foreground safe zone 조정
- **스크린샷 보강**: 5장 캡처해서 Play Store에 추가 업로드 (출시 후 변경 가능)
- **태블릿 스크린샷**: Play Console이 요구하면 폰 스크린샷 비율 맞춰 변환
- **GitHub Pages 미리보기 (`index.html`)**: `https://gyubam.github.io/hidemoney/` 실제 동작 확인. 깨진 거 fix

**3순위 — 사용자 피드백 반영**:
- 베타 테스터 (지인 5~10명) Play Store 내부 테스트 링크 공유
- 피드백 받아서 다음 라운드 작업
- AdMob 광고 노출·클릭 통계 모니터링 (~1주일 후 통계 보임)
- Firebase Auth 가입자 / Firestore 사용량 / 알림 수신 통계

**4순위 — 추후 R 라운드**:
- R10 LLM 정련 백필 (출시 후 데이터 품질 보강)
- R8d 후속: 트리거 활성 정책 홈 missed 가중 노출 / WorkManager 알림 강화
- Crashlytics 추가 (출시 후 크래시 모니터링)
- 마이 → 알림 세부 옵션 (D-1/D-3/D-7 토글) — M7
- 어필리에이트 (이사·결혼·임신·창업 시점) — 사업자 등록 후

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

| 라운드 | 작업 | 사용자 개입 | 예상 | 상태 |
|---|---|---|---|---|
| **R1** | GitHub Pages 활성화 + remote fetch 검증 | Settings → Pages 클릭 | 5분 | ✅ 완료 |
| **R2** | GitHub Actions 크롤러 + Gemini Flash + data.go.kr API + 9,923개 풀빌드 | Gemini/data.go.kr API key + GitHub Secret | 다 끝 | ✅ 완료 |
| **R3** | MainActivity 홈 집계 동적 + 매칭 알고리즘 정밀화 | 없음 | 다 끝 | ✅ 완료 |
| **R4** | 즐겨찾기 목록 화면 + 검색·필터 화면 | 없음 | 다 끝 | ✅ 완료 |
| **R5** | 매칭 알고리즘 정밀화 (PolicyRelevance broad sentinel fix + sensitive 7토글) | 없음 | 다 끝 | ✅ 완료 |
| **R6** | WorkManager 알림 (D-3/D-1/D-0) + 알림 deep-link + 로딩 인디케이터 | 없음 | 다 끝 | ✅ 완료 |
| **R7** | 홈 카드 초기 깜박임 fix (`isLoading` 분기) | 없음 | 5분 | ✅ 완료 (2026-05-18) |
| **R8a** ⭐ 톱픽 | **빈 액션·하드코딩 fix 묶음** (탭별 미구현 체크리스트 R8 참조) | 없음 | 1 세션 | 🔜 다음 |
| **R8b** | 마이 카드 동적화 (신청한/받은 상태 추적 + 수령 확인 흐름) | 없음 | 1 세션 | |
| **R8c** | 캘린더 실데이터 전환 (9,923개 deadline 자동 캘린더화 + 즐겨찾기 강조) | 없음 | 1 세션 | |
| **R8d** | 이벤트 실데이터 전환 (LifeEvent별 키워드 매칭 + "이사해요" 트리거) | 없음 | 1~2 세션 | |
| **R9** | Firebase 연동 (Auth Google 로그인 + Firestore + FCM) | Firebase 콘솔 + `google-services.json` | 2~3 세션 | |
| **R10** | LLM 정련 백필 (amount 42→80%, period 0%→채움, summary 토스 톤) | 워크플로 트리거 1회 | 1 세션 | |
| **R11** | 출시 자산 (아이콘 512 / 피처그래픽 1024×500 / 스크린샷 4~5장 / `docs/privacy.md`) | 자산 검수 | 1 세션 | |
| **R12** | 서명 빌드 (keystore + ProGuard + AAB) + Play Console 등록 | keystore 백업 + **25 USD 결제** | 1~2 세션 | |
| **R13** | 내부/Closed 베타 → 버그 수정 → **Production 출시** | 베타 테스터 모집 + 심사 1~3일 | 지속~1주 | |

**남은 작업: 약 9~13 세션 후 정식 출시**. 사용자 개입은 콘솔 설정·결제 위주.

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

### 2026-05-15 (R2.7.7 — 서버 필터 비활성 + 빈 결과 방어 + 0건 사고 복구)

- **R2.7.6 검증 사고**: 빌드 결과 `docs/policies.json`이 **`[]` 빈 배열로 갱신됨**.
  - 원인: `cond[사용자구분::LIKE]=개인,가구` — 정부24 서버가 콤마를 **literal로 해석** → "개인,가구"라는 단일 값에 매치되는 정책 0건 → fetch 0개 → normalize 0개 → write_if_changed가 `[]` 정상 쓰기 → bot auto-commit으로 push
  - 우리 가정: 콤마=OR 분리자 / 실제: 콤마=literal 문자
  - 추가 학습: 서버 LIKE 필터는 단일 값만 받음. OR 매치 불가.
- **R2.7.7 fix**:
  - `crawl.py` `fetch_policies` — 서버 필터(`user_type=`)를 **`None`으로 강제**. 한국어 키 + 콤마 OR 처리 모두 불안정하므로 서버 필터 자체 비활성. 클라이언트 측(`client_filter_user_type=`)에서만 substring OR 매치. 서버는 모든 정책 반환 → 클라가 개인/가구 매치만 yield → limit 채울 때까지 페이지 추가 fetch.
  - `build_policies.py` `run` — **crawl 모드에서 0건 결과 시 write 스킵**. 기존 `docs/policies.json` 그대로 유지. 다음 빌드에서 정상 fetch되면 그때 갱신. 빈 list가 사고로 push되는 패턴 영구 방지.
- **다음 빌드 후 회복 예상**: 정찰 기준 200건 중 ~170건(85%)이 개인/가구 매치 → per_page=100 한 페이지에서 약 85개 통과 → limit=20 채우기 충분 → 진짜 데이터로 복구.

### 2026-05-15 (R2.8 — 하이브리드 풀빌드 + 증분 merge)

- **방향 전환**: 사용자 제안 "LLM 안 쓰고 정부 API만으로?" 검토 결과 — 가능하나 summary 톤이 정부 문서체. **하이브리드 톱픽**: 결정론으로 빠른 풀빌드 + LLM은 점진 백필.
- **R2.8.A 구현 — `--merge` 모드**:
  - `build_policies.py` `_load_existing_policies()` 헬퍼 + `--merge` 플래그
  - merge 로직: 기존 `docs/policies.json` 로드 → id 기준 dict 병합 → 같은 id면 새 데이터로 덮어쓰기, 없는 id면 추가 → **기존 정책 절대 사라지지 않음**
  - 로그에 `merge: existing=N, added=N, updated=N, total=N` 출력
  - `workflow_dispatch` `merge` input 추가 (기본 `'true'`) — 매일 cron도 자동 merge로 안전 모드
- **세 가지 운영 시나리오**:
  1. **첫 풀빌드 (사용자 1회 트리거)**:
     - `crawl=true, enrich=false, limit=500, user_type=개인,가구, merge=false`
     - LLM 없음 → throttling 없음 → ~5분에 500개 catalog 구축
     - 결정론으로 다 채움: title/category(서비스분야→6카테고리)/eligibilityRule(JA0xxx)/amount(정규식)/summary(정부 raw)/applicationOrg+Url
     - LLM 백필 안 됨 → summary 톤이 정부 문서체이긴 함
  2. **매일 cron (자동)**:
     - `crawl=true, enrich=true, limit=30, user_type=개인,가구, merge=true`
     - 30개 fetch + LLM 정련 + 기존 catalog에 merge → 매일 30개씩 LLM 정련 누적
     - throttling 효과로 RPM 안전 + merge로 사고 0
  3. **수동 LLM 백필 (사용자가 가끔 트리거)**:
     - 옵션 1과 옵션 2 사이. 사용자가 빠르게 LLM 정련 늘리고 싶을 때.
- **2~3개월 후 예상**: 풀 catalog (수백~수천 정책) + LLM 정련률 점진 상승 → 일정 시점에 100% 정련.
- **즉시 사용자 액션 (지금 한 번만)**:
  - push 후 `정책 자동 빌드` → Run workflow 입력:
    - crawl: true / enrich: **false** / limit: **500** / user_type: 개인,가구 / merge: **false**
    - 5분 정도 빌드 → 500개 풀 catalog 즉시 구축
  - 그 다음 매일 새벽 3시 cron이 알아서 LLM 정련 + merge 갱신

### 2026-05-15 (R2.8.X — LLM cron 제거 + list-only 풀 fetch 전환)

- **사용자 결정**: "LLM 없애고 정부api만 전체 끌고와줘 하루한번. LLM은 한번에 수동으로 너한테 맡길게"
  - 매일 cron은 LLM 안 씀. 정부 데이터만 매일 풀 fetch.
  - LLM 정련은 별개 작업. 사용자가 Claude에게 요청 → Claude가 detail+conditions fetch + LLM + merge + push.
- **R2.8.X 구현**:
  - `crawl.py` `fetch_list_only(client, limit, user_type)` — list_services만 페이징 fetch, detail+conditions는 받지 않음. 10,000개 정책을 ~3분에 완료 가능 (페이지당 0.3s sleep). RPolicy 만들 때 detail/conditions=None.
  - `build_policies.py` `--list-only` 플래그 추가 + `--limit 0` (또는 음수)이면 무제한 fetch. list-only 모드에서는 LLM 자동 비활성(`enrich and not list_only` 가드)
  - `workflow_dispatch`에 `list_only` input 신설 (기본 `'true'`)
  - cron 기본 동작 변경: `crawl=true list_only=true enrich=false limit=0 user_type=개인,가구 merge=true`
    - 매일 새벽 3시: 정부24 catalog 풀 fetch (~10,000개) → 결정론 매핑 → merge → push
    - LLM·RPM 걱정 0
- **list-only 모드의 결정론 채움**:
  - ✅ title, applicationOrg, applicationUrl
  - ✅ category (서비스분야 → 6 카테고리 매핑)
  - ✅ amount (정규식, list_row의 지원내용 텍스트)
  - ✅ summary (list_row.서비스목적요약 — 정부 raw 톤)
  - ❌ eligibilityRule (supportConditions 별도 endpoint, 받지 않음)
  - ❌ documents/procedure/eligibility (detail에서만 충실, LLM 백필에서 채움)
- **다음 라운드 (R2.9 — LLM 백필)**: 사용자가 "정책 LLM 정련해줘 N개" 부탁 시 Claude가 수동 처리. 별도 워크플로우 또는 직접 코드 실행:
  1. 기존 `docs/policies.json` 로드
  2. LLM 정련 안 된 정책(예: `summary`가 정부 raw 톤이거나 `eligibilityRule` 없음) N개 선별
  3. 각 정책에 `serviceDetail` + `supportConditions` fetch
  4. `_llm_summary` + `_llm_items` 호출 (throttling 포함)
  5. merge → push
  - 사용자 부담: Run workflow 1회 클릭 또는 채팅 한 줄
  - LLM 호출은 quota 안전권에서 100개 단위 처리

### 2026-05-16 (집 PC — list-only 풀빌드 결과 진단 + detail 풀빌드 준비)

**상황**: 어제 list-only 풀빌드로 `docs/policies.json`에 9,919개 들어옴. 폰 설치 후 홈 화면 확인 — 그러나 **여전히 하드코딩 샘플 그대로 표시됨** (당신이 놓친 돈 2,400,000원 / 청년 월세 지원 / 출산장려금).

**원인 진단** (2단계):

1. **MainActivity가 `SampleData.home` 그대로 사용** (MainActivity.kt:149-161):
   - `allPolicies` 9,919개는 calendar/detail/favorites 용도로만 쓰임
   - 홈 집계(missedTotal/thisWeek/deadlineSoon)는 하드코딩
   - → MainActivity 홈 집계를 `allPolicies` 기반으로 전환 필요 (Task #4, detail 받은 후)

2. **9,919개 데이터가 너무 빈약** — 필드 채움 현황:
   - title / category / summary / applicationUrl: 100%
   - amount: 42% (4,191건만)
   - **deadline / eligibility / documents / procedure / region / eligibilityRule: 0%**
   - 원인: `normalize.py`가 LLM 없으면 list_row의 신청기한/지원대상/신청방법 등을 안 활용. LLM이 채우는 코드만 있었음.

**정부 API 응답 스키마 정리** (사용자 공유):
- `serviceList` (list_row): 서비스ID/지원유형/서비스명/서비스목적요약/**지원대상/선정기준/지원내용/신청방법/신청기한**/상세조회URL/소관기관코드/소관기관명/부서명/조회수/소관기관유형/사용자구분/서비스분야/접수기관/전화문의/등록일시/수정일시
- `serviceDetail`: 위 + 서비스목적/**구비서류**/접수기관명/문의처/**온라인신청사이트URL**/행정규칙/자치법규/법령/공무원확인구비서류/본인확인필요구비서류
- `supportConditions`: **JA0xxx 코드** (연령/지역/직업/가족형태/학력/기타)
- → list_row만으로도 deadline + eligibility + procedure 추출 가능. **구비서류는 detail에서만**, **eligibilityRule(JA0xxx)는 supportConditions에서만**.

**일일 호출 한도 확인**: data.go.kr 일일 한도 **500,000콜**. 9,919 × 3엔드포인트 = 29,757콜 = 한도의 6%. 풀빌드 1회 여유 충분.

**오늘 변경 (R2.9.1 — normalize 결정론 보강 + detail 풀빌드 준비)**:

| 파일 | 변경 |
|---|---|
| `tools/normalize.py` | `parse_deadline()` 추가 — list_row.신청기한 → ISO yyyy-MM-dd 추출 ("상시"/"수시"/"예산소진"은 빈 값). `split_to_items()` 추가 — 줄바꿈/글머리표/번호로 자유 텍스트 항목화. 결정론 단계에서 deadline + eligibility(지원대상+선정기준) + procedure(신청방법) + documents(구비서류) 채움. LLM 단계는 빈 리스트로 결정론 결과 덮어쓰지 않게 fix |
| `tools/build_policies.py` | `--limit 0` + list-only off 조합 시 30개 제한 버그 fix (`fetch_policies(limit=effective_limit or 30)` → `fetch_policies(limit=effective_limit)`) |
| `tools/crawl.py` | `fetch_policies(limit: Optional[int] = None)` 시그니처 변경 + 로그 포맷 안전화. `REQUEST_GAP_SEC` 0.3s → 0.15s (한도 50만이라 여유). |
| `.github/workflows/crawl-policies.yml` | `timeout-minutes` 15 → 360 (풀빌드 1.5~2h 수용) |

**내일(2026-05-17) 할 일** — 사용자 트리거 + 결과 확인 워크플로:

1. **사용자**: GitHub Actions → `정책 자동 빌드` → **Run workflow** 클릭. 입력:
   - `crawl: true`, `list_only: **false**`, `enrich: false`, `limit: 0`, `user_type: 개인,가구`, `merge: false`(덮어쓰기로 풀 재생성)
2. 빌드 진행 ~1.5~2시간 (9,919개 × 약 0.6s = 약 100분)
3. 완료 후 `docs/policies.json` 검증 (Claude):
   - deadline 채움률 (예상 20~40% — 한국 정책 다수는 상시신청)
   - eligibility/procedure/documents 채움률 (예상 70%+)
   - eligibilityRule 채움률 (JA0xxx 코드가 채워진 정책만)
4. 검증 OK면 **MainActivity 홈 집계 코드 작업** (Task #4):
   - `SampleData.home` 하드코딩 제거
   - `allPolicies` + profile로 thisWeek/deadlineSoon 동적 계산
   - missed/missedTotal은 deadline 지난 자격 충족 정책으로 계산 (또는 별도 휴리스틱)
5. 폰 빌드/설치 + 실제 9,919개로 작동하는 홈 화면 검증
6. 데이터 검증 후 사용자 톤 피드백 → 차후 라운드 (즐겨찾기 화면 / WorkManager 알림 / Firebase 등)

**다음 라운드 (R3) 후보**:
- 즐겨찾기 목록 화면 (MyScreen → 받을 예정 카드 탭 시)
- WorkManager 알림 스케줄링 (D-3, D-1 푸시)
- Firebase Auth 통합 (Google 로그인)
- 검색 + 필터 화면 (9,919개 중 자격 충족인 것만 또는 카테고리·지역 필터)

### 2026-05-17 (R2.9.3 — 홈 집계 동적 + 매칭 정밀화 + JA 코드 신규 매핑)

**상황**: R2.9.2로 풀빌드 데이터 복구 후 R3 진행. MainActivity 홈 집계를 9,923개 실데이터로 전환. 그 과정에서 사용자 피드백 받아가며 매칭 정확화 라운드.

**A. 홈 집계 전환 (R3 Task #4 완료)**:
- `HomeAggregator.kt` 신규 — `computeHome(allPolicies, profile, today): HomeData` 동적 계산. thisWeek(7일 이내, amount 최대) / deadlineSoon(30일 이내, daysLeft 오름차순 5개) / missedTotal·Count(자격 충족+amount>0 전체) / missedGrants(amount 큰 순 100개)
- `PolicyMatching.kt:withFreshDaysLeft(today)` 추가 — 빌드 시점 stale daysLeft를 today 기준 재계산
- `MainActivity.kt` — `SampleData.home` 하드코딩 제거, `allPolicies + profile + today`로 동적
- `MissedSheet` 의 SampleData.home 참조도 `home` 사용

**B. AnimatedAmount Long overflow fix**:
- `amount.toInt()` Int 범위 ±21억 넘으면 wrap around → 음수 표시 버그
- `animateFloatAsState`로 Long 보존
- `formatAmount` 토스 톤 한글 단위 통합 (1만 미만: "9,500원" / 1만~1억: "120만원" / 1억~10억: "1.2억원" / 10억~1조: "1,303억원" / 1조+: "1.5조원")
- `suffix` 파라미터 제거 (3 호출자 다 안 씀)
- `rememberSaveable` — LazyColumn 스크롤로 dispose/recompose 시 카운트업 다시 발동 버그 fix

**C. 자격 매칭 strict 모드** (광범위 정책 → 매칭 X):
- 정부 API sentinel 무시: `maxAge >= 100`, `minAge < 2`
- thin rule (유효 조건 0개) 매칭 불가 처리
- 선택 정보 부재 시 false (occupation/married/hasChildren 입력 안 했으면 그 룰 정책 제외)
- `eligibilityRule null`이면 false
- "minAge ≤ 19 + maxAge sentinel" 조합도 broad → 매칭 제외 (성인 누구나 패턴)
- broad occupation detection: `requiresOccupation` 가 [학생/직장인/구직 중] 다 포함이면 무관 처리

**D. UserProfile 4 필드 확장**:
- `incomeMonthly` (월 소득 구간 — IncomeBrackets), `householdSize` (1~4+), `education` (4단계), `housingType` (4단계)
- `Educations`, `HousingTypes`, `HouseholdSizes`, `IncomeBrackets` 옵션 객체 추가
- `UserPrefs` 저장/로드 확장
- `EligibilityRule` 5 신규 필드 (maxIncomeMonthly/maxIncomePercent/maxHouseholdSize/minHouseholdSize/requiresEducation/requiresHousingType)
- `PolicyMatching` 매칭 로직 확장 + 가구원수별 중위소득 테이블 (2026년 기준 1인 222만, 2인 368만, 3인 471만, 4인 572만)
- `OnboardingScreen` ProfileInputPage에 4 신규 OptionPickerField + Bottom Sheet picker 4개 추가
- `ProfileEditScreen` 재사용 → 자동 적용

**E. supportConditions JA 코드 정찰** (사용자 swagger URL 공유):
- 100개 sample inspect — `tools/inspect_conditions.py` 신규
- swagger 명세 확보:
  - **소득 (JA0201~JA0205)**: 중위소득 0~50% / 51~75% / 76~100% / 101~200% / 200%+
  - **가족형태**: JA0303(출산/입양), JA0404(1인가구), JA0411(다자녀), JA0412(무주택)
  - **직업**: JA0317~0320(학생류), JA0326(직장인), JA0327(구직자)
  - **장애/보훈/질병**: JA0328~JA0330
  - 학력 코드는 swagger에 없음 — 매핑 보류
- 정찰 결과: 30 중 거의 100% Y인 코드(JA0101/JA0102/JA0111/JA0201/JA0202 등)는 sentinel, 일부만 Y인 코드(JA0322 47%, JA0303 13% 등)가 의미 있는 매칭 후보

**F. normalize.py 매핑 확장 + broad detection**:
- 소득: JA0201~JA0205 활성 중 max 비율 → `maxIncomePercent`. JA0205(200%+) 활성 시 무관 처리
- 가족형태: JA0404 → `maxHouseholdSize=1`. JA0412 → `requiresHousingType=["전세","월세","기타"]`
- 직업: 학생/직장인/구직 3개 다 활성이면 무관(룰 제외). 1~2개만 활성이면 해당 직업 list
- 출산: JA0303 → `requiresChildren=true` (기존 유지)

**G. 효과**:
- 매칭 998건 → 372건 (broad detection + age sentinel + strict mode 누적)
- MissedSheet 표시 3개 → 100개 cap (LazyColumn 스크롤)

**남은 한계 (이번 풀빌드 트리거로 해결 예정)**:
- normalize.py 새 매핑은 어제 데이터에 없음. 즉 소득/1인가구/무주택 매칭 효과 0 (클라이언트엔 적용됐지만 데이터 측에 룰 없음)
- 풀빌드 1회 더 필요 — 사용자가 GitHub Actions trigger (`list_only=false, merge=false, ~1.5시간`)
- 풀빌드 후 예상: 372건 → 100~200건 (진짜 소득/가구 조건 통과한 정책만)

**다음 라운드 트리거 (사용자)**:
- GitHub Actions → "정책 자동 빌드" → Run workflow
- `crawl: true, list_only: false, enrich: false, limit: 0, user_type: 개인,가구, merge: false`

### 2026-05-17 (R2.9.4 — grantType 필터 + occupation 기반 카테고리 제외)

**상황**: R2.9.3 후 사용자 피드백 — "청년 창업 30억 같은 거 임팩트 카드에 들어가면 거짓 임팩트". 정찰 결과 정부 `list_row.지원유형` 필드가 `||` 분리 multi-value (현금/현금(감면)/현금(장학금)/현금(융자)/현물/이용권/서비스(*)/의료지원/상담/문화/기타/시설이용 등).

**구현 (A+C 조합)**:
- **A**: `Policy.grantType: List<String>` 추가. normalize에서 `||` split. HomeAggregator의 missed 필터에서 융자(`현금(융자)`) 제외, 현금성·바우처(`현금/현금(감면)/현금(장학금)/현물/이용권`)만 포함.
- **C**: HomeAggregator에 `isCategoryRelevant(category, occupation)` — occupation != "사업자"면 category "창업" 제외.
- backward compat: `grantType.isEmpty()`면 통과 (어제 데이터 호환).
- `tools/schema.py` — Pydantic에 grantType + 신규 EligibilityRule 필드 추가 (검증 통과).

**효과 (즉시, 어제 데이터)**:
- C만 작용: 사업자 아닌 사용자의 "창업" 카테고리 정책 제외. 372건 → 일부 감소.

**효과 (풀빌드 후)**:
- A 발현: 융자 제외 + 현금성/바우처만 → 진짜 받을 수 있는 지원금만.
- 372건 → 50~150건 예상 (실제 받을 수 있는 정책만)

**향후 옵션 (B 보류)**:
- 사용자 토글로 "융자 포함" / "사업 자금 포함" 등 활성화. 디폴트는 OFF.

### 2026-05-17 (R4 — 즐겨찾기 목록 화면 + R5 검색·필터 화면)

**R4 즐겨찾기 화면**:
- `FavoritesScreen.kt` 신규 — 임팩트 카드(받을 예정 총액) + 정책 카드 리스트 (마감 임박 우선, amount 큰 순)
- MyScreen "받을 예정" 카드 탭 → FavoritesScreen 진입
- `MainActivity.Screen.Favorites` sealed class 추가
- 빈 상태 + D-day pill (D-3 이내 빨간 강조)

**R5 검색·필터 화면**:
- `SearchScreen.kt` 신규 — 검색바 + 카테고리 chip(6개) + 자격 충족/전체 토글 + LazyColumn 결과
- 홈 TopBar 우상단 🔍 아이콘 → SearchScreen 진입
- "자격 충족만" 토글 = 홈 missed와 같은 PolicyRelevance (양쪽 일관 통일)
- 카운트 표시 — "84건 / 전체 9923건 중 자격 충족" 형식

### 2026-05-17 (R6 — 매칭 알고리즘 정밀화 + MissedSheet 통째 교체)

**A. PolicyRelevance 정밀화 (즉시 효과)**:
- gender FEMALE_KEYWORDS에서 **"출산" 제거** — 부부/남성 아빠도 대상 (false negative fix)
- business 키워드 — `BUSINESS_STRONG_KEYWORDS` (단독 OK) + `BUSINESS_COMPETITION_BIGRAMS` (결합어만) 분리. 일반 미술/문학 공모전 false positive 해소
- region — 시군구 → 광역 매핑 테이블 신규 (서울 25구 + 경기 30+ + 광역시·지방 100+개). applicationOrg 추출 정확도 ↑

**B. PolicyMatching 개선**:
- 소득 매칭 `householdSize` 부재 시 **1인 가정 fallback** — 소득 조건 정책 21.5% 다 사라지던 문제 해소
- `minChildCount` 필드 추가 (다자녀 매칭)
- `hasEffectiveCondition`에 minChildCount 포함

**C. HomeAggregator strict**:
- `grantType.isEmpty()` → false (이전엔 backward compat로 통과). 풀빌드 후 100% 채워졌으니 strict.

**D. UserProfile 확장**:
- `childCount: Int?` 추가 + Onboarding ChildCount picker (hasChildren=true일 때만 표시)
- UserPrefs 저장/로드 확장

**E. normalize.py 매핑 추가**:
- JA0411 (다자녀가구) → `minChildCount=2 + requiresChildren=true`
- `tools/schema.py` Pydantic에 minChildCount 추가

**F. MissedSheet 통째 교체 — Dialog 방식**:
- Material3 ModalBottomSheet의 sheet drag gesture가 `confirmValueChange`로도 visual 멈춤 안 되는 한계
- `androidx.compose.ui.window.Dialog` + 자체 Box bottom-aligned sheet로 교체
- sheet 자체 swipe gesture 0 — content LazyColumn만 자유 scroll
- 닫기: 헤더 X 버튼 / outside dim 탭 / dragHandle swipe (SwipeableDragHandle)
- 카드/카드 사이 빈 공간/자세히보기 클릭+스크롤 어떤 동작에도 sheet 안 닫힘

**G. 학력 매핑 정찰 결과**:
- JA21xx (JA2101/JA2103) → 학력이 아니라 **농어업 종사자** 코드 (원양어업/천일염/수산자원). swagger에 학력 코드 없음
- 학력 매핑 보류 — 사용자 입력은 받지만 매칭 X (향후 추가 정찰 필요)

**효과 누적**:
- 매칭 84건 → 추가 정밀화 (출산 정책 포함 + 시군구 정밀 region + 소득 fallback)
- 검색 화면 카운트 = 홈 missed 카운트 (PolicyRelevance 공통)
- MissedSheet 스크롤 사고 0
- 자녀 수 입력 + 다자녀 매칭 (풀빌드 후 발현)

**사용자 풀빌드 트리거 (R6의 E 부분 발현 위해)**:
- normalize.py에 JA0411 매핑 추가됨 → minChildCount 데이터 채움 필요
- GitHub Actions → "정책 자동 빌드" → Run workflow (같은 input)
- ~5~10분 (concurrent 가속)

### 2026-05-17 (R7 — WorkManager 알림 + 알림 deep-link + 사용자 토글 확장 + 매칭 broad sentinel fix + 로딩 인디케이터 + 라벨 정리)

> **회사 PC에서 이어할 때 핵심 컨텍스트 — 이 라운드가 매우 큼**

#### A. WorkManager 알림 시스템 (R7-A)
즐겨찾기 정책 D-3/D-1/D-0 마감 알림. 토스 톤 "받게 만드는 앱" 핵심.

| 파일 | 역할 |
|---|---|
| `notification/NotificationHelper.kt` | NotificationChannel 생성 + 알림 발송. PendingIntent(MainActivity + extra "policy_id") |
| `notification/PolicyDeadlineWorker.kt` | CoroutineWorker. 캐시(filesDir/policies-cache.json) 우선 로드 + remote fallback → favorites 정책 검사 → D-3/D-1/D-0이면 알림 발송 |
| `notification/NotificationScheduler.kt` | `schedulePeriodic`(24h ± 4h flex, KEEP) + `runOnce`(즐겨찾기 toggle 시 즉시) |
| `MainActivity.onCreate` | `NotificationScheduler.schedulePeriodic(applicationContext)` 호출 |
| `MainActivity` 정책 detail onToggleFavorite | favorites toggle 시 `runOnce` 호출 |

의존성: `androidx.work:work-runtime-ktx:2.10.0` 추가 (`libs.versions.toml` + `app/build.gradle.kts`).
권한: `POST_NOTIFICATIONS` 이미 manifest에 있음.

#### B. 알림 deep-link (R7-B)
알림 탭 시 정책 detail 직진. `MainActivity`에 `pendingPolicyId: MutableState<String?>` 추가. onCreate/onNewIntent에서 intent extra 픽업. AppRoot LaunchedEffect가 소비 후 `Screen.PolicyDetail`로 진입.

#### C. UserProfile 토글 확장 (R7-C)
sensitive 5종 + 융자 + 저소득 → 7개 토글 추가.

```kotlin
val isMulticultural: Boolean = false       // 다문화가족
val isSingleParent: Boolean = false        // 한부모/조손
val isDisabled: Boolean = false            // 장애인
val isVeteran: Boolean = false             // 국가보훈
val isDefector: Boolean = false            // 북한이탈주민
val isLowIncome: Boolean = false           // 저소득·수급자·차상위 (R7-G에서 추가)
val includeLoanGrants: Boolean = false     // 융자 정책 포함
```

`Occupations.all`에 "농어업", "예술인" 추가 (총 7개: 직장인/학생/사업자/프리랜서/구직 중/농어업/예술인).

Onboarding에 "특수 대상 정책" 섹션 신규 + "표시 옵션" 섹션. `BooleanToggleRow` 신규 컴포저블 (토스 톤 스위치).

UserPrefs 저장/로드 확장.

#### D. 매칭 알고리즘 broad sentinel fix (R7-D) — 핵심 진단
**근본 원인**: 정부 supportConditions JA 코드가 broad sentinel.
- 100 sample 정찰에서 JA0401~JA0411 (가족 형태): 60~80% 모든 정책에 활성
- normalize.py가 JA0411(다자녀) → `requiresChildren=true` inject로 누적 → 풀빌드 데이터 `requiresChildren: 86.3%`, `requiresHousingType: 79.8%`, `maxHouseholdSize: 76.7%` inflate
- 사용자 hasChildren=false + 자가 → 86% × 79.8% ≈ 60% 정책 자동 fail (거짓 양성 누적)

**fix**: `PolicyMatching`에서 매칭 무시 + `PolicyRelevance` 키워드 기반 분기
- `hasEffectiveCondition`에서 `requiresChildren / requiresHousingType / maxHouseholdSize / minHouseholdSize / minChildCount / sensitive 5종 (requiresMulticultural 등)` 제외 (broad sentinel)
- 매칭 통과는 신뢰도 높은 필드만: `effectiveMinAge / effectiveMaxAge / regions / effectiveRequiresOccupation / requiresMarried / maxIncomeMonthly / maxIncomePercent / requiresEducation`
- 키워드는 `PolicyRelevance.isEligibleForUser`에서 처리 (홈/검색 공통)

#### E. PolicyRelevance 키워드 그룹 (R7-E) — 매우 세세함
`isEligibleForUser(policy, profile)` 결정 순서:

```kotlin
1. policy.isEligible 통과
2. !isSensitiveExclusion(policy, profile)
3. !isChildPolicyMismatched(policy, profile)
4. !isHousingPolicyMismatched(policy, profile)
5. !isSingleHouseholdMismatched(policy, profile)
6. !isSeniorPolicyMismatched(policy, profile)
7. isCategoryRelevant(policy, occupation) — 사업자 외 창업 제외
8. isRegionRelevant(policy, region) — 광역 추출 + 시군구 → 광역 매핑
9. isGenderRelevant(policy, gender) — 출산은 부부 정책 (남성도 OK)
```

**isSensitiveExclusion 키워드 그룹** (PolicyRelevance.kt):
- **항상 제외 (사용자 토글 X)**:
  - `ADMIN_KEYWORDS`: 범죄수익/환수/벌금/재소자/보호관찰/교정
  - `VICTIM_KEYWORDS`: 가정폭력/성폭력/학교폭력/스토킹/아동학대
  - `FACILITY_KEYWORDS`: LPG용기/보일러 교체/지하수/축사/온실
  - `DISEASE_KEYWORDS`: C형간염/희귀질환/암환자/치매/투석
  - `SPECIAL_OCCUPATION_KEYWORDS`: 광업/원양어업/임업/축산농가/건축주/택시사업자
  - `ADOPTION_KEYWORDS`: 입양/위탁가정/유기동물
  - `VEHICLE_EQUIPMENT_KEYWORDS`: 노후경유차/이륜차/전기차 보조/어선/농기계
  - `FACILITY_OWNER_KEYWORDS`: 태양광 설치/노후주택/단열/보일러 교체/주택 개보수
  - `PET_KEYWORDS`: 반려동물/반려견/반려묘/광견병
  - `SCHOOL_STAFF_KEYWORDS`: 교원/교사 대상/학교장/사립학교 교직원
  - `DISASTER_VICTIM_KEYWORDS`: 이재민/재난피해/수해/한파/재난지원금
  - `RND_KEYWORDS`: R&D 과제/특허 출원/연구장비
- **사용자 토글/occupation 분기**:
  - `위안부` → gender="여"만
  - `국가유공자/보훈/참전유공/독립유공` → isVeteran=true만
  - `다문화가족/다문화가정` → isMulticultural=true만
  - `북한이탈주민/탈북/새터민` → isDefector=true만
  - `한부모/조손` → isSingleParent=true만
  - `장애인/장애아동/장애학생` → isDisabled=true만
  - `AGRI_FORESTRY_KEYWORDS` (농업인/어업인/축산농가/임업 종사/농가소득/농어가...) → occupation="농어업"만
  - `ARTIST_KEYWORDS` (예술인 지원/공연예술 종사/전통예술 종사/창작 지원금) → occupation="예술인"만
  - `BUSINESS_OWNER_KEYWORDS` (소상공인/자영업/중소기업/법인 대상/협동조합 지원/전통시장 상인) → occupation="사업자"만
  - `YOUTH_KEYWORDS` (청소년/학교밖/학교 밖 청소년/중도탈락) → age ≤ 18만
  - `LOW_INCOME_KEYWORDS` (수급자/수급권자/차상위/기초생활/저소득/취약계층/긴급복지) → isLowIncome=true만

**키워드 기반 분기 (별도 함수)**:
- `isChildPolicyMismatched`: `CHILD_KEYWORDS` (다자녀/둘째/셋째/유아/영유아/어린이/초등학생/유치원/어린이집/영아/신생아) → hasChildren=false면 제외
- `isHousingPolicyMismatched`: `HOMELESS_KEYWORDS` (무주택/전세자금/월세 지원/임차/임대주택/주택 매입) → housingType="자가"만 제외
- `isSingleHouseholdMismatched`: `SINGLE_HOUSEHOLD_KEYWORDS` (1인가구/일인가구/독거) → householdSize != 1이면 제외
- `isSeniorPolicyMismatched`: `SENIOR_KEYWORDS` (노인/어르신/고령자/장년/노년/치매 어르신) → age < 60이면 제외

**business 정밀화**:
- `BUSINESS_STRONG_KEYWORDS` (창업/스타트업/사업화/벤처/기업가/K-스타트업/사업자): 단독 OK
- `BUSINESS_COMPETITION_BIGRAMS` (창업 공모전/사업화 공모전/스타트업 경진대회/K-스타트업): 결합어만. 일반 공모전 false positive 회피

**region 정밀화**:
- `PROVINCIAL_REGIONS` (17 광역): applicationOrg substring 우선
- `MUNICIPALITY_TO_REGION` 시군구 매핑: 서울 25개 구 + 경기 30+개 시 + 광역시·지방 100+개 시군구
- `LOCAL_GOV_PATTERN = (시|군|구)청?$`: 광역 추출 실패 + 시군구 패턴이면 strict 제외 (여수시·곡성군 케이스)

**gender 정밀화**:
- `FEMALE_KEYWORDS = 여성/여학생/여자/임산부/산모/산후/임신` (출산 제거 — 부부 정책)
- `MALE_KEYWORDS = 남성/남학생/남자`
- 양쪽 다 등장 = 일반 정책 통과

#### F. HomeAggregator 매칭 + 표시 (R7-F)
```kotlin
val matched = allPolicies.matchedWith(profile).withFreshDaysLeft(today)
val eligible = matched.filter { PolicyRelevance.isEligibleForUser(it, profile) }
val withDeadline = eligible.filter { it.deadline.isNotBlank() }

// missed 후보 — eligible에서 amount/grantType 추가 필터
val missedCandidates = eligible.filter { p ->
    if (p.amount <= 0) return@filter false
    if (p.grantType.isEmpty()) return@filter false           // 풀빌드 후 100% 채워짐 — strict
    val isLoan = p.grantType.any { it in LOAN_GRANT_TYPES }
    if (isLoan && !profile.includeLoanGrants) return@filter false
    isLoan || p.grantType.any { it in MISSED_GRANT_TYPES }
}
```

`MISSED_GRANT_TYPES = 현금/현금(감면)/현금(장학금)/현물/이용권`
`LOAN_GRANT_TYPES = 현금(융자)`

#### G. 로딩 인디케이터 (R7-G)
첫 진입 시 SampleData 19개로 시작 → cache loadAll → remote refresh → 9923개 갱신. 그 사이 "0원 0건" 보이던 거 fix.
- `MainActivity`에 `isLoading: Boolean` state
- HomeScreen.kt `HomeScreen(isLoading)` prop + `ImpactCardLoading` 컴포저블 (CircularProgressIndicator + "분석 중… / 정부 9,923개 정책을 살펴보고 있어요")
- LaunchedEffect refresh 완료 시 `isLoading = false`

#### H. MissedSheet Dialog 교체 + 라벨 정리 (R7-H)
**Dialog 교체** (R6 일부 — 정리):
- Material3 ModalBottomSheet 한계 (confirmValueChange로도 sheet drag gesture visual 멈춤 X)
- `androidx.compose.ui.window.Dialog` + 자체 Box bottom-aligned sheet로 교체
- 닫기: 헤더 X 버튼 / outside dim 탭 / `SwipeableDragHandle`(80px+ 아래로 swipe)
- LazyColumn 안 nestedScroll connection으로 drag만 차단, fling은 자유

**라벨**:
- HomeScreen ImpactCard: "당신이 놓친 돈" → "놓치고 있는 돈"
- "지난 3년 · 미신청 N건" → "신청 안 한 N건"
- MissedSheet 헤더: "당신이 놓친 돈" → "놓치고 있는 돈"
- "N건 · 최근 3년" → "신청 안 한 N건"

#### I. normalize.py + schema.py (R7-I — 풀빌드 후 발현)
JA 코드 매핑 추가 (다음 풀빌드에서 적용):
- JA0411 (다자녀) → `minChildCount=2 + requiresChildren=true` (현재 sentinel inflate 원인. 향후 broad detection 추가 필요)
- JA0401 (다문화) → `requiresMulticultural=true`
- JA0402 (북한이탈) → `requiresDefector=true`
- JA0403 (한부모/조손) → `requiresSingleParent=true`
- JA0328 (장애인) → `requiresDisabled=true`
- JA0329 (보훈) → `requiresVeteran=true`

`tools/schema.py` Pydantic도 확장.

⚠️ **데이터는 풀빌드 결과(`docs/policies.json`)에 이미 들어가 있음** (사용자가 R6 이후 풀빌드 한 번 트리거함, commit `344e8cc`).
하지만 클라이언트는 이 필드 매칭을 무시 (sentinel inflate). PolicyRelevance 키워드만 활용.

#### J. workflow push 충돌 fix (R7-J)
풀빌드 도는 사이 main 갱신되면 push reject되던 사고. `.github/workflows/crawl-policies.yml`에 `git pull --rebase origin main` + 최대 3회 재시도 추가.

---

### 회사 PC에서 이어할 때 — 다음 라운드 후보

#### 우선순위 높음
1. **Firebase Auth (Google 로그인)** — 계정 동기화 (favorites/profile cross-device). 큰 작업
2. **검색 필터 확장** — 금액 범위 / amount 정렬 / 마감 임박 정렬 등
3. **사용자 피드백 학습** — "이 정책 안 맞아요" 버튼 → Firebase 기록 → 향후 ML/규칙 개선

#### 중기 (LLM 정련 — R2.9 백필)
- amount 정밀화 (정규식 42% → LLM 80%+)
- period 채움 (현재 0%)
- summary 토스 톤 정련
- Gemini Flash로 정책 1개당 ~2회 호출. 100건씩 수동 트리거. throttling 5초

#### 보류
- 다자녀 자녀 수 매칭 — UserProfile에 `childCount` 추가됨. 근데 normalize의 minChildCount(JA0411)가 sentinel 78%라 매칭 무시 중. 향후 broad detection 후 활용
- 학력 매핑 — JA21xx/JA22xx swagger에 없음. 100 sample 정찰에선 농어업 코드였음
- normalize broad detection — JA04xx 시리즈 N개 이상 활성 시 룰 제외 (다음 풀빌드 트리거 필요)



**상황**: 사용자 풀빌드 1.5시간 너무 느림.

**fix (`tools/crawl.py`)**:
- `fetch_policies`를 2 phase로:
  1. iter list_row 다 받기 (가벼움, ~1분)
  2. detail+conditions를 `ThreadPoolExecutor(max_workers=8)`로 동시 fetch
- `fetch_full` 내부 sleep 제거 (concurrent에선 thread 수가 rate limit 역할)
- `requests.Session` thread-safe — 안전

**로컬 검증**: 100 정책 5.4초 (sequential ~75초 → 14배 빨라짐). 9,923개 풀빌드 ~5~10분 예상.

**안전성**:
- 일일 한도 50만콜 중 ~20K (4%)
- 400 error 일부 발생하지만 retry로 회복 (have_detail/have_conditions 100/100)
- workflow timeout은 360분 그대로 — 더 줄여도 무방

**다음 액션 (사용자)**:
- 진행 중 풀빌드 cancel → R2.9.5 + R2.9.4 다 적용된 새 풀빌드 재 trigger
- 같은 input (crawl=true, list_only=false, enrich=false, limit=0, user_type=개인,가구, merge=false)
- ~5~10분에 끝남

### 2026-05-17 (R2.9.6 — 추가 정밀화 + UX fix)

**상황**: 풀빌드 완료 후 사용자 피드백 4건 받음. 매칭 84건 → 정밀화 추가.

**A. K-스타트업 등 청년 카테고리에 분류된 창업 정책 제외**:
- normalize.py 카테고리 매핑 "고용·창업" → 기존 "청년" → **"창업"**으로 변경 (다음 풀빌드부터 적용)
- HomeAggregator에 키워드 기반 fallback — title/summary에 "창업/스타트업/사업화/벤처/K-스타트업/경진대회/공모전/콘테스트" 들어가면 사업자 외 제외
- 즉시 효과 + 영구 효과 둘 다

**B. 여수시·곡성군 등 시군구 정책이 서울 사용자에게 노출되던 버그**:
- HomeAggregator에 `extractRegionFromOrg` + `isLocalGovOrg` 추가
- applicationOrg 텍스트에서 17 광역 region 추출. 추출되면 사용자 region과 strict 매칭
- 광역 추출 실패 + 시군구청 패턴 (시|군|구청?$) → strict 제외
- 중앙부처(보건복지부 등) → 전국 대상 → 통과

**C. 여성농업인 정책이 남성 사용자에게 노출되던 버그**:
- UserProfile에 `gender: String?` 추가 ("남"/"여")
- UserPrefs, Onboarding, ProfileEdit에 picker 추가 (Genders 옵션 객체)
- HomeAggregator `isGenderRelevant` — title/summary 키워드("여성/여학생/여자/임산부/산모/산후/출산/임신" vs "남성/남학생/남자")
- 양쪽 키워드 다 있으면 일반 정책으로 통과
- 정부 supportConditions JA0101/JA0102는 거의 100% 둘 다 Y(sentinel)이라 데이터로 매칭 불가, 키워드만으로 처리

**D. MissedSheet swipe 닫기 사고 fix**:
- LazyColumn 안 스크롤 시 ModalBottomSheet swipe로 forwarding되어 sheet 닫히는 사고
- 1차 시도: NestedScrollConnection 차단 → LazyColumn 스크롤 자체 막힘
- 최종 fix: `sheetState`에 `confirmValueChange = { it != SheetValue.Hidden }` — Hidden 상태 변경 차단 → swipe 닫기 무력화
- 닫기는 outside dim 영역 탭만 (ModalBottomSheet 기본 동작)
- LazyColumn 스크롤은 정상 작동

**채움률 검증 (사용자 질문 응답)**:
- eligibility: 98% / eligibilityRule: 99.8% / documents: 93.9% / procedure: 98.3% ✅
- deadline: 5.1% (한국 정책 95%가 "상시신청") — 정상
- period: 0% (정부 raw에 구조화 안 됨, summary 텍스트에 묻혀있음) — LLM 백필 시 채움 가능하지만 우선순위 낮음

**효과**:
- 매칭 84건 → 추가 감소 예상 (region+gender+business 키워드 누적)
- MissedSheet 스크롤 사고 0
- 사용자 신뢰도 향상

**남은 한계 + 향후**:
- 학력 매핑 (swagger에 코드 없음, 정찰 추가 필요) — 보류
- 사용자 토글 (B 옵션 — 융자 포함/사업 자금 포함) — 향후
- LLM 백필 (amount 정밀화, summary 토스 톤) — 우선순위 낮음



**상황**: 어제(05-16) 사용자가 `list_only=false`로 detail 풀빌드 정상 트리거 → `ef15eb4` 커밋(KST 01:09) 18만 줄 추가. documents/eligibilityRule 다 채워짐. 그러나 3시간 뒤 cron이 KST 03:00 정시 + 1시간 14분 실행으로 `0531d25`(KST 04:14) 커밋 떨어지면서 **풀빌드 데이터 통째로 날아감**.

**진단** (커밋 시간 + git show + cron 로그 + merge 코드 검토):
- cron 기본 input `list_only=true` → `fetch_list_only` 호출 → detail/conditions 없는 RawPolicy 생산
- normalize 결과 documents=[] / eligibilityRule None인 new policy
- `build_policies.py:223` merge 로직 `by_id[pid] = new` → 풀빌드 기존 데이터를 빈 new로 통째 덮어씀
- 결과: documents 9,320→0, eligibilityRule 9,896→0

**Fix (`tools/build_policies.py:208~232`)**:
```python
# merge 시 new가 비었으면 detail/conditions 데이터 보존
existing_policy = by_id[pid]
if not new.get("documents") and existing_policy.get("documents"):
    new["documents"] = existing_policy["documents"]
    preserved_docs += 1
if not new.get("eligibilityRule") and existing_policy.get("eligibilityRule"):
    new["eligibilityRule"] = existing_policy["eligibilityRule"]
    preserved_rule += 1
```

cron이 list-only로 매일 돌아도 풀빌드 데이터 안 사라짐. 로그에 `preserved_documents=N, preserved_rule=N` 출력.

**데이터 복구**: `git checkout ef15eb4 -- docs/policies.json` → 풀빌드 데이터 그대로 살림.

**현재 채움률 (복구 후)**:
- title/category/summary/applicationUrl/applicationOrg: 100%
- eligibility: 98.0%
- procedure: 98.3%
- **documents: 93.9% ✅** (복구 전 0%)
- **eligibilityRule: 99.7% ✅** (복구 전 0%)
- amount: 42.3%
- deadline: 5.1%
- period: 0%, region: 키 없음

**한계 (사이드 케이스)**:
- 정부 측에서 정책의 detail 데이터가 실제 변경/삭제돼도 cron list-only는 갱신 못 받음 (기존 값 그대로 유지). stale 가능성.
- LLM 정련 결과(차후 R2.9 백필) 보존은 별개 문제. summary 등 list_row에서 받는 필드는 list-only cron이 정부 raw 톤으로 덮음.
- 해결책: 풀 detail 빌드를 가끔(주 1회?) 수동 트리거하면 stale 갱신 + 모든 필드 최신화.

**다음 라운드 (R3 — MainActivity 홈 집계 전환)**:
- 데이터 준비 완료 → 폰 빌드/설치 + 홈 화면이 9,923개 실데이터로 작동하는지 검증
- `MainActivity.kt:149-161` `SampleData.home` 하드코딩 제거 → `allPolicies` + UserProfile 동적 계산


### 2026-05-18 (R7 — 홈 카드 초기 깜박임 fix)

**상황**: 앱 첫 진입 시 "이번 주 받을 수 있어요" / "곧 마감돼요" 카드가 잠깐 떠 있다가 로딩 후 사라지는 사고.

**원인**: `MainActivity.kt:157` 초기 `allPolicies = SampleData.allPolicies` (하드코딩 19개 샘플). `HomeAggregator.computeHome`이 샘플 정책으로 매칭 돌려서 thisWeek/deadlineSoon 채움. 그 뒤 cache/remote 로드되면 9,923개 실데이터로 교체되는데 eligible × deadline 있는 정책이 거의 0이라 빈 상태로 갱신. `HomeScreen.kt:80-89` 임팩트 카드만 `isLoading` spinner였고 그 아래 thisWeek/deadlineSoon 카드는 `data` 그대로 그렸음.

**Fix** (`MainActivity.kt:184-193`):
```kotlin
val home = remember(profile, allPolicies, isLoading) {
    if (isLoading) {
        com.hiddensubsidy.app.data.model.HomeData(0L, 0, emptyList(), emptyList(), emptyList())
    } else {
        HomeAggregator.computeHome(allPolicies, profile, today)
    }
}
```

isLoading=true 동안엔 home이 빈 HomeData → HomeScreen이 `firstOrNull?.let` / `isNotEmpty()` 분기로 카드 자체를 안 그림 → 임팩트 spinner만 보임 → 깔끔. 빌드 OK (`assembleDebug` exit 0).

---

### 2026-05-18 (R8 — 탭별 디자인-only / 미구현 체크리스트)

> 각 탭 코드 정찰 결과. 디자인은 다 되어 있지만 실기능 빠진 곳·하드코딩 묶음·잘못 매핑된 액션 정리. R8a~R8d로 분할.

#### 🏠 홈 탭 (`ui/home/HomeScreen.kt`)

✅ 동작:
- 임팩트 카드 (놓치고 있는 돈) — HomeAggregator 동적 OK
- 이번 주 카드 / 마감 임박 카드 — 동적 OK (deadline 데이터 5%만 채워져서 실제로 거의 안 보임)

❌ 미구현:
| # | 위치 | 문제 | 라운드 |
|---|---|---|---|
| H1 | `HomeScreen.kt:142` | TopBar 🔔 NotificationsNone 아이콘 onClick 없음 — 장식 | R8a |
| H2 | `HomeScreen.kt:143` | TopBar 👤 Person 아이콘 onClick 없음 (마이 탭으로 가야) | R8a |
| H3 | `HomeScreen.kt:100` | "이번 주" 카드 우측 "전체" 버튼 — `onSeeAllThisWeek` 빈 함수 | R8a (Search로 라우팅) |
| H4 | `HomeScreen.kt:361` | 마감 임박 카드 footer 링크 "마감 임박 전체 보기" — `onSeeAllDeadlines` 빈 함수 | R8a (Search로 라우팅) |
| H5 | 데이터 | deadline 채움률 5% — 한국 정책 95%가 상시신청 | R10 (LLM 백필) 또는 영구 한계 수용 |

#### 📅 캘린더 탭 (`ui/calendar/CalendarScreen.kt`)

✅ 동작: 7×6 그리드, 월 전환, 오늘로, 날짜 선택, dot, 일정 카드

❌ 미구현/버그:
| # | 위치 | 문제 | 라운드 |
|---|---|---|---|
| C1 🔴 | `CalendarScreen.kt:57` | **`today = LocalDate.of(2026, 5, 15)` 하드코딩** — 항상 5/15가 "오늘"로 표시됨. `LocalDate.now()`로 바꿔야 | R8a |
| C2 | `MainActivity.kt:187-191` | `calendarEvents`가 SampleData.calendarEvents 14개 고정 (사용자 매칭으로 필터만). 실데이터 9,923개의 deadline 활용 X | R8c |
| C3 | 데이터 | CalendarEvent 4종(신청시작/마감/발표/지급) — 정부 API엔 deadline만 있음. 신청시작·발표·지급은 영구 미보유 → 마감만 표시하는 게 현실 | R8c (Deadline kind만 유지) |
| C4 | UI | 즐겨찾기 정책 마감일 강조 X — ⭐ 정책이 캘린더에 다른 색/굵게 떠야 가치 있음 | R8c |
| C5 | UI | 빈 월 처리 — 일정 1건도 없는 달이면 빈 그리드만. "이번 달 일정 없음" 카드 없음 | R8c |

#### 🎉 이벤트 탭 (`ui/events/EventListScreen.kt`)

✅ 동작: 6개 카드 그리드 (이사/퇴사/임신/결혼/창업/취업) → EventDetailScreen

❌ 미구현:
| # | 위치 | 문제 | 라운드 |
|---|---|---|---|
| E1 | `MainActivity:402` | `events = SampleData.events` 고정 — 6 EventBundle 묶음 정책이 SampleData 19개에서만 뽑힘 | R8d |
| E2 | `EventListScreen.kt:128` | `bundle.count` = SampleData 정책 수. 실데이터 늘어도 안 변함 | R8d |
| E3 | EventDetailScreen | 그룹 안 정책 묶음도 SampleData 하드코딩 — 9,923개에서 키워드·카테고리로 자동 분류 X | R8d |
| E4 | UI | 타임라인 그룹 ("이사 전/이사 후" 등) 데이터 기반 분기 X | R8d |
| E5 ⭐ | 미구현 | **"내가 곧 이사해요" 트리거** — 사용자가 누르면 시점 저장 + 알림 강화. PLAN 1.③ 차별화 핵심인데 0% | R8d |

#### 👤 마이 탭 (`ui/my/MyScreen.kt`)

✅ 동작: 프로필 카드 + 정확도 %, 받을 예정 (즐겨찾기 동적), 알림 권한 요청, 친구 초대/개인정보처리방침/의견 보내기

❌ 미구현/버그:
| # | 위치 | 문제 | 라운드 |
|---|---|---|---|
| M1 | `MyScreen.kt:97-104` | "신청한 지원금" 카드 — `summary.appliedCount=1 / appliedAmount=600,000` 하드코딩 (SampleData.mySummary) | R8b |
| M2 | `MyScreen.kt:97-104` | "수령 확인" 액션 라벨 — Box에 `clickable` 없음. 버튼처럼 보이는데 클릭 안 됨 | R8b |
| M3 | `MyScreen.kt:108-114` | "받은 지원금" 카드 — `receivedCount=5 / receivedAmount=2,300,000` 하드코딩 | R8b |
| M4 | 흐름 | "신청 → 수령 확인 → 받음" 상태 머신 자체가 없음 — 즐겨찾기 ⭐ 외엔 진척 추적 0 | R8b |
| M5 🟡 | `MyScreen.kt:128` | **"가족 진단 (프리미엄)" 메뉴 onClick이 `onInviteFriends`로 잘못 매핑** — 친구 초대로 감 | R8a |
| M6 | 미구현 | 프리미엄 IAP 자체 미구현 — 메뉴만 있고 결제 흐름 0 | (스코프 밖. MVP는 무료) |
| M7 | `MyScreen.kt:127` | "알림 설정" 클릭 = 권한 요청만. D-day 1/3/7 토글, 알림 시간대 등 세부 옵션 없음 | R8b 또는 별도 |
| M8 | 미구현 | 로그아웃 / 계정 동기화 / 탈퇴 메뉴 없음 | R9 (Firebase Auth) |
| M9 | `MyScreen.kt:215` | 프로필 정확도 100%여도 "프로필 더 채우기" 동일 문구. 완료 상태 분기 X | R8a |

---

#### R8 라운드별 묶음

| 라운드 | 묶음 | 항목 | 예상 |
|---|---|---|---|
| **R8a** ⭐ 톱픽 | **빠른 fix 묶음** | C1 / H1 / H2 / H3 / H4 / M5 / M9 | 1 세션 |
| **R8b** | **마이 카드 동적화** | M1 / M2 / M3 / M4 / (M7) | 1 세션 |
| **R8c** | **캘린더 실데이터** | C2 / C3 / C4 / C5 | 1 세션 |
| **R8d** | **이벤트 실데이터 + 트리거** | E1 / E2 / E3 / E4 / E5 | 1~2 세션 |

#### R8a 상세 (다음 작업)

1. **C1 캘린더 today 하드코딩 fix**: `CalendarScreen.kt:57` `LocalDate.of(2026, 5, 15)` → `LocalDate.now()`. MainActivity에서 명시 전달도 OK
2. **H1 홈 🔔 아이콘 onClick**: 알림 권한 요청 또는 마이 탭 알림 설정 라우팅
3. **H2 홈 👤 아이콘 onClick**: `tab = 3` (마이 탭으로 이동) 또는 ProfileEdit 직진
4. **H3 / H4 "전체 보기"**: `onSeeAllThisWeek` / `onSeeAllDeadlines` → `screen = Screen.Search` 라우팅. Search 화면이 자격 충족 토글 가지고 있어서 자연 연결
5. **M5 가족 진단 onClick fix**: `onInviteFriends` → `onPrivacyPolicy` 같은 placeholder 또는 "준비 중" Toast. 진짜 IAP는 출시 후
6. **M9 프로필 100% 상태 분기**: `if (percent == 100) "프로필 완성!" else "프로필 더 채우기"` (CTA 자체 숨기는 것도 옵션)

**개발 흐름**: 파일 5개 (`CalendarScreen.kt` / `HomeScreen.kt` / `MainActivity.kt` / `MyScreen.kt`) 묶어 수정 → `./gradlew.bat installDebug` → 폰 검증 → PLAN.md "R8a 완료" 기록.

#### R8a 완료 ✅ (2026-05-18)

| # | 파일·라인 | 변경 |
|---|---|---|
| C1 🔴 | `CalendarScreen.kt:57` | `today = LocalDate.of(2026, 5, 15)` → `LocalDate.now()` (default 파라미터) |
| H1 | `HomeScreen.kt:54-63 + TopBar` | `onNotificationClick` prop 추가 + TopBar 🔔 onClick 연결 |
| H2 | `HomeScreen.kt:54-63 + TopBar` | `onProfileClick` prop 추가 + TopBar 👤 onClick 연결 |
| H3 / H4 | `HomeScreen.kt` props 기존 활용 | `onSeeAllThisWeek` / `onSeeAllDeadlines`에 SearchScreen 라우팅 binding |
| M5 🟡 | `MyScreen.kt:128` | "가족 진단" onClick `onInviteFriends` → 신규 `onPremiumClick` prop |
| M9 | `MyScreen.kt:205-226` | `percent >= 100`이면 "프로필 다 채우셨어요" / 아니면 "프로필 더 채우기" 분기 |
| MainActivity | `MainActivity.kt:253-272 + TabsHost` | 새 props 4개(`onNotificationIconClick`/`onProfileIconClick`/`onSeeAllClick`/`onPremiumClick`) AppRoot에서 람다로 binding → TabsHost → 각 화면 전달. `onProfileIconClick = { tab = 3 }` (마이 탭 이동), `onPremiumClick`은 Toast "프리미엄 기능은 출시 후 준비 중이에요" |

**빌드 검증**: `./gradlew.bat assembleDebug` BUILD SUCCESSFUL.

**다음 (R8b — 마이 카드 동적화)**:
- "신청한 지원금" / "받은 지원금" 카드를 SharedPreferences 기반 상태 머신으로 (즐겨찾기 ⭐ 처럼)
- 즐겨찾기 ⭐ → 신청 → 수령 확인 → 받음 흐름. 정책 상세 화면에 진척 토글 추가
- `MySummary` 신청한/받은 동적 합산. SampleData.mySummary 의존 제거
- `AppliedRepository`/`ReceivedRepository` 또는 단일 `PolicyStatusRepository` (status: SAVED / APPLIED / RECEIVED) 톱픽

#### R8b 완료 ✅ (2026-05-18)

| 파일 | 변경 |
|---|---|
| `data/ApplicationStatusRepository.kt` 🆕 | SharedPreferences `applied` / `received` Set 관리. `toggleApplied` 시 received 해제, `toggleReceived` 시 applied 해제 (느슨한 상호배타 머신) |
| `ui/detail/PolicyDetailScreen.kt` | `isApplied/isReceived/onToggleApplied/onToggleReceived` props 추가. EligibilityBadge 아래 **"내 진척" 카드** 신규 — "📝 신청했어요" / "✅ 받았어요" 두 칩 (`ProgressChip` 컴포저블). 활성 시 민트 채움 |
| `ui/favorites/FavoritesScreen.kt` | `PolicyStatusKind` enum (Saved/Applied/Received) 신규. 라벨·이모지·빈 상태 메시지 일원화 → 받을 예정/신청한/받은 화면 1개 컴포저블로 공유 |
| `ui/my/MyScreen.kt` | 신청한 카드 onClick → `onAppliedClick`. 받은 카드 onClick → `onReceivedClick` (Row에 `clickable` 추가). actionLabel "수령 확인" 제거 (탭 자체가 액션) |
| `MainActivity.kt` | `applied/received` state + `MySummary` 동적 합산 (`appliedCount/Amount/receivedCount/Amount`). `Screen.Applied`/`Screen.Received` sealed class 추가. PolicyDetail 진척 토글 시 SharedPreferences + Toast 4종 ("신청한 지원금에 추가했어요" 등) |

**빌드 검증**: `assembleDebug` BUILD SUCCESSFUL (compileDebugKotlin 실행). 폰 설치 OK.

**상태 머신 정의** (느슨):
- 사용자가 ⭐(즐겨찾기) 없이 바로 "신청했어요" OK
- 신청 ON → 받음 자동 OFF (받았으면 진행 중 X)
- 받음 ON → 신청 자동 OFF (받았으니 진행 중 X)
- 즐겨찾기는 독립 — 신청·받음과 무관하게 토글 가능

**다음 (R8c — 캘린더 실데이터 전환)**:
- C2: 9,923개 deadline 활용 → CalendarAggregator
- C3: Deadline kind만 (신청시작/발표/지급은 정부 API에 없음)
- C4: 즐겨찾기 정책 마감일 강조
- C5: 빈 월 안내 카드

#### R8c 완료 ✅ (2026-05-18)

| 파일 | 변경 |
|---|---|
| `data/CalendarAggregator.kt` 🆕 | `compute(allPolicies, favorites, profile, today): List<PolicyCalendarEvent>`. 자격 충족 + PolicyRelevance 통과 + deadline 있는 정책 ∪ 즐겨찾기 정책의 deadline. today 기준 ±6개월 윈도우, cap 200개. kind는 Deadline 하나만 |
| `ui/calendar/CalendarScreen.kt` | `favorites: Set<String>` props 추가. DayCell에 `hasFavorite` 플래그 → 즐겨찾기 마감일이면 dot 대신 ⭐ 표시. CalendarEventCard에 isFavorite → 우측에 ⭐. `isMonthEmpty` 분기 → `EmptyMonthCard` 안내 카드 (정부 정책 95% 상시신청 설명) |
| `MainActivity.kt` | `SampleData.calendarEvents` 필터 → `CalendarAggregator.compute` 호출. TabsHost에 favorites prop 추가 → CalendarScreen 전달 |

**효과**:
- 캘린더가 9,923개 실데이터의 deadline 활용 (이전엔 SampleData 14개 일정만)
- 즐겨찾기한 정책 마감일 시각적 강조 (⭐) — 사용자가 직접 마크한 건 더 눈에 띄게
- 데이터 5% 채움률(deadline) 한계는 빈 월 안내로 자연스럽게 설명

**남은 한계** (영구):
- 신청시작/발표/지급 일자 — 정부 API에 없는 필드. 영영 미보유. 4 CalendarEventKind 중 Deadline만 실데이터로 동작 (나머지는 모델에 남아있지만 실데이터엔 안 들어옴)

**다음 (R8d — 이벤트 실데이터 전환 + 트리거)**:
- E1/E2/E3: SampleData.events 6개 EventBundle → 9,923개에서 LifeEvent별 키워드 자동 분류
- E4: 타임라인 그룹 (이사 전/이사 후 등) 데이터 기반
- E5 ⭐ **"내가 이사해요" 트리거** — 사용자가 누르면 시점 저장 + 그 시점부터 N일 동안 알림 강화. PLAN 1.③ 차별화 핵심

#### R8d 완료 ✅ (2026-05-18)

| 파일 | 변경 |
|---|---|
| `data/EventTriggerRepository.kt` 🆕 | SharedPreferences `event_trigger_<eventId>` = epoch millis. `loadActive()` 만료(180일) 자동 필터. `toggle()` 활성 → 해제 / 비활성 → 현재 시각 마크 |
| `data/EventAggregator.kt` 🆕 | `compute(allPolicies, profile): List<EventBundle>` 6개 자동 생성. LifeEvent별 키워드 매칭 (이사/퇴사/임신·출산/결혼/창업/취업 각 ~10개 키워드). 자격 충족 + PolicyRelevance 통과 + amount 큰 순. 이벤트당 cap 50개. 단일 group 구조 (timeline 메타 정부 API에 없음) |
| `ui/events/EventListScreen.kt` | `activeTriggers: Set<String>` props. 트리거 활성 카드는 `accentBg` + "진행 중" pill 표시. `bundle.count` 그대로 동적 (EventAggregator 출력) |
| `ui/events/EventDetailScreen.kt` | `isActive` / `onToggleTrigger` props 추가. HeroCard 아래 **TriggerToggleCard** 신규 — 비활성 "내가 ${label}해요" / 활성 "${label} 중이에요 — 6개월간 강조". 우측 마크/해제 칩. 정책 0건이면 `EmptyEventCard` ("프로필 더 채우면 매칭률 ↑") |
| `MainActivity.kt` | `eventBundles` / `activeTriggers` state. `SampleData.events` 의존 제거. EventDetail 트리거 토글 시 SharedPreferences + Toast ("이사 시점을 마크했어요" 등). EventDetail은 `eventBundles.firstOrNull { it.eventId == s.id }` 로 lookup (SampleData.findEvent 제거) |

**키워드 분류** (영구 한계 X, 점진 확장 가능):
- 이사: "이사/전입/전세자금/월세 지원/임차/임대주택/주거안정/보증금" 등 12개
- 퇴사: "실업/실업급여/구직/고용보험/재취업/이직/퇴직" 9개
- 임신·출산: "임신/출산/산모/산후/신생아/영아/영유아/임산부/첫만남/부모급여" 13개
- 결혼: "신혼/결혼/혼인/신혼부부/신혼희망" 6개
- 창업: "창업/스타트업/사업화/벤처/K-스타트업/예비창업" 9개
- 취업: "취업/신입/청년채용/내일채움/도약계좌/청년수당" 9개

**상태 머신**:
- 트리거 활성 → 180일 후 자동 만료 (재마크 가능)
- MVP는 시점 저장 + 시각 표시까지. 알림 강화·홈 가중 노출은 추후 (PolicyDeadlineWorker + HomeAggregator 연동)

**남은 (이번 라운드 밖)**:
- 트리거 활성 정책을 홈 missed/thisWeek에 가중 노출 (현재는 EventDetail에만 효과)
- WorkManager에서 트리거 활성 이벤트 정책의 D-day 알림 우선순위 상향
- 신청시작/발표/지급 일자 타임라인 분리 (정부 API에 필드 없음 — 영구 한계)

### 2026-05-18 (R9 — Firebase Auth: Google 로그인 옵션)

**사용자 준비**:
- Firebase 콘솔 프로젝트 생성 (Analytics OFF, Spark 무료)
- Android 앱 추가 (debug 패키지 `com.hiddensubsidy.app.debug`)
- SHA-1 등록 (debug keystore)
- `google-services.json` 다운 → `app/` 폴더 (.gitignore 등재 확인)
- Authentication → Sign-in method → Google 활성화

**코드 작업**:

| 파일 | 변경 |
|---|---|
| `gradle/libs.versions.toml` | `firebaseBom=33.7.0` / `playServicesAuth=21.2.0` / `googleServicesPlugin=4.4.2` / `kotlinxCoroutinesPlayServices=1.9.0` versions + libraries + plugin alias |
| `build.gradle.kts` (root) | `alias(libs.plugins.google.services) apply false` |
| `app/build.gradle.kts` | `alias(libs.plugins.google.services)` plugin + `platform(libs.firebase.bom)` / `firebase-auth-ktx` / `play-services-auth` / `kotlinx-coroutines-play-services` dependency |
| `data/AuthRepository.kt` 🆕 | `getSignInIntent` (Google 계정 선택 UI) / `signInWithIdToken` (Firebase Auth) / `signOut` (양쪽 sign-out). `currentUser` / `authState: Flow<FirebaseUser?>` (AuthStateListener → callbackFlow) |
| `ui/auth/LoginScreen.kt` 🆕 | 🔐 이모지 + 안내 + Google 로그인 큰 검정 버튼. `rememberLauncherForActivityResult` → `signInWithIdToken` → onSuccess. 게스트 모드 유지 안내 ("로그인 없이 계속 사용해도 돼요") |
| `ui/my/MyScreen.kt` | `signedInName` / `signedInEmail` / `onSignInClick` / `onSignOutClick` props. 미로그인 시 "🔐 Google 로그인" (신규 배지) / 로그인 시 "🔓 로그아웃 (이름)" 메뉴로 동적 분기 |
| `MainActivity.kt` | `Screen.Login` sealed class 추가. `AuthRepository.authState.collectAsState()` 로 실시간 user 관찰. TabsHost props 4종 추가 → MyScreen 연결. 로그아웃 시 `scope.launch { AuthRepository.signOut(context) }` + Toast |

**설계 결정**:
- **게스트 모드 유지** (강제 로그인 X) — 1인 개발자 가치관 "3탭 안에 답" + 진입 장벽 최소화. 로그인 원하는 사용자만 마이 → Google 로그인 진입
- Firestore 동기화 (즐겨찾기/신청/받음/프로필/트리거 cross-device 저장)는 별도 라운드(**R9.5**)
- FCM 푸시도 별도 (R9.5 또는 R10에 묶음)

**Deprecated 경고**:
- `GoogleSignIn` / `GoogleSignInClient` deprecated → Google이 **Credential Manager API** 권장
- 동작은 안정 (Play Services 21.2.0 기준 2025+ 보장). 빠른 MVP 안정성 위해 일단 유지
- 출시 후 별도 라운드(**R9.x**)에서 Credential Manager + GoogleId Credential Provider로 마이그레이션 — 추후

**빌드 검증**: `assembleDebug` BUILD SUCCESSFUL (warning만, error X). 폰 설치 + 로그인 동작 검증 OK ✅.

**검증 결과**:
1. 마이 탭 첫 메뉴 "🔐 Google 로그인" 표시 ✅
2. LoginScreen 진입 + Google 계정 선택 시트 ✅
3. Firebase Auth 연동 + "환영해요, {이름}님 🎉" Toast ✅
4. 마이 탭 "🔓 로그아웃 ({이름})"로 동적 갱신 ✅
5. 로그아웃 시 양쪽 sign-out 정상 ✅

**디버깅 사고 기록**:
- 첫 빌드에서 `DEVELOPER_ERROR (10)` 발생
- 원인: 첫 google-services.json의 `oauth_client: []` 비어있음. 두 가지 콘솔 작업이 안 끝났던 상태:
  - (A) Authentication → Sign-in method → **Google 활성화** 안 됨
  - (B) **SHA-1 디지털 지문 미등록**
- Fix: 콘솔에서 A·B 둘 다 처리 → `google-services.json` 새로 다운로드 → app/ 폴더 덮어쓰기 → 재빌드
- 새 파일 검증: `client_type: 1` (Android, certificate_hash 매칭) + `client_type: 3` (Web, default_web_client_id) 채워짐 → 로그인 성공
- 향후 사고 대응 절차로 PLAN에 박힘 (이 항목)

**다음 라운드 후보**:
- **R9.5** Firestore 동기화 — favorites/applied/received/profile/triggers cross-device
- **R10** LLM 정련 백필 (amount 42→80%, period 0→채움, summary 토스 톤)
- **R11** 출시 자산 + 개인정보처리방침

### 2026-05-18 (R9.5 — Firestore 동기화 cross-device)

**사용자 준비**:
- Firebase 콘솔 → Build → Firestore Database → 데이터베이스 만들기
- 위치: **asia-northeast3 (Seoul)** ★ 한국 사용자 latency 최소화
- 모드: **프로덕션 모드** (rules로 보안)
- 규칙 탭에 보안 룰 게시:
  ```
  rules_version = '2';
  service cloud.firestore {
    match /databases/{database}/documents {
      match /users/{uid} {
        allow read, write: if request.auth != null && request.auth.uid == uid;
      }
    }
  }
  ```

**코드 작업**:

| 파일 | 변경 |
|---|---|
| `gradle/libs.versions.toml` | `firebase-firestore-ktx` library 추가 (BOM 33.7.0 통해 버전 자동) |
| `app/build.gradle.kts` | `implementation(libs.firebase.firestore.ktx)` 추가 |
| `data/CloudSyncRepository.kt` 🆕 | `pullFromCloud(context, uid)` — Firestore `users/{uid}` 문서 → 로컬 SharedPreferences 덮어쓰기. 문서 없으면 (신규 사용자) `pushToCloud`로 백업. `pushToCloud(context, uid)` — 로컬 상태 → 클라우드 (last-write-wins). 자동 직렬화용 `CloudUserData` / `CloudUserProfile` data class |
| `MainActivity.kt` | 두 개 LaunchedEffect 추가: (1) `authUser.uid` 변경 시 → `pullFromCloud` → 모든 state 갱신 (favorites/applied/received/activeTriggers/profile). (2) 로컬 state 변경 시 → 800ms debounce 후 `pushToCloud`. 로그아웃 시 자동 동기화 정지 |

**데이터 모델** (`users/{uid}` 문서):
```kotlin
CloudUserData(
  favorites: List<String>,
  applied: List<String>,
  received: List<String>,
  triggers: Map<String, Long>,
  profile: CloudUserProfile?,
  updatedAt: Long,
)
```
~1KB / 사용자. Spark 무료 티어(1GB 저장) → **100만명까지 0원**.

**동기화 정책**:
- 로그인 = pull (클라우드 → 로컬 덮어쓰기, 신규면 로컬 push로 백업)
- 변경 = push (debounce 800ms, 연속 토글 합침)
- 로그아웃 = 동기화 정지 (로컬은 그대로 유지 → 게스트 모드)
- 충돌 = **last-write-wins** (단순. multi-device 동시 사용은 MVP 밖)

**빌드 검증**: `assembleDebug` BUILD SUCCESSFUL (deprecated toObject warning만). 폰 설치 + cross-device 복원 검증 OK ✅ (2026-05-18).

**검증 결과**:
1. 첫 로그인 → `push local backup` 동작 (신규 사용자, 클라우드 비어있음 → 로컬 push) ✅
2. 즐겨찾기/신청/받음/트리거 토글 시 `push OK` 로그 ✅
3. 앱 데이터 클리어 후 재로그인 → `pull OK: fav=N, ...` 로그 + 화면에 데이터 복원 ✅
4. 폰 바꿔도 같은 Google 계정이면 모든 상태 복원 가능 (cross-device)

**다음 라운드 후보**:
- **R10** LLM 정련 백필 (amount 42→80%, period 0→채움, summary 토스 톤). 1세션, 사용자 트리거 1회
- **R11** 출시 자산 + 개인정보처리방침 (아이콘/스크린샷/privacy.md)
- **R12** Keystore + AAB + Play Console (25 USD 결제)

### 2026-05-18 (홈 풍성화 + dismissed + 광고 + 신규 정책 알림 미니 라운드들)

R11 대기 중 사용자 피드백 반영해 추가 작업들:

| 작업 | 파일 |
|---|---|
| **홈 풍성화** — 카테고리 6 매트릭스 / 트리거 미니카드 / 내 진척 / 프로필 완성도 칩 | `data/model/Policy.kt`(CategoryStats), `data/HomeAggregator.kt`, `ui/home/HomeScreen.kt`, `MainActivity.kt` |
| **신규 정책 자동 알림** — WorkManager에 자격 충족 신규 정책 diff 검사 (baseline 보호) | `notification/PolicyDeadlineWorker.kt`, `notification/NotificationHelper.kt` (`notifyNewEligible`) |
| **워커 remote-first fetch** — 캐시 stale 방지 | `notification/PolicyDeadlineWorker.kt:loadPolicies` |
| **매일 cron detail까지 풀빌드** — `list_only` default `true→false` | `.github/workflows/crawl-policies.yml` |
| **AdMob 전면 광고** — 정책 상세 진입 시 5번에 1번, 30초 보호 + 5분 쿨다운 | `ads/AdManager.kt`, `app/build.gradle.kts`, `AndroidManifest.xml` |
| **"관심 없음" 토글** — 홈 임팩트 카드 + missed에서 제외 | `data/DismissedRepository.kt`, `ui/detail/PolicyDetailScreen.kt`(DismissRow), `data/HomeAggregator.kt`, `ui/favorites/FavoritesScreen.kt`(PolicyStatusKind.Dismissed), `ui/my/MyScreen.kt`, `data/CloudSyncRepository.kt`(dismissed 동기화) |
| **MissedSheet 정책 클릭 액션** — `onGrantClick` MainActivity binding | `MainActivity.kt` |
| **"이번 주" D-0 사고 fix** — fallback 제거 (deadline 빈 정책 D-0 표시 버그) | `data/HomeAggregator.kt` |
| **자격 조건 중복 표시 fix** — clean + normalize.py preserve_polished | `ui/detail/PolicyDetailScreen.kt`, `tools/normalize.py`, `tools/build_policies.py` |

**AdMob 정보**:
- App ID: `ca-app-pub-2968584390793166~2516614596`
- Interstitial 단위 ID: `ca-app-pub-2968584390793166/8781576983`
- 디버그 빌드는 Google 테스트 ID 사용 (BuildConfig.DEBUG 분기) — 자기 폰 클릭 안전

### 2026-05-18 (R10 결정 — 스킵, R11로 점프)

**상황**: 사용자 환기 — R2.8.X에서 "Gemini는 가끔 수동 트리거"로 결정했고 R10이 정확히 그 작업이지만, 현재 데이터 충분히 쓸 만함:
- eligibility 98% / eligibilityRule 99.7% / documents 93.9% / procedure 98.3% / amount 42%
- summary 정부 raw 톤이지만 정보 다 들어있음

**결정**: **R10 스킵 → R11(출시 자산)로 점프**. 출시 후 사용자 피드백 보고 필요하면 R10 돌림.

**코드 변경은 보존**:
- `tools/normalize.py` LLM_SUMMARY_PROMPT 강화 (period 적극 추론 + 좋은 예 3개)
- `tools/build_policies.py` merge 로직 `_is_polished_summary` + `preserved_polished` 카운터
- 둘 다 미래 R10 트리거 시 사용. 코드 자체는 출시에 영향 0. push 안 해도 OK (다음 cron 트리거 시 자동 반영)

### 2026-05-18 (R12 완료 — 출시 심사 신청) 🎉

**Claude 작업**:
- `proguard-rules.pro` — kotlinx-serialization / Ktor / Firebase / AdMob / WorkManager / CloudUserData 보존 룰 완비
- `app/build.gradle.kts` — `signingConfigs { release { ... } }` + keystore.properties 자동 로드 + `versionName 1.0.0`
- `keystore.properties.example` — 사용자 참고용 (gitignore된 실 파일 외에)
- `hidemoney-release.jks` 생성 (24자 랜덤 비밀번호 자동 생성, 비대화식 keytool)
- `keystore.properties` 작성 (storeFile/Password/keyAlias/keyPassword)
- `bundleRelease` 통과 → `app-release.aab` 10.4 MB
- `docs/account-deletion.md` (Play Console 요구 — 계정 삭제 절차 페이지)
- `docs/index.html` 토스 톤 랜딩 페이지 (https://gyubam.github.io/hidemoney/)
- `docs/app-icon-512.png` 갱신 (원본 1254×1254 → 512×512 누끼 처리, RGBA 4 모서리 alpha=0 검증)
- `mipmap-xxxhdpi/ic_launcher_foreground.png` 신 디자인으로 변경 (700×700 캔버스 + 467×467 safe zone 합성)

**사용자 작업 (Play Console)**:
- 앱 만들기 (`com.hiddensubsidy.app`, 한국어, 무료, 카테고리=금융)
- 앱 액세스 권한: 제한 없음
- 광고: 예 (AdMob 사용)
- 콘텐츠 등급: 다른 모든 앱 / 모든 항목 아니요 → 만 3세 이상
- 타겟층: 만 16~17세 + 만 18세 이상 (광고 단가 일부 ↓이지만 청년 정책 대상 포함)
- 정부 앱: 아니요 (민간 1인 개발)
- 금융 기능: 제공 안 함 (정보·매칭만)
- 데이터 안전성:
  - 위치(대략적), 개인정보(이름·이메일·UID·기타), 금융정보(소득), 앱활동(상호작용·기타), 기기ID(AdMob)
  - 다 수집됨 / 임시처리 아니요 / 사용자 선택 / 이유는 항목별 (계정관리·앱기능·맞춤설정·광고)
- 광고 ID 선언: 예 + 광고 또는 마케팅
- 메인 스토어 등록정보: 짧은 설명·자세한 설명·아이콘·피처 그래픽 업로드
- AAB 업로드 → 내부 테스트 트랙 → **검토 신청 완료** ✅

**Firebase 콘솔**:
- release 패키지 `com.hiddensubsidy.app` 추가 (debug용 `com.hiddensubsidy.app.debug`와 별도)
- release SHA-1 등록
- `google-services.json` 새로 받음 (두 패키지 모두 포함)

**남은 사용자 작업 (검토 후)**:
- 휴대전화 스크린샷 4~5장 (홈/마이/정책상세/캘린더/이벤트) — Play Console에서 출시 후에도 보강 가능
- 베타 테스터 등록 (지인 5~10명 이메일을 Play Console 내부 테스트에 추가)
- 심사 결과 대기 (~1~3일)



