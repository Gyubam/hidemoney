package com.hiddensubsidy.app

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.LaunchedEffect
import com.hiddensubsidy.app.ads.AdManager
import com.hiddensubsidy.app.data.ApplicationStatusRepository
import com.hiddensubsidy.app.data.AuthRepository
import com.hiddensubsidy.app.data.CachedPolicyRepository
import com.hiddensubsidy.app.data.CalendarAggregator
import com.hiddensubsidy.app.data.CloudSyncRepository
import com.hiddensubsidy.app.data.DismissedRepository
import com.hiddensubsidy.app.data.EventAggregator
import com.hiddensubsidy.app.data.EventTriggerRepository
import com.hiddensubsidy.app.data.FavoritesRepository
import com.hiddensubsidy.app.notification.NotificationScheduler
import com.hiddensubsidy.app.data.HomeAggregator
import com.hiddensubsidy.app.data.InMemoryPolicyRepository
import com.hiddensubsidy.app.data.PolicyRepository
import com.hiddensubsidy.app.data.RemotePolicyRepository
import com.hiddensubsidy.app.data.SampleData
import com.hiddensubsidy.app.data.UserPrefs
import com.hiddensubsidy.app.data.matchedWith
import com.hiddensubsidy.app.data.withFreshDaysLeft
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.hiddensubsidy.app.util.ShareHelper
import com.hiddensubsidy.app.util.rememberNotificationPermissionRequest
import com.hiddensubsidy.app.ui.calendar.CalendarScreen
import com.hiddensubsidy.app.ui.components.BottomTabBar
import com.hiddensubsidy.app.ui.detail.PolicyDetailScreen
import com.hiddensubsidy.app.ui.events.EventDetailScreen
import com.hiddensubsidy.app.ui.events.EventListScreen
import com.hiddensubsidy.app.ui.auth.LoginScreen
import com.hiddensubsidy.app.ui.favorites.FavoritesScreen
import com.hiddensubsidy.app.ui.home.HomeScreen
import com.hiddensubsidy.app.ui.search.SearchScreen
import com.hiddensubsidy.app.ui.missed.MissedSheet
import com.hiddensubsidy.app.ui.my.MyScreen
import com.hiddensubsidy.app.ui.onboarding.OnboardingScreen
import com.hiddensubsidy.app.ui.profile.ProfileEditScreen
import com.hiddensubsidy.app.ui.theme.HiddenSubsidyTheme

class MainActivity : ComponentActivity() {

    // 알림 deep-link로 전달된 정책 ID. AppRoot LaunchedEffect가 소비.
    private val pendingPolicyId = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = getSharedPreferences("hs_prefs", Context.MODE_PRIVATE)
        // 매일 1회 정책 마감 검사 (즐겨찾기 정책 D-3/D-1/D-0 알림). KEEP 정책이라 중복 X
        NotificationScheduler.schedulePeriodic(applicationContext)
        // AdMob SDK 초기화 + 첫 광고 미리 로드 (5번째 정책 상세 진입 시 표시)
        AdManager.initialize(applicationContext)
        // 알림 클릭으로 진입한 경우 policy id 픽업
        pendingPolicyId.value = intent?.getStringExtra(
            com.hiddensubsidy.app.notification.NotificationHelper.EXTRA_POLICY_ID
        )
        setContent {
            HiddenSubsidyTheme {
                Root(prefs, pendingPolicyId.value) { pendingPolicyId.value = null }
            }
        }
    }

    /** 앱이 이미 실행 중일 때 알림 클릭으로 들어온 경우 */
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra(
            com.hiddensubsidy.app.notification.NotificationHelper.EXTRA_POLICY_ID
        )?.let { pendingPolicyId.value = it }
    }
}

@Composable
private fun Root(
    prefs: SharedPreferences,
    pendingPolicyId: String? = null,
    onPolicyIdConsumed: () -> Unit = {},
) {
    val context = LocalContext.current
    var onboarded by remember { mutableStateOf(prefs.getBoolean("onboarded", false)) }

    AnimatedContent(
        targetState = onboarded,
        transitionSpec = {
            fadeIn(tween(320)) togetherWith fadeOut(tween(200))
        },
        label = "onboard-gate",
    ) { done ->
        if (done) {
            AppRoot(pendingPolicyId = pendingPolicyId, onPolicyIdConsumed = onPolicyIdConsumed)
        } else {
            OnboardingScreen(onComplete = { profile ->
                // onboarded flag만 마크. 프로필 자체는 UserPrefs로 저장 (모든 필드 일관 처리).
                prefs.edit().putBoolean("onboarded", true).apply()
                UserPrefs.save(context, profile)
                onboarded = true
            })
        }
    }
}

