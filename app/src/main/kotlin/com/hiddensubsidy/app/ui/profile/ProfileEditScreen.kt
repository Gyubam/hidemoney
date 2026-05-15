package com.hiddensubsidy.app.ui.profile

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.hiddensubsidy.app.data.model.UserProfile
import com.hiddensubsidy.app.ui.onboarding.ProfileInputPage
import com.hiddensubsidy.app.ui.theme.AppTheme

@Composable
fun ProfileEditScreen(
    initialProfile: UserProfile,
    onBack: () -> Unit,
    onSave: (UserProfile) -> Unit,
) {
    val colors = AppTheme.colors
    var profile by remember { mutableStateOf(initialProfile) }
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        ProfileInputPage(
            topInset = topInset,
            profile = profile,
            onChange = { profile = it },
            onBack = onBack,
            onSubmit = { onSave(profile) },
            title = "프로필 편집",
            submitLabel = "저장하기",
        )
    }
}
