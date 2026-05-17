package com.hiddensubsidy.app.ui.missed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
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

/**
 * Material3 ModalBottomSheet 안 씀 — sheet drag gesture가 confirmValueChange로도 visual 멈춤 안 됨.
 * Dialog 안 직접 sheet 구현: dim background + bottom 영역 fixed. sheet 자체 swipe X.
 * 닫기: dragHandle swipe down / 헤더 X 버튼 / outside dim tap.
 */
@Composable
fun MissedSheet(
    data: HomeData,
    onDismiss: () -> Unit,
    onShare: () -> Unit = {},
    onNotifyOptIn: () -> Unit = {},
    onGrantClick: (MissedGrant) -> Unit = {},
) {
    val colors = AppTheme.colors
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
                // outside tap → onDismiss (Dialog의 dismissOnClickOutside가 처리하므로 추가 click X)
                ,
            contentAlignment = Alignment.BottomCenter,
        ) {
            // sheet 자체 — click 처리해서 outside tap 영향 안 받게
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(colors.background)
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
            ) {
                androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
                    SwipeableDragHandle(onDismiss = onDismiss)
                    MissedSheetBody(
                        data = data,
                        onShare = onShare,
                        onNotifyOptIn = onNotifyOptIn,
                        onGrantClick = onGrantClick,
                        onClose = onDismiss,
                    )
                }
            }
        }
    }
}

/**
 * dragHandle 영역만 swipe-to-close 가능하게 직접 gesture detector 부착.
 * confirmValueChange로 ModalBottomSheet의 기본 swipe close는 다 차단해놨고,
 * 여기서만 명시적으로 일정 거리 아래로 swipe 시 onDismiss 콜백 호출.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableDragHandle(onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    var dragAccum by remember { mutableFloatStateOf(0f) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (dragAccum > 80f) onDismiss()
                        dragAccum = 0f
                    },
                    onDragCancel = { dragAccum = 0f },
                ) { change, dragDelta ->
                    if (dragDelta > 0) dragAccum += dragDelta
                    change.consume()
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        BottomSheetDefaults.DragHandle(color = colors.cardBorder)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MissedSheetBody(
    data: HomeData,
    onShare: () -> Unit,
    onNotifyOptIn: () -> Unit,
    onGrantClick: (MissedGrant) -> Unit,
    onClose: () -> Unit,
) {
    val colors = AppTheme.colors
    val isEmpty = data.missedGrants.isEmpty() || data.missedTotalAmount == 0L

    // drag만 차단 (onPostScroll). fling(손 뗀 후 inertia)은 차단 안 함 — LazyColumn scroll 자연스럽게.
    // confirmValueChange로 sheet close는 이미 다 막혀있어서 fling forward돼도 닫히지 않음.
    val contentScrollBlocker = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset = available
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .nestedScroll(contentScrollBlocker),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = if (isEmpty) 32.dp else 112.dp),
        ) {
            item {
                MissedHeader(
                    amount = data.missedTotalAmount,
                    count = data.missedCount,
                    onClose = onClose,
                )
            }

            if (isEmpty) {
                item { EmptyMissed(onNotifyOptIn = onNotifyOptIn) }
            } else {
                item {
                    // fillMaxWidth로 wrap해서 touch 영역 확보 — Spacer 단독은 touch 못 받아 sheet swipe로 forward
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 20.dp, bottom = 20.dp)
                    ) {
                        ShareCard(onClick = onShare)
                    }
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
                        // 카드 + 카드 사이 간격을 한 Box로 묶기 → 전체가 touch 영역 → LazyColumn으로 forward
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 12.dp)
                        ) {
                            MissedGrantCard(
                                grant = grant,
                                onCtaClick = { onGrantClick(grant) },
                            )
                        }
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
private fun MissedHeader(amount: Long, count: Int, onClose: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
    ) {
        // X 닫기 — 헤더 안 우상단. LazyColumn item이라 스크롤하면 같이 사라짐 (sticky 아님).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material3.Icon(
                    imageVector = androidx.compose.material.icons.Icons.Rounded.Close,
                    contentDescription = "닫기",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
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
