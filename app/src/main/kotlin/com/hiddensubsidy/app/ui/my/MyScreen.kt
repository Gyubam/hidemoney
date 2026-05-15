package com.hiddensubsidy.app.ui.my

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hiddensubsidy.app.data.SampleData
import com.hiddensubsidy.app.data.model.MySummary
import com.hiddensubsidy.app.data.model.UserProfile
import com.hiddensubsidy.app.ui.components.IconBubble
import com.hiddensubsidy.app.ui.components.formatAmount
import com.hiddensubsidy.app.ui.theme.AppTheme
import com.hiddensubsidy.app.ui.theme.Bubble

private const val SIDE = 16

@Composable
fun MyScreen(
    profile: UserProfile,
    summary: MySummary = SampleData.mySummary,
    onEditProfile: () -> Unit = {},
    onNotificationSettings: () -> Unit = {},
    onInviteFriends: () -> Unit = {},
    onPrivacyPolicy: () -> Unit = {},
    onFeedback: () -> Unit = {},
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
            item { TopBarTitle() }

            // ===== 프로필 카드 =====
            item {
                Box(modifier = Modifier.padding(horizontal = SIDE.dp)) {
                    ProfileCard(profile = profile, onEdit = onEditProfile)
                }
                Spacer(Modifier.height(24.dp))
            }

            // ===== 내 지원금 =====
            item {
                SectionHeader(text = "내 지원금")
                Spacer(Modifier.height(4.dp))
            }
            item {
                Box(modifier = Modifier.padding(horizontal = SIDE.dp)) {
                    GrantSummaryCard(
                        emoji = "⭐",
                        bubble = Bubble.Lemon,
                        label = "받을 예정",
                        count = summary.savedCount,
                        amount = summary.savedAmount,
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
            item {
                Box(modifier = Modifier.padding(horizontal = SIDE.dp)) {
                    GrantSummaryCard(
                        emoji = "📝",
                        bubble = Bubble.Sky,
                        label = "신청한 지원금",
                        count = summary.appliedCount,
                        amount = summary.appliedAmount,
                        actionLabel = "수령 확인",
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
            item {
                Box(modifier = Modifier.padding(horizontal = SIDE.dp)) {
                    ReceivedCard(
                        count = summary.receivedCount,
                        amount = summary.receivedAmount,
                    )
                }
                Spacer(Modifier.height(28.dp))
            }

            // ===== 설정 =====
            item {
                SectionHeader(text = "설정")
                Spacer(Modifier.height(4.dp))
            }
            item {
                Box(modifier = Modifier.padding(horizontal = SIDE.dp)) {
                    SettingsCard(
                        items = listOf(
                            SettingItem("🔔", "알림 설정", onNotificationSettings),
                            SettingItem("👨‍👩‍👧", "가족 진단", onInviteFriends, badge = "프리미엄"),
                            SettingItem("💌", "친구 초대", onInviteFriends),
                            SettingItem("📋", "개인정보 처리방침", onPrivacyPolicy),
                            SettingItem("✉️", "의견 보내기", onFeedback),
                        ),
                    )
                }
            }
        }
    }
}

// =============================================================
// 상단 타이틀
// =============================================================
@Composable
private fun TopBarTitle() {
    val colors = AppTheme.colors
    Text(
        text = "마이",
        style = MaterialTheme.typography.titleLarge,
        color = colors.textPrimary,
        modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 8.dp),
    )
}

// =============================================================
// 섹션 헤더
// =============================================================
@Composable
private fun SectionHeader(text: String) {
    val colors = AppTheme.colors
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = colors.textTertiary,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 8.dp),
    )
}

// =============================================================
// 프로필 카드
// =============================================================
@Composable
private fun ProfileCard(profile: UserProfile, onEdit: () -> Unit) {
    val colors = AppTheme.colors
    val percent = (profile.completeness * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.cardBg)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(emoji = "🙂", background = Bubble.Mint, size = 56.dp, fontSize = 28)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.summary,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "프로필 정확도 ${percent}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textTertiary,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        ProgressBar(value = profile.completeness)
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.accentBg)
                .clickable(onClick = onEdit)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "프로필 더 채우기",
                style = MaterialTheme.typography.titleSmall,
                color = colors.accentText,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.accentText,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun ProgressBar(value: Float) {
    val colors = AppTheme.colors
    val animated by animateFloatAsState(
        targetValue = value.coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "progress",
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(colors.background),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(colors.accent)
        )
    }
}

// =============================================================
// 내 지원금 — 카드 1 (받을 예정 / 신청한)
// =============================================================
@Composable
private fun GrantSummaryCard(
    emoji: String,
    bubble: androidx.compose.ui.graphics.Color,
    label: String,
    count: Int,
    amount: Long,
    actionLabel: String? = null,
    onClick: () -> Unit = {},
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.cardBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBubble(emoji = emoji, background = bubble, size = 48.dp, fontSize = 24)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${count}건",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textTertiary,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = formatAmount(amount),
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
            )
        }
        if (actionLabel != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.accentBg)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.accentText,
                )
            }
        }
    }
}

// =============================================================
// 받은 지원금 (게이미피케이션 - 강조 카드)
// =============================================================
@Composable
private fun ReceivedCard(count: Int, amount: Long) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.accentBg)
            .padding(horizontal = 22.dp, vertical = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "✅ 받은 지원금",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.accentText,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${count}건",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.accentText,
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = formatAmount(amount),
                style = MaterialTheme.typography.displaySmall,
                color = colors.accentText,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "지금까지 누적 수령",
                style = MaterialTheme.typography.bodySmall,
                color = colors.accentText,
            )
        }
    }
}

// =============================================================
// 설정 리스트 카드
// =============================================================
private data class SettingItem(
    val emoji: String,
    val label: String,
    val onClick: () -> Unit,
    val badge: String? = null,
)

@Composable
private fun SettingsCard(items: List<SettingItem>) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.cardBg),
    ) {
        items.forEachIndexed { idx, item ->
            SettingRow(item = item)
            if (idx != items.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(start = 60.dp, end = 18.dp)
                        .background(colors.divider)
                )
            }
        }
    }
}

@Composable
private fun SettingRow(item: SettingItem) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = item.onClick)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.emoji,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        if (item.badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.accentBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = item.badge,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.accentText,
                )
            }
            Spacer(Modifier.width(8.dp))
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(20.dp),
        )
    }
}
