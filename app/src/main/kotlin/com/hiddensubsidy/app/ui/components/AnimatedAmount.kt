package com.hiddensubsidy.app.ui.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
 * 임팩트 카드의 핵심 모션. 큰 수는 한글 단위(억/만/조)로 표기.
 *
 * Long → Float 변환으로 큰 수 보존 (이전 toInt() overflow 버그 fix).
 * Float 정밀도 한계로 1조 이상은 약간 round될 수 있음 (시각적 카운트업이라 허용).
 */
@Composable
fun AnimatedAmount(
    amount: Long,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    durationMillis: Int = 1200,
    textAlign: TextAlign? = null,
) {
    // rememberSaveable로 LazyColumn dispose/recompose 시에도 target 보존.
    // 첫 진입엔 0→amount 카운트업, 스크롤 후 재진입엔 target=amount 그대로라 애니메이션 X.
    var target by rememberSaveable { mutableStateOf(0f) }
    LaunchedEffect(amount) {
        target = amount.toFloat()
    }
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = LinearOutSlowInEasing
        ),
        label = "amount-countup"
    )
    Text(
        text = formatAmount(animated.toLong()),
        style = style,
        color = color,
        modifier = modifier,
        textAlign = textAlign,
    )
}

/**
 * 토스 톤 금액 표기 — 단위에 따라 압축.
 * - < 1만원: "9,500원"
 * - 1만 ~ 1억: "120만원"
 * - 1억 ~ 10억: "1.2억원" (소수 1자리)
 * - 10억 ~ 1조: "1,303억원"
 * - 1조 이상: "1.5조원"
 */
fun formatAmount(amount: Long): String = when {
    amount < 0L -> "0원"
    amount >= 1_000_000_000_000L ->
        "${"%.1f".format(amount / 1_000_000_000_000.0)}조원"
    amount >= 1_000_000_000L ->
        "${krFormatter.format(amount / 100_000_000L)}억원"
    amount >= 100_000_000L ->
        "${"%.1f".format(amount / 100_000_000.0)}억원"
    amount >= 10_000L ->
        "${krFormatter.format(amount / 10_000L)}만원"
    else -> "${krFormatter.format(amount)}원"
}
