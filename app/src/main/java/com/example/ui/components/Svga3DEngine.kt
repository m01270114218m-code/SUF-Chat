package com.example.ui.components

import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.config.DynamicThemeManager
import com.example.models.FrameStyle3D
import com.example.models.SvgaEffectType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Unified App Background matching the Main Interfaces (`HomeLobbyScreen`, `ProfileScreen`, `DiscoverScreen`, `MessagesScreen`):
 * Soft Lavender-Purple top aura transitioning into a clean Pearl-White backdrop with subtle animated royal sparkles.
 */
@Composable
fun PalaceNightAnimatedBackground(
    modifier: Modifier = Modifier,
    imageAlpha: Float = 0.14f,
    showFireworks: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "palace_bg_7d")
    val particlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_phase"
    )
    val fireworkPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "firework_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F6F9))
    ) {
        // Soft lavender-purple top header gradient matching ProfileScreen & HomeLobbyScreen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE7D6FA),
                            Color(0xFFF2EAFC),
                            Color(0xFFF6F6F9)
                        )
                    )
                )
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft ambient purple-pink & gold bokeh circles matching the main screens
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFD8B4FE).copy(alpha = 0.28f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.85f, h * 0.08f),
                    radius = w * 0.45f
                ),
                center = Offset(w * 0.85f, h * 0.08f),
                radius = w * 0.45f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFBCFE8).copy(alpha = 0.24f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.15f, h * 0.14f),
                    radius = w * 0.38f
                ),
                center = Offset(w * 0.15f, h * 0.14f),
                radius = w * 0.38f
            )

            if (showFireworks) {
                for (i in 0 until 14) {
                    val seedX = ((i * 37) % 100) / 100f
                    val baseSeedY = ((i * 53) % 100) / 100f
                    val animatedY = ((baseSeedY - particlePhase + 1f) % 1f)
                    val px = w * seedX
                    val py = h * animatedY
                    val dotColor = when (i % 3) {
                        0 -> Color(0xFFA855F7)
                        1 -> Color(0xFFEC4899)
                        else -> Color(0xFFF59E0B)
                    }
                    drawCircle(
                        color = dotColor.copy(alpha = 0.18f * fireworkPulse),
                        radius = ((i % 3 + 1) * 1.8f).dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}

/**
 * Circular 3D Avatar & Microphone Frame:
 * 1. Supports picking custom photo from device storage (`customImageUri` via Coil `AsyncImage`).
 * 2. VIP badges are NEVER shown on the avatar (`علامات vip غير ظاهرة على الأفاتار بل موجودة كشارة على صفحة الحساب`).
 * 3. Supports double-tapping (`onDoubleClick`) to open the user's inner account details (`عند لمس مرتين على الصورة تفتح الحساب من الداخل`).
 * 4. Renders a dynamic animated speaking ring around the photo when speaking (`إطار متحرك حول الصورة عند الكلام`).
 * 5. Renders a distinctive sculptural outer frame around the photo when a store frame is equipped (`وعند شراء إطار من المتجر يكون حول الصورة خارجي مميز`).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Ornate3DAvatarWithFrame(
    avatarType: String,
    frameStyle: FrameStyle3D,
    size: Dp = 68.dp,
    isSpeaking: Boolean = false,
    showCrown: Boolean = true,
    badgeText: String? = null,
    customSoundWaveColor: Color? = null,
    customImageUri: String? = null,
    modifier: Modifier = Modifier,
    onDoubleClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circular_avatar_3d_frame")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )
    val counterOrbitAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_orbit_angle"
    )
    val waveExpand by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.46f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 760, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_expand"
    )
    val speakingRingPulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speaking_ring_pulse"
    )

    val outerBoxSize = size * 1.44f
    val avatarDrawable = if (avatarType == "PRINCESS" || avatarType == "QUEEN") {
        R.drawable.img_avatar_princess
    } else {
        R.drawable.img_avatar_prince
    }
    val primaryWaveColor = customSoundWaveColor ?: frameStyle.soundWaveColor
    val hasStoreOuterFrame = frameStyle != FrameStyle3D.NONE

    // Ensure VIP tags are NEVER shown on the avatar (`علامات vip غير ظاهرة على الأفاتار`)
    val sanitizedBadgeText = badgeText?.takeIf {
        !it.trim().startsWith("VIP", ignoreCase = true)
    }

    val interactionModifier = if (onClick != null || onDoubleClick != null) {
        Modifier.combinedClickable(
            onClick = { onClick?.invoke() },
            onDoubleClick = { (onDoubleClick ?: onClick)?.invoke() }
        )
    } else {
        Modifier
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(outerBoxSize)
            .then(interactionModifier)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = (size.toPx() / 2f)

            // =========================================================================
            // 1. ANIMATED SPEAKING RING AROUND THE PHOTO (`إطار متحرك حول الصورة عند الكلام`)
            // =========================================================================
            if (isSpeaking) {
                val waveAlpha = (1.48f - waveExpand).coerceIn(0f, 0.95f)

                // Animated glowing sonic ring tightly hugging the avatar photo
                rotate(degrees = counterOrbitAngle, pivot = center) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                primaryWaveColor,
                                Color(0xFF00E676),
                                frameStyle.primaryColor,
                                Color(0xFFFF4081),
                                primaryWaveColor
                            ),
                            center = center
                        ),
                        radius = baseRadius * 0.96f * speakingRingPulse,
                        center = center,
                        style = Stroke(width = 3.8.dp.toPx())
                    )
                }

                // Expanding concentric audio wave rings
                drawCircle(
                    color = primaryWaveColor.copy(alpha = waveAlpha * 0.88f),
                    radius = baseRadius * waveExpand,
                    center = center,
                    style = Stroke(width = 3.4.dp.toPx())
                )
                drawCircle(
                    color = frameStyle.accentColor.copy(alpha = waveAlpha * 0.68f),
                    radius = baseRadius * (waveExpand + 0.12f),
                    center = center,
                    style = Stroke(width = 2.4.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = waveAlpha * 0.50f),
                    radius = baseRadius * (waveExpand + 0.22f),
                    center = center,
                    style = Stroke(width = 1.8.dp.toPx())
                )

                // 20 Radial Audio Equalizer Bars pulsing dynamically around the photo
                for (bar in 0 until 20) {
                    val angleRad = ((bar * 18.0) + orbitAngle * 0.7) * (PI / 180.0)
                    val pulseFactor = if (bar % 2 == 0) waveExpand else (2.38f - waveExpand)
                    val innerR = baseRadius * 1.06f
                    val outerR = baseRadius * (1.14f + (pulseFactor - 0.9f) * 0.34f)
                    val sx = center.x + (cos(angleRad) * innerR).toFloat()
                    val sy = center.y + (sin(angleRad) * innerR).toFloat()
                    val ex = center.x + (cos(angleRad) * outerR).toFloat()
                    val ey = center.y + (sin(angleRad) * outerR).toFloat()
                    val barColor = when (bar % 4) {
                        0 -> primaryWaveColor
                        1 -> frameStyle.primaryColor
                        2 -> Color(0xFF00E676)
                        else -> Color(0xFFFF4081)
                    }
                    drawLine(
                        color = barColor.copy(alpha = waveAlpha),
                        start = Offset(sx, sy),
                        end = Offset(ex, ey),
                        strokeWidth = 2.6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // =========================================================================
            // 2. DISTINCTIVE STORE-PURCHASED OUTER FRAME (`عند شراء إطار من المتجر يكون حول الصورة خارجي مميز`)
            // =========================================================================
            if (hasStoreOuterFrame) {
                // Ambient outer store frame aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            frameStyle.primaryColor.copy(alpha = 0.32f),
                            frameStyle.accentColor.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius * 1.38f
                    ),
                    radius = baseRadius * 1.38f,
                    center = center
                )

                // Outer Sculptural Store Frame Ring (distinctive outside the photo)
                rotate(degrees = orbitAngle, pivot = center) {
                    // Primary sculpted outer ring
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(
                                frameStyle.primaryColor,
                                Color(0xFFFFF9C4),
                                frameStyle.accentColor,
                                frameStyle.primaryColor,
                                Color.White,
                                frameStyle.accentColor,
                                frameStyle.primaryColor
                            ),
                            center = center
                        ),
                        radius = baseRadius * 1.12f,
                        center = center,
                        style = Stroke(width = 5.2.dp.toPx())
                    )

                    // Outer decorative wing/crest spikes for winged/royal store frames
                    val outerPointsCount = if (frameStyle.hasWings) 12 else 8
                    for (p in 0 until outerPointsCount) {
                        val deg = p * (360.0 / outerPointsCount)
                        val rad = deg * (PI / 180.0)
                        val innerSpikeR = baseRadius * 1.12f
                        val outerSpikeR = baseRadius * (if (p % 2 == 0) 1.27f else 1.20f)
                        val sx = center.x + (cos(rad) * innerSpikeR).toFloat()
                        val sy = center.y + (sin(rad) * innerSpikeR).toFloat()
                        val ex = center.x + (cos(rad) * outerSpikeR).toFloat()
                        val ey = center.y + (sin(rad) * outerSpikeR).toFloat()

                        drawLine(
                            color = if (p % 2 == 0) frameStyle.primaryColor else frameStyle.accentColor,
                            start = Offset(sx, sy),
                            end = Offset(ex, ey),
                            strokeWidth = 3.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawCircle(
                            color = Color(0xFFFFF59D),
                            radius = 2.8.dp.toPx(),
                            center = Offset(ex, ey)
                        )
                    }

                    // 8 Royal Gemstones embedded around the outer store frame bezel
                    for (j in 0 until 8) {
                        val rad = (j * 45.0) * (PI / 180.0)
                        val jx = center.x + (cos(rad) * baseRadius * 1.12f).toFloat()
                        val jy = center.y + (sin(rad) * baseRadius * 1.12f).toFloat()
                        drawCircle(
                            color = Color.White,
                            radius = 3.8.dp.toPx(),
                            center = Offset(jx, jy)
                        )
                        drawCircle(
                            color = if (j % 2 == 0) frameStyle.primaryColor else frameStyle.accentColor,
                            radius = 2.5.dp.toPx(),
                            center = Offset(jx, jy)
                        )
                    }
                }

                // Counter-rotating inner filigree ring between photo and outer store frame
                rotate(degrees = counterOrbitAngle, pivot = center) {
                    drawCircle(
                        color = Color(0xFFFFF59D).copy(alpha = 0.9f),
                        radius = baseRadius * 0.98f,
                        center = center,
                        style = Stroke(width = 1.8.dp.toPx())
                    )
                }
            }
        }

        // Core Circular Avatar Image (Supports custom photo from Device Storage OR default royal avatar)
        if (!customImageUri.isNullOrBlank()) {
            AsyncImage(
                model = Uri.parse(customImageUri),
                contentDescription = "صورة الحساب الشخصي",
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = avatarDrawable),
                error = painterResource(id = avatarDrawable),
                modifier = Modifier
                    .size(size * 0.88f)
                    .clip(CircleShape)
                    .border(
                        width = if (isSpeaking) 2.5.dp else 2.dp,
                        color = if (isSpeaking) primaryWaveColor else frameStyle.primaryColor,
                        shape = CircleShape
                    )
            )
        } else {
            Image(
                painter = painterResource(id = avatarDrawable),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size * 0.88f)
                    .clip(CircleShape)
                    .border(
                        width = if (isSpeaking) 2.5.dp else 2.dp,
                        color = if (isSpeaking) primaryWaveColor else frameStyle.primaryColor,
                        shape = CircleShape
                    )
            )
        }

        // Top 3D Royal Crown on the Store Outer Frame
        if (showCrown && hasStoreOuterFrame && frameStyle.hasCrown) {
            Text(
                text = "👑",
                fontSize = (size.value * 0.24f).sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-4).dp)
            )
        }

        // Optional Non-VIP Tag (e.g. HOST or store dimension preview)
        if (sanitizedBadgeText != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 2.dp)
                    .shadow(4.dp, RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.horizontalGradient(
                            listOf(frameStyle.accentColor, frameStyle.primaryColor)
                        )
                    )
                    .border(1.dp, Color(0xFFFFF59D), RoundedCornerShape(50))
                    .padding(horizontal = 7.dp, vertical = 1.dp)
            ) {
                Text(
                    text = sanitizedBadgeText,
                    color = Color(0xFF1A0736),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/**
 * Renders a Vivid Colored User Name (`الاسم ملون`) with a multi-stop shimmering gradient.
 */
@Composable
fun ColoredUserNameText(
    name: String,
    primaryColor: Color = Color(0xFFFFD700),
    secondaryColor: Color = Color(0xFF00E5FF),
    fontSize: TextUnit = 12.sp,
    maxLines: Int = 1,
    modifier: Modifier = Modifier
) {
    Text(
        text = name,
        style = TextStyle(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    primaryColor,
                    Color(0xFFFFF9C4),
                    secondaryColor,
                    primaryColor
                )
            ),
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        ),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

/**
 * Renders the two signature 3D Glossy Asset Boards (Crystal Blue Diamonds & Imperial Gold Coins).
 */
@Composable
fun Glossy3DDiamondAndCoinBoards(
    diamondsCount: Long,
    coinsCount: Long,
    onDiamondsClick: () -> Unit,
    onCoinsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlossySingleAssetBoard(
            amountText = "%,d".format(coinsCount),
            labelAr = "العملات الذهبية",
            isGoldBoard = true,
            onClick = onCoinsClick,
            modifier = Modifier.weight(1f)
        )

        GlossySingleAssetBoard(
            amountText = "%,d".format(diamondsCount),
            labelAr = "الألماس الملكي",
            isGoldBoard = false,
            onClick = onDiamondsClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GlossySingleAssetBoard(
    amountText: String,
    labelAr: String,
    isGoldBoard: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val boardBrush = if (isGoldBoard) {
        DynamicThemeManager.goldVertical3DBrush
    } else {
        DynamicThemeManager.crystalBlue3DBrush
    }
    val rimColor = if (isGoldBoard) Color(0xFFFFF59D) else Color(0xFFB3F6FF)
    val deepBorderColor = if (isGoldBoard) Color(0xFF8C5808) else Color(0xFF0A2E6E)

    Box(
        modifier = modifier
            .height(92.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = rimColor)
                .clip(RoundedCornerShape(20.dp))
                .background(boardBrush)
                .border(
                    width = 2.5.dp,
                    brush = Brush.verticalGradient(listOf(rimColor, deepBorderColor, rimColor)),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp, start = 10.dp, end = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.28f))
                        .border(1.dp, rimColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = amountText,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Canvas(
            modifier = Modifier
                .size(width = 110.dp, height = 46.dp)
                .align(Alignment.TopCenter)
        ) {
            val w = size.width
            val h = size.height
            if (isGoldBoard) {
                val coinPositions = listOf(
                    Offset(w * 0.28f, h * 0.65f),
                    Offset(w * 0.72f, h * 0.65f),
                    Offset(w * 0.42f, h * 0.52f),
                    Offset(w * 0.58f, h * 0.52f),
                    Offset(w * 0.50f, h * 0.38f)
                )
                coinPositions.forEach { pos ->
                    drawCircle(
                        color = Color(0xFF8C5808),
                        radius = 13.dp.toPx(),
                        center = Offset(pos.x, pos.y + 2.dp.toPx())
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFD700), Color(0xFFD49B1A)),
                            center = Offset(pos.x - 3f, pos.y - 3f),
                            radius = 15.dp.toPx()
                        ),
                        radius = 13.dp.toPx(),
                        center = pos
                    )
                    drawCircle(
                        color = Color(0xFFFFF59D),
                        radius = 9.5.dp.toPx(),
                        center = pos,
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }
            } else {
                val diamondCenters = listOf(
                    Offset(w * 0.30f, h * 0.64f),
                    Offset(w * 0.70f, h * 0.64f),
                    Offset(w * 0.50f, h * 0.44f)
                )
                diamondCenters.forEach { center ->
                    val r = 15.dp.toPx()
                    val path = Path().apply {
                        moveTo(center.x - r * 0.55f, center.y - r * 0.45f)
                        lineTo(center.x + r * 0.55f, center.y - r * 0.45f)
                        lineTo(center.x + r * 0.85f, center.y - r * 0.08f)
                        lineTo(center.x, center.y + r * 0.78f)
                        lineTo(center.x - r * 0.85f, center.y - r * 0.08f)
                        close()
                    }
                    drawPath(
                        path = path,
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFE0FFFF), Color(0xFF00E5FF), Color(0xFF1565C0))
                        )
                    )
                    drawPath(
                        path = path,
                        color = Color.White.copy(alpha = 0.85f),
                        style = Stroke(width = 1.3.dp.toPx())
                    )
                }
            }
        }
    }
}

/**
 * 3D Metallic Tag Badge.
 */
@Composable
fun Metallic3DBadgeTag(
    text: String,
    iconEmoji: String? = null,
    primaryColor: Color = Color(0xFFFFD700),
    secondaryColor: Color = Color(0xFFB26A00),
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(50), spotColor = primaryColor)
            .clip(RoundedCornerShape(50))
            .background(
                Brush.horizontalGradient(listOf(secondaryColor, primaryColor, secondaryColor))
            )
            .border(1.dp, Color(0xFFFFF8E1), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        if (iconEmoji != null) {
            Text(text = iconEmoji, fontSize = 10.sp)
            Spacer(modifier = Modifier.width(3.dp))
        }
        Text(
            text = text,
            color = Color(0xFF1A0736),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/**
 * Full-Screen Interactive 3D / 7D SVGA-Style Gift & Entrance Mount Animation Overlay.
 * - Entry Mounts (`الدخوليات المتحركة`) dynamically glide and move across the screen upon entering a room.
 * - Supports all formats (`SVGA / GIF / PNG / WEBP / MP4 / 7D / ثابتة ومتحركة`) via vector 7D + Coil `AsyncImage`.
 * - Auto-dismisses smoothly after 4.2 seconds or when tapped.
 */
@Composable
fun FullScreenSvga7DOverlay(
    effectType: SvgaEffectType,
    titleAr: String,
    senderName: String,
    receiverName: String,
    comboCount: Int = 1,
    isEntranceMount: Boolean = false,
    customAssetUri: String? = null,
    assetFormat: String = "SVGA / GIF / PNG / 7D",
    onDismiss: () -> Unit
) {
    androidx.compose.runtime.LaunchedEffect(titleAr, senderName, comboCount) {
        kotlinx.coroutines.delay(4200L)
        onDismiss()
    }

    val isEntryMount = isEntranceMount || receiverName.contains("دخول") || titleAr.contains("دخول")
    val infiniteTransition = rememberInfiniteTransition(label = "svga_7d_fullscreen")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "svga_progress"
    )
    val horizontalTraversal by infiniteTransition.animateFloat(
        initialValue = -130f,
        targetValue = 130f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "horizontal_traversal"
    )
    val verticalFloat by infiniteTransition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vertical_float"
    )
    val spinDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "svga_spin"
    )

    val (centerEmoji, primaryCol, secondaryCol) = when (effectType) {
        SvgaEffectType.GOLDEN_DRAGON_7D -> Triple("🐉", Color(0xFFFFD700), Color(0xFF9333EA))
        SvgaEffectType.ROYAL_PALACE_CASTLE_7D -> Triple("🏰", Color(0xFFFFD700), Color(0xFF7C3AED))
        SvgaEffectType.SPORTS_CAR_BUGATTI_5D -> Triple("🏎️", Color(0xFF00E5FF), Color(0xFF6D28D9))
        SvgaEffectType.CRYSTAL_PHOENIX_7D -> Triple("🦅", Color(0xFFEC4899), Color(0xFFFFD700))
        SvgaEffectType.LUXURY_YACHT_6D -> Triple("🛥️", Color(0xFF00E5FF), Color(0xFF8B5CF6))
        SvgaEffectType.DIAMOND_CROWN_4D -> Triple("💎", Color(0xFF00E5FF), Color(0xFFE040FB))
        SvgaEffectType.ROMANTIC_ROSES_3D -> Triple("🌹", Color(0xFFFF4081), Color(0xFFFFD700))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x991E1B4B))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f + (if (isEntryMount) horizontalTraversal * 1.4f else horizontalTraversal * 0.45f)
            val cy = size.height / 2f + verticalFloat
            val maxR = size.minDimension * 0.42f

            rotate(degrees = spinDeg, pivot = Offset(cx, cy)) {
                for (i in 0 until 16) {
                    val angle = (i * 22.5) * (PI / 180.0)
                    val ex = cx + (cos(angle) * maxR * 1.35f).toFloat()
                    val ey = cy + (sin(angle) * maxR * 1.35f).toFloat()
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(primaryCol.copy(alpha = 0.55f), Color.Transparent),
                            start = Offset(cx, cy),
                            end = Offset(ex, ey)
                        ),
                        start = Offset(cx, cy),
                        end = Offset(ex, ey),
                        strokeWidth = 12.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryCol.copy(alpha = 0.38f), secondaryCol.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = maxR * (0.8f + animProgress * 0.35f)
                ),
                radius = maxR * (0.8f + animProgress * 0.35f),
                center = Offset(cx, cy)
            )

            drawCircle(
                color = primaryCol,
                radius = maxR * (0.52f + animProgress * 0.18f),
                center = Offset(cx, cy),
                style = Stroke(width = 3.5.dp.toPx())
            )

            for (p in 0 until 24) {
                val a = (p * 15.0 + spinDeg) * (PI / 180.0)
                val dist = maxR * (0.35f + ((p % 4) * 0.2f) * animProgress)
                val px = cx + (cos(a) * dist).toFloat()
                val py = cy + (sin(a) * dist).toFloat()
                drawCircle(
                    color = if (p % 2 == 0) Color(0xFFFFF59D) else Color(0xFFE9D5FF),
                    radius = 4.5.dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Sliding Royal Entry Banner or Gift Header
            Box(
                modifier = Modifier
                    .offset(x = (horizontalTraversal * 0.25f).dp)
                    .shadow(16.dp, RoundedCornerShape(30.dp), spotColor = primaryCol)
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF6D28D9), Color(0xFF9333EA), Color(0xFFEC4899), Color(0xFF6D28D9))
                        )
                    )
                    .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(30.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isEntryMount) "🚀 دخولية ملكية متحركة • $titleAr ✨" else "✨ عرض 7D متحرك • $titleAr ✨",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "يدعم جميع الصيغ ($assetFormat)",
                        color = Color(0xFFFFF59D),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Moving / Gliding Animated Mount or Gift Vehicle across the screen
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(
                        x = horizontalTraversal.dp,
                        y = verticalFloat.dp
                    )
                    .size(188.dp)
                    .shadow(24.dp, CircleShape, spotColor = primaryCol)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.92f), Color(0xFFF3E8FF), primaryCol.copy(alpha = 0.45f), secondaryCol.copy(alpha = 0.85f))
                        )
                    )
                    .border(4.dp, DynamicThemeManager.goldMetallicBrush, CircleShape)
            ) {
                if (!customAssetUri.isNullOrBlank()) {
                    AsyncImage(
                        model = Uri.parse(customAssetUri),
                        contentDescription = titleAr,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Text(
                        text = centerEmoji,
                        fontSize = (78 + (animProgress * 18)).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Unified Light-Lavender & Royal Purple Info Banner matching the 4 main pages
            Box(
                modifier = Modifier
                    .offset(x = (-horizontalTraversal * 0.18f).dp)
                    .shadow(14.dp, RoundedCornerShape(24.dp), spotColor = Color(0xFF7C3AED))
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White, Color(0xFFF5EEFE))
                        )
                    )
                    .border(2.dp, Color(0xFFD8B4FE), RoundedCornerShape(24.dp))
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isEntryMount) "👑 $senderName دخل الغرفة الآن!" else "$senderName 👑  •  $receiverName",
                        color = Color(0xFF1E1B4B),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isEntryMount) "على متن: $titleAr 🏎️✨" else "$titleAr  •  COMBO X$comboCount 🔥",
                        color = Color(0xFF7C3AED),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "اضغط في أي مكان للإغلاق",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Super-Distinctive Animated 3D Emoji Reaction Floating Above Seat Avatar
 * (`والاموجي المتحرك يظهر فوق الصور يكون متحرك ومميز جدا`)
 */
