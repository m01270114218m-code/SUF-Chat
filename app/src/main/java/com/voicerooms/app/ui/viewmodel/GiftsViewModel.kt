package com.voicerooms.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicerooms.app.data.model.Gift
import com.voicerooms.app.data.repository.GiftRepository
import com.voicerooms.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GiftsUiState(
    val loading: Boolean = true,
    val gifts: List<Gift> = emptyList(),
    val error: String? = null,
)

class GiftsViewModel(
    private val giftRepo: GiftRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(GiftsUiState())
    val state: StateFlow<GiftsUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val r = giftRepo.getGifts()) {
                is ApiResult.Success -> _state.value = _state.value.copy(gifts = r.data, loading = false)
                is ApiResult.Error -> _state.value = _state.value.copy(loading = false, error = r.message)
                else -> {}
            }
        }
    }
}
