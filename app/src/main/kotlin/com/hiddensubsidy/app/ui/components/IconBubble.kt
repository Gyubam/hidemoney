package com.hiddensubsidy.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 토스의 3D 컬러 일러스트 자리에 우리는 이모지 + 컬러 버블로 대체.
 * MVP 단계에서 디자이너 일러스트 없이 모던한 톤을 내는 가장 빠른 방법.
 */
@Composable
fun IconBubble(
    emoji: String,
    background: Color,
    size: Dp = 44.dp,
    fontSize: Int = 22,
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = emoji,
            style = TextStyle(fontSize = fontSize.sp, fontWeight = FontWeight.Normal),
        )
    }
}