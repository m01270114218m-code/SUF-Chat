package com.voicerooms.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicerooms.app.data.model.Banner
import com.voicerooms.app.data.model.Category
import com.voicerooms.app.data.model.Room
import com.voicerooms.app.data.repository.RoomRepository
import com.voicerooms.app.data.repository.SettingsRepository
import com.voicerooms.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val rooms: List<Room> = emptyList(),
    val featured: List<Room> = emptyList(),
    val categories: List<Category> = emptyList(),
    val banners: List<Banner> = emptyList(),
    val selectedCategory: Int = 0,
    val query: String = "",
    val error: String? = null,
)

class HomeViewModel(
    private val roomRepo: RoomRepository,
    private val settingsRepo: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)

            (settingsRepo.getCategories() as? ApiResult.Success)?.let {
                _state.value = _state.value.copy(categories = it.data)
            }
            (settingsRepo.getBanners("home") as? ApiResult.Success)?.let {
                _state.value = _state.value.copy(banners = it.data)
            }
            (roomRepo.getFeaturedRooms() as? ApiResult.Success)?.let {
                _state.value = _state.value.copy(featured = it.data)
            }

            loadRooms()
        }
    }

    fun loadRooms() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val cat = _state.value.selectedCategory
            val q = _state.value.query
            when (val r = roomRepo.getRooms(categoryId = cat, query = q)) {
                is ApiResult.Success -> _state.value = _state.value.copy(loading = false, rooms = r.data)
                is ApiResult.Error -> _state.value = _state.value.copy(loading = false, error = r.message)
                else -> {}
            }
        }
    }

    fun selectCategory(id: Int) {
        _state.value = _state.value.copy(selectedCategory = id)
        loadRooms()
    }

    fun search(query: String) {
        _state.value = _state.value.copy(query = query)
        loadRooms()
    }
}