// =====================================================
// 화면 트리
// =====================================================
private sealed class Screen {
    data object Tabs : Screen()
    data class PolicyDetail(val id: String) : Screen()
    data class EventDetail(val id: String) : Screen()
    data object ProfileEdit : Screen()
    data object Favorites : Screen()
    data object Applied : Screen()
    data object Received : Screen()
    data object Dismissed : Screen()
    data class Search(val initialCategory: String? = null) : Screen()
    data object Login : Screen()
}

@Composable
private fun AppRoot(
    pendingPolicyId: String? = null,
    onPolicyIdConsumed: () -> Unit = {},
) {
    val context = LocalContext.current
    val httpClient = remember {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; explicitNulls = false })
            }
        }
    }
    val repository: CachedPolicyRepository = remember {
        val remote = RemotePolicyRepository(
            client = httpClient,
            url = "https://gyubam.github.io/hidemoney/policies.json",
        )
        val fallback = InMemoryPolicyRepository(SampleData.allPolicies)
        CachedPolicyRepository(context, remote, fallback)
    }
    val today = remember { java.time.LocalDate.now() }
    var allPolicies by remember { mutableStateOf(SampleData.allPolicies.withFreshDaysLeft(today)) }
    // 첫 진입 — remote refresh 끝날 때까지 home 카드들 spinner로
    var isLoading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        // 1) 캐시 또는 fallback으로 즉시 응답 (daysLeft는 today 기준으로 재계산)
        allPolicies = repository.loadAll().withFreshDaysLeft(today)
        // 2) background refresh — 성공 시 갱신, 실패 시 기존 유지
        runCatching {
            allPolicies = repository.refresh().withFreshDaysLeft(today)
            android.util.Log.i("policies-fetch", "Refreshed from remote: ${allPolicies.size}")
        }.onFailure {
            android.util.Log.w("policies-fetch", "Remote refresh failed: ${it.message}")
        }
        isLoading = false
        // [dev] export — internal cacheDir
        runCatching {
            val file = java.io.File(context.cacheDir, "policies.json")
            file.writeText(SampleData.exportPoliciesJson())
        }
    }
    val byId = remember(allPolicies) { allPolicies.associateBy { it.id } }

    var profile by remember { mutableStateOf(UserPrefs.load(context)) }
    var dismissed by remember { mutableStateOf(DismissedRepository.load(context)) }
    val requestNotif = rememberNotificationPermissionRequest { granted ->
        val msg = if (granted) "🔔 알림이 켜졌어요" else "알림 권한이 거부됐어요"
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
    // isLoading 동안엔 SampleData 19개로 매칭된 가짜 thisWeek/deadlineSoon이 잠깐 보이는 사고 방지.
    // 빈 HomeData면 HomeScreen이 카드 자체를 안 그림 (firstOrNull?.let / isNotEmpty 분기).
    val home = remember(profile, allPolicies, isLoading, dismissed) {
        if (isLoading) {
            com.hiddensubsidy.app.data.model.HomeData(0L, 0, emptyList(), emptyList(), emptyList())
        } else {
            HomeAggregator.computeHome(allPolicies, profile, today, dismissed)
        }
    }
    var favorites by remember { mutableStateOf(FavoritesRepository.load(context)) }
    val calendarEvents = remember(profile, allPolicies, favorites, today) {
        CalendarAggregator.compute(allPolicies, favorites, profile, today)
    }
    val eventBundles = remember(profile, allPolicies) {
        EventAggregator.compute(allPolicies, profile)
    }
    var activeTriggers by remember { mutableStateOf(EventTriggerRepository.loadActive(context)) }
    // Firebase Auth — 미로그인이어도 앱 그대로 동작 (게스트 모드). 로그인 시 displayName/email 표시
    val authUser by AuthRepository.authState.collectAsState(initial = AuthRepository.currentUser)
    val scope = rememberCoroutineScope()
    var applied by remember { mutableStateOf(ApplicationStatusRepository.loadApplied(context)) }
    var received by remember { mutableStateOf(ApplicationStatusRepository.loadReceived(context)) }

    // 로그인 시 Firestore pull → 모든 로컬 state 갱신 (uid 변경 감지)
    LaunchedEffect(authUser?.uid) {
        val uid = authUser?.uid ?: return@LaunchedEffect
        val pulled = CloudSyncRepository.pullFromCloud(context, uid)
        if (pulled) {
            favorites = FavoritesRepository.load(context)
            applied = ApplicationStatusRepository.loadApplied(context)
            received = ApplicationStatusRepository.loadReceived(context)
            dismissed = DismissedRepository.load(context)
            activeTriggers = EventTriggerRepository.loadActive(context)
            profile = UserPrefs.load(context)
        }
    }
    // 로컬 변경 시 클라우드 push (로그인 상태일 때만, debounce 800ms로 연속 토글 합침)
    LaunchedEffect(favorites, applied, received, dismissed, activeTriggers, profile, authUser?.uid) {
        val uid = authUser?.uid ?: return@LaunchedEffect
        kotlinx.coroutines.delay(800)
        CloudSyncRepository.pushToCloud(context, uid)
    }
    val mySummary = remember(favorites, applied, received, allPolicies) {
        val favoritePolicies = favorites.mapNotNull { byId[it] }
        val appliedPolicies = applied.mapNotNull { byId[it] }
        val receivedPolicies = received.mapNotNull { byId[it] }
        com.hiddensubsidy.app.data.model.MySummary(
            savedCount = favoritePolicies.size,
            savedAmount = favoritePolicies.sumOf { it.amount },
            appliedCount = appliedPolicies.size,
            appliedAmount = appliedPolicies.sumOf { it.amount },
            receivedCount = receivedPolicies.size,
            receivedAmount = receivedPolicies.sumOf { it.amount },
        )
    }
    var tab by remember { mutableStateOf(0) }
    var screen by remember { mutableStateOf<Screen>(Screen.Tabs) }
    // detail 진입 직전 screen 보관 — 뒤로 가면 그 화면으로 복원 (Search/Favorites에서 detail 갔다 와도 유지)
    var detailReturnScreen by remember { mutableStateOf<Screen>(Screen.Tabs) }
    var showMissed by remember { mutableStateOf(false) }

    // detail 진입 시 현재 screen을 returnScreen으로 저장
    val navigateToPolicyDetail: (com.hiddensubsidy.app.data.model.Policy) -> Unit = { policy ->
        if (screen !is Screen.PolicyDetail && screen !is Screen.EventDetail) {
            detailReturnScreen = screen
        }
        screen = Screen.PolicyDetail(policy.id)
        // 전면 광고 hook — 5번에 1번, 30초 보호 + 5분 쿨다운은 AdManager 내부 정책
        (context as? android.app.Activity)?.let { AdManager.onPolicyDetailEntered(it) }
    }
    val navigateToEventDetail: (com.hiddensubsidy.app.data.model.EventBundle) -> Unit = { bundle ->
        if (screen !is Screen.PolicyDetail && screen !is Screen.EventDetail) {
            detailReturnScreen = screen
        }
        screen = Screen.EventDetail(bundle.eventId)
    }

    BackHandler(enabled = screen !is Screen.Tabs) {
        screen = when (screen) {
            is Screen.PolicyDetail, is Screen.EventDetail -> detailReturnScreen
            else -> Screen.Tabs
        }
    }

    // 알림 deep-link — 정책 id 들어오면 detail 진입. 한 번 소비 후 클리어.
    LaunchedEffect(pendingPolicyId, allPolicies) {
        val pid = pendingPolicyId ?: return@LaunchedEffect
        if (allPolicies.any { it.id == pid }) {
            detailReturnScreen = Screen.Tabs
            screen = Screen.PolicyDetail(pid)
            onPolicyIdConsumed()
        }
    }

    // 온보딩 직후 진입 시 prefs 변경분 반영 — Compose가 ProfileEdit 후 자동 재컴포지션 처리하므로 추가 동기화 불필요

    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            val dur = 280
            if (targetState is Screen.Tabs) {
                (slideInHorizontally(animationSpec = tween(dur)) { -it / 5 } + fadeIn(tween(dur))) togetherWith
                    (slideOutHorizontally(animationSpec = tween(dur)) { it } + fadeOut(tween(dur)))
            } else {
                (slideInHorizontally(animationSpec = tween(dur)) { it } + fadeIn(tween(dur))) togetherWith
                    (slideOutHorizontally(animationSpec = tween(dur)) { -it / 5 } + fadeOut(tween(dur)))
            }
        },
        label = "screen-transition",
    ) { s ->
        when (s) {
            is Screen.Tabs -> TabsHost(
                tab = tab,
                onTabChange = { tab = it },
                onMissedCardClick = { showMissed = true },
                onPolicyClick = navigateToPolicyDetail,
                onEventClick = navigateToEventDetail,
                home = home,
                calendarEvents = calendarEvents,
                favorites = favorites,
                eventBundles = eventBundles,
                activeTriggers = activeTriggers,
                onRequestNotification = requestNotif,
                profile = profile,
                onEditProfile = { screen = Screen.ProfileEdit },
                mySummary = mySummary,
                byId = byId,
                onFavoritesClick = { screen = Screen.Favorites },
                onAppliedClick = { screen = Screen.Applied },
                onReceivedClick = { screen = Screen.Received },
                onCategoryClick = { cat -> screen = Screen.Search(initialCategory = cat) },
                onTriggerCardClick = { tab = 2 },  // 이벤트 탭으로 이동
                onProgressClick = { screen = Screen.Favorites },  // 마이 진척 → 받을 예정 화면
                onProfileEditClick = { screen = Screen.ProfileEdit },
                signedInName = authUser?.displayName,
                signedInEmail = authUser?.email,
                onSignInClick = { screen = Screen.Login },
                onSignOutClick = {
                    scope.launch {
                        AuthRepository.signOut(context)
                        Toast.makeText(context, "로그아웃했어요", Toast.LENGTH_SHORT).show()
                    }
                },
                dismissedCount = dismissed.size,
                onDismissedClick = { screen = Screen.Dismissed },
                onSearchClick = { screen = Screen.Search() },
                onNotificationIconClick = requestNotif,
                onProfileIconClick = { tab = 3 },
                onSeeAllClick = { screen = Screen.Search() },
                onPremiumClick = {
                    Toast.makeText(context, "프리미엄 기능은 출시 후 준비 중이에요", Toast.LENGTH_SHORT).show()
                },
                isLoading = isLoading,
            )

            is Screen.PolicyDetail -> {
                val p = byId[s.id]?.matchedWith(profile)
                if (p != null) {
                    PolicyDetailScreen(
                        policy = p,
                        isFavorite = p.id in favorites,
                        isApplied = p.id in applied,
                        isReceived = p.id in received,
                        isDismissed = p.id in dismissed,
                        onBack = { screen = detailReturnScreen },
                        onToggleFavorite = {
                            favorites = FavoritesRepository.toggle(context, p.id)
                            val msg = if (p.id in favorites) "받을 예정에 추가됐어요" else "받을 예정에서 빠졌어요"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            // 즐겨찾기 변경 시 즉시 1회 검사 (당일 D-3/D-1/D-0 알림 빠진 거 채움)
                            NotificationScheduler.runOnce(context)
                        },
                        onToggleApplied = {
                            val (newApplied, newReceived) = ApplicationStatusRepository.toggleApplied(context, p.id)
                            applied = newApplied
                            received = newReceived
                            val msg = if (p.id in newApplied) "신청한 지원금에 추가했어요" else "신청 상태를 해제했어요"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        onToggleReceived = {
                            val (newApplied, newReceived) = ApplicationStatusRepository.toggleReceived(context, p.id)
                            applied = newApplied
                            received = newReceived
                            val msg = if (p.id in newReceived) "받은 지원금에 추가했어요 🎉" else "받음 상태를 해제했어요"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        onToggleDismissed = {
                            dismissed = DismissedRepository.toggle(context, p.id)
                            val msg = if (p.id in dismissed) "관심 없음으로 표시했어요" else "다시 표시되도록 했어요"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                    )
                }
            }

            is Screen.EventDetail -> {
                val e = eventBundles.firstOrNull { it.eventId == s.id }
                if (e != null) {
                    EventDetailScreen(
                        bundle = e,
                        isActive = e.eventId in activeTriggers,
                        onBack = { screen = detailReturnScreen },
                        onPolicyClick = navigateToPolicyDetail,
                        onToggleTrigger = {
                            val nowActive = EventTriggerRepository.toggle(context, e.eventId)
                            activeTriggers = EventTriggerRepository.loadActive(context)
                            val label = e.event?.label ?: "이벤트"
                            val msg = if (nowActive) "${label} 시점을 마크했어요" else "${label} 마크를 해제했어요"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                    )
                }
            }

            is Screen.ProfileEdit -> {
                ProfileEditScreen(
                    initialProfile = profile,
                    onBack = { screen = Screen.Tabs },
                    onSave = { newProfile ->
                        UserPrefs.save(context, newProfile)
                        profile = newProfile
                        screen = Screen.Tabs
                    },
                )
            }

            is Screen.Search -> {
                val searchablePolicies = remember(allPolicies, profile) {
                    allPolicies.matchedWith(profile)
                }
                SearchScreen(
                    allPolicies = searchablePolicies,
                    profile = profile,
                    onBack = { screen = Screen.Tabs },
                    onPolicyClick = navigateToPolicyDetail,
                    initialCategory = s.initialCategory,
                )
            }

            is Screen.Favorites -> {
                val favoritePolicies = remember(favorites, allPolicies, profile) {
                    favorites.mapNotNull { byId[it] }
                        .map { it.matchedWith(profile).withFreshDaysLeft(today) }
                        .sortedWith(
                            compareBy(
                                // 마감 임박 우선 (daysLeft 0~30), 그 외는 amount 큰 순
                                { if (it.deadline.isNotBlank() && it.daysLeft in 0..30) it.daysLeft else Int.MAX_VALUE },
                                { -it.amount },
                            )
                        )
                }
                FavoritesScreen(
                    favorites = favoritePolicies,
                    onBack = { screen = Screen.Tabs },
                    onPolicyClick = navigateToPolicyDetail,
                    kind = com.hiddensubsidy.app.ui.favorites.PolicyStatusKind.Saved,
                )
            }

            is Screen.Applied -> {
                val appliedPolicies = remember(applied, allPolicies, profile) {
                    applied.mapNotNull { byId[it] }
                        .map { it.matchedWith(profile).withFreshDaysLeft(today) }
                        .sortedByDescending { it.amount }
                }
                FavoritesScreen(
                    favorites = appliedPolicies,
                    onBack = { screen = Screen.Tabs },
                    onPolicyClick = navigateToPolicyDetail,
                    kind = com.hiddensubsidy.app.ui.favorites.PolicyStatusKind.Applied,
                )
            }

            is Screen.Received -> {
                val receivedPolicies = remember(received, allPolicies, profile) {
                    received.mapNotNull { byId[it] }
                        .map { it.matchedWith(profile).withFreshDaysLeft(today) }
                        .sortedByDescending { it.amount }
                }
                FavoritesScreen(
                    favorites = receivedPolicies,
                    onBack = { screen = Screen.Tabs },
                    onPolicyClick = navigateToPolicyDetail,
                    kind = com.hiddensubsidy.app.ui.favorites.PolicyStatusKind.Received,
                )
            }

            is Screen.Dismissed -> {
                val dismissedPolicies = remember(dismissed, allPolicies, profile) {
                    dismissed.mapNotNull { byId[it] }
                        .map { it.matchedWith(profile).withFreshDaysLeft(today) }
                        .sortedByDescending { it.amount }
                }
                FavoritesScreen(
                    favorites = dismissedPolicies,
                    onBack = { screen = Screen.Tabs },
                    onPolicyClick = navigateToPolicyDetail,
                    kind = com.hiddensubsidy.app.ui.favorites.PolicyStatusKind.Dismissed,
                )
            }

            is Screen.Login -> {
                LoginScreen(
                    onBack = { screen = Screen.Tabs },
                    onSuccess = { screen = Screen.Tabs },
                )
            }
        }
    }

    if (showMissed) {
        MissedSheet(
            data = home,
            onDismiss = { showMissed = false },
            onGrantClick = { grant ->
                byId[grant.id]?.let {
                    showMissed = false
                    navigateToPolicyDetail(it)
                }
            },
            onShare = {
                ShareHelper.shareMissed(
                    context = context,
                    missedAmount = home.missedTotalAmount,
                    missedCount = home.missedCount,
                )
            },
            onNotifyOptIn = {
                requestNotif()
                showMissed = false
            },
        )
    }
}

@Composable
private fun TabsHost(
    tab: Int,
    onTabChange: (Int) -> Unit,
    onMissedCardClick: () -> Unit,
    onPolicyClick: (com.hiddensubsidy.app.data.model.Policy) -> Unit,
    onEventClick: (com.hiddensubsidy.app.data.model.EventBundle) -> Unit,
    home: com.hiddensubsidy.app.data.model.HomeData,
    calendarEvents: List<com.hiddensubsidy.app.data.model.PolicyCalendarEvent>,
    favorites: Set<String>,
    eventBundles: List<com.hiddensubsidy.app.data.model.EventBundle>,
    activeTriggers: Set<String>,
    onRequestNotification: () -> Unit,
    profile: com.hiddensubsidy.app.data.model.UserProfile,
    onEditProfile: () -> Unit,
    mySummary: com.hiddensubsidy.app.data.model.MySummary,
    byId: Map<String, com.hiddensubsidy.app.data.model.Policy>,
    onFavoritesClick: () -> Unit,
    onAppliedClick: () -> Unit,
    onReceivedClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onTriggerCardClick: () -> Unit,
    onProgressClick: () -> Unit,
    onProfileEditClick: () -> Unit,
    onNotificationIconClick: () -> Unit,
    onProfileIconClick: () -> Unit,
    onSeeAllClick: () -> Unit,
    onPremiumClick: () -> Unit,
    signedInName: String?,
    signedInEmail: String?,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    dismissedCount: Int,
    onDismissedClick: () -> Unit,
    isLoading: Boolean,
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .fillMaxSize()
            .weight(1f)
        ) {
            when (tab) {
                0 -> HomeScreen(
                    data = home,
                    profile = profile,
                    mySummary = mySummary,
                    activeTriggers = activeTriggers,
                    eventBundles = eventBundles,
                    onMissedCardClick = onMissedCardClick,
                    onPolicyClick = onPolicyClick,
                    onCategoryClick = onCategoryClick,
                    onTriggerCardClick = onTriggerCardClick,
                    onProgressClick = onProgressClick,
                    onProfileEditClick = onProfileEditClick,
                    onSearchClick = onSearchClick,
                    onNotificationClick = onNotificationIconClick,
                    onProfileClick = onProfileIconClick,
                    onSeeAllThisWeek = onSeeAllClick,
                    onSeeAllDeadlines = onSeeAllClick,
                    isLoading = isLoading,
                )
                1 -> CalendarScreen(
                    events = calendarEvents,
                    favorites = favorites,
                    onPolicyClick = { id -> byId[id]?.let(onPolicyClick) },
                )
                2 -> EventListScreen(
                    events = eventBundles,
                    activeTriggers = activeTriggers,
                    onEventClick = onEventClick,
                )
                else -> MyScreen(
                    profile = profile,
                    summary = mySummary,
                    onEditProfile = onEditProfile,
                    onNotificationSettings = onRequestNotification,
                    onInviteFriends = { ShareHelper.inviteFriends(context) },
                    onPrivacyPolicy = { ShareHelper.openPrivacyPolicy(context) },
                    onFeedback = { ShareHelper.sendFeedback(context) },
                    onFavoritesClick = onFavoritesClick,
                    onAppliedClick = onAppliedClick,
                    onReceivedClick = onReceivedClick,
                    onPremiumClick = onPremiumClick,
                    signedInName = signedInName,
                    signedInEmail = signedInEmail,
                    onSignInClick = onSignInClick,
                    onSignOutClick = onSignOutClick,
                    dismissedCount = dismissedCount,
                    onDismissedClick = onDismissedClick,
                )
            }
        }
        BottomTabBar(current = tab, onChange = onTabChange)
    }
}
