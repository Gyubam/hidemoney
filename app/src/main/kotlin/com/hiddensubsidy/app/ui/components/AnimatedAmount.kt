package com.hiddensubsidy.app.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import java.text.NumberFormat
import java.util.Locale

private val krFormatter = NumberFormat.getNumberInstance(Locale.KOREA)

/**
 * 금액 카운트업 애니메이션 (0 → target).
 * 임팩트 카드의 핵심 모션.
 */
@Composable
fun AnimatedAmount(
    amount: Long,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    durationMillis: Int = 1200,
    suffix: String = "원",
    textAlign: TextAlign? = null,
) {
    var target by remember { mutableStateOf(0) }
    LaunchedEffect(amount) {
        target = amount.toInt().coerceAtMost(Int.MAX_VALUE)
    }
    val animated by animateIntAsState(
        targetValue = target,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = LinearOutSlowInEasing
        ),
        label = "amount-countup"
    )
    Text(
        text = "${krFormatter.format(animated)}$suffix",
        style = style,
        color = color,
        modifier = modifier,
        textAlign = textAlign,
    )
}

fun formatAmount(amount: Long): String =
    "${krFormatter.format(amount)}원"