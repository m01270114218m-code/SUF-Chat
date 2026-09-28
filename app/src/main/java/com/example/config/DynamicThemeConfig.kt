package com.example.config

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Server-Driven Dynamic Theme & Style Configuration Table Mirror.
 * Every color, metallic bevel, button shape, border style, and 7D animation toggle
 * is controlled from this configuration object, which mirrors `app_theme_style_config`
 * in the database and `assets/config/server_master_config.json`.
 */
data class DynamicStyleTable(
    val themeId: String = "royal_arabian_night_7d",
    val deepNightBg: Color = Color(0xFF120629),
    val royalPurpleBg: Color = Color(0xFF210B46),
    val velvetCardBg: Color = Color(0xFF2D125E),
    val sheetDarkBlueBg: Color = Color(0xFF1D243B),
    val sheetNavyBottomBg: Color = Color(0xFF121829),
    val goldHighlight: Color = Color(0xFFFFF6B3),
    val goldPrimary: Color = Color(0xFFFFD700),
    val goldMetallic: Color = Color(0xFFE5A93B),
    val goldDeepBronze: Color = Color(0xFF8A540A),
    val crystalIceLight: Color = Color(0xFFB3F6FF),
    val crystalCyan: Color = Color(0xFF00E5FF),
    val crystalRoyalBlue: Color = Color(0xFF1565C0),
    val crystalDeepBlue: Color = Color(0xFF0A2E6E),
    val neonMagenta: Color = Color(0xFFFF2A85),
    val neonViolet: Color = Color(0xFF9C27B0),
    val emeraldGlow: Color = Color(0xFF00E676),
    val buttonCornerRadiusDp: Int = 24,
    val cardCornerRadiusDp: Int = 18,
    val borderThicknessDp: Float = 2.2f,
    val enable7DParticles: Boolean = true,
    val enableSvgaAnimations: Boolean = true
)

object DynamicThemeManager {
    var currentStyle = DynamicStyleTable()
        private set

    fun updateFromServerConfig(newConfig: DynamicStyleTable) {
        currentStyle = newConfig
    }

    val goldMetallicBrush: Brush
        get() = Brush.linearGradient(
            colors = listOf(
                currentStyle.goldHighlight,
                currentStyle.goldPrimary,
                currentStyle.goldMetallic,
                currentStyle.goldDeepBronze,
                currentStyle.goldPrimary,
                currentStyle.goldHighlight
            )
        )

    val goldVertical3DBrush: Brush
        get() = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFF3A1),
                Color(0xFFF5C242),
                Color(0xFFC88A1B),
                Color(0xFF8C5808)
            )
        )

    val crystalBlue3DBrush: Brush
        get() = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF88E2FF),
                Color(0xFF2B86DF),
                Color(0xFF1652A7),
                Color(0xFF0C3270)
            )
        )

    val royalPurpleBackgroundBrush: Brush
        get() = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF280E54),
                Color(0xFF180836),
                Color(0xFF0E0422)
            )
        )

    val vipBannerBrush: Brush
        get() = Brush.horizontalGradient(
            colors = listOf(
                Color(0xFF6A1B9A),
                Color(0xFF8E24AA),
                Color(0xFFAB47BC),
                Color(0xFF5E35B1)
            )
        )

    val bottomNavArcBrush: Brush
        get() = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF5A1D9A),
                Color(0xFF380E6B),
                Color(0xFF1E063D)
            )
        )
}

/**
 * Applies a multi-layered 3D metallic golden ornate border with corner jewels and inner specular glow.
 */
fun Modifier.ornateGolden3DContainer(
    cornerRadius: Dp = 18.dp,
    backgroundBrush: Brush = Brush.verticalGradient(
        listOf(Color(0xFF6D28D9), Color(0xFF5B21B6), Color(0xFF4C1D95))
    ),
    borderWidth: Dp = 1.8.dp,
    glowColor: Color = Color(0xFFC084FC)
): Modifier = this
    .shadow(
        elevation = 10.dp,
        shape = RoundedCornerShape(cornerRadius),
        ambientColor = glowColor,
        spotColor = glowColor
    )
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundBrush)
    .drawWithCache {
        val radiusPx = cornerRadius.toPx()
        val strokePx = borderWidth.toPx()
        val outerBrush = DynamicThemeManager.goldMetallicBrush
        val innerHighlight = Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.42f), Color.Transparent, Color(0xFFFFD700).copy(alpha = 0.2f))
        )
        onDrawWithContent {
            drawContent()
            // Outer 3D metallic gold bevel
            drawRoundRect(
                brush = outerBrush,
                topLeft = Offset(strokePx / 2, strokePx / 2),
                size = Size(size.width - strokePx, size.height - strokePx),
                cornerRadius = CornerRadius(radiusPx, radiusPx),
                style = Stroke(width = strokePx)
            )
            // Inner specular 3D rim
            drawRoundRect(
                brush = innerHighlight,
                topLeft = Offset(strokePx * 1.7f, strokePx * 1.7f),
                size = Size(size.width - strokePx * 3.4f, size.height - strokePx * 3.4f),
                cornerRadius = CornerRadius((radiusPx - strokePx).coerceAtLeast(2f)),
                style = Stroke(width = 1.dp.toPx())
            )
            // Corner jewel rivets
            val jewelOffset = 8.dp.toPx()
            val corners = listOf(
                Offset(jewelOffset, jewelOffset),
                Offset(size.width - jewelOffset, jewelOffset),
                Offset(jewelOffset, size.height - jewelOffset),
                Offset(size.width - jewelOffset, size.height - jewelOffset)
            )
            corners.forEach { pt ->
                drawCircle(
                    color = Color(0xFFFFF59D),
                    radius = 2.6.dp.toPx(),
                    center = pt
                )
            }
        }
    }

/**
 * Applies a 3D glossy button effect with top specular shine and metallic rim.
 */
fun Modifier.glossy3DButton(
    cornerRadius: Dp = 24.dp,
    baseBrush: Brush = DynamicThemeManager.goldVertical3DBrush,
    rimBrush: Brush = DynamicThemeManager.goldMetallicBrush
): Modifier = this
    .shadow(8.dp, RoundedCornerShape(cornerRadius), spotColor = Color(0xFFFFD700))
    .clip(RoundedCornerShape(cornerRadius))
    .background(baseBrush)
    .border(1.8.dp, rimBrush, RoundedCornerShape(cornerRadius))
    .drawWithCache {
        val r = cornerRadius.toPx()
        onDrawWithContent {
            // Top glossy specular reflection band
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.45f), Color.Transparent)
                ),
                topLeft = Offset(4.dp.toPx(), 2.dp.toPx()),
                size = Size(size.width - 8.dp.toPx(), size.height * 0.45f),
                cornerRadius = CornerRadius(r, r)
            )
            drawContent()
        }
    }
