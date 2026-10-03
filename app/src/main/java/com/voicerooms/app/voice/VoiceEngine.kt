package com.voicerooms.app.voice

import android.content.Context
import android.media.AudioManager
import android.util.Log
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.JavaAudioDeviceModule
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import java.util.concurrent.ConcurrentHashMap

/**
 * محرّك الصوت عبر WebRTC.
 *
 * يدير:
 *  - تهيئة PeerConnectionFactory + وحدة الصوت (JavaAudioDeviceModule) مع إلغاء الصدى وتنقية الضوضاء.
 *  - مسار الصوت المحلي (الميكروفون) كـ AudioTrack يُضاف لكل اتصال.
 *  - اتصال PeerConnection لكل مشارك في الغرفة (Mesh).
 *  - توجيه الصوت (سماعة الأذن / مكبر الصوت / البلوتوث).
 *
 * التوقيع (Signaling) يتم خارجيًا عبر [SignalingClient] (Supabase Realtime Broadcast)،
 * ويربط هذا المحرّك أحداثه عبر [onLocalSdp] و [onLocalIce].
 */
class VoiceEngine(private val context: Context) {

    companion object {
        private const val TAG = "VoiceEngine"
        private const val AUDIO_TRACK_ID = "voicerooms-audio"
        private const val STREAM_ID = "voicerooms-stream"

        // خوادم STUN عامة — يمكن استبدالها بخادم TURN خاص عند الحاجة
        private val ICE_SERVERS = listOf(
            "stun:stun.l.google.com:19302",
            "stun:stun1.l.google.com:19302",
        )
    }

    private var factory: PeerConnectionFactory? = null
    private var audioDeviceModule: JavaAudioDeviceModule? = null
    private var audioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null

    private val peers = ConcurrentHashMap<String, PeerConnection>()
    private val pendingIce = ConcurrentHashMap<String, MutableList<IceCandidate>>()

    private val audioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    @Volatile private var initialized = false
    @Volatile private var muted = true
    @Volatile private var speakerOn = true

    /** يُستدعى عند توليد SDP محلي (offer أو answer) لإرساله للمشارك. */
    var onLocalSdp: ((peerId: String, sdp: SessionDescription) -> Unit)? = null

    /** يُستدعى عند توليد مرشّح ICE محلي لإرساله للمشارك. */
    var onLocalIce: ((peerId: String, candidate: IceCandidate) -> Unit)? = null

    /** يُستدعى عند تغيّر حالة الاتصال بمشارك. */
    var onConnectionState: ((peerId: String, state: PeerConnection.PeerConnectionState) -> Unit)? = null

    val isInitialized: Boolean get() = initialized

    /** تهيئة محرّك WebRTC — تُستدعى مرة واحدة عند دخول الغرفة. */
    @Synchronized
    fun init(): Boolean {
        if (initialized) return true
        return try {
            val initOptions = PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(initOptions)

            val adm = JavaAudioDeviceModule.builder(context)
                .setUseHardwareAcousticEchoCanceler(true)
                .setUseHardwareNoiseSuppressor(true)
                .createAudioDeviceModule()
            audioDeviceModule = adm

            val eglBase = EglBase.create()
            val encoderFactory = DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
            val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

            factory = PeerConnectionFactory.builder()
                .setOptions(PeerConnectionFactory.Options())
                .setAudioDeviceModule(adm)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory()

            audioSource = factory?.createAudioSource(MediaConstraints())
            localAudioTrack = factory?.createAudioTrack(AUDIO_TRACK_ID, audioSource)
            localAudioTrack?.setEnabled(!muted)

            initialized = true
            applyAudioRoute()
            Log.i(TAG, "VoiceEngine initialized")
            true
        } catch (t: Throwable) {
            Log.e(TAG, "VoiceEngine init failed", t)
            initialized = false
            false
        }
    }

    /** إنشاء اتصال بمشارك. إذا كان [isOfferer] صحيحًا يتم توليد offer تلقائيًا. */
    @Synchronized
    fun createPeer(peerId: String, isOfferer: Boolean) {
        if (!initialized && !init()) return
        if (peers.containsKey(peerId)) return

        val iceServers = ICE_SERVERS.map { url ->
            PeerConnection.IceServer.builder(url).createIceServer()
        }
        val config = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }

