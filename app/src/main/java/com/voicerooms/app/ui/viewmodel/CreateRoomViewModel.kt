package com.voicerooms.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicerooms.app.data.model.Category
import com.voicerooms.app.data.model.CreateRoomRequest
import com.voicerooms.app.data.model.Room
import com.voicerooms.app.data.repository.RoomRepository
import com.voicerooms.app.data.repository.SettingsRepository
import com.voicerooms.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateRoomUiState(
    val loading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val creating: Boolean = false,
    val createdRoom: Room? = null,
    val error: String? = null,
)

class CreateRoomViewModel(
    private val roomRepo: RoomRepository,
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CreateRoomUiState())
    val state: StateFlow<CreateRoomUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val cats = settingsRepo.getCategories()
            if (cats is ApiResult.Success) {
                _state.value = _state.value.copy(categories = cats.data, loading = false)
            } else {
                _state.value = _state.value.copy(loading = false)
            }
        }
    }

    fun create(
        userId: String,
        name: String,
        description: String?,
        categoryId: Int?,
        roomType: String,
        maxSeats: Int,
        welcomeMsg: String?,
        coverUrl: String? = null,
        onCreated: (Room) -> Unit,
    ) {
        if (name.isBlank()) {
            _state.value = _state.value.copy(error = "أدخل اسم الغرفة")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(creating = true, error = null)
            val req = CreateRoomRequest(
                name = name.trim(),
                description = description?.trim(),
                coverUrl = coverUrl,
                categoryId = categoryId,
                roomType = roomType,
                maxSeats = maxSeats,
                welcomeMsg = welcomeMsg?.trim(),
            )
            when (val r = roomRepo.createRoom(userId, req)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(creating = false, createdRoom = r.data)
                    onCreated(r.data)
                }
                is ApiResult.Error -> _state.value = _state.value.copy(creating = false, error = r.message)
                else -> {}
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
