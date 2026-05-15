package com.hiddensubsidy.app.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================
// Brand — 액센트 컬러: 민트/그린 ("돈이 들어온다")
// 사용 빈도는 매우 절제 (숫자/메인 CTA에만)
// =============================================================
object Brand {
    val Mint50  = Color(0xFFE8FFF6)
    val Mint100 = Color(0xFFD0FFF0)
    val Mint500 = Color(0xFF00C896)   // primary
    val Mint600 = Color(0xFF00A87D)
    val Mint700 = Color(0xFF00805C)   // 진한 텍스트용
    val Mint800 = Color(0xFF005A40)
}

// =============================================================
// Toss-style gray palette — 거의 모든 텍스트/표면이 이 안에서 결정됨
// =============================================================
object Gray {
    val G900 = Color(0xFF191F28)   // primary text
    val G800 = Color(0xFF333D4B)
    val G700 = Color(0xFF4E5968)   // secondary text
    val G600 = Color(0xFF6B7684)
    val G500 = Color(0xFF8B95A1)   // tertiary text
    val G400 = Color(0xFFB0B8C1)
    val G300 = Color(0xFFD1D6DB)
    val G200 = Color(0xFFE5E8EB)   // divider
    val G100 = Color(0xFFF2F4F6)   // pill 배경, 알약 액션
    val G50  = Color(0xFFF9FAFB)
    val White = Color(0xFFFFFFFF)
    // 토스 분석에서 발견: 배경은 살짝 푸른 톤
    val Background = Color(0xFFF2F4F8)   // bluish gray
}

// 일러스트 아이콘 버블 컬러 (토스 3D 일러스트 대신 이모지 + 컬러 버블)
object Bubble {
    val Mint   = Color(0xFFE0FBF1)
    val Sky    = Color(0xFFE3F0FF)
    val Lemon  = Color(0xFFFFF6D6)
    val Coral  = Color(0xFFFFE4E0)
    val Lilac  = Color(0xFFEEE6FF)
    val Sand   = Color(0xFFF6EFE3)
}

object Semantic {
    val Warning = Color(0xFFF04438)   // 마감 임박, 못 받은 돈 (살짝 부드러운 적색)
    val WarningBg = Color(0xFFFEF3F2)
    val Success = Color(0xFF12B76A)
}

// =============================================================
// 앱 전반에서 참조하는 의미적 컬러 (라이트 전용)
// =============================================================
data class AppColors(
    val background: Color,
    val cardBg: Color,
    val cardBorder: Color,
    val divider: Color,

    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,

    val accent: Color,
    val accentText: Color,
    val accentBg: Color,
    val onAccent: Color,

    val warning: Color,
    val warningBg: Color,
)

val LightAppColors = AppColors(
    background    = Gray.Background,   // 살짝 푸른 톤 (토스 시그니처)
    cardBg        = Gray.White,        // 카드 순백
    cardBorder    = Gray.G200,
    divider       = Gray.G200,

    textPrimary   = Gray.G900,
    textSecondary = Gray.G700,
    textTertiary  = Gray.G500,

    accent        = Brand.Mint500,
    accentText    = Brand.Mint700,
    accentBg      = Brand.Mint50,
    onAccent      = Gray.White,

    warning       = Semantic.Warning,
    warningBg     = Semantic.WarningBg,
)

// 다크 모드는 만들지 않음 (사용자 결정: 항상 라이트)
val DarkAppColors = LightAppColors