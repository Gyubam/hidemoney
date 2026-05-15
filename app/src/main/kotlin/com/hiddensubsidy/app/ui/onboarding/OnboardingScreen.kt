package com.hiddensubsidy.app.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.hiddensubsidy.app.data.model.Occupations
import com.hiddensubsidy.app.data.model.Regions
import com.hiddensubsidy.app.data.model.UserProfile
import com.hiddensubsidy.app.ui.components.AnimatedAmount
import com.hiddensubsidy.app.ui.components.IconBubble
import com.hiddensubsidy.app.ui.components.PrimaryCtaButton
import com.hiddensubsidy.app.ui.theme.AppTheme
import com.hiddensubsidy.app.ui.theme.Bubble
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: (UserProfile) -> Unit,
) {
    val colors = AppTheme.colors
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    var profile by remember { mutableStateOf(UserProfile()) }

    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false,
            ) { page ->
                when (page) {
                    0 -> HookPage(
                        topInset = statusTop,
                        onStart = { scope.launch { pagerState.animateScrollToPage(1) } },
                    )
                    1 -> HowItWorksPage(
                        topInset = statusTop,
                        onBack = { scope.launch { pagerState.animateScrollToPage(0) } },
                        onNext = { scope.launch { pagerState.animateScrollToPage(2) } },
                    )
                    2 -> ProfileInputPage(
                        topInset = statusTop,
                        profile = profile,
                        onChange = { profile = it },
                        onBack = { scope.launch { pagerState.animateScrollToPage(1) } },
                        onSubmit = { onComplete(profile) },
                    )
                }
            }

            // Page indicator (sticky bottom, 위 페이지의 CTA 위에 자연스럽게 겹치지 않게 — 작게 표시)
            PageDots(
                count = 3,
                current = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = statusTop + 18.dp),
            )
        }
    }
}

// =============================================================
// Page 1 — 후크
// =============================================================
@Composable
private fun HookPage(topInset: androidx.compose.ui.unit.Dp, onStart: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topInset)
            .padding(horizontal = 28.dp)
            .navigationBarsPadding(),
    ) {
        Spacer(Modifier.height(60.dp))

        // 중앙 임팩트 블록
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = "당신은 정부 지원금",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(8.dp))
            AnimatedAmount(
                amount = 2_400_000L,
                style = MaterialTheme.typography.displayLarge,
                color = colors.textPrimary,
                durationMillis = 1400,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "놓치고 있을지도 몰라요",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(28.dp))
            Box(
                modifier = Modifier
                    .height(2.dp)
                    .width(36.dp)
                    .background(colors.accent, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "30초면 알 수 있어요",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textTertiary,
            )
        }

        Spacer(Modifier.height(24.dp))
        PrimaryCtaButton(text = "시작하기", onClick = onStart)
        Spacer(Modifier.height(20.dp))
    }
}

// =============================================================
// Page 2 — 작동 원리 (3 step cards)
// =============================================================
@Composable
private fun HowItWorksPage(
    topInset: androidx.compose.ui.unit.Dp,
    onBack: () -> Unit,
    onNext: () -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topInset)
            .navigationBarsPadding(),
    ) {
        BackBar(onBack = onBack)
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "어떻게 찾아드리냐면요",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(28.dp))
            StepRow(
                number = 1,
                bubble = Bubble.Sky,
                title = "매일 자동으로 모아요",
                body = "정부24·복지로의 모든 지원금을 새벽마다 자동 수집해요.",
            )
            Spacer(Modifier.height(14.dp))
            StepRow(
                number = 2,
                bubble = Bubble.Mint,
                title = "당신 상황에 맞춰요",
                body = "나이·지역·생활 상황에 맞는 것만 골라드려요.",
            )
            Spacer(Modifier.height(14.dp))
            StepRow(
                number = 3,
                bubble = Bubble.Lemon,
                title = "신청까지 안내해요",
                body = "필요한 서류와 신청 단계를 친절하게 알려드려요.",
            )
            Spacer(Modifier.height(24.dp))
        }
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            PrimaryCtaButton(text = "다음", onClick = onNext)
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun StepRow(
    number: Int,
    bubble: androidx.compose.ui.graphics.Color,
    title: String,
    body: String,
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.cardBg)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(bubble),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        }
    }
}

