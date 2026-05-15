package com.hiddensubsidy.app.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hiddensubsidy.app.data.SampleData
import com.hiddensubsidy.app.data.model.EventBundle
import com.hiddensubsidy.app.data.model.LifeEvent
import com.hiddensubsidy.app.ui.theme.AppTheme
import com.hiddensubsidy.app.ui.theme.Bubble

@Composable
fun EventListScreen(
    events: List<EventBundle> = SampleData.events,
    onEventClick: (EventBundle) -> Unit = {},
) {
    val colors = AppTheme.colors
    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 8.dp,
                bottom = 24.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 헤더 영역 (그리드 전체 너비)
            item(span = { GridItemSpan(maxLineSpan) }) {
                HeaderArea()
            }
            items(events, key = { it.eventId }) { bundle ->
                EventCard(bundle = bundle, onClick = { onEventClick(bundle) })
            }
        }
    }
}

@Composable
private fun HeaderArea() {
    val colors = AppTheme.colors
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 16.dp)
    ) {
        Text(
            text = "이벤트",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "인생에 변화가 있을 때\n받을 수 있는 지원금이 있어요",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "이사·결혼·창업 같은 순간을 골라보세요",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textTertiary,
        )
    }
}

@Composable
private fun EventCard(bundle: EventBundle, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val event = bundle.event ?: return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.cardBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 20.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(eventBubble(event)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = event.emoji,
                style = TextStyle(fontSize = 30.sp),
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = event.label,
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "${bundle.count}건",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textTertiary,
        )
    }
}

fun eventBubble(event: LifeEvent) = when (event) {
    LifeEvent.Move -> Bubble.Sky
    LifeEvent.Resign -> Bubble.Sand
    LifeEvent.Pregnancy -> Bubble.Coral
    LifeEvent.Marriage -> Bubble.Lilac
    LifeEvent.Startup -> Bubble.Lemon
    LifeEvent.Employment -> Bubble.Mint
}
