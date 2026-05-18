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

/** 정책 진척 단계 — 받을 예정 / 신청한 / 받은 */
enum class PolicyStatusKind(
    val title: String,
    val impactLabel: String,
    val emptyEmoji: String,
    val emptyTitle: String,
    val emptyBody: String,
) {
    Saved(
        title = "받을 예정",
        impactLabel = "받을 예정 금액",
        emptyEmoji = "⭐",
        emptyTitle = "받을 예정 정책이 없어요",
        emptyBody = "홈에서 마음에 드는 정책을 ⭐로 추가해보세요",
    ),
    Applied(
        title = "신청한 지원금",
        impactLabel = "신청한 금액",
        emptyEmoji = "📝",
        emptyTitle = "신청한 정책이 없어요",
        emptyBody = "정책 상세에서 '신청했어요'를 눌러보세요",
    ),
    Received(
        title = "받은 지원금",
        impactLabel = "받은 금액",
        emptyEmoji = "✅",
        emptyTitle = "받은 정책이 없어요",
        emptyBody = "정책 상세에서 '받았어요'를 눌러보세요",
    ),
    Dismissed(
        title = "관심 없음 정책",
        impactLabel = "숨긴 금액 합계",
        emptyEmoji = "🙈",
        emptyTitle = "숨긴 정책이 없어요",
        emptyBody = "정책 상세에서 '이 정책은 관심 없어요'를 누르면 여기로 들어와요",
    ),
}

@Composable
fun FavoritesScreen(
    favorites: List<Policy>,
    onBack: () -> Unit,
    onPolicyClick: (Policy) -> Unit,
    kind: PolicyStatusKind = PolicyStatusKind.Saved,
) {
    val colors = AppTheme.colors

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(title = kind.title, onBack = onBack)

            if (favorites.isEmpty()) {
                EmptyState(kind = kind)
            } else {
                FavoritesList(
                    favorites = favorites,
                    onPolicyClick = onPolicyClick,
                    kind = kind,
                )
            }
        }
    }
}

// =============================================================
// 상단 바 — 좌측 ← + 동적 제목
// =============================================================
@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
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
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
        )
    }
}

// =============================================================
// 빈 상태
// =============================================================
@Composable
private fun EmptyState(kind: PolicyStatusKind) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        IconBubble(emoji = kind.emptyEmoji, background = colors.cardBg, size = 80.dp, fontSize = 40)
        Spacer(Modifier.height(20.dp))
        Text(
            text = kind.emptyTitle,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = kind.emptyBody,
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
    kind: PolicyStatusKind,
) {
    val colors = AppTheme.colors
    val totalAmount = favorites.sumOf { it.amount }
    val count = favorites.size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        // 임팩트 카드 — 단계별 총액 라벨
        item {
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                ImpactCard(amount = totalAmount, count = count, label = kind.impactLabel)
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
private fun ImpactCard(amount: Long, count: Int, label: String) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(colors.cardBg)
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Text(
            text = label,
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