// =============================================================
// Page 3 — 정보 입력
// =============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileInputPage(
    topInset: androidx.compose.ui.unit.Dp,
    profile: UserProfile,
    onChange: (UserProfile) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
) {
    val colors = AppTheme.colors
    var openSheet by remember { mutableStateOf<PickerSheet?>(null) }
    val ready = profile.age != null && profile.region != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topInset)
            .navigationBarsPadding(),
    ) {
        BackBar(onBack = onBack)
        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = "딱 두 가지만\n알려주세요",
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(28.dp))

            FieldLabel("나이")
            Spacer(Modifier.height(6.dp))
            PickerField(
                value = profile.age?.let { "${it}세" },
                placeholder = "나이 선택",
                onClick = { openSheet = PickerSheet.Age },
            )

            Spacer(Modifier.height(20.dp))

            FieldLabel("사는 지역")
            Spacer(Modifier.height(6.dp))
            PickerField(
                value = profile.region,
                placeholder = "지역 선택",
                onClick = { openSheet = PickerSheet.Region },
            )

            Spacer(Modifier.height(36.dp))

            // 점선 divider
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.divider))

            Spacer(Modifier.height(28.dp))
            Text(
                text = "더 정확하게 찾고 싶다면",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "선택사항 · 나중에 채워도 돼요",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textTertiary,
            )
            Spacer(Modifier.height(16.dp))

            OptionPickerField(
                label = "직업",
                value = profile.occupation,
                placeholder = "선택 안 함",
                onClick = { openSheet = PickerSheet.Occupation },
            )
            Spacer(Modifier.height(12.dp))
            ToggleRow(
                label = "결혼",
                value = profile.married,
                onChange = { onChange(profile.copy(married = it)) },
            )
            Spacer(Modifier.height(12.dp))
            ToggleRow(
                label = "자녀 있음",
                value = profile.hasChildren,
                onChange = { onChange(profile.copy(hasChildren = it)) },
            )
            Spacer(Modifier.height(40.dp))
        }

        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            CtaWithReadiness(
                text = "내가 받을 지원금 보기",
                enabled = ready,
                onClick = onSubmit,
            )
        }
        Spacer(Modifier.height(20.dp))
    }

    // === Bottom sheet pickers ===
    when (openSheet) {
        PickerSheet.Age -> AgeSheet(
            current = profile.age,
            onPick = { onChange(profile.copy(age = it)); openSheet = null },
            onDismiss = { openSheet = null },
        )
        PickerSheet.Region -> RegionSheet(
            current = profile.region,
            onPick = { onChange(profile.copy(region = it)); openSheet = null },
            onDismiss = { openSheet = null },
        )
        PickerSheet.Occupation -> OccupationSheet(
            current = profile.occupation,
            onPick = { onChange(profile.copy(occupation = it)); openSheet = null },
            onDismiss = { openSheet = null },
        )
        null -> {}
    }
}

private enum class PickerSheet { Age, Region, Occupation }

// =============================================================
// 입력 필드들
// =============================================================
@Composable
private fun FieldLabel(text: String) {
    val colors = AppTheme.colors
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = colors.textSecondary,
    )
}

@Composable
private fun PickerField(
    value: String?,
    placeholder: String,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.cardBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = value ?: placeholder,
            style = MaterialTheme.typography.titleMedium,
            color = if (value == null) colors.textTertiary else colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun OptionPickerField(
    label: String,
    value: String?,
    placeholder: String,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.cardBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value ?: placeholder,
            style = MaterialTheme.typography.bodyMedium,
            color = if (value == null) colors.textTertiary else colors.textPrimary,
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowDown,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun ToggleRow(
    label: String,
    value: Boolean?,
    onChange: (Boolean?) -> Unit,
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.cardBg)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        ToggleChip(text = "예", selected = value == true) {
            onChange(if (value == true) null else true)
        }
        Spacer(Modifier.width(6.dp))
        ToggleChip(text = "아니오", selected = value == false) {
            onChange(if (value == false) null else false)
        }
    }
}

@Composable
private fun ToggleChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val bg = if (selected) colors.accent else colors.background
    val fg = if (selected) colors.onAccent else colors.textSecondary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = fg,
        )
    }
}

@Composable
private fun CtaWithReadiness(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val bg = if (enabled) colors.accent else colors.cardBorder
    val fg = if (enabled) colors.onAccent else colors.textTertiary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = fg,
        )
    }
}

// =============================================================
// Back bar (모든 페이지 공통 — 좌측 화살표만)
// =============================================================
@Composable
private fun BackBar(onBack: () -> Unit) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .size(44.dp)
            .padding(start = 4.dp)
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
}

// =============================================================
// 페이지 인디케이터
// =============================================================
@Composable
private fun PageDots(
    count: Int,
    current: Int,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(count) { i ->
            val w by animateDpAsState(
                targetValue = if (i == current) 18.dp else 6.dp,
                animationSpec = tween(220),
                label = "dot-width",
            )
            Box(
                modifier = Modifier
                    .width(w)
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (i == current) colors.accent else colors.cardBorder)
            )
        }
    }
}

// =============================================================
// Picker sheets
// =============================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgeSheet(current: Int?, onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    val ages = (18..80).toList()
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = colors.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.cardBorder) },
    ) {
        SheetTitle("나이 선택")
        LazyColumn(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.65f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            items(ages, key = { it }) { age ->
                PickerRow(
                    text = "${age}세",
                    selected = age == current,
                    onClick = { onPick(age) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegionSheet(current: String?, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = colors.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.cardBorder) },
    ) {
        SheetTitle("사는 지역")
        LazyColumn(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            items(Regions.all, key = { it }) { r ->
                PickerRow(
                    text = r,
                    selected = r == current,
                    onClick = { onPick(r) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OccupationSheet(current: String?, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = colors.background,
        dragHandle = { BottomSheetDefaults.DragHandle(color = colors.cardBorder) },
    ) {
        SheetTitle("직업")
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).navigationBarsPadding()) {
            Occupations.all.forEach { o ->
                PickerRow(
                    text = o,
                    selected = o == current,
                    onClick = { onPick(o) },
                )
            }
        }
    }
}

@Composable
private fun SheetTitle(text: String) {
    val colors = AppTheme.colors
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = colors.textPrimary,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
    )
}

@Composable
private fun PickerRow(text: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) colors.accentText else colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        AnimatedVisibility(visible = selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
