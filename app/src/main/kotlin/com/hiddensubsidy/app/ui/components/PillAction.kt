package com.hiddensubsidy.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hiddensubsidy.app.ui.theme.AppTheme

/**
 * 토스의 회색 알약 액션 ("송금" / "내역" / "보기").
 * 카드 행 우측에서 행마다 빠른 액션을 제공.
 */
@Composable
fun PillAction(
    text: String,
    onClick: () -> Unit = {},
    background: Color? = null,
    contentColor: Color? = null,
) {
    val colors = AppTheme.colors
    val bg = background ?: colors.cardBorder.copy(alpha = 0.6f)
    val fg = contentColor ?: colors.textSecondary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = fg,
        )
    }
}