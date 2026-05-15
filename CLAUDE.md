# Claude Code 컨텍스트 — 숨은지원금

> Claude Code가 다음 세션에서 자동으로 로드하는 컨텍스트 파일.
> 다른 PC에서 풀 받았을 때 사용자 프로필·디자인 톤·협업 규칙이 그대로 살아있도록 운반.

---

## 사용자 (개발자) 프로필

- **1인 개발자** — 외부 계약/제휴 맺기 어려움
- **한국어로 협업** — 반말 캐주얼 톤 (예: "엉ㄱㄱ", "다음 진행해줘", "왜케 유치하지")
- **사용자는 코딩 안 함** — Claude가 100% 코드 작성, 사용자는 Android Studio로 빌드/테스트/스토어 업로드만
- **GitHub**: [Gyubam/hidemoney](https://github.com/Gyubam/hidemoney)
- **개발 PC 이동**: 회사 ↔ 집 모두에서 작업. 작업 디렉토리명은 `whatsapp/`(IdeaProjects 하위)이지만 앱 자체는 정부 지원금 앱이라 무관

### 가치관 / 우선순위
1. **운영비 0원** — 유료 인프라 절대 불가. 무료 티어로만 끝까지
2. **디자인 최신식·모던** — 토스 수준의 디자인. "디자인 마니 신경써주어야해" 명시
3. **사용자 편의성 1순위** — 복잡한 UX 절대 안 됨, "3탭 안에 답"
4. **추천(픽) 명확히** — 톱픽 선정해서 제시, 열린 결말 X
5. **아이디어 한 번에 여러 개 제시 환영** ("10개 더 해줘" 같은 요청)

---

## 협업 규칙 (절대 지킬 것)

### 1. git push는 명시 요청 시만
- 푸시 요청 신호: **"푸시해", "push해", "올려", "깃 올려"** 같은 분명한 명시
- 그 외에는 PLAN.md/코드 변경 후에도 **자동 push 금지**
- **이유**: 자잘한 변경까지 자동 push되는 게 번거롭다는 사용자 명시 피드백
- commit도 사용자 명시 요청 시만 (Claude Code 기본 가이드와 일치)
- 변경이 누적되어 있어도 사용자 요청 시점에 묶어서 한 번에 commit + push

### 2. 토스 톤 디자인 의무 — "허접함" 절대 금기
- **다크모드 만들지 말 것** — 시스템 다크여도 항상 라이트(흰 톤). 사용자 명시 결정
- **컬러 사용 극단적 절제** — 검정·회색 90%, 액센트(민트)는 숫자/메인 CTA에만
- **여백·구분선·타이포 위계로 디자인** — 그라데이션·진한 그림자·과도한 보더 금지
- **숫자가 압도적이어야 함** — 메인 임팩트 60sp Black weight, 라벨 12-14sp (위계 5배 이상)
- **카드는 순백 + 보더 없음** — 옅은 회색 배경 위에 흰 카드, 그림자 없거나 매우 미세
- **CTA는 큼지막한 채워진 버튼** — 56dp 높이, 16dp 라운드, 액센트 풀
- **D-day 같은 칩은 pill 형태** (999dp 라운드)
- **카드 안 헤더 + 일러스트 행 + footer 링크 패턴** (토스 6장 스샷 분석 결과)
- **카드 사이 간격 12~16dp** (가깝게) — 섹션 간격 X, "카드 안에 헤더 포함" 구조
- **배경 살짝 푸른 톤** `#F2F4F8` (순회색 `#F2F4F6` X)
- **이모지 + 컬러 버블 = IconBubble** — 디자이너 일러스트 자리 대체
- 절대 금기: 그라데이션 카드, 진한 그림자, 카드 안 보더, 카드 사이 큰 여백(40dp+), 회색만으로 된 배경, **카툰체/굵은 검은 아웃라인 폰트(게임 톤)**

### 3. 진행 관리는 PLAN.md
- 모든 계획·결정·진행은 `PLAN.md`에 기록
- 새 결정/진행 시 PLAN.md 업데이트 우선
- 사용자가 매 라운드 "확인해줘" 후 피드백 → 다음 라운드

### 4. 한 번에 여러 작업 묶기
- 사용자가 "다음 진행해줘" 식으로 빠른 진행 선호
- 큰 작업은 라운드로 분할하되 한 라운드 안에 여러 단위 작업 묶어서 처리
- AskUserQuestion은 큰 갈래 결정에만 (작은 결정은 톱픽으로 그냥 진행)

---

## 프로젝트 컨텍스트

### 한 줄 포지션
> "검색하는 앱"이 아니라 **"받게 만드는 앱"**.
> 기존 보조금24·정책알리미는 100개 띄우고 끝. 우리는 매주 1개를 받게 만든다.

### 핵심 차별화 (MVP 3개)
1. **"못 받은 돈" 후행 진단** ⭐ 첫인상·바이럴 담당
2. **"이번 주 1개" ROI 큐레이션** ⭐ 재방문 담당
3. **생애 이벤트 트리거** ⭐ 데이터 수집 담당

### 절대 제약
- 운영비 0원 (Firebase Spark 무료 티어 + GitHub Actions 크롤러)
- 디자인·UX 1순위
- 다크모드 안 만듦

### 기술 스택 (전부 무료)
| 영역 | 도구 |
|---|---|
| 프론트 | **Kotlin + Jetpack Compose** (Android 전용, Android-only 확정) |
| Build | Gradle 9.2 / AGP 8.7.3 / Kotlin 2.0.21 / JDK 17 |
| Compose | BOM 2024.12.01 + Material 3 + material-icons-extended |
| Font | Pretendard Variable (Google Fonts) |
| HTTP | Ktor 3.0.2 (OkHttp engine + content-negotiation + serialization-kotlinx-json) |
| 저장 | SharedPreferences (`hs_prefs`) + `filesDir/policies-cache.json` |
| 푸시(예정) | FCM |
| 인증(예정) | Firebase Auth (Google) |
| 데이터(예정) | GitHub Actions 크론 + Gemini Flash + GitHub Pages 호스팅 |

### 디자인 토큰 (토스 톤 확정)
- **폰트**: Pretendard (Bold, SemiBold, Medium, Black)
- **액센트**: 민트 `#00C896` (Brand.Mint500), 진한 텍스트 `#00805C` (Brand.Mint700)
- **그레이 팔레트**: G900 `#191F28`(텍스트) / G700 `#4E5968`(보조) / G500 `#8B95A1`(약) / G200 `#E5E8EB`(divider) / G100 `#F2F4F6`(pill bg)
- **배경**: `#F2F4F8` (살짝 푸른 톤)
- **카드 bg**: `#FFFFFF`
- **임팩트 숫자**: 60sp Black weight (이전 72sp에서 축소 — 사용자 피드백)
- **모서리**: 카드 20dp, 임팩트 카드 24dp, D-day pill 999dp
- **CTA 버튼**: 56dp 높이 + 16dp 라운드
- **카테고리 색**: 주거=Sky / 출산=Coral / 생활=Mint / 교육=Lemon / 청년=Lilac / 창업=Sand
- **이벤트 색**: 이사=Sky / 퇴사=Sand / 임신=Coral / 결혼=Lilac / 창업=Lemon / 취업=Mint

---

## 파일 구조 (핵심만)

```
app/src/main/kotlin/com/hiddensubsidy/app/
├── HiddenSubsidyApp.kt                  # Application
├── MainActivity.kt                      # Root + AppRoot + sealed Screen + AnimatedContent
├── data/
│   ├── SampleData.kt                    # 19 정책 + 6 이벤트 + 14 캘린더 이벤트 + 3 missed grants
│   ├── PolicyRepository.kt              # 인터페이스 + InMemory 구현
│   ├── RemotePolicyRepository.kt        # Ktor HTTP GET
│   ├── CachedPolicyRepository.kt        # filesDir 캐시 + remote refresh + fallback
│   ├── PolicyMatching.kt                # EligibilityRule.matches(profile) / Policy.matchedWith
│   ├── FavoritesRepository.kt           # SharedPreferences Set<String>
│   ├── UserPrefs.kt                     # 프로필 load/save + rememberUserProfile
│   └── model/
│       ├── Policy.kt                    # Policy / DocumentRequirement / EligibilityRule
│       ├── LifeEvent.kt                 # 6 events enum + TimelineGroup + EventBundle
│       ├── CalendarEvent.kt             # 4 kind + PolicyCalendarEvent
│       ├── UserProfile.kt               # age/region/occupation/married/hasChildren + summary + MySummary
│       ├── (HomeData, MissedGrant 등)
├── ui/
│   ├── home/HomeScreen.kt               # 임팩트 카드 + this week + 마감 임박
│   ├── calendar/CalendarScreen.kt       # 7x6 그리드 + dot
│   ├── events/                          # EventListScreen + EventDetailScreen
│   ├── detail/PolicyDetailScreen.kt     # 자격 배지 + 섹션 + sticky CTA + ⭐ 토글
│   ├── missed/MissedSheet.kt            # 놓친 내역 바텀시트 (바이럴 엔진)
│   ├── onboarding/OnboardingScreen.kt   # 3페이지 pager + ProfileInputPage(internal)
│   ├── profile/ProfileEditScreen.kt     # ProfileInputPage 재사용
│   ├── my/MyScreen.kt                   # 프로필 카드 + 받을 예정/신청한/받은 + 설정
│   ├── components/                      # AnimatedAmount / IconBubble / PillAction / CardFooterLink / PrimaryButton / BottomTabBar
│   └── theme/                           # Color / Type / Spacing / Shape / Theme / CategoryStyle
└── util/
    ├── ShareHelper.kt                   # 공유 / 친구 초대 / mailto / 정책 URL
    └── NotificationPermission.kt        # Android 13+ POST_NOTIFICATIONS launcher

docs/
└── policies.json                        # SampleData export + 빌드 보강 (19 정책 + difficultyScore/roiScore)

tools/                                   # GitHub Actions 빌드 도구 (Python 3.11)
├── schema.py                            # Pydantic Policy/EligibilityRule (Kotlin 모델 미러)
├── summarize.py                         # Gemini Flash 호출 + 화이트리스트 머지
├── build_policies.py                    # orchestrator (load → enrich → validate → save)
└── requirements.txt

.github/workflows/
└── crawl-policies.yml                   # cron 0 18 * * * + workflow_dispatch, secrets.GEMINI_API_KEY 주입
```

---

## 빌드 / 실행

```bash
# Windows / PowerShell
./gradlew.bat installDebug              # 빌드 + 폰 설치
adb devices                              # 폰 연결 확인 (SM-A356N = 갤럭시 A35)
adb shell pm clear com.hiddensubsidy.app.debug    # 앱 데이터 초기화 (온보딩 재진입)
adb logcat -s policies-fetch             # remote fetch 로그
adb exec-out run-as com.hiddensubsidy.app.debug cat cache/policies.json   # JSON 추출
```

**적용된 비밀 정보**: `local.properties`에 SDK 경로만 (gitignore 제외). Gemini API key 등은 아직 없음. Firebase google-services.json 아직 없음.

**현재 디바이스**: 갤럭시 A35 (SM-A356N), Android 16, USB 디버깅 OK.

---

## 메모/관용구

- `[[name]]` 식 메모리 링크는 다른 PC에서 안 살아남으므로 — 컨텍스트는 이 파일과 PLAN.md에 다 박혀있음
- 사용자가 "엉ㄱㄱ" / "진행해줘" 하면 톱픽 결정해서 바로 작업 (의사결정 부담 줄이기)
- 사용자가 의견 다를 때 명확히 표현 ("아 좀 너무 유치한데", "왜케 유치하지") — 톤 즉시 조정
