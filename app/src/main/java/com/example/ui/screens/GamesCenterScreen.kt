package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.config.DynamicThemeManager
import com.example.config.glossy3DButton
import com.example.config.ornateGolden3DContainer
import com.example.models.CasualGameItem
import com.example.models.GameArtworkTheme
import com.example.models.UserProfile
import com.example.models.WinTickerNotice
import com.example.ui.components.PalaceNightAnimatedBackground

/**
 * Screen 3: لعبة (THE LOBBY GAMES - "BEST LUCKY" 3x4 Grid)
 * Faithfully matches Screenshot 2 ("2. THE LOBBY GAMES"):
 * - Top Header with User Avatar, Nickname, Gold Coins (`5.000 🪙`) & Diamonds (`0 💎`)
 * - "BEST LUCKY" Golden Treasure Banner with overflowing gold coins
 * - Dual-row Live Win Ticker (`WIN 🪙 5500 >>`, `WIN 🪙 1000 >>`)
 * - 3x4 Grid of 12 Fantasy 3D Games encased in thick glowing golden metallic borders
 * - Playable 3D Mini-Game Modal when tapping any game card!
 */
@Composable
fun GamesCenterScreen(
    userProfile: UserProfile,
    gamesCatalog: List<CasualGameItem>,
    winTickers: List<WinTickerNotice>,
    activePlayableGame: CasualGameItem?,
    lastGameWinResult: String?,
    onOpenGame: (CasualGameItem) -> Unit,
    onPlayRound: (Long) -> Unit,
    onCloseGame: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        PalaceNightAnimatedBackground(imageAlpha = 0.42f, showFireworks = true)

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 105.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Top User Coins & Avatar Bar (Matching Screenshot 2 top bar)
            item(span = { GridItemSpan(3) }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // User Avatar + Name on Right in RTL
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.img_avatar_prince),
                            contentDescription = userProfile.nickname,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color(0xFFFFD700), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = userProfile.nickname,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Gold Coins & Diamonds Glossy Pills
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF3E1C08))
                                .border(1.5.dp, Color(0xFFFFD700), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "🪙 %,d".format(userProfile.goldCoins),
                                color = Color(0xFFFFF59D),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF10284E))
                                .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "💎 %,d".format(userProfile.crystalDiamonds),
                                color = Color(0xFFB3F6FF),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // 2. "BEST LUCKY" Golden Treasure Header + Dual-Row WIN Ticker (Matching Screenshot 2)
            item(span = { GridItemSpan(3) }) {
                BestLuckyHeaderAndTicker(winTickers = winTickers)
            }

            // 3. 3x4 Grid of 12 Fantasy 3D Games in Thick Metallic Gold Borders
            items(gamesCatalog, key = { it.id }) { game ->
                Ornate3DFantasyGameCard(
                    game = game,
                    onClick = { onOpenGame(game) }
                )
            }
        }

        // Interactive Playable 3D Mini-Game Modal
        if (activePlayableGame != null) {
            Playable3DGameModal(
                game = activePlayableGame,
                userCoins = userProfile.goldCoins,
                lastResult = lastGameWinResult,
                onBetAndSpin = onPlayRound,
                onDismiss = onCloseGame
            )
        }
    }
}

