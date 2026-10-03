package com.voicerooms.app.di

import com.voicerooms.app.data.repository.AgencyRepository
import com.voicerooms.app.data.repository.AuthRepository
import com.voicerooms.app.data.repository.GiftRepository
import com.voicerooms.app.data.repository.ProfileRepository
import com.voicerooms.app.data.repository.RoomRepository
import com.voicerooms.app.data.repository.SettingsRepository

/**
 * حاوية التبعيات اليدوية (Manual DI) — تُنشأ مرة واحدة في Application.
 * بسيطة وموثوقة بدون مكتبات حقن إضافية.
 */
class AppContainer {
    val authRepository: AuthRepository by lazy { AuthRepository() }
    val profileRepository: ProfileRepository by lazy { ProfileRepository() }
    val roomRepository: RoomRepository by lazy { RoomRepository() }
    val giftRepository: GiftRepository by lazy { GiftRepository() }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository() }
    val agencyRepository: AgencyRepository by lazy { AgencyRepository() }
}
