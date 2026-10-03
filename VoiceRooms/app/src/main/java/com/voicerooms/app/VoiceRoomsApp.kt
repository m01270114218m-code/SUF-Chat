package com.voicerooms.app

import android.app.Application
import com.voicerooms.app.di.AppContainer

/**
 * كلاس التطبيق — يهيّئ الحاوية العامة للتبعيات.
 */
class VoiceRoomsApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