@Composable
private fun BestLuckyHeaderAndTicker(winTickers: List<WinTickerNotice>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Golden "BEST LUCKY" Treasure Banner
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw piles of glowing gold coins along bottom of title
                for (i in 0 until 18) {
                    val cx = (size.width / 18f) * i + 14f
                    val cy = size.height * 0.76f + (i % 3) * 4f
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFFFFF9C4), Color(0xFFFFD700), Color(0xFFB26A00)),
                            center = Offset(cx, cy),
                            radius = 12.dp.toPx()
                        ),
                        radius = 10.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }
            }

            Text(
                text = "✨ BEST LUCKY ✨",
                color = Color(0xFFFFF59D),
                fontSize = 27.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.shadow(12.dp, spotColor = Color(0xFFFFD700))
            )
        }

        // Ornate Golden Win Ticker Box (2 rows like Screenshot 2)
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .ornateGolden3DContainer(
                    cornerRadius = 18.dp,
                    backgroundBrush = Brush.verticalGradient(
                        listOf(Color(0xFF4A1828), Color(0xFF2B0B1E))
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            winTickers.take(2).forEach { ticker ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.img_avatar_prince),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color(0xFFFFD700), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WIN 🪙 ${ticker.winAmount}  >>>",
                            color = Color(0xFFFFF59D),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DynamicThemeManager.goldVertical3DBrush)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = ticker.gameTitleAr,
                            color = Color(0xFF1A0736),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Ornate3DFantasyGameCard(
    game: CasualGameItem,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "game_card_shine")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "game_pulse"
    )

    val centerIcon = when (game.artworkTheme) {
        GameArtworkTheme.SHARK_HUNTER -> "🦈"
        GameArtworkTheme.DRAGON_VS_TIGER -> "🐉🐅"
        GameArtworkTheme.GREEDY_FRUIT -> "🍒🎡"
        GameArtworkTheme.TEEN_PATTI -> "🃏👑"
        GameArtworkTheme.SOCCER_STRIKER -> "⚽🔥"
        GameArtworkTheme.SLOTS_777 -> "🎰7️⃣"
        GameArtworkTheme.OLYMPUS_ZEUS -> "⚡👑"
        GameArtworkTheme.TRENZY_CARS -> "🏎️💨"
        GameArtworkTheme.FOOTBALL_CLUB -> "🏆⚽"
        GameArtworkTheme.SPEED_CAR -> "🚔⚡"
        GameArtworkTheme.ROYAL_KING -> "🤴⚔️"
        GameArtworkTheme.GOLDEN_EMPIRE -> "🏛️🪙"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.95f)
            .shadow(10.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFFFFD700))
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.radialGradient(
                    listOf(game.primaryColor, game.secondaryColor, Color(0xFF120526))
                )
            )
            .border(
                width = 3.dp,
                brush = DynamicThemeManager.goldMetallicBrush,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
    ) {
        // Decorative 3D radial rays inside card
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.14f),
                radius = size.minDimension * 0.38f * pulse,
                center = Offset(size.width / 2f, size.height * 0.42f)
            )
        }

        // Center 3D Game Icon Artwork
        Text(
            text = centerIcon,
            fontSize = 32.sp,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 18.dp)
        )

        // Bottom Metallic Golden Title Plate (matching Screenshot 2 game cards)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xEE180630), Color(0xFF2D1206))
                    )
                )
                .padding(horizontal = 4.dp, vertical = 5.dp)
        ) {
            Text(
                text = game.subtitleEn.uppercase(),
                color = Color(0xFFFFD700),
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
            Text(
                text = game.titleAr,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun Playable3DGameModal(
    game: CasualGameItem,
    userCoins: Long,
    lastResult: String?,
    onBetAndSpin: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedBet by remember { mutableLongStateOf(game.minBet) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1D093E),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🎰 ${game.titleAr} (${game.subtitleEn})",
                    color = Color(0xFFFFD700),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "الجائزة الكبرى: %,d 🪙 • مضاعفة حتى X${game.maxMultiplier}".format(game.jackpotPool),
                    color = Color(0xFF00E5FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .ornateGolden3DContainer(cornerRadius = 18.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🎡 🪙 💎 👑", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "رصيدك الحالي: %,d عملة ذهبية 🪙".format(userCoins),
                            color = Color(0xFFFFF59D),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Text(
                    text = "اختر قيمة الرهان للجولة:",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(100L, 500L, 1000L, 2500L).forEach { bet ->
                        val active = selectedBet == bet
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (active) DynamicThemeManager.goldVertical3DBrush
                                    else Brush.verticalGradient(listOf(Color(0xFF2B1156), Color(0xFF180734)))
                                )
                                .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(12.dp))
                                .clickable { selectedBet = bet }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = "$bet 🪙",
                                color = if (active) Color(0xFF1A0736) else Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                if (lastResult != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF2E125E))
                            .border(1.dp, Color(0xFFFFD700), RoundedCornerShape(14.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = lastResult,
                            color = Color(0xFFFFF59D),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .glossy3DButton(cornerRadius = 20.dp)
                    .clickable { onBetAndSpin(selectedBet) }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "العب الآن ($selectedBet 🪙) 🎲",
                    color = Color(0xFF1A0736),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = Color.White)
            }
        }
    )
}
