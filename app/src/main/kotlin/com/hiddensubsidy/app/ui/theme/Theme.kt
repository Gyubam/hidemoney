package com.hiddensubsidy.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightScheme = lightColorScheme(
    primary           = Brand.Mint500,
    onPrimary          = Gray.White,
    primaryContainer   = Brand.Mint50,
    onPrimaryContainer = Brand.Mint700,
    secondary          = Brand.Mint600,
    background         = Gray.Background,
    onBackground       = Gray.G900,
    surface            = Gray.White,
    onSurface          = Gray.G900,
    surfaceVariant     = Gray.G50,
    onSurfaceVariant   = Gray.G700,
    outline            = Gray.G200,
    outlineVariant     = Gray.G200,
    error              = Semantic.Warning,
    onError            = Gray.White,
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
val LocalSpacing   = staticCompositionLocalOf { DefaultSpacing }

/**
 * 앱 테마 — 사용자 결정에 따라 시스템 다크모드를 무시하고 항상 라이트 톤.
 */
@Composable
fun HiddenSubsidyTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true       // 상태바 아이콘 어둡게 (밝은 배경)
                isAppearanceLightNavigationBars = true
            }
        }
    }

    CompositionLocalProvider(
        LocalAppColors provides LightAppColors,
        LocalSpacing   provides DefaultSpacing,
    ) {
        MaterialTheme(
            colorScheme = LightScheme,
            typography  = AppTypography,
            shapes      = AppShapes,
            content     = content,
        )
    }
}

object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
    val spacing: Spacing
        @Composable get() = LocalSpacing.current
}