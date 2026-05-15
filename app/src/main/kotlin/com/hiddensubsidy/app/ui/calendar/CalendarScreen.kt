package com.hiddensubsidy.app.ui.calendar

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hiddensubsidy.app.data.SampleData
import com.hiddensubsidy.app.data.model.CalendarEventKind
import com.hiddensubsidy.app.data.model.PolicyCalendarEvent
import com.hiddensubsidy.app.ui.theme.AppTheme
import com.hiddensubsidy.app.ui.theme.Brand
import com.hiddensubsidy.app.ui.theme.Gray
import com.hiddensubsidy.app.ui.theme.Semantic
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarScreen(
    events: List<PolicyCalendarEvent> = SampleData.calendarEvents,
    today: LocalDate = LocalDate.of(2026, 5, 15),
    onPolicyClick: (String) -> Unit = {},
) {
    val colors = AppTheme.colors
    var displayedMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(today) }

    val eventsByDate = remember(events) { events.groupBy { it.date } }
    val selectedEvents: List<PolicyCalendarEvent> =
        selectedDate?.let { eventsByDate[it.toString()] } ?: emptyList()

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = 24.dp,
            ),
        ) {
            item {
                MonthHeader(
                    month = displayedMonth,
                    onPrev = { displayedMonth = displayedMonth.minusMonths(1) },
                    onNext = { displayedMonth = displayedMonth.plusMonths(1) },
                    onToday = {
                        displayedMonth = YearMonth.from(today)
                        selectedDate = today
                    },
                    isOnCurrent = displayedMonth == YearMonth.from(today),
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    CalendarBlock(
                        month = displayedMonth,
                        eventsByDate = eventsByDate,
                        today = today,
                        selected = selectedDate,
                        onSelect = { d -> selectedDate = d },
                    )
                }
            }

            // dot 범례
            item {
                Spacer(Modifier.height(12.dp))
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    LegendBar()
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                SelectedDaySectionTitle(date = selectedDate, count = selectedEvents.size)
                Spacer(Modifier.height(8.dp))
            }

            if (selectedDate == null) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        EmptyHint(text = "날짜를 선택하면 일정이 보여요")
                    }
                }
            } else if (selectedEvents.isEmpty()) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        EmptyHint(text = "이 날엔 예정된 일정이 없어요")
                    }
                }
            } else {
                items(selectedEvents, key = { "${it.date}-${it.policyId}-${it.kindName}" }) { e ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        CalendarEventCard(
                            event = e,
                            onClick = { onPolicyClick(e.policyId) },
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

// =============================================================
// 월 헤더 — 화살표 + 오늘로
// =============================================================
@Composable
private fun MonthHeader(
    month: YearMonth,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    isOnCurrent: Boolean,
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${month.year}년 ${month.monthValue}월",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        if (!isOnCurrent) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.accentBg)
                    .clickable(onClick = onToday)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "오늘로",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.accentText,
                )
            }
            Spacer(Modifier.width(8.dp))
        }
        HeaderIconBtn(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, onPrev)
        HeaderIconBtn(Icons.AutoMirrored.Rounded.KeyboardArrowRight, onNext)
    }
}

@Composable
private fun HeaderIconBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .size(36.dp)
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
// 캘린더 그리드 (흰 카드로 감싼)
// =============================================================
@Composable
private fun CalendarBlock(
    month: YearMonth,
    eventsByDate: Map<String, List<PolicyCalendarEvent>>,
    today: LocalDate,
    selected: LocalDate?,
    onSelect: (LocalDate) -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.cardBg)
            .padding(horizontal = 8.dp, vertical = 12.dp),
    ) {
        WeekdayHeader()
        Spacer(Modifier.height(4.dp))
        CalendarGrid(
            month = month,
            eventsByDate = eventsByDate,
            today = today,
            selected = selected,
            onSelect = onSelect,
        )
    }
}

