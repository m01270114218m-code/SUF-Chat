package com.voicerooms.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicerooms.app.data.model.AppNotification
import com.voicerooms.app.data.model.Profile
import com.voicerooms.app.data.repository.AuthRepository
import com.voicerooms.app.data.repository.ProfileRepository
import com.voicerooms.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SocialUiState(
    val loading: Boolean = true,
    val notifications: List<AppNotification> = emptyList(),
    val searchResults: List<Profile> = emptyList(),
    val query: String = "",
)

class SocialViewModel(
    private val profileRepo: ProfileRepository,
    private val authRepo: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SocialUiState())
    val state: StateFlow<SocialUiState> = _state.asStateFlow()

    init { loadNotifications() }

    fun loadNotifications() {
        val uid = authRepo.currentUserId ?: run {
            _state.value = _state.value.copy(loading = false)
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            when (val r = profileRepo.getNotifications(uid)) {
                is ApiResult.Success -> _state.value = _state.value.copy(notifications = r.data, loading = false)
                else -> _state.value = _state.value.copy(loading = false)
            }
        }
    }

    fun search(query: String) {
        _state.value = _state.value.copy(query = query)
        if (query.isBlank()) {
            _state.value = _state.value.copy(searchResults = emptyList())
            return
        }
        viewModelScope.launch {
            (profileRepo.searchUsers(query) as? ApiResult.Success)?.let {
                _state.value = _state.value.copy(searchResults = it.data)
            }
        }
    }
}
