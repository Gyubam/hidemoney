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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.hiddensubsidy.app.data.SampleData
import com.hiddensubsidy.app.ui.calendar.CalendarScreen
import com.hiddensubsidy.app.ui.components.BottomTabBar
import com.hiddensubsidy.app.ui.detail.PolicyDetailScreen
import com.hiddensubsidy.app.ui.events.EventDetailScreen
import com.hiddensubsidy.app.ui.events.EventListScreen
import com.hiddensubsidy.app.ui.home.HomeScreen
import com.hiddensubsidy.app.ui.missed.MissedSheet
import com.hiddensubsidy.app.ui.my.MyScreen
import com.hiddensubsidy.app.ui.onboarding.OnboardingScreen
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
}

@Composable
private fun AppRoot() {
    var tab by remember { mutableStateOf(0) }
    var screen by remember { mutableStateOf<Screen>(Screen.Tabs) }
    var showMissed by remember { mutableStateOf(false) }

    BackHandler(enabled = screen !is Screen.Tabs) {
        screen = Screen.Tabs
    }

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
            )

            is Screen.PolicyDetail -> {
                val p = SampleData.findPolicy(s.id)
                if (p != null) {
                    PolicyDetailScreen(
                        policy = p,
                        onBack = { screen = Screen.Tabs },
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
        }
    }

    if (showMissed) {
        MissedSheet(
            data = SampleData.home,
            onDismiss = { showMissed = false },
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
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .fillMaxSize()
            .weight(1f)
        ) {
            when (tab) {
                0 -> HomeScreen(
                    data = SampleData.home,
                    onMissedCardClick = onMissedCardClick,
                    onPolicyClick = onPolicyClick,
                )
                1 -> CalendarScreen(
                    events = SampleData.calendarEvents,
                    onPolicyClick = { id -> SampleData.findPolicy(id)?.let(onPolicyClick) },
                )
                2 -> EventListScreen(
                    events = SampleData.events,
                    onEventClick = onEventClick,
                )
                else -> MyScreen()
            }
        }
        BottomTabBar(current = tab, onChange = onTabChange)
    }
}
