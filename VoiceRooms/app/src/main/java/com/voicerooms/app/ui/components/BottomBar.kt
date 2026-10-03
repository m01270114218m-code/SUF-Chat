package com.voicerooms.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voicerooms.app.ui.theme.Cyan
import com.voicerooms.app.ui.theme.SurfaceDark
import com.voicerooms.app.ui.theme.TextHint

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

private val items = listOf(
    NavItem("home", "الرئيسية", Icons.Filled.Home),
    NavItem("explore", "استكشف", Icons.Filled.Explore),
    NavItem("messages", "الرسائل", Icons.Filled.ChatBubble),
    NavItem("profile", "حسابي", Icons.Filled.Person),
)

@Composable
fun AppBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .background(SurfaceDark)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onNavigate(item.route) }
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                Icon(
                    item.icon,
                    contentDescription = item.label,
                    tint = if (selected) Cyan else TextHint,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    item.label,
                    color = if (selected) Cyan else TextHint,
                    fontSize = 11.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}
