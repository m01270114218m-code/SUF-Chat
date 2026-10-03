package com.voicerooms.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicerooms.app.data.model.Gift
import com.voicerooms.app.data.model.Room
import com.voicerooms.app.data.model.RoomMember
import com.voicerooms.app.data.model.RoomMessage
import com.voicerooms.app.data.repository.GiftRepository
import com.voicerooms.app.data.repository.ProfileRepository
import com.voicerooms.app.data.repository.RoomRepository
import com.voicerooms.app.util.ApiResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class RoomUiState(
    val loading: Boolean = true,
    val room: Room? = null,
    val members: List<RoomMember> = emptyList(),
    val messages: List<RoomMessage> = emptyList(),
    val gifts: List<Gift> = emptyList(),
    val myMember: RoomMember? = null,
    val myUserId: String = "",
    val isMuted: Boolean = true,
    val isHandUp: Boolean = false,
    val onSeat: Boolean = false,
    val showGiftSheet: Boolean = false,
    val activeGiftAnim: Gift? = null,
    val activeGiftSender: String? = null,
    val error: String? = null,
)

class RoomViewModel(
    private val roomRepo: RoomRepository,
    private val giftRepo: GiftRepository,
    private val profileRepo: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RoomUiState())
    val state: StateFlow<RoomUiState> = _state.asStateFlow()

    private var roomId: String = ""
    private var pollJob: kotlinx.coroutines.Job? = null

    fun enter(roomId: String, userId: String, password: String? = null, onNeedPassword: () -> Unit) {
        this.roomId = roomId
        _state.value = _state.value.copy(myUserId = userId, loading = true)
        viewModelScope.launch {
            when (val r = roomRepo.joinRoom(roomId, password)) {
                is ApiResult.Error -> {
                    if (r.message.contains("كلمة المرور")) { onNeedPassword(); _state.value = _state.value.copy(loading = false) }
                    else _state.value = _state.value.copy(loading = false, error = r.message)
                }
                else -> {
                    loadRoom()
                    (giftRepo.getGifts() as? ApiResult.Success)?.let {
                        _state.value = _state.value.copy(gifts = it.data)
                    }
                    startPolling()
                }
            }
        }
    }

    private fun loadRoom() {
        viewModelScope.launch {
            (roomRepo.getRoom(roomId) as? ApiResult.Success)?.let { _state.value = _state.value.copy(room = it.data) }
            refreshMembers()
            (roomRepo.getMessages(roomId) as? ApiResult.Success)?.let { _state.value = _state.value.copy(messages = it.data) }
            _state.value = _state.value.copy(loading = false)
        }
    }

    private fun refreshMembers() {
        viewModelScope.launch {
            when (val r = roomRepo.getMembers(roomId)) {
                is ApiResult.Success -> {
                    val mine = r.data.firstOrNull { it.userId == _state.value.myUserId }
                    _state.value = _state.value.copy(
                        members = r.data,
                        myMember = mine,
                        isMuted = mine?.isMuted ?: true,
                        isHandUp = mine?.isHandUp ?: false,
                        onSeat = mine?.seatIndex != null,
                    )
                }
                else -> {}
            }
        }
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                refreshMembers()
                (roomRepo.getMessages(roomId) as? ApiResult.Success)?.let {
                    _state.value = _state.value.copy(messages = it.data)
                }
                delay(3000)
            }
        }
    }

    // ---- المقاعد ----
    fun takeSeat(seat: Int) {
        viewModelScope.launch {
            when (val r = roomRepo.takeSeat(roomId, seat)) {
                is ApiResult.Error -> _state.value = _state.value.copy(error = r.message)
                else -> refreshMembers()
            }
        }
    }

    fun leaveSeat() {
        viewModelScope.launch {
            roomRepo.leaveSeat(roomId)
            refreshMembers()
        }
    }

    fun toggleMute() {
        val newMute = !_state.value.isMuted
        viewModelScope.launch {
            roomRepo.setMuted(roomId, _state.value.myUserId, newMute)
            _state.value = _state.value.copy(isMuted = newMute)
        }
    }

    fun toggleHand() {
        val up = !_state.value.isHandUp
        viewModelScope.launch {
            roomRepo.setHandUp(roomId, _state.value.myUserId, up)
            _state.value = _state.value.copy(isHandUp = up)
        }
    }

    // ---- الشات ----
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            roomRepo.sendMessage(roomId, _state.value.myUserId, text.trim())
            (roomRepo.getMessages(roomId) as? ApiResult.Success)?.let {
                _state.value = _state.value.copy(messages = it.data)
            }
        }
    }

    // ---- الهدايا ----
    fun showGifts(show: Boolean) { _state.value = _state.value.copy(showGiftSheet = show) }

    fun sendGift(gift: Gift, receiverId: String) {
        viewModelScope.launch {
            when (val r = giftRepo.sendGift(roomId, receiverId, gift.id, 1)) {
                is ApiResult.Error -> _state.value = _state.value.copy(error = r.message)
                else -> {
                    _state.value = _state.value.copy(
                        showGiftSheet = false,
                        activeGiftAnim = gift,
                        activeGiftSender = _state.value.myUserId,
                    )
                    // تحديث رصيد المستخدم
                    profileRepo.getProfile(_state.value.myUserId)
                }
            }
        }
    }

    fun clearGiftAnim() { _state.value = _state.value.copy(activeGiftAnim = null) }

    // ---- أدوات المشرف ----
    fun adminMute(userId: String, mute: Boolean) = viewModelScope.launch {
        roomRepo.adminMute(roomId, userId, mute); refreshMembers()
    }

    fun kick(userId: String) = viewModelScope.launch {
        roomRepo.kickUser(roomId, userId); refreshMembers()
    }

    fun ban(userId: String) = viewModelScope.launch {
        roomRepo.banUser(roomId, userId); refreshMembers()
    }

    fun setRole(userId: String, role: String) = viewModelScope.launch {
        roomRepo.setMemberRole(roomId, userId, role); refreshMembers()
    }

    fun toggleRoomLock() = viewModelScope.launch {
        val locked = !(_state.value.room?.isLocked ?: false)
        roomRepo.updateRoom(roomId, mapOf("is_locked" to locked))
        (roomRepo.getRoom(roomId) as? ApiResult.Success)?.let { _state.value = _state.value.copy(room = it.data) }
    }

    fun toggleMicLock() = viewModelScope.launch {
        val locked = !(_state.value.room?.micLocked ?: false)
        roomRepo.updateRoom(roomId, mapOf("mic_locked" to locked))
        (roomRepo.getRoom(roomId) as? ApiResult.Success)?.let { _state.value = _state.value.copy(room = it.data) }
    }

    fun leave(onDone: () -> Unit) {
        viewModelScope.launch {
            roomRepo.leaveRoom(roomId)
            onDone()
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }
}
