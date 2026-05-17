package com.hiddensubsidy.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hiddensubsidy.app.data.PolicyRelevance
import com.hiddensubsidy.app.data.model.Policy
import com.hiddensubsidy.app.data.model.UserProfile
import com.hiddensubsidy.app.ui.components.IconBubble
import com.hiddensubsidy.app.ui.components.PillAction
import com.hiddensubsidy.app.ui.components.formatAmount
import com.hiddensubsidy.app.ui.theme.AppTheme
import com.hiddensubsidy.app.ui.theme.categoryBubble
import com.hiddensubsidy.app.ui.theme.categoryEmoji

private val CATEGORIES = listOf("주거", "출산", "생활", "교육", "청년", "창업")

@Composable
fun SearchScreen(
    allPolicies: List<Policy>,
    profile: UserProfile,
    onBack: () -> Unit,
    onPolicyClick: (Policy) -> Unit,
) {
    val colors = AppTheme.colors
    var query by rememberSaveableString()
    var selectedCategory by rememberSaveableNullableString()
    var eligibleOnly by remember { mutableStateOf(true) }

    // 카테고리 + 검색 키워드 먼저 적용 (eligibleOnly 무관 base)
    val baseFiltered = remember(allPolicies, query, selectedCategory) {
        var list = allPolicies
        if (selectedCategory != null) list = list.filter { it.category == selectedCategory }
        if (query.isNotBlank()) {
            val q = query.trim()
            list = list.filter { it.title.contains(q) || it.summary.contains(q) || (it.applicationOrg?.contains(q) ?: false) }
        }
        list
    }
    // "자격 충족" = 홈 missed와 같은 정의 (PolicyRelevance — 자격+region+gender+business 키워드 다 통과)
    val eligibleCount = remember(baseFiltered, profile) {
        baseFiltered.count { PolicyRelevance.isEligibleForUser(it, profile) }
    }
    val totalCount = baseFiltered.size
    val filtered = remember(baseFiltered, eligibleOnly, profile) {
        val list = if (eligibleOnly) baseFiltered.filter { PolicyRelevance.isEligibleForUser(it, profile) } else baseFiltered
        list.sortedByDescending { it.amount }
    }

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchTopBar(
                query = query,
                onQueryChange = { query = it },
                onBack = onBack,
            )

            // 카테고리 chip filter
            CategoryChips(
                selected = selectedCategory,
                onSelect = { selectedCategory = if (selectedCategory == it) null else it },
            )

            // 자격 충족 토글 — 양쪽 카운트 표시
            EligibleToggle(
                eligibleOnly = eligibleOnly,
                onChange = { eligibleOnly = it },
                eligibleCount = eligibleCount,
                totalCount = totalCount,
            )

            // 결과 리스트
            if (filtered.isEmpty()) {
                EmptyState(query = query)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    items(filtered, key = { it.id }) { policy ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                            PolicyResultCard(policy = policy, onClick = { onPolicyClick(policy) })
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberSaveableString(): androidx.compose.runtime.MutableState<String> =
    androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }

@Composable
private fun rememberSaveableNullableString(): androidx.compose.runtime.MutableState<String?> =
    androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }

// =============================================================
// 상단 검색바 — 좌측 ← + 검색 입력
// =============================================================
@Composable
private fun SearchTopBar(query: String, onQueryChange: (String) -> Unit, onBack: () -> Unit) {
    val colors = AppTheme.colors
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topInset)
            .padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 8.dp),
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
        Row(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.cardBg)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(colors.accent),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (query.isEmpty()) {
                    Text(
                        text = "정책명, 키워드 검색",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textTertiary,
                    )
                }
            }
            if (query.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { onQueryChange("") },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "지우기",
                        tint = colors.textTertiary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

// =============================================================
// 카테고리 chip filter
// =============================================================
@Composable
private fun CategoryChips(selected: String?, onSelect: (String) -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CATEGORIES.forEach { cat ->
            val isSelected = selected == cat
            val bg = if (isSelected) colors.accent else colors.cardBg
            val fg = if (isSelected) colors.onAccent else colors.textSecondary
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(bg)
                    .clickable { onSelect(cat) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(
                    text = cat,
                    style = MaterialTheme.typography.labelLarge,
                    color = fg,
                )
            }
        }
    }
}

// =============================================================
// 자격 충족 토글 + 결과 카운트
// =============================================================
@Composable
private fun EligibleToggle(
    eligibleOnly: Boolean,
    onChange: (Boolean) -> Unit,
    eligibleCount: Int,
    totalCount: Int,
) {
    val colors = AppTheme.colors
    val currentCount = if (eligibleOnly) eligibleCount else totalCount
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${currentCount}건",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
            Text(
                text = if (eligibleOnly) "전체 ${totalCount}건 중 자격 충족" else "자격 충족 ${eligibleCount}건 · 전체",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(if (eligibleOnly) colors.accentBg else colors.cardBg)
                .clickable { onChange(!eligibleOnly) }
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = if (eligibleOnly) "✓ 자격 충족만" else "전체 보기",
                style = MaterialTheme.typography.labelLarge,
                color = if (eligibleOnly) colors.accentText else colors.textSecondary,
            )
        }
    }
}

// =============================================================
// 결과 카드 — IllustratedPolicyRow 패턴
// =============================================================
@Composable
private fun PolicyResultCard(policy: Policy, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.cardBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        IconBubble(
            emoji = categoryEmoji(policy.category),
            background = categoryBubble(policy.category),
            size = 44.dp,
            fontSize = 22,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = policy.title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (policy.amount > 0) {
                    Text(
                        text = formatAmount(policy.amount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = policy.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }
        if (policy.deadline.isNotBlank() && policy.daysLeft in 0..30) {
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

// =============================================================
// 빈 결과
// =============================================================
@Composable
private fun EmptyState(query: String) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        IconBubble(emoji = "🔍", background = colors.cardBg, size = 64.dp, fontSize = 32)
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (query.isBlank()) "조건에 맞는 정책이 없어요" else "'${query}' 검색 결과가 없어요",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "다른 키워드나 카테고리를 시도해보세요",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textTertiary,
        )
    }
}