@Composable
fun AnimatedSeatEmojiOverlay(
    emoji: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "seat_emoji_3d_anim")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.32f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 460, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emoji_scale"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = 2f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emoji_float_y"
    )
    val sparkleSpin by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "emoji_sparkle_spin"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .offset(y = floatY.dp)
            .size(48.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = size.minDimension / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFD700).copy(alpha = 0.55f),
                        Color(0xFFEC4899).copy(alpha = 0.28f),
                        Color.Transparent
                    ),
                    center = c,
                    radius = r * scalePulse
                ),
                radius = r * scalePulse,
                center = c
            )
            rotate(degrees = sparkleSpin, pivot = c) {
                for (i in 0 until 6) {
                    val rad = (i * 60.0) * (PI / 180.0)
                    val sx = c.x + (cos(rad) * r * 0.92f).toFloat()
                    val sy = c.y + (sin(rad) * r * 0.92f).toFloat()
                    drawCircle(
                        color = if (i % 2 == 0) Color(0xFFFFF59D) else Color(0xFF00E5FF),
                        radius = 2.6.dp.toPx(),
                        center = Offset(sx, sy)
                    )
                }
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size((34 * scalePulse.coerceIn(0.95f, 1.22f)).dp)
                .shadow(8.dp, CircleShape, spotColor = Color(0xFFEC4899))
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color.White, Color(0xFFF5EEFE), Color(0xFFE9D5FF))
                    )
                )
                .border(1.5.dp, Color(0xFFFFD700), CircleShape)
        ) {
            Text(
                text = emoji,
                fontSize = (20 * scalePulse.coerceIn(0.95f, 1.25f)).sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AnimatedPalaceNightBackdrop(
    modifier: Modifier = Modifier,
    darkenAlpha: Float = 0.45f
) {
    PalaceNightAnimatedBackground(
        modifier = modifier,
        imageAlpha = (1f - darkenAlpha).coerceIn(0.35f, 0.85f),
        showFireworks = true
    )
}

@Composable
fun Ornate3DAvatarFrame(
    avatarType: String,
    frameStyle: FrameStyle3D = FrameStyle3D.IMPERIAL_GOLD_WINGS,
    size: Dp = 74.dp,
    isSpeaking: Boolean = false,
    badgeText: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Ornate3DAvatarWithFrame(
        avatarType = avatarType,
        frameStyle = frameStyle,
        size = size,
        isSpeaking = isSpeaking,
        showCrown = true,
        badgeText = badgeText,
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
fun Glossy3DAssetBoard(
    amount: Long,
    isDiamondBoard: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    GlossySingleAssetBoard(
        amountText = "%,d".format(amount),
        labelAr = if (isDiamondBoard) "الألماس الملكي" else "العملات الذهبية",
        isGoldBoard = !isDiamondBoard,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun FullScreenSvga7DGiftOverlay(
    senderName: String,
    receiverName: String,
    giftTitleAr: String,
    effectType: SvgaEffectType,
    comboCount: Int,
    onDismiss: () -> Unit
) {
    FullScreenSvga7DOverlay(
        effectType = effectType,
        titleAr = giftTitleAr,
        senderName = senderName,
        receiverName = receiverName,
        comboCount = comboCount,
        onDismiss = onDismiss
    )
}

/**
 * 1. Luxurious Oriental / Islamic Gold Login Background (`المظهر الذهبي الشرقي - Luxurious Oriental / Islamic Gold Theme`)
 * Combines the 8K `img_oriental_gold_login_bg` artwork with infinite-resolution Vector Graphics (`Canvas`):
 * - Deep obsidian black & dark espresso brown backdrop
 * - Golden Islamic 8-point star arabesque geometric patterns (`زخارف إسلامية ذهبية`)
 * - Glowing Mosque Domes, Minarets, and Crescent Moon silhouettes at the bottom (`ظلال المساجد والقباب في الأسفل`)
 */
@Composable
fun OrientalGoldIslamicLoginBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "oriental_gold_login_bg")
    val shimmerPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_phase"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF080504),
                        Color(0xFF160E07),
                        Color(0xFF1E1208),
                        Color(0xFF090604)
                    )
                )
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_oriental_gold_login_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.62f,
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val goldBright = Color(0xFFFFD700)
            val goldSoft = Color(0xFFFFF59D)
            val goldDeep = Color(0xFF8C5808)

            // Subtle dark vignette for ultra-clean contrast
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070403).copy(alpha = 0.55f),
                        Color(0xFF120A05).copy(alpha = 0.25f),
                        Color(0xFF080503).copy(alpha = 0.85f)
                    )
                )
            )

            // Top Islamic Geometric 8-Pointed Star Rosettes (Vector Graphics / 4K precision)
            val rosetteCenters = listOf(
                Offset(w * 0.12f, h * 0.08f),
                Offset(w * 0.88f, h * 0.08f),
                Offset(w * 0.50f, h * 0.05f)
            )
            rosetteCenters.forEachIndexed { idx, center ->
                val radius = if (idx == 2) 42.dp.toPx() else 34.dp.toPx()
                rotate(degrees = shimmerPhase * 360f * (if (idx % 2 == 0) 0.15f else -0.15f), pivot = center) {
                    for (step in 0 until 8) {
                        val angleRad = (step * 45.0) * (PI / 180.0)
                        val nextAngleRad = ((step + 3) * 45.0) * (PI / 180.0)
                        val sx = center.x + (cos(angleRad) * radius).toFloat()
                        val sy = center.y + (sin(angleRad) * radius).toFloat()
                        val ex = center.x + (cos(nextAngleRad) * radius).toFloat()
                        val ey = center.y + (sin(nextAngleRad) * radius).toFloat()
                        drawLine(
                            color = goldBright.copy(alpha = 0.28f * glowPulse),
                            start = Offset(sx, sy),
                            end = Offset(ex, ey),
                            strokeWidth = 1.4.dp.toPx()
                        )
                    }
                    drawCircle(
                        color = goldSoft.copy(alpha = 0.22f * glowPulse),
                        radius = radius * 0.68f,
                        center = center,
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }
            }

            // Bottom Golden Mosque Domes & Minarets Silhouette (`ظلال المساجد والزخارف الإسلامية في الأسفل`)
            val horizonY = h * 0.92f
            // Golden horizon glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        goldBright.copy(alpha = 0.26f * glowPulse),
                        goldDeep.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, horizonY),
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = Offset(w * 0.5f, horizonY)
            )

            // Left & Right Minaret spires (Vector paths)
            val minaretXs = listOf(w * 0.10f, w * 0.24f, w * 0.76f, w * 0.90f)
            minaretXs.forEachIndexed { idx, mx ->
                val mh = if (idx == 1 || idx == 2) 78.dp.toPx() else 60.dp.toPx()
                val mw = 8.dp.toPx()
                val minaretPath = Path().apply {
                    moveTo(mx - mw, h)
                    lineTo(mx - mw * 0.7f, horizonY - mh * 0.65f)
                    lineTo(mx, horizonY - mh)
                    lineTo(mx + mw * 0.7f, horizonY - mh * 0.65f)
                    lineTo(mx + mw, h)
                    close()
                }
                drawPath(
                    path = minaretPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2B1B0C), Color(0xFF0A0603))
                    )
                )
                drawPath(
                    path = minaretPath,
                    color = goldBright.copy(alpha = 0.48f * glowPulse),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }

            // Central & Side Mosque Domes
            val domes = listOf(
                Triple(w * 0.35f, horizonY, 34.dp.toPx()),
                Triple(w * 0.50f, horizonY, 50.dp.toPx()),
                Triple(w * 0.65f, horizonY, 34.dp.toPx())
            )
            domes.forEach { (dx, dy, dr) ->
                drawCircle(
                    color = Color(0xFF140C06),
                    radius = dr,
                    center = Offset(dx, dy)
                )
                drawCircle(
                    color = goldBright.copy(alpha = 0.52f * glowPulse),
                    radius = dr,
                    center = Offset(dx, dy),
                    style = Stroke(width = 1.5.dp.toPx())
                )
                // Crescent finial on top of dome
                drawLine(
                    color = goldSoft.copy(alpha = 0.75f),
                    start = Offset(dx, dy - dr),
                    end = Offset(dx, dy - dr - 10.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx()
                )
                drawCircle(
                    color = goldBright,
                    radius = 3.dp.toPx(),
                    center = Offset(dx, dy - dr - 12.dp.toPx())
                )
            }

            // Floating Golden Dust Particles
            for (i in 0 until 18) {
                val px = w * (((i * 41) % 100) / 100f)
                val py = h * ((((i * 67) % 100) / 100f - shimmerPhase + 1f) % 1f)
                drawCircle(
                    color = if (i % 2 == 0) goldBright.copy(alpha = 0.45f * glowPulse) else goldSoft.copy(alpha = 0.35f),
                    radius = ((i % 3 + 1) * 1.4f).dp.toPx(),
                    center = Offset(px, py)
                )
            }
        }
    }
}

