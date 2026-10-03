package com.voicerooms.app.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.voicerooms.app.VoiceRoomsApp
import com.voicerooms.app.ui.viewmodel.AgencyViewModel
import com.voicerooms.app.ui.viewmodel.AuthViewModel
import com.voicerooms.app.ui.viewmodel.CreateRoomViewModel
import com.voicerooms.app.ui.viewmodel.GiftsViewModel
import com.voicerooms.app.ui.viewmodel.HomeViewModel
import com.voicerooms.app.ui.viewmodel.ProfileViewModel
import com.voicerooms.app.ui.viewmodel.RoomViewModel
import com.voicerooms.app.ui.viewmodel.SettingsViewModel
import com.voicerooms.app.ui.viewmodel.SocialViewModel

/**
 * مصنع موحّد لكل الـ ViewModels — يمرّر المستودعات من AppContainer.
 */
object AppViewModelProvider {

    val Factory = viewModelFactory {
        initializer {
            val c = app().container
            AuthViewModel(c.authRepository, c.profileRepository)
        }
        initializer {
            val c = app().container
            HomeViewModel(c.roomRepository, c.settingsRepository)
        }
        initializer {
            val c = app().container
            RoomViewModel(c.roomRepository, c.giftRepository, c.profileRepository)
        }
        initializer {
            val c = app().container
            ProfileViewModel(c.profileRepository, c.authRepository, c.settingsRepository, c.agencyRepository, c.roomRepository)
        }
        initializer {
            val c = app().container
            CreateRoomViewModel(c.roomRepository, c.settingsRepository)
        }
        initializer {
            val c = app().container
            AgencyViewModel(c.agencyRepository, c.profileRepository)
        }
        initializer {
            val c = app().container
            SettingsViewModel(c.settingsRepository)
        }
        initializer {
            val c = app().container
            SocialViewModel(c.profileRepository, c.authRepository)
        }
        initializer {
            val c = app().container
            GiftsViewModel(c.giftRepository)
        }
    }

    private fun androidx.lifecycle.viewmodel.CreationExtras.app(): VoiceRoomsApp =
        this[APPLICATION_KEY] as VoiceRoomsApp
}
