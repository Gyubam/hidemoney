package com.hiddensubsidy.app.ui.home

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hiddensubsidy.app.data.SampleData
import com.hiddensubsidy.app.data.model.HomeData
import com.hiddensubsidy.app.data.model.Policy
import com.hiddensubsidy.app.ui.components.AnimatedAmount
import com.hiddensubsidy.app.ui.components.CardFooterLink
import com.hiddensubsidy.app.ui.components.IconBubble
import com.hiddensubsidy.app.ui.components.PillAction
import com.hiddensubsidy.app.ui.components.PrimaryCtaButton
import com.hiddensubsidy.app.ui.components.formatAmount
import com.hiddensubsidy.app.ui.theme.AppTheme
import com.hiddensubsidy.app.ui.theme.HiddenSubsidyTheme
import com.hiddensubsidy.app.ui.theme.categoryBubble
import com.hiddensubsidy.app.ui.theme.categoryEmoji

private const val SIDE_PADDING = 16

@Composable
fun HomeScreen(
    data: HomeData = SampleData.home,
    onMissedCardClick: () -> Unit = {},
    onPolicyClick: (Policy) -> Unit = {},
    onSeeAllDeadlines: () -> Unit = {},
    onSeeAllThisWeek: () -> Unit = {},
) {
    val colors = AppTheme.colors

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = 24.dp,
            ),
        ) {
            item { TopBar() }

            // === 임팩트 카드 ===
            item {
                Spacer(Modifier.height(4.dp))
                Box(modifier = Modifier.padding(horizontal = SIDE_PADDING.dp)) {
                    ImpactCard(
                        amount = data.missedTotalAmount,
                        count  = data.missedCount,
                        onClick = onMissedCardClick,
                    )
                }
            }

            // === 이번 주 받을 수 있어요 (카드형) ===
            data.thisWeekPolicies.firstOrNull()?.let { policy ->
                item {
                    Spacer(Modifier.height(12.dp))
                    Box(modifier = Modifier.padding(horizontal = SIDE_PADDING.dp)) {
                        ThisWeekCard(
                            policy = policy,
                            onPolicyClick = { onPolicyClick(policy) },
                            onSeeAll = onSeeAllThisWeek,
                        )
                    }
                }
            }

            // === 곧 마감돼요 (카드형) ===
            if (data.deadlineSoon.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    Box(modifier = Modifier.padding(horizontal = SIDE_PADDING.dp)) {
                        DeadlineCard(
                            policies = data.deadlineSoon,
                            onPolicyClick = onPolicyClick,
                            onSeeAll = onSeeAllDeadlines,
                        )
                    }
                }
            }
        }
    }
}

// =============================================================
// 상단바 (토스 메인 홈 36 톤)
// =============================================================
@Composable
private fun TopBar() {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "숨은지원금",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        TopBarIcon(icon = Icons.Rounded.NotificationsNone)
        TopBarIcon(icon = Icons.Rounded.Person)
    }
}

@Composable
private fun TopBarIcon(icon: ImageVector, onClick: () -> Unit = {}) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.textPrimary,
            modifier = Modifier.size(22.dp),
        )
    }
}

// =============================================================
// 임팩트 카드 — 핵심 후크
// =============================================================
@Composable
private fun ImpactCard(amount: Long, count: Int, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(colors.cardBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Column {
            Text(
                text = "당신이 놓친 돈",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textTertiary,
            )
            Spacer(Modifier.height(12.dp))
            AnimatedAmount(
                amount = amount,
                style  = MaterialTheme.typography.displayLarge,
                color  = colors.textPrimary,
            )
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.divider)
            )
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "지난 3년 · 미신청 ${count}건",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "내역 보기",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.accentText,
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = colors.accentText,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// =============================================================
// 이번 주 카드 — 카드 안 헤더 + 일러스트 행 + 큰 CTA
// =============================================================
@Composable
private fun ThisWeekCard(
    policy: Policy,
    onPolicyClick: () -> Unit,
    onSeeAll: () -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(colors.cardBg)
            .padding(top = 24.dp, bottom = 8.dp),
    ) {
        // 카드 헤더 (좌측 굵은 라벨 + 우측 미니 액션)
        CardHeader(
            title = "이번 주 받을 수 있어요",
            actionText = "전체",
            onAction = onSeeAll,
        )

        Spacer(Modifier.height(8.dp))

        // 일러스트 행 (대표 정책 1개)
        IllustratedPolicyRow(
            emoji = categoryEmoji(policy.category),
            bubble = categoryBubble(policy.category),
            policy = policy,
            onClick = onPolicyClick,
        )

        Spacer(Modifier.height(16.dp))

        // 큰 CTA 버튼 (토스 시그니처)
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            PrimaryCtaButton(
                text = "신청 가이드 보기",
                onClick = onPolicyClick,
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

// =============================================================
// 마감 임박 카드 — 카드 안 헤더 + 일러스트 행 N개 + footer 링크
// =============================================================
@Composable
private fun DeadlineCard(
    policies: List<Policy>,
    onPolicyClick: (Policy) -> Unit,
    onSeeAll: () -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(colors.cardBg)
            .padding(top = 24.dp),
    ) {
        CardHeader(title = "곧 마감돼요")

        Spacer(Modifier.height(4.dp))

        policies.forEachIndexed { idx, p ->
            IllustratedPolicyRow(
                emoji = categoryEmoji(p.category),
                bubble = categoryBubble(p.category),
                policy = p,
                onClick = { onPolicyClick(p) },
            )
            if (idx != policies.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(start = 80.dp, end = 20.dp)
                        .background(colors.divider)
                )
            }
        }

        CardFooterLink(text = "마감 임박 전체 보기", onClick = onSeeAll)
    }
}

// =============================================================
// 공통 — 카드 헤더 (좌측 라벨 + 우측 미니 액션)
// =============================================================
@Composable
private fun CardHeader(
    title: String,
    actionText: String? = null,
    onAction: () -> Unit = {},
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        if (actionText != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textTertiary,
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

// =============================================================
// 일러스트 정책 행 — 토스의 대표 행 패턴
// 좌측 컬러 버블 + 제목/금액 + 우측 D-day 알약
// =============================================================
@Composable
private fun IllustratedPolicyRow(
    emoji: String,
    bubble: androidx.compose.ui.graphics.Color,
    policy: Policy,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        IconBubble(emoji = emoji, background = bubble, size = 48.dp, fontSize = 24)
        Spacer(Modifier.width(16.dp))
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
        DDayPill(daysLeft = policy.daysLeft)
    }
}

@Composable
private fun DDayPill(daysLeft: Int) {
    val colors = AppTheme.colors
    val isUrgent = daysLeft <= 3
    val bg   = if (isUrgent) colors.warningBg else colors.cardBorder.copy(alpha = 0.6f)
    val text = if (isUrgent) colors.warning else colors.textSecondary
    PillAction(
        text = "D-$daysLeft",
        background = bg,
        contentColor = text,
    )
}

// =============================================================
// 프리뷰
// =============================================================
@Preview(showBackground = true, widthDp = 360, heightDp = 880)
@Composable
private fun HomeScreenPreview() {
    HiddenSubsidyTheme {
        HomeScreen()
    }
}