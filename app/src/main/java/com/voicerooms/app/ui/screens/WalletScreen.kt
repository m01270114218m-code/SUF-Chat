package com.voicerooms.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voicerooms.app.ui.AppViewModelProvider
import com.voicerooms.app.ui.components.GradientBackground
import com.voicerooms.app.ui.components.SectionHeader
import com.voicerooms.app.ui.theme.Amber
import com.voicerooms.app.ui.theme.BgDark
import com.voicerooms.app.ui.theme.CardDark
import com.voicerooms.app.ui.theme.Cyan
import com.voicerooms.app.ui.theme.GradientEnd
import com.voicerooms.app.ui.theme.GradientStart
import com.voicerooms.app.ui.theme.Green
import com.voicerooms.app.ui.theme.Pink
import com.voicerooms.app.ui.theme.TextPrimary
import com.voicerooms.app.ui.theme.TextSecondary
import com.voicerooms.app.ui.viewmodel.ProfileViewModel

private val coinPacks = listOf(100L to 1.0, 500L to 4.5, 1200L to 9.9, 5000L to 34.9, 12000L to 79.9, 30000L to 179.9)

@Composable
fun WalletScreen(onBack: () -> Unit) {
    val vm: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.loadMine()
        vm.loadWallet()
    }

    Scaffold(containerColor = BgDark) { padding ->
        GradientBackground {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = TextPrimary)
                        }
                        Text("المحفظة", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                            .padding(22.dp),
                    ) {
                        Column {
                            Text("رصيد العملات", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                            Spacer(Modifier.height(6.dp))
                            Text("${state.profile?.coins ?: 0}", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text("الألماس: ${state.profile?.diamonds ?: 0} 💎", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
                        }
                    }
                }
                item {
                    SectionHeader(title = "شحن العملات", modifier = Modifier.padding(horizontal = 16.dp))
                }
                items(coinPacks.chunked(2)) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        row.forEach { (coins, price) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(CardDark)
                                    .padding(16.dp),
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Text("🪙", fontSize = 26.sp)
                                    Spacer(Modifier.height(6.dp))
                                    Text("$coins عملة", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Cyan)
                                            .padding(horizontal = 16.dp, vertical = 6.dp),
                                    ) {
                                        Text("\$$price", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                item {
                    SectionHeader(title = "سجل المعاملات", modifier = Modifier.padding(horizontal = 16.dp))
                }
                if (state.walletTransactions.isEmpty()) {
                    item {
                        Text(
                            "لا توجد معاملات بعد",
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                } else {
                    items(state.walletTransactions) { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CardDark)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tx.note ?: tx.txType ?: "معاملة", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(tx.createdAt?.take(10) ?: "", color = TextSecondary, fontSize = 11.sp)
                            }
                            Text(
                                (if (tx.amount >= 0) "+" else "") + "${tx.amount}",
                                color = if (tx.amount >= 0) Green else Pink,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}
