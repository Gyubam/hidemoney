package com.hiddensubsidy.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Toss-style spacing — 큰 여백이 디자인의 일부.
 */
@Immutable
data class Spacing(
    val xxs: Dp = 2.dp,
    val xs:  Dp = 4.dp,
    val sm:  Dp = 8.dp,
    val md:  Dp = 12.dp,
    val lg:  Dp = 16.dp,
    val xl:  Dp = 20.dp,
    val xxl: Dp = 24.dp,
    val card: Dp = 24.dp,        // 카드 안쪽 패딩
    val section: Dp = 32.dp,     // 섹션과 섹션 사이
    val sectionLg: Dp = 40.dp,   // 큰 섹션 단위
)

val DefaultSpacing = Spacing()