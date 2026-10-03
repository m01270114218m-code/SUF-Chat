package com.voicerooms.app.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.Text
import com.opensource.svgaplayer.SVGACallback
import com.opensource.svgaplayer.SVGAImageView
import com.opensource.svgaplayer.SVGAParser
import com.opensource.svgaplayer.SVGAVideoEntity
import com.voicerooms.app.ui.theme.Cyan
import com.voicerooms.app.ui.theme.TextPrimary
import java.net.URL

/**
 * مشغّل SVGA (الهدايا والوجهات المتحركة).
 * يحمّل ملف .svga من رابط (عادةً من Supabase Storage) ويشغّله داخل Compose.
 */
@Composable
fun SvgaPlayer(
    url: String?,
    modifier: Modifier = Modifier,
    loops: Int = 1,
    onFinished: (() -> Unit)? = null,
) {
    if (url.isNullOrBlank()) return

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            SVGAImageView(ctx).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                isClickable = false
                this.loops = loops
            }
        },
        update = { view ->
            view.callback = object : SVGACallback {
                override fun onFinished() { onFinished?.invoke() }
                override fun onPause() {}
                override fun onRepeat() {}
                override fun onStep(frame: Int, percentage: Double) {}
            }
            try {
                val parser = SVGAParser(view.context)
                parser.decodeFromURL(URL(url), object : SVGAParser.ParseCompletion {
                    override fun onComplete(videoItem: SVGAVideoEntity) {
                        view.setVideoItem(videoItem)
                        view.startAnimation()
                    }
                    override fun onError() {
                        onFinished?.invoke()
                    }
                })
            } catch (e: Exception) {
                onFinished?.invoke()
            }
        },
    )
}

/**
 * طبقة عرض الهدية المتحركة فوق شاشة الغرفة — تظهر الاسم + الأنيميشن ثم تختفي تلقائيًا.
 */
@Composable
fun GiftAnimationOverlay(
    visible: Boolean,
    animationUrl: String?,
    senderName: String?,
    receiverName: String?,
    giftName: String?,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (!animationUrl.isNullOrBlank()) {
                    SvgaPlayer(
                        url = animationUrl,
                        loops = 1,
                        modifier = Modifier.size(240.dp),
                        onFinished = onFinished,
                    )
                } else {
                    Spacer(Modifier.height(120.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "${senderName ?: ""} أرسل ${giftName ?: "هدية"} إلى ${receiverName ?: ""}",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.horizontalGradient(listOf(Cyan, Color.White)),
                    ),
                )
            }
        }
    }
}
