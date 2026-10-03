package com.voicerooms.app.voice

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription

/**
 * مُنسّق جلسة الصوت داخل الغرفة.
 * يربط [VoiceEngine] (WebRTC) مع [SignalingClient] (Supabase Realtime) لإتمام الاتصال بين المشاركين.
 *
 * نموذج الاتصال: Mesh — كل مشارك يتصل مباشرة بكل مشارك آخر.
 * عند دخول عضو جديد يبثّ رسالة "join"، فيُنشئ كل عضو موجود عرضًا (offer) له.
 */
class VoiceRoomController(
    context: Context,
    private val roomId: String,
    private val myUserId: String,
) {

    companion object { private const val TAG = "VoiceRoomController" }

    private val engine = VoiceEngine(context.applicationContext)
    private val signaling = SignalingClient()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /** حالة اتصال مشارك (متصل/فشل/منقطع). */
    var onPeerState: ((userId: String, state: PeerConnection.PeerConnectionState) -> Unit)? = null

    /** عدد المشاركين المتصلين صوتيًا. */
    var onPeerCount: ((Int) -> Unit)? = null

    private val connected = mutableSetOf<String>()

    fun start() {
        // ربط أحداث المحرّك بالتوقيع
        engine.onLocalSdp = { peerId, sdp ->
            scope.launch {
                signaling.send(
                    SignalMessage(
                        type = "sdp",
                        from = myUserId,
                        to = peerId,
                        sdp = sdp.description,
                        sdpType = sdp.type.canonicalForm(),
                    )
                )
            }
        }
        engine.onLocalIce = { peerId, cand ->
            scope.launch {
                signaling.send(
                    SignalMessage(
                        type = "ice",
                        from = myUserId,
                        to = peerId,
                        candidate = cand.sdp,
                        sdpMid = cand.sdpMid,
                        sdpMLineIndex = cand.sdpMLineIndex,
                    )
                )
            }
        }
        engine.onConnectionState = { peerId, state ->
            when (state) {
                PeerConnection.PeerConnectionState.CONNECTED -> connected.add(peerId)
                PeerConnection.PeerConnectionState.FAILED,
                PeerConnection.PeerConnectionState.CLOSED,
                PeerConnection.PeerConnectionState.DISCONNECTED -> connected.remove(peerId)
                else -> {}
            }
            onPeerState?.invoke(peerId, state)
            onPeerCount?.invoke(connected.size)
        }

        signaling.onMessage = { msg -> handleSignal(msg) }

        engine.init()
        engine.setMuted(true) // يبدأ مكتومًا حتى يفتح المستخدم الميكروفون

        scope.launch {
            try {
                signaling.connect(roomId, scope)
                signaling.send(SignalMessage(type = "join", from = myUserId))
            } catch (t: Throwable) {
                Log.e(TAG, "signaling connect failed", t)
            }
        }
    }

    private fun handleSignal(msg: SignalMessage) {
        if (msg.from == myUserId) return
        when (msg.type) {
            "join" -> {
                // عضو جديد دخل → نحن ننشئ العرض له
                engine.createPeer(msg.from, isOfferer = true)
            }
            "sdp" -> {
                val type = when (msg.sdpType) {
                    "offer" -> SessionDescription.Type.OFFER
                    "answer" -> SessionDescription.Type.ANSWER
                    else -> SessionDescription.Type.ANSWER
                }
                val sdp = msg.sdp ?: return
                if (!engine.hasPeer(msg.from)) {
                    engine.createPeer(msg.from, isOfferer = false)
                }
                engine.setRemoteSdp(msg.from, SessionDescription(type, sdp))
            }
            "ice" -> {
                val cand = msg.candidate ?: return
                engine.addRemoteIce(
                    msg.from,
                    IceCandidate(msg.sdpMid ?: "0", msg.sdpMLineIndex ?: 0, cand),
                )
            }
            "leave" -> {
                engine.removePeer(msg.from)
                connected.remove(msg.from)
                onPeerCount?.invoke(connected.size)
            }
        }
    }

    fun setMuted(mute: Boolean) = engine.setMuted(mute)

    fun setSpeaker(on: Boolean) = engine.setSpeaker(on)

    fun isSpeakerOn(): Boolean = engine.isSpeakerOn()

    fun stop() {
        runCatching {
            scope.launch { signaling.send(SignalMessage(type = "leave", from = myUserId)) }
        }
        signaling.onMessage = null
        runCatching { scope.launch { signaling.disconnect() } }
        engine.dispose()
        scope.cancel()
    }
}
