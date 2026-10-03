package com.voicerooms.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicerooms.app.data.model.Agency
import com.voicerooms.app.data.model.Level
import com.voicerooms.app.data.model.Profile
import com.voicerooms.app.data.model.ProfileUpdate
import com.voicerooms.app.data.model.Room
import com.voicerooms.app.data.model.WalletTransaction
import com.voicerooms.app.data.repository.AgencyRepository
import com.voicerooms.app.data.repository.AuthRepository
import com.voicerooms.app.data.repository.ProfileRepository
import com.voicerooms.app.data.repository.RoomRepository
import com.voicerooms.app.data.repository.SettingsRepository
import com.voicerooms.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val profile: Profile? = null,
    val levels: List<Level> = emptyList(),
    val myRooms: List<Room> = emptyList(),
    val walletTransactions: List<WalletTransaction> = emptyList(),
    val agency: Agency? = null,
    val isFollowing: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isMe: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

/**
 * ViewModel الملف الشخصي — يعمل لملفي الشخصي ولملف أي مستخدم آخر.
 */
class ProfileViewModel(
    private val profileRepo: ProfileRepository,
    private val authRepo: AuthRepository,
    private val settingsRepo: SettingsRepository,
    private val agencyRepo: AgencyRepository,
    private val roomRepo: RoomRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    val myUserId: String? get() = authRepo.currentUserId

    init {
        viewModelScope.launch {
            (settingsRepo.getLevels() as? ApiResult.Success)?.let {
                _state.value = _state.value.copy(levels = it.data)
            }
        }
    }

    fun loadMine() {
        val uid = myUserId ?: return
        load(uid)
    }

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val isMe = userId == myUserId
            val result = if (isMe) authRepo.getMyProfile() else authRepo.getProfile(userId)
            when (result) {
                is ApiResult.Success -> _state.value = _state.value.copy(profile = result.data, isMe = isMe)
                is ApiResult.Error -> _state.value = _state.value.copy(loading = false, error = result.message)
                else -> {}
            }
            val followers = profileRepo.getFollowersCount(userId)
            val following = profileRepo.getFollowingCount(userId)
            val isFollowing = myUserId?.let { profileRepo.isFollowing(it, userId) } ?: false
            _state.value = _state.value.copy(
                followersCount = followers,
                followingCount = following,
                isFollowing = isFollowing,
                loading = false,
            )
        }
    }

    fun loadMyRooms() {
        val uid = myUserId ?: return
        viewModelScope.launch {
            (roomRepo.getMyRooms(uid) as? ApiResult.Success)?.let {
                _state.value = _state.value.copy(myRooms = it.data)
            }
        }
    }

    fun loadAgency() {
        val uid = myUserId ?: return
        viewModelScope.launch {
            when (val r = agencyRepo.getMyAgency(uid)) {
                is ApiResult.Success -> _state.value = _state.value.copy(agency = r.data)
                else -> {}
            }
        }
    }

    fun loadWallet() {
        val uid = myUserId ?: return
        viewModelScope.launch {
            (profileRepo.getWalletTransactions(uid) as? ApiResult.Success)?.let {
                _state.value = _state.value.copy(walletTransactions = it.data)
            }
        }
    }

    fun updateProfile(update: ProfileUpdate, onDone: () -> Unit = {}) {
        val uid = myUserId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true, error = null, message = null)
            when (val r = profileRepo.updateProfile(uid, update)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(saving = false, profile = r.data, message = "تم حفظ التغييرات")
                    onDone()
                }
                is ApiResult.Error -> _state.value = _state.value.copy(saving = false, error = r.message)
                else -> {}
            }
        }
    }

    fun uploadAvatar(bytes: ByteArray, ext: String = "jpg") {
        val uid = myUserId ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true, error = null)
            when (val up = profileRepo.uploadAvatar(uid, bytes, ext)) {
                is ApiResult.Success -> {
                    when (val upd = profileRepo.updateProfile(uid, ProfileUpdate(avatarUrl = up.data))) {
                        is ApiResult.Success -> _state.value = _state.value.copy(
                            saving = false, profile = upd.data, message = "تم تحديث الصورة"
                        )
                        is ApiResult.Error -> _state.value = _state.value.copy(saving = false, error = upd.message)
                        else -> {}
                    }
                }
                is ApiResult.Error -> _state.value = _state.value.copy(saving = false, error = up.message)
                else -> {}
            }
        }
    }

    fun toggleFollow() {
        val uid = myUserId ?: return
        val target = _state.value.profile?.id ?: return
        if (target == uid) return
        viewModelScope.launch {
            if (_state.value.isFollowing) profileRepo.unfollow(uid, target)
            else profileRepo.follow(uid, target)
            load(target)
        }
    }

    fun recharge(amount: Long) {
        val uid = myUserId ?: return
        viewModelScope.launch {
            when (val r = profileRepo.recharge(uid, amount)) {
                is ApiResult.Error -> _state.value = _state.value.copy(error = r.message)
                else -> {
                    _state.value = _state.value.copy(message = "تم شحن $amount عملة")
                    load(uid)
                }
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, message = null)
    }
}
