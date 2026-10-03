package com.voicerooms.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
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
import com.voicerooms.app.data.model.Gift
import com.voicerooms.app.ui.theme.Amber
import com.voicerooms.app.ui.theme.Border
import com.voicerooms.app.ui.theme.CardDark
import com.voicerooms.app.ui.theme.Cyan
import com.voicerooms.app.ui.theme.Purple
import com.voicerooms.app.ui.theme.SurfaceDark
import com.voicerooms.app.ui.theme.TextPrimary
import com.voicerooms.app.ui.theme.TextSecondary

/**
 * لوحة الهدايا المنبثقة — تُعرض شبكة الهدايا القادمة من قاعدة البيانات.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftSheet(
    gifts: List<Gift>,
    coins: Long,
    receiverName: String,
    onSelect: (Gift) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("إرسال هدية", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("إلى: $receiverName", color = TextSecondary, fontSize = 12.sp)
                }
                CoinsChip(coins)
            }
            Spacer(Modifier.height(16.dp))
            if (gifts.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    Text("لا توجد هدايا متاحة حاليًا", color = TextSecondary)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.height(360.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(gifts) { gift ->
                        GiftItem(gift = gift, affordable = coins >= gift.price, onClick = { onSelect(gift) })
                    }
                }
            }
        }
    }
}

@Composable
private fun GiftItem(gift: Gift, affordable: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, if (affordable) Border else Border.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .clickable(enabled = affordable) { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(Purple.copy(alpha = 0.25f), Cyan.copy(alpha = 0.25f)))),
            contentAlignment = Alignment.Center,
        ) {
            if (!gift.iconUrl.isNullOrBlank()) {
                AsyncImage(
                    model = gift.iconUrl,
                    contentDescription = gift.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(40.dp),
                )
            } else {
                Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = Cyan, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            gift.name,
            color = TextPrimary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = Amber, modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(2.dp))
            Text(formatNumber(gift.price.toLong()), color = Amber, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
