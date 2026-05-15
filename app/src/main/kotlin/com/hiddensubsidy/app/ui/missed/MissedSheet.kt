package com.hiddensubsidy.app.ui.missed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiddensubsidy.app.data.model.HomeData
import com.hiddensubsidy.app.data.model.MissedGrant
import com.hiddensubsidy.app.ui.components.AnimatedAmount
import com.hiddensubsidy.app.ui.components.IconBubble
import com.hiddensubsidy.app.ui.components.PrimaryCtaButton
import com.hiddensubsidy.app.ui.components.formatAmount
import com.hiddensubsidy.app.ui.theme.AppTheme
import com.hiddensubsidy.app.ui.theme.Bubble

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissedSheet(
    data: HomeData,
    onDismiss: () -> Unit,
    onShare: () -> Unit = {},
    onNotifyOptIn: () -> Unit = {},
    onGrantClick: (MissedGrant) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = AppTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        contentColor = colors.textPrimary,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.cardBorder) },
    ) {
        MissedSheetBody(
            data = data,
            onShare = onShare,
            onNotifyOptIn = onNotifyOptIn,
            onGrantClick = onGrantClick,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MissedSheetBody(
    data: HomeData,
    onShare: () -> Unit,
    onNotifyOptIn: () -> Unit,
    onGrantClick: (MissedGrant) -> Unit,
) {
    val colors = AppTheme.colors
    val isEmpty = data.missedGrants.isEmpty() || data.missedTotalAmount == 0L

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = if (isEmpty) 32.dp else 112.dp),
        ) {
            item {
                MissedHeader(amount = data.missedTotalAmount, count = data.missedCount)
            }

            if (isEmpty) {
                item { EmptyMissed(onNotifyOptIn = onNotifyOptIn) }
            } else {
                item {
                    Spacer(Modifier.height(20.dp))
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ShareCard(onClick = onShare)
                    }
                    Spacer(Modifier.height(20.dp))
                }

                val grouped = data.missedGrants
                    .groupBy { it.year }
                    .toSortedMap(compareByDescending { it })

                grouped.forEach { (year, grants) ->
                    val yearTotal = grants.sumOf { it.amount }
                    stickyHeader(key = "year-$year") {
                        YearHeader(year = year, total = yearTotal)
                    }
                    items(grants, key = { it.id }) { grant ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            MissedGrantCard(
                                grant = grant,
                                onCtaClick = { onGrantClick(grant) },
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }

        if (!isEmpty) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(
                            Brush.verticalGradient(
                                0f to colors.background.copy(alpha = 0f),
                                1f to colors.background,
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.background)
                        .padding(horizontal = 16.dp)
                        .padding(top = 4.dp, bottom = 16.dp)
                        .navigationBarsPadding(),
                ) {
                    PrimaryCtaButton(
                        text = "🔔  올해는 놓치지 않을게요",
                        onClick = onNotifyOptIn,
                    )
                }
            }
        }
    }
}

// =============================================================
// 시트 헤더 — "당신이 놓친 돈 / 2,400,000원 / 12건 · 최근 3년"
// =============================================================
@Composable
private fun MissedHeader(amount: Long, count: Int) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 4.dp),
    ) {
        Text(
            text = "당신이 놓친 돈",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textTertiary,
        )
        Spacer(Modifier.height(8.dp))
        AnimatedAmount(
            amount = amount,
            style = MaterialTheme.typography.displayMedium,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "${count}건  ·  최근 3년",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
    }
}

// =============================================================
// 공유 카드 — 시트 상단 sticky 자리에 노출 (스크롤 안 해도 보임)
// =============================================================
@Composable
private fun ShareCard(onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.cardBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBubble(emoji = "📤", background = Bubble.Mint, size = 44.dp, fontSize = 22)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "친구에게 공유하기",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "카카오톡 · 인스타로 보내기",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(20.dp),
        )
    }
}

// =============================================================
// 연도 헤더 (sticky)
// =============================================================
@Composable
private fun YearHeader(year: Int, total: Long) {
    val colors = AppTheme.colors
    Column(modifier = Modifier
        .fillMaxWidth()
        .background(colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${year}년",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formatAmount(total),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textSecondary,
            )
        }
    }
}

// =============================================================
// 미수령 정책 카드 — 탭하면 펼침
// =============================================================
@Composable
private fun MissedGrantCard(
    grant: MissedGrant,
    onCtaClick: () -> Unit,
) {
    val colors = AppTheme.colors
    var expanded by rememberSaveable(grant.id) { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.cardBg)
            .clickable { expanded = !expanded }
            .padding(20.dp),
    ) {
        Text(
            text = grant.title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = formatAmount(grant.amount),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "${grant.eligibleFrom} · 자격 충족",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textTertiary,
        )

        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.divider))
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "요약",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textTertiary,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = grant.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.accentBg)
                        .clickable(onClick = onCtaClick)
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "지금이라도 알아보기",
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

        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (expanded) "접기" else "자세히 보기",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textTertiary,
            )
            Spacer(Modifier.width(2.dp))
            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// =============================================================
// 빈 상태 (놓친 돈 0원) — 긍정 톤 전환
// =============================================================
@Composable
private fun EmptyMissed(onNotifyOptIn: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 40.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "🎉",
            style = TextStyle(fontSize = 56.sp),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "완벽해요!",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "받을 수 있는 건 다 받으셨네요",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(28.dp))
        PrimaryCtaButton(
            text = "🔔  새 지원금 알림 받기",
            onClick = onNotifyOptIn,
        )
    }
}
