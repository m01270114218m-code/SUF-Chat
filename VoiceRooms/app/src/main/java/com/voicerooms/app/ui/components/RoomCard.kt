package com.voicerooms.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.voicerooms.app.data.model.Room
import com.voicerooms.app.ui.theme.Amber
import com.voicerooms.app.ui.theme.Border
import com.voicerooms.app.ui.theme.CardDark
import com.voicerooms.app.ui.theme.Cyan
import com.voicerooms.app.ui.theme.Purple
import com.voicerooms.app.ui.theme.TextPrimary
import com.voicerooms.app.ui.theme.TextSecondary

/** بطاقة غرفة بشكل شبكي */
@Composable
fun RoomCard(room: Room, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .width(180.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(CardDark)
            .border(1.dp, Border, RoundedCornerShape(18.dp))
            .clickable { onClick() },
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(110.dp)) {
            if (!room.coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = room.coverUrl,
                    contentDescription = room.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(Purple, Cyan))),
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Headset, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text(formatNumber(room.listeners.toLong()), color = Color.White, fontSize = 11.sp)
            }
            if (room.isLocked) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Amber,
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp).size(16.dp),
                )
            }
        }
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                room.name,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(url = room.owner?.avatarUrl, name = room.owner?.name ?: "?", size = 20.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    room.owner?.name ?: "—",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** صف غرفة بشكل قائمة أفقية */
@Composable
fun RoomListTile(room: Room, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(1.dp, Border, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))) {
            if (!room.coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = room.coverUrl,
                    contentDescription = room.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.linearGradient(listOf(Purple, Cyan))),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    room.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (room.isLocked) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = Amber, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                room.category?.name ?: "غرفة صوتية",
                color = TextSecondary,
                fontSize = 12.sp,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            Icon(Icons.Filled.Headset, contentDescription = null, tint = Cyan, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(formatNumber(room.listeners.toLong()), color = TextSecondary, fontSize = 12.sp)
        }
    }
}
