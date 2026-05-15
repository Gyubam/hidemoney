package com.hiddensubsidy.app.ui.detail

import android.content.Intent
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.hiddensubsidy.app.data.model.DocumentRequirement
import com.hiddensubsidy.app.data.model.Policy
import com.hiddensubsidy.app.ui.components.IconBubble
import com.hiddensubsidy.app.ui.components.PillAction
import com.hiddensubsidy.app.ui.components.PrimaryCtaButton
import com.hiddensubsidy.app.ui.components.formatAmount
import com.hiddensubsidy.app.ui.theme.AppTheme
import com.hiddensubsidy.app.ui.theme.Bubble

private const val SIDE = 16

@Composable
fun PolicyDetailScreen(
    policy: Policy,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val colors = AppTheme.colors
    val context = LocalContext.current

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                    bottom = 120.dp,
                ),
            ) {
                item {
                    DetailTopBar(
                        onBack = onBack,
                        isFavorite = isFavorite,
                        onToggleFavorite = onToggleFavorite,
                        onShare = {
                            com.hiddensubsidy.app.util.ShareHelper.inviteFriends(context)
                        },
                    )
                }

                // Hero (제목 + 금액)
                item { HeroSection(policy = policy) }

                // 자격 충족 배지
                item {
                    Spacer(Modifier.height(16.dp))
                    Box(modifier = Modifier.padding(horizontal = SIDE.dp)) {
                        EligibilityBadge(eligible = policy.isEligible)
                    }
                }

                // 마감일 카드
                item {
                    Spacer(Modifier.height(12.dp))
                    Box(modifier = Modifier.padding(horizontal = SIDE.dp)) {
                        DeadlineCard(deadline = policy.deadline, daysLeft = policy.daysLeft)
                    }
                }

                // 한 줄 요약
                item {
                    Spacer(Modifier.height(12.dp))
                    Box(modifier = Modifier.padding(horizontal = SIDE.dp)) {
                        TextCard(
                            label = "한 줄 요약",
                            body = policy.summary,
                        )
                    }
                }

                // 자격 조건
                if (policy.eligibility.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(12.dp))
                        Box(modifier = Modifier.padding(horizontal = SIDE.dp)) {
                            EligibilityListCard(items = policy.eligibility, isEligible = policy.isEligible)
                        }
                    }
                }

                // 필요 서류
                if (policy.documents.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(12.dp))
                        Box(modifier = Modifier.padding(horizontal = SIDE.dp)) {
                            DocumentsCard(documents = policy.documents, onOpen = { url ->
                                openUrl(context, url)
                            })
                        }
                    }
                }

                // 신청 절차
                if (policy.procedure.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(12.dp))
                        Box(modifier = Modifier.padding(horizontal = SIDE.dp)) {
                            ProcedureCard(steps = policy.procedure)
                        }
                    }
                }
            }

            // Sticky 하단 CTA
            StickyApplyBar(
                policy = policy,
                onApply = { url -> openUrl(context, url) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

// =============================================================
// TopBar
// =============================================================
@Composable
private fun DetailTopBar(
    onBack: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TopBarIcon(icon = Icons.AutoMirrored.Rounded.ArrowBack, onClick = onBack)
        Spacer(Modifier.weight(1f))
        TopBarIcon(
            icon = if (isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
            onClick = onToggleFavorite,
            tint = if (isFavorite) colors.accent else colors.textPrimary,
        )
        TopBarIcon(icon = Icons.Rounded.IosShare, onClick = onShare)
    }
}

@Composable
private fun TopBarIcon(
    icon: ImageVector,
    onClick: () -> Unit = {},
    tint: androidx.compose.ui.graphics.Color = AppTheme.colors.textPrimary,
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}

// =============================================================
// Hero — 카테고리 + 제목 + 큰 금액
// =============================================================
@Composable
private fun HeroSection(policy: Policy) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 8.dp),
    ) {
        CategoryChip(text = policy.category)
        Spacer(Modifier.height(16.dp))
        Text(
            text = policy.title,
            style = MaterialTheme.typography.headlineLarge,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = formatAmount(policy.amount),
            style = MaterialTheme.typography.displaySmall,
            color = colors.textPrimary,
        )
        policy.period?.let {
            Spacer(Modifier.height(6.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textTertiary,
            )
        }
    }
}

@Composable
private fun CategoryChip(text: String) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(colors.accentBg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = colors.accentText,
        )
    }
}

