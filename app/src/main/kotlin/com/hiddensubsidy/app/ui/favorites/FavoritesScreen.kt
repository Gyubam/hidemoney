package com.hiddensubsidy.app.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hiddensubsidy.app.data.model.Policy
import com.hiddensubsidy.app.ui.components.IconBubble
import com.hiddensubsidy.app.ui.components.PillAction
import com.hiddensubsidy.app.ui.components.formatAmount
import com.hiddensubsidy.app.ui.theme.AppTheme
import com.hiddensubsidy.app.ui.theme.categoryBubble
import com.hiddensubsidy.app.ui.theme.categoryEmoji

@Composable
fun FavoritesScreen(
    favorites: List<Policy>,
    onBack: () -> Unit,
    onPolicyClick: (Policy) -> Unit,
) {
    val colors = AppTheme.colors

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(onBack = onBack)

            if (favorites.isEmpty()) {
                EmptyState()
            } else {
                FavoritesList(
                    favorites = favorites,
                    onPolicyClick = onPolicyClick,
                )
            }
        }
    }
}

// =============================================================
// 상단 바 — 좌측 ← + 제목 "받을 예정"
// =============================================================
@Composable
private fun TopBar(onBack: () -> Unit) {
    val colors = AppTheme.colors
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topInset)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "뒤로",
                tint = colors.textPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = "받을 예정",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
        )
    }
}

// =============================================================
// 빈 상태
// =============================================================
@Composable
private fun EmptyState() {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        IconBubble(emoji = "⭐", background = colors.cardBg, size = 80.dp, fontSize = 40)
        Spacer(Modifier.height(20.dp))
        Text(
            text = "받을 예정 정책이 없어요",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "홈에서 마음에 드는 정책을 ⭐로 추가해보세요",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textTertiary,
        )
    }
}

// =============================================================
// 목록 — 임팩트 카드 + 정책 카드 N개
// =============================================================
@Composable
private fun FavoritesList(
    favorites: List<Policy>,
    onPolicyClick: (Policy) -> Unit,
) {
    val colors = AppTheme.colors
    val totalAmount = favorites.sumOf { it.amount }
    val count = favorites.size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        // 임팩트 카드 — 받을 예정 총액
        item {
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                ImpactCard(amount = totalAmount, count = count)
            }
            Spacer(Modifier.height(16.dp))
        }

        item {
            Text(
                text = "정책 ${count}건",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textTertiary,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 8.dp),
            )
        }

        items(favorites, key = { it.id }) { policy ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                FavoritePolicyCard(
                    policy = policy,
                    onClick = { onPolicyClick(policy) },
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ImpactCard(amount: Long, count: Int) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(colors.cardBg)
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Text(
            text = "받을 예정 금액",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textTertiary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = formatAmount(amount),
            style = MaterialTheme.typography.displayMedium,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "정책 ${count}건",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun FavoritePolicyCard(policy: Policy, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.cardBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        IconBubble(
            emoji = categoryEmoji(policy.category),
            background = categoryBubble(policy.category),
            size = 48.dp,
            fontSize = 24,
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = policy.title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatAmount(policy.amount),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        }
        if (policy.deadline.isNotBlank() && policy.daysLeft >= 0) {
            val isUrgent = policy.daysLeft <= 3
            val bg = if (isUrgent) colors.warningBg else colors.cardBorder.copy(alpha = 0.6f)
            val fg = if (isUrgent) colors.warning else colors.textSecondary
            PillAction(text = "D-${policy.daysLeft}", background = bg, contentColor = fg)
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