/**
 * Crisp 4K Vector Golden Oriental Logo Emblem (`شعار ذهبي لامع في الأعلى مع سيوف وتاج وهلال`).
 */
@Composable
fun OrientalGoldenCrownLogoEmblem(
    size: Dp = 96.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "golden_emblem")
    val ringSpin by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_spin"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val r = this.size.minDimension / 2f

            // Outer golden radial aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFD700).copy(alpha = 0.38f),
                        Color(0xFFD49B1A).copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = r * 1.15f
                ),
                radius = r * 1.15f,
                center = center
            )

            // Rotating Islamic 8-point geometric gold bezel
            rotate(degrees = ringSpin, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        listOf(
                            Color(0xFFFFD700),
                            Color(0xFFFFF8E1),
                            Color(0xFFB26A00),
                            Color(0xFFFFD700),
                            Color(0xFFFFF59D),
                            Color(0xFFFFD700)
                        ),
                        center = center
                    ),
                    radius = r * 0.88f,
                    center = center,
                    style = Stroke(width = 3.8.dp.toPx())
                )
                for (i in 0 until 8) {
                    val rad = (i * 45.0) * (PI / 180.0)
                    val jx = center.x + (cos(rad) * r * 0.88f).toFloat()
                    val jy = center.y + (sin(rad) * r * 0.88f).toFloat()
                    drawCircle(
                        color = Color(0xFFFFF9C4),
                        radius = 3.2.dp.toPx(),
                        center = Offset(jx, jy)
                    )
                }
            }

            // Deep obsidian inner shield
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2B190B), Color(0xFF0E0804)),
                    center = center,
                    radius = r * 0.80f
                ),
                radius = r * 0.80f,
                center = center
            )

            // Golden Crescent Moon & Crossed Scimitars Hint inside shield
            drawCircle(
                color = Color(0xFFFFD700),
                radius = r * 0.56f,
                center = Offset(center.x, center.y + 4.dp.toPx()),
                style = Stroke(width = 2.4.dp.toPx())
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "👑", fontSize = 28.sp)
            Text(
                text = "ZADIRA",
                color = Color(0xFFFFF59D),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

/**
 * 2. Dark Neon & Glassmorphism Voice Chat Room Background (`النمط الحديث مع تأثيرات النيون والزجاج`)
 * Uses `img_neon_voice_room_bg` + animated neon purple, electric blue, and pink aura waves.
 */
@Composable
fun DarkNeonGlassVoiceRoomBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neon_room_bg")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_phase"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF090314),
                        Color(0xFF16072E),
                        Color(0xFF1F093D),
                        Color(0xFF0A0316)
                    )
                )
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_neon_voice_room_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.64f,
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Neon Purple, Pink, and Electric Blue ambient stage glows
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFE040FB).copy(alpha = 0.24f + wavePhase * 0.1f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.2f, h * 0.28f),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = Offset(w * 0.2f, h * 0.28f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00E5FF).copy(alpha = 0.22f + (1f - wavePhase) * 0.1f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.82f, h * 0.34f),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = Offset(w * 0.82f, h * 0.34f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF4081).copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.45f),
                    radius = w * 0.6f
                ),
                radius = w * 0.6f,
                center = Offset(w * 0.5f, h * 0.45f)
            )
        }
    }
}