@Composable
private fun WeekdayHeader() {
    val colors = AppTheme.colors
    val days = listOf("일", "월", "화", "수", "목", "금", "토")
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        days.forEachIndexed { idx, d ->
            val tint = when (idx) {
                0 -> Semantic.Warning.copy(alpha = 0.85f)
                6 -> Color(0xFF2A77E0).copy(alpha = 0.8f)
                else -> colors.textTertiary
            }
            Text(
                text = d,
                style = MaterialTheme.typography.labelMedium,
                color = tint,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    eventsByDate: Map<String, List<PolicyCalendarEvent>>,
    today: LocalDate,
    selected: LocalDate?,
    onSelect: (LocalDate) -> Unit,
) {
    val firstOfMonth = month.atDay(1)
    // 일요일 = 0 으로 맞추기 (한국 캘린더 관례)
    val firstDow = firstOfMonth.dayOfWeek.toSundayBased()
    val daysInMonth = month.lengthOfMonth()

    Column(modifier = Modifier.fillMaxWidth()) {
        for (week in 0 until 6) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dow in 0 until 7) {
                    val cellIdx = week * 7 + dow
                    val dayNum = cellIdx - firstDow + 1
                    if (dayNum in 1..daysInMonth) {
                        val date = month.atDay(dayNum)
                        val key = date.toString()
                        val events = eventsByDate[key].orEmpty()
                        DayCell(
                            day = dayNum,
                            isToday = date == today,
                            isSelected = date == selected,
                            isSunday = dow == 0,
                            isSaturday = dow == 6,
                            events = events,
                            onClick = { onSelect(date) },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f).height(56.dp))
                    }
                }
            }
        }
    }
}

private fun DayOfWeek.toSundayBased(): Int = (this.value % 7) // 일=0

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    isSunday: Boolean,
    isSaturday: Boolean,
    events: List<PolicyCalendarEvent>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val numberColor = when {
        isSelected -> colors.onAccent
        isSunday -> Semantic.Warning.copy(alpha = 0.9f)
        isSaturday -> Color(0xFF2A77E0).copy(alpha = 0.85f)
        else -> colors.textPrimary
    }
    val bg = when {
        isSelected -> colors.accent
        isToday -> colors.accentBg
        else -> Color.Transparent
    }
    Column(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                ),
                color = numberColor,
            )
        }
        Spacer(Modifier.height(3.dp))
        // 일정 dot — 최대 3개
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val distinctKinds = events.map { it.kind }.distinct().take(3)
            distinctKinds.forEach { k ->
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(kindColor(k))
                )
            }
        }
    }
}

private fun kindColor(kind: CalendarEventKind): Color = when (kind) {
    CalendarEventKind.ApplicationOpen -> Brand.Mint500
    CalendarEventKind.Deadline -> Semantic.Warning
    CalendarEventKind.Announcement -> Gray.G500
    CalendarEventKind.Disbursement -> Gray.G500
}

// =============================================================
// 범례
// =============================================================
@Composable
private fun LegendBar() {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendDot(label = "신청 시작", color = kindColor(CalendarEventKind.ApplicationOpen))
        LegendDot(label = "마감", color = kindColor(CalendarEventKind.Deadline))
        LegendDot(label = "발표·지급", color = kindColor(CalendarEventKind.Announcement))
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    val colors = AppTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textTertiary,
        )
    }
}

// =============================================================
// 선택 날짜 섹션 헤더 + 일정 카드
// =============================================================
@Composable
private fun SelectedDaySectionTitle(date: LocalDate?, count: Int) {
    val colors = AppTheme.colors
    val title = date?.let { "${it.monthValue}월 ${it.dayOfMonth}일 (${koreanDow(it.dayOfWeek)})" } ?: "오늘"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        if (count > 0) {
            Text(
                text = "${count}건",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textTertiary,
            )
        }
    }
}

private fun koreanDow(dow: DayOfWeek): String = when (dow) {
    DayOfWeek.MONDAY -> "월"
    DayOfWeek.TUESDAY -> "화"
    DayOfWeek.WEDNESDAY -> "수"
    DayOfWeek.THURSDAY -> "목"
    DayOfWeek.FRIDAY -> "금"
    DayOfWeek.SATURDAY -> "토"
    DayOfWeek.SUNDAY -> "일"
}

@Composable
private fun CalendarEventCard(event: PolicyCalendarEvent, onClick: () -> Unit) {
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
        // 좌측 컬러 바 + 종류 라벨
        Column(modifier = Modifier.width(80.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(kindColor(event.kind))
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = event.kind.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textSecondary,
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = event.policyTitle,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun EmptyHint(text: String) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.cardBg)
            .padding(horizontal = 20.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textTertiary,
        )
    }
}
