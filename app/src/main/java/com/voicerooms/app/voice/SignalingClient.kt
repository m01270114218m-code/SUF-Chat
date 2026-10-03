package com.voicerooms.app.voice

import com.voicerooms.app.data.remote.SupabaseProvider
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.broadcast
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * رسالة توقيع (Signaling) بين المشاركين عبر Supabase Realtime Broadcast.
 */
@Serializable
data class SignalMessage(
    val type: String,          // join | sdp | ice | leave
    val from: String,
    val to: String? = null,
    val sdp: String? = null,
    val sdpType: String? = null,
    val candidate: String? = null,
    val sdpMid: String? = null,
    val sdpMLineIndex: Int? = null,
)

/**
 * عميل التوقيع — يستخدم قناة Realtime عامة لكل غرفة ("room-signal-<roomId>").
 * كل الرسائل تُبثّ على الحدث "signal"، ويتم فلترة المرسل/المستقبل في الطبقة الأعلى.
 */
class SignalingClient {

    private val supabase get() = SupabaseProvider.client
    private var channel: RealtimeChannel? = null

    /** يُستدعى عند وصول رسالة توقيع من مشارك آخر. */
    var onMessage: ((SignalMessage) -> Unit)? = null

    /** الاتصال بقناة الغرفة والاشتراك في البث. */
    suspend fun connect(roomId: String, scope: CoroutineScope) {
        val ch = supabase.channel("room-signal-$roomId")
        channel = ch

        ch.broadcastFlow<SignalMessage>(event = "signal")
            .onEach { msg -> onMessage?.invoke(msg) }
            .launchIn(scope)

        ch.subscribe(blockUntilSubscribed = true)
    }

    /** إرسال رسالة توقيع لكل المشاركين في القناة. */
    suspend fun send(msg: SignalMessage) {
        val ch = channel ?: return
        ch.broadcast(
            event = "signal",
            message = buildJsonObject {
                put("type", msg.type)
                put("from", msg.from)
                put("to", msg.to)
                put("sdp", msg.sdp)
                put("sdpType", msg.sdpType)
                put("candidate", msg.candidate)
                put("sdpMid", msg.sdpMid)
                put("sdpMLineIndex", msg.sdpMLineIndex)
            },
        )
    }

    /** قطع الاتصال وإزالة القناة. */
    suspend fun disconnect() {
        channel?.let { runCatching { supabase.realtime.removeChannel(it) } }
        channel = null
    }
}