/**
 * Centerpiece Neon Audio Wave Visualizer (`مؤشر الموجات الصوتية المتحركة في المنتصف - Audio Wave Visualizer in the center`)
 * Displays real-time animated neon purple, cyan, and magenta pink soundwave bars in the center of the Voice Chat Room.
 */
@Composable
fun CenterNeonAudioWaveVisualizer(
    activeSpeakersCount: Int,
    roomHeatScore: Long,
    isMyMicOn: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "center_audio_visualizer")
    val eqPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "eq_phase"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(50))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xB3190736),
                        Color(0xCC2A0B52),
                        Color(0xB3190736)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF00E5FF), Color(0xFFE040FB), Color(0xFFFF4081), Color(0xFFFFD700))
                ),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Left Status Badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isMyMicOn) Color(0xFF00E676) else Color(0xFFFF5252))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isMyMicOn) "MIC ON" else "MIC OFF",
                color = if (isMyMicOn) Color(0xFF00E676) else Color(0xFFFF8A80),
                fontSize = 10.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Center 24-Bar Neon Audio Wave Visualizer (Vector Canvas)
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .padding(horizontal = 12.dp)
        ) {
            val barCount = 24
            val spacing = size.width / barCount
            val centerY = size.height / 2f
            for (i in 0 until barCount) {
                val wave = sin(eqPhase + i * 0.55f)
                val normalized = ((wave + 1f) / 2f).coerceIn(0.18f, 1f)
                val barHeight = size.height * normalized
                val x = i * spacing + spacing / 2f
                val barColor = when (i % 3) {
                    0 -> Color(0xFFE040FB) // Neon Purple
                    1 -> Color(0xFF00E5FF) // Neon Blue/Cyan
                    else -> Color(0xFFFF4081) // Neon Pink
                }
                drawLine(
                    color = barColor,
                    start = Offset(x, centerY - barHeight / 2f),
                    end = Offset(x, centerY + barHeight / 2f),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Right Acoustic Telemetry
        Text(
            text = "🎙️ $activeSpeakersCount • 🔥 $roomHeatScore",
            color = Color(0xFFFFF59D),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
