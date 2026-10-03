package com.voicerooms.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicerooms.app.data.model.Level
import com.voicerooms.app.data.repository.SettingsRepository
import com.voicerooms.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val loading: Boolean = true,
    val settings: Map<String, String> = emptyMap(),
    val levels: List<Level> = emptyList(),
    val error: String? = null,
)

/**
 * ViewModel الإعدادات — كل القيم تأتي من جدول app_settings و levels في قاعدة البيانات.
 */
class SettingsViewModel(
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val s = settingsRepo.getSettings()) {
                is ApiResult.Success -> _state.value = _state.value.copy(settings = s.data)
                is ApiResult.Error -> _state.value = _state.value.copy(error = s.message)
                else -> {}
            }
            (settingsRepo.getLevels() as? ApiResult.Success)?.let {
                _state.value = _state.value.copy(levels = it.data)
            }
            _state.value = _state.value.copy(loading = false)
        }
    }

    fun value(key: String, def: String = ""): String = _state.value.settings[key] ?: def

    fun bool(key: String, def: Boolean = false): Boolean =
        (_state.value.settings[key] ?: def.toString()).equals("true", ignoreCase = true)
}
