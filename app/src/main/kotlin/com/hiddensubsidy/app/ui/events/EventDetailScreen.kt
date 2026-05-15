package com.hiddensubsidy.app.ui.events

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
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
import com.hiddensubsidy.app.data.model.EventBundle
import com.hiddensubsidy.app.data.model.Policy
import com.hiddensubsidy.app.data.model.TimelineGroup
import com.hiddensubsidy.app.ui.components.IconBubble
import com.hiddensubsidy.app.ui.components.formatAmount
import com.hiddensubsidy.app.ui.theme.AppTheme
import com.hiddensubsidy.app.ui.theme.categoryBubble
import com.hiddensubsidy.app.ui.theme.categoryEmoji

private const val SIDE = 16

@Composable
fun EventDetailScreen(
    bundle: EventBundle,
    onBack: () -> Unit,
    onPolicyClick: (Policy) -> Unit,
) {
    val colors = AppTheme.colors
    val event = bundle.event ?: return

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = 32.dp,
            ),
        ) {
            item { TopBar(title = "${event.label}할 때 받는 지원금", onBack = onBack) }

            item {
                Spacer(Modifier.height(12.dp))
                Box(modifier = Modifier.padding(horizontal = SIDE.dp)) {
                    HeroCard(bundle = bundle)
                }
            }

            bundle.groups.forEach { group ->
                item {
                    Spacer(Modifier.height(20.dp))
                    GroupHeader(group = group)
                    Spacer(Modifier.height(8.dp))
                }
                group.policies.forEachIndexed { idx, policy ->
                    item(key = "${group.label}-${policy.id}-$idx") {
                        Box(modifier = Modifier.padding(horizontal = SIDE.dp)) {
                            PolicyRow(policy = policy, onClick = { onPolicyClick(policy) })
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
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

@Composable
private fun HeroCard(bundle: EventBundle) {
    val colors = AppTheme.colors
    val event = bundle.event ?: return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(colors.cardBg)
            .padding(horizontal = 22.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "최대 받을 수 있어요",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textTertiary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = bundle.maxAmountLabel,
                style = MaterialTheme.typography.displaySmall,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "${bundle.count}건  ·  ${bundle.tagline}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(eventBubble(event)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = event.emoji,
                style = TextStyle(fontSize = 32.sp),
            )
        }
    }
}

@Composable
private fun GroupHeader(group: TimelineGroup) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = group.emoji,
            style = TextStyle(fontSize = 16.sp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = group.label,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
        )
    }
}

@Composable
private fun PolicyRow(policy: Policy, onClick: () -> Unit) {
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
        IconBubble(
            emoji = categoryEmoji(policy.category),
            background = categoryBubble(policy.category),
            size = 44.dp,
            fontSize = 22,
        )
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = policy.title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            val tag = policy.period ?: formatAmount(policy.amount)
            Text(
                text = tag,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        }
    }
}
