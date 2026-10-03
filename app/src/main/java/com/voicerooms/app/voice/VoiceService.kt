package com.voicerooms.app.voice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.voicerooms.app.MainActivity
import com.voicerooms.app.R

/**
 * خدمة أمامية (Foreground Service) تُبقي جلسة الصوت نشطة أثناء وجود المستخدم داخل الغرفة
 * حتى لو انتقل التطبيق للخلفية أو أُطفئت الشاشة.
 *
 * تُشغَّل عند دخول الغرفة وتُوقَف عند مغادرتها. النوع ForegroundServiceType = microphone
 * (مُعلَن في AndroidManifest) وهو مطلوب على أندرويد 14+.
 */
class VoiceService : Service() {

    companion object {
        const val CHANNEL_ID = "voice_room_channel"
        const val NOTIFICATION_ID = 4201

        const val EXTRA_ROOM_NAME = "extra_room_name"
        const val ACTION_START = "com.voicerooms.app.action.VOICE_START"
        const val ACTION_STOP = "com.voicerooms.app.action.VOICE_STOP"

        /** تشغيل الخدمة (بدء الجلسة الصوتية). */
        fun start(context: Context, roomName: String) {
            val intent = Intent(context, VoiceService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_ROOM_NAME, roomName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /** إيقاف الخدمة (إنهاء الجلسة الصوتية). */
        fun stop(context: Context) {
            val intent = Intent(context, VoiceService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var roomName: String = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForegroundCompat()
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                roomName = intent?.getStringExtra(EXTRA_ROOM_NAME) ?: "غرفة صوتية"
                startForeground(NOTIFICATION_ID, buildNotification(roomName))
            }
        }
        return START_STICKY
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "المحادثة الصوتية",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "إبقاء الصوت نشطًا أثناء التواجد في الغرفة"
                    setShowBadge(false)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    private fun buildNotification(roomName: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPending = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val stopIntent = Intent(this, VoiceService::class.java).apply { action = ACTION_STOP }
        val stopPending = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val icon = applicationInfo.icon.takeIf { it != 0 } ?: android.R.drawable.ic_btn_speak_now

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("أنت داخل: $roomName")
            .setContentText("المحادثة الصوتية جارية… اضغط للعودة")
            .setSmallIcon(icon)
            .setOngoing(true)
            .setContentIntent(contentPending)
            .addAction(icon, "خروج", stopPending)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .build()
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