        val pc = factory?.createPeerConnection(config, object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(candidate: IceCandidate?) {
                candidate?.let { onLocalIce?.invoke(peerId, it) }
            }
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
            override fun onAddStream(stream: MediaStream?) {}
            override fun onRemoveStream(stream: MediaStream?) {}
            override fun onDataChannel(channel: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {}
            override fun onTrack(transceiver: RtpTransceiver?) {}
            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState?) {
                newState?.let { onConnectionState?.invoke(peerId, it) }
            }
        }) ?: return

        localAudioTrack?.let { pc.addTrack(it, listOf(STREAM_ID)) }
        peers[peerId] = pc

        if (isOfferer) {
            val constraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            }
            pc.createOffer(object : SdpAdapter() {
                override fun onCreateSuccess(sdp: SessionDescription?) {
                    sdp ?: return
                    pc.setLocalDescription(SdpAdapter(), sdp)
                    onLocalSdp?.invoke(peerId, sdp)
                }
            }, constraints)
        }
    }

    /** معالجة وصف جلسة قادم (offer/answer) من مشارك. */
    @Synchronized
    fun setRemoteSdp(peerId: String, sdp: SessionDescription) {
        val pc = peers[peerId] ?: return
        pc.setRemoteDescription(object : SdpAdapter() {
            override fun onSetSuccess() {
                // إذا كان offer، نولّد answer
                if (sdp.type == SessionDescription.Type.OFFER) {
                    val constraints = MediaConstraints().apply {
                        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                    }
                    pc.createAnswer(object : SdpAdapter() {
                        override fun onCreateSuccess(answer: SessionDescription?) {
                            answer ?: return
                            pc.setLocalDescription(SdpAdapter(), answer)
                            onLocalSdp?.invoke(peerId, answer)
                        }
                    }, constraints)
                }
                // أضف أي مرشّحات كانت في الانتظار
                pendingIce.remove(peerId)?.forEach { pc.addIceCandidate(it) }
            }
        }, sdp)
    }

    /** معالجة مرشّح ICE قادم من مشارك. */
    @Synchronized
    fun addRemoteIce(peerId: String, candidate: IceCandidate) {
        val pc = peers[peerId]
        if (pc == null) {
            pendingIce.getOrPut(peerId) { mutableListOf() }.add(candidate)
            return
        }
        pc.addIceCandidate(candidate)
    }

    fun hasPeer(peerId: String): Boolean = peers.containsKey(peerId)

    /** إزالة اتصال مشارك. */
    @Synchronized
    fun removePeer(peerId: String) {
        peers.remove(peerId)?.let {
            runCatching { it.close() }
            runCatching { it.dispose() }
        }
        pendingIce.remove(peerId)
    }

    /** كتم/فتح الميكروفون المحلي. */
    fun setMuted(mute: Boolean) {
        muted = mute
        localAudioTrack?.setEnabled(!mute)
    }

    /** تشغيل/إيقاف مكبر الصوت. */
    fun setSpeaker(on: Boolean) {
        speakerOn = on
        applyAudioRoute()
    }

    fun isSpeakerOn(): Boolean = speakerOn

    private fun applyAudioRoute() {
        val am = audioManager ?: return
        runCatching {
            am.mode = AudioManager.MODE_IN_COMMUNICATION
            am.isSpeakerphoneOn = speakerOn
            @Suppress("DEPRECATION")
            am.isMicrophoneMute = muted
        }
    }

    /** تحرير كل الموارد — تُستدعى عند مغادرة الغرفة. */
    @Synchronized
    fun dispose() {
        peers.keys.toList().forEach { removePeer(it) }
        peers.clear()
        pendingIce.clear()
        runCatching { localAudioTrack?.dispose() }
        runCatching { audioSource?.dispose() }
        runCatching { factory?.dispose() }
        runCatching { audioDeviceModule?.release() }
        localAudioTrack = null
        audioSource = null
        factory = null
        audioDeviceModule = null
        initialized = false
        runCatching {
            audioManager?.let {
                it.mode = AudioManager.MODE_NORMAL
                it.isSpeakerphoneOn = false
            }
        }
        Log.i(TAG, "VoiceEngine disposed")
    }

    /** مُنفّذ SdpObserver مبسّط. */
    private open class SdpAdapter : SdpObserver {
        override fun onCreateSuccess(sdp: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(error: String?) { Log.e(TAG, "createFailure: $error") }
        override fun onSetFailure(error: String?) { Log.e(TAG, "setFailure: $error") }
    }
}
