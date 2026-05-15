package com.hiddensubsidy.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 카테고리 → 이모지/컬러 버블 매핑.
 * 디자이너 일러스트 자리에 들어가는 시각 자산을 일관되게 유지.
 */
fun categoryEmoji(category: String): String = when (category) {
    "주거" -> "🏠"
    "출산" -> "👶"
    "생활" -> "📡"
    "교육" -> "🎓"
    "청년" -> "🧑"
    "창업" -> "🚀"
    "결혼" -> "💍"
    else   -> "💰"
}

fun categoryBubble(category: String): Color = when (category) {
    "주거" -> Bubble.Sky
    "출산" -> Bubble.Coral
    "생활" -> Bubble.Mint
    "교육" -> Bubble.Lemon
    "청년" -> Bubble.Lilac
    "창업" -> Bubble.Sand
    "결혼" -> Bubble.Coral
    else   -> Bubble.Mint
}
