package com.hiddensubsidy.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hiddensubsidy.app.ui.theme.AppTheme

data class BottomTab(
    val label: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector,
)

@Composable
fun BottomTabBar(
    current: Int,
    onChange: (Int) -> Unit,
) {
    val colors = AppTheme.colors
    val items = listOf(
        BottomTab("홈", Icons.Rounded.Home, Icons.Outlined.Home),
        BottomTab("캘린더", Icons.Rounded.CalendarMonth, Icons.Outlined.CalendarMonth),
        BottomTab("이벤트", Icons.Rounded.AutoAwesome, Icons.Outlined.AutoAwesome),
        BottomTab("마이", Icons.Rounded.Person, Icons.Outlined.Person),
    )
    Column(modifier = Modifier
        .fillMaxWidth()
        .background(colors.cardBg)
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.divider))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 6.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { idx, tab ->
                val selected = idx == current
                TabItem(
                    label = tab.label,
                    icon = if (selected) tab.iconFilled else tab.iconOutlined,
                    selected = selected,
                    onClick = { onChange(idx) },
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val tint = if (selected) colors.textPrimary else colors.textTertiary
    Column(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(26.dp),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = tint,
        )
    }
}