// =============================================================
// 자격 충족 배지 카드
// =============================================================
@Composable
private fun EligibilityBadge(eligible: Boolean) {
    val colors = AppTheme.colors
    val bg = if (eligible) colors.accentBg else colors.cardBg
    val accentText = if (eligible) colors.accentText else colors.textTertiary
    val primaryText = if (eligible) colors.accentText else colors.textSecondary
    val bubbleBg = if (eligible) colors.accent else colors.cardBorder

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(bg)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(bubbleBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = if (eligible) colors.onAccent else colors.textTertiary,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (eligible) "자격 충족" else "지금은 자격이 아니에요",
                style = MaterialTheme.typography.titleMedium,
                color = accentText,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (eligible) "당신은 받을 수 있어요" else "조건이 충족되면 알려드릴게요",
                style = MaterialTheme.typography.bodyMedium,
                color = primaryText,
            )
        }
    }
}

// =============================================================
// 마감일 카드 — 좌측 라벨, 우측 날짜 + D-day pill
// =============================================================
@Composable
private fun DeadlineCard(deadline: String, daysLeft: Int) {
    val colors = AppTheme.colors
    val urgent = daysLeft <= 3
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.cardBg)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "마감일",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textTertiary,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = deadline,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
        )
        Spacer(Modifier.width(10.dp))
        PillAction(
            text = "D-$daysLeft",
            background = if (urgent) colors.warningBg else colors.cardBorder.copy(alpha = 0.6f),
            contentColor = if (urgent) colors.warning else colors.textSecondary,
        )
    }
}

// =============================================================
// 텍스트 섹션 카드 — 라벨 + 본문
// =============================================================
@Composable
private fun TextCard(label: String, body: String) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.cardBg)
            .padding(20.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textTertiary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textPrimary,
        )
    }
}

// =============================================================
// 자격 조건 카드 — 체크리스트
// =============================================================
@Composable
private fun EligibilityListCard(items: List<String>, isEligible: Boolean) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.cardBg)
            .padding(20.dp),
    ) {
        Text(
            text = "자격 조건",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textTertiary,
        )
        Spacer(Modifier.height(12.dp))
        items.forEachIndexed { idx, txt ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = if (isEligible) colors.accent else colors.textTertiary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = txt,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                )
            }
            if (idx != items.lastIndex) Spacer(Modifier.height(0.dp))
        }
    }
}

// =============================================================
// 필요 서류 카드 — 항목 + 발급처 PillAction
// =============================================================
@Composable
private fun DocumentsCard(
    documents: List<DocumentRequirement>,
    onOpen: (String) -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.cardBg)
            .padding(20.dp),
    ) {
        Text(
            text = "필요 서류",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textTertiary,
        )
        Spacer(Modifier.height(8.dp))
        documents.forEachIndexed { idx, doc ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = doc.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                doc.sourceUrl?.let { url ->
                    PillAction(
                        text = "발급처",
                        onClick = { onOpen(url) },
                    )
                }
            }
            if (idx != documents.lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(colors.divider)
                )
            }
        }
    }
}

// =============================================================
// 신청 절차 카드 — 번호 매긴 단계
// =============================================================
@Composable
private fun ProcedureCard(steps: List<String>) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.cardBg)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "신청 절차",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textTertiary,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${steps.size}단계",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textTertiary,
            )
        }
        Spacer(Modifier.height(12.dp))
        steps.forEachIndexed { idx, step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                StepBadge(number = idx + 1)
                Spacer(Modifier.width(14.dp))
                Text(
                    text = step,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun StepBadge(number: Int) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(colors.accentBg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = colors.accentText,
        )
    }
}

// =============================================================
// 하단 sticky CTA — "복지로에서 신청하기"
// =============================================================
@Composable
private fun StickyApplyBar(
    policy: Policy,
    onApply: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val org = policy.applicationOrg
    val url = policy.applicationUrl
    val text = if (org != null) "${org}에서 신청하기" else "신청하기"

    Column(modifier = modifier.fillMaxWidth()) {
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
                .padding(horizontal = SIDE.dp)
                .padding(top = 4.dp, bottom = 16.dp)
                .navigationBarsPadding(),
        ) {
            PrimaryCtaButton(
                text = text,
                onClick = { url?.let(onApply) },
            )
        }
    }
}

// =============================================================
// 외부 URL 열기
// =============================================================
private fun openUrl(context: android.content.Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }
}
