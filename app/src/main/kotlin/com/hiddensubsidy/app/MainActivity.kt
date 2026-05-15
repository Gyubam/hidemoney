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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.LaunchedEffect
import com.hiddensubsidy.app.data.CachedPolicyRepository
import com.hiddensubsidy.app.data.FavoritesRepository
import com.hiddensubsidy.app.data.InMemoryPolicyRepository
import com.hiddensubsidy.app.data.PolicyRepository
import com.hiddensubsidy.app.data.RemotePolicyRepository
import com.hiddensubsidy.app.data.SampleData
import com.hiddensubsidy.app.data.UserPrefs
import com.hiddensubsidy.app.data.eligibleOnly
import com.hiddensubsidy.app.data.matchedWith
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
import com.hiddensubsidy.app.ui.home.HomeScreen
import com.hiddensubsidy.app.ui.missed.MissedSheet
import com.hiddensubsidy.app.ui.my.MyScreen
import com.hiddensubsidy.app.ui.onboarding.OnboardingScreen
import com.hiddensubsidy.app.ui.profile.ProfileEditScreen
import com.hiddensubsidy.app.ui.theme.HiddenSubsidyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = getSharedPreferences("hs_prefs", Context.MODE_PRIVATE)
        setContent {
            HiddenSubsidyTheme {
                Root(prefs)
            }
        }
    }
}

@Composable
private fun Root(prefs: SharedPreferences) {
    var onboarded by remember { mutableStateOf(prefs.getBoolean("onboarded", false)) }

    AnimatedContent(
        targetState = onboarded,
        transitionSpec = {
            fadeIn(tween(320)) togetherWith fadeOut(tween(200))
        },
        label = "onboard-gate",
    ) { done ->
        if (done) {
            AppRoot()
        } else {
            OnboardingScreen(onComplete = { profile ->
                prefs.edit().apply {
                    putBoolean("onboarded", true)
                    profile.age?.let { putInt("age", it) }
                    profile.region?.let { putString("region", it) }
                    profile.occupation?.let { putString("occupation", it) }
                    profile.married?.let { putBoolean("married", it) }
                    profile.hasChildren?.let { putBoolean("has_children", it) }
                }.apply()
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
}

@Composable
private fun AppRoot() {
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
    var allPolicies by remember { mutableStateOf(SampleData.allPolicies) }
    LaunchedEffect(Unit) {
        // 1) 캐시 또는 fallback으로 즉시 응답
        allPolicies = repository.loadAll()
        // 2) background refresh — 성공 시 갱신, 실패 시 기존 유지
        runCatching {
            allPolicies = repository.refresh()
            android.util.Log.i("policies-fetch", "Refreshed from remote: ${allPolicies.size}")
        }.onFailure {
            android.util.Log.w("policies-fetch", "Remote refresh failed: ${it.message}")
        }
        // [dev] export — internal cacheDir
        runCatching {
            val file = java.io.File(context.cacheDir, "policies.json")
            file.writeText(SampleData.exportPoliciesJson())
        }
    }
    val byId = remember(allPolicies) { allPolicies.associateBy { it.id } }

    var profile by remember { mutableStateOf(UserPrefs.load(context)) }
    val baseHome = SampleData.home
    val requestNotif = rememberNotificationPermissionRequest { granted ->
        val msg = if (granted) "🔔 알림이 켜졌어요" else "알림 권한이 거부됐어요"
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
    val home = remember(profile) {
        baseHome.copy(
            // 자격 충족 정책만 (행동 유도)
            thisWeekPolicies = baseHome.thisWeekPolicies.eligibleOnly(profile),
            // 매칭 결과만 inject (충족/미충족 모두 표시)
            deadlineSoon = baseHome.deadlineSoon.matchedWith(profile),
        )
    }
    val calendarEvents = remember(profile, allPolicies) {
        SampleData.calendarEvents.filter { e ->
            byId[e.policyId]?.matchedWith(profile)?.isEligible == true
        }
    }
    var favorites by remember { mutableStateOf(FavoritesRepository.load(context)) }
    val mySummary = remember(favorites, allPolicies) {
        val favoritePolicies = favorites.mapNotNull { byId[it] }
        SampleData.mySummary.copy(
            savedCount = favoritePolicies.size,
            savedAmount = favoritePolicies.sumOf { it.amount },
        )
    }
    var tab by remember { mutableStateOf(0) }
    var screen by remember { mutableStateOf<Screen>(Screen.Tabs) }
    var showMissed by remember { mutableStateOf(false) }

    BackHandler(enabled = screen !is Screen.Tabs) {
        screen = Screen.Tabs
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
                onPolicyClick = { policy -> screen = Screen.PolicyDetail(policy.id) },
                onEventClick = { bundle -> screen = Screen.EventDetail(bundle.eventId) },
                home = home,
                calendarEvents = calendarEvents,
                onRequestNotification = requestNotif,
                profile = profile,
                onEditProfile = { screen = Screen.ProfileEdit },
                mySummary = mySummary,
                byId = byId,
            )

            is Screen.PolicyDetail -> {
                val p = byId[s.id]?.matchedWith(profile)
                if (p != null) {
                    PolicyDetailScreen(
                        policy = p,
                        isFavorite = p.id in favorites,
                        onBack = { screen = Screen.Tabs },
                        onToggleFavorite = {
                            favorites = FavoritesRepository.toggle(context, p.id)
                            val msg = if (p.id in favorites) "받을 예정에 추가됐어요" else "받을 예정에서 빠졌어요"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                    )
                }
            }

            is Screen.EventDetail -> {
                val e = SampleData.findEvent(s.id)
                if (e != null) {
                    EventDetailScreen(
                        bundle = e,
                        onBack = { screen = Screen.Tabs },
                        onPolicyClick = { policy -> screen = Screen.PolicyDetail(policy.id) },
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
        }
    }

    if (showMissed) {
        MissedSheet(
            data = SampleData.home,
            onDismiss = { showMissed = false },
            onShare = {
                ShareHelper.shareMissed(
                    context = context,
                    missedAmount = SampleData.home.missedTotalAmount,
                    missedCount = SampleData.home.missedCount,
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
    onRequestNotification: () -> Unit,
    profile: com.hiddensubsidy.app.data.model.UserProfile,
    onEditProfile: () -> Unit,
    mySummary: com.hiddensubsidy.app.data.model.MySummary,
    byId: Map<String, com.hiddensubsidy.app.data.model.Policy>,
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
                    onMissedCardClick = onMissedCardClick,
                    onPolicyClick = onPolicyClick,
                )
                1 -> CalendarScreen(
                    events = calendarEvents,
                    onPolicyClick = { id -> byId[id]?.let(onPolicyClick) },
                )
                2 -> EventListScreen(
                    events = SampleData.events,
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
                )
            }
        }
        BottomTabBar(current = tab, onChange = onTabChange)
    }
}
