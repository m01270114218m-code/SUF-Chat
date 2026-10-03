package com.voicerooms.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.voicerooms.app.ui.AppViewModelProvider
import com.voicerooms.app.ui.components.EmptyState
import com.voicerooms.app.ui.components.GradientBackground
import com.voicerooms.app.ui.components.LoadingBox
import com.voicerooms.app.ui.theme.Amber
import com.voicerooms.app.ui.theme.BgDark
import com.voicerooms.app.ui.theme.Border
import com.voicerooms.app.ui.theme.CardDark
import com.voicerooms.app.ui.theme.Cyan
import com.voicerooms.app.ui.theme.Purple
import com.voicerooms.app.ui.theme.TextPrimary
import com.voicerooms.app.ui.theme.TextSecondary
import com.voicerooms.app.ui.viewmodel.GiftsViewModel

@Composable
fun GiftsScreen(onBack: () -> Unit) {
    val vm: GiftsViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(containerColor = BgDark) { padding ->
        GradientBackground {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = TextPrimary)
                    }
                    Text("متجر الهدايا", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    "كل الهدايا المتحركة (SVGA) القادمة من قاعدة البيانات",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(12.dp))
                when {
                    state.loading -> LoadingBox()
                    state.gifts.isEmpty() -> Box(modifier = Modifier.fillMaxSize()) { EmptyState("لا توجد هدايا") }
                    else -> LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.gifts) { gift ->
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(CardDark)
                                    .border(1.dp, Border, RoundedCornerShape(16.dp))
                                    .padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Brush.linearGradient(listOf(Purple.copy(alpha = 0.25f), Cyan.copy(alpha = 0.25f)))),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (!gift.iconUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = gift.iconUrl,
                                            contentDescription = gift.name,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.size(48.dp),
                                        )
                                    } else {
                                        Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = Cyan, modifier = Modifier.size(32.dp))
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(gift.name, color = TextPrimary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CardGiftcard, contentDescription = null, tint = Amber, modifier = Modifier.size(12.dp))
                                    Spacer(Modifier.width(3.dp))
                                    Text("${gift.price}", color = Amber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
