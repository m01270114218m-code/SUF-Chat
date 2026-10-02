package com.pharaohparty.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Login : Screen
    data object Home : Screen
    data class RoomScreen(val room: Room) : Screen
    data object Store : Screen
    data object Profile : Screen
}

class PartyViewModel(app: Application) : AndroidViewModel(app) {
    private val api = PartyApi(app)
    private val liveKit = LiveKitManager(app)

    private val _screen = MutableStateFlow<Screen>(Screen.Login)
    val screen: StateFlow<Screen> = _screen

    private val _profile = MutableStateFlow<Profile?>(null)
    val profile: StateFlow<Profile?> = _profile

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms

    private val _store = MutableStateFlow<List<StoreItem>>(emptyList())
    val store: StateFlow<List<StoreItem>> = _store

    private val _seats = MutableStateFlow<List<RoomSeat>>(emptyList())
    val seats: StateFlow<List<RoomSeat>> = _seats

    private val _messages = MutableStateFlow<List<RoomMessage>>(emptyList())
    val messages: StateFlow<List<RoomMessage>> = _messages

    private val _gifts = MutableStateFlow<List<Gift>>(emptyList())
    val gifts: StateFlow<List<Gift>> = _gifts

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy

    private val _micEnabled = MutableStateFlow(false)
    val micEnabled: StateFlow<Boolean> = _micEnabled

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        restore()
    }

    private fun restore() {
        viewModelScope.launch {
            runCatching {
                if (api.restore()) {
                    _profile.value = api.profile()
                    refresh()
                    _screen.value = Screen.Home
                }
            }
        }
    }

    fun login(username: String, password: String, create: Boolean) {
        viewModelScope.launch {
            _error.value = null
            runCatching {
                api.auth(
                    AuthRequest(
                        action = if (create) "register" else "login",
                        username = username.trim(),
                        password = password
                    )
                )
                _profile.value = api.profile()
                if (_profile.value?.is_banned == true) error("هذا الحساب محظور")
                refresh()
                _screen.value = Screen.Home
            }.onFailure {
                _error.value = it.message ?: "حدث خطأ"
            }
        }
    }

    fun quickLogin() {
        viewModelScope.launch {
            _error.value = null
            runCatching {
                api.auth(
                    AuthRequest(
                        action = "quick_login",
                        device_key = api.deviceKey()
                    )
                )
                _profile.value = api.profile()
                refresh()
                _screen.value = Screen.Home
            }.onFailure {
                _error.value = it.message ?: "تعذر الدخول السريع"
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching {
                _rooms.value = api.rooms()
                _store.value = api.store()
            }.onFailure {
                _error.value = it.message
            }
        }
    }

    fun open(room: Room) {
        _screen.value = Screen.RoomScreen(room)
        viewModelScope.launch {
            runCatching {
                api.joinRoom(room.id)
                liveKit.connect(api, room.id)
                _micEnabled.value =
                    liveKit.room?.localParticipant?.setMicrophoneEnabled(true) == true
            }.onFailure {
                _error.value = it.message
                _screen.value = Screen.Home
            }
        }
    }

    fun sendMessage(roomId: String, message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            runCatching {
                api.sendMessage(roomId, message.trim())
                _messages.value = api.messages(roomId)
            }.onFailure { _error.value = it.message }
        }
    }

    fun sendGift(roomId: String, receiverId: String, giftId: String) {
        viewModelScope.launch {
            runCatching {
                api.sendGift(roomId, receiverId, giftId)
                _profile.value = api.profile()
            }.onFailure { _error.value = it.message }
        }
    }

    fun buy(itemId: String) {
        viewModelScope.launch {
            runCatching {
                api.purchase(itemId)
                _profile.value = api.profile()
            }.onFailure { _error.value = it.message }
        }
    }

    fun createRoom(name: String, title: String) {
        viewModelScope.launch {
            _busy.value = true
            runCatching {
                val room = api.createRoom(name, title)
                _rooms.value = api.rooms()
                open(room)
            }.onFailure { _error.value = it.message }
            _busy.value = false
        }
    }

    fun toggleMic() {
        viewModelScope.launch {
            runCatching {
                val room = liveKit.room ?: return@runCatching
                _micEnabled.value =
                    room.localParticipant.setMicrophoneEnabled(!_micEnabled.value)
            }.onFailure {
                _error.value = it.message
            }
        }
    }

    fun home() {
        val current = _screen.value
        if (current is Screen.RoomScreen) {
            viewModelScope.launch {
                runCatching { api.leaveRoom(current.room.id) }
                liveKit.disconnect()
                _micEnabled.value = false
                _screen.value = Screen.Home
            }
        } else {
            _screen.value = Screen.Home
        }
    }

    fun store() {
        _screen.value = Screen.Store
    }

    fun profile() {
        _screen.value = Screen.Profile
    }

    fun clearError() {
        _error.value = null
    }

    override fun onCleared() {
        liveKit.disconnect()
        super.onCleared()
    }
}
