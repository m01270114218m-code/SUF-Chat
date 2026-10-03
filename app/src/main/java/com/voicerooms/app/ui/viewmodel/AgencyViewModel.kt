package com.voicerooms.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicerooms.app.data.model.Agency
import com.voicerooms.app.data.model.AgencyMember
import com.voicerooms.app.data.model.Profile
import com.voicerooms.app.data.repository.AgencyRepository
import com.voicerooms.app.data.repository.ProfileRepository
import com.voicerooms.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AgencyUiState(
    val loading: Boolean = true,
    val agencies: List<Agency> = emptyList(),
    val myAgency: Agency? = null,
    val members: List<AgencyMember> = emptyList(),
    val searchResults: List<Profile> = emptyList(),
    val creating: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

class AgencyViewModel(
    private val agencyRepo: AgencyRepository,
    private val profileRepo: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AgencyUiState())
    val state: StateFlow<AgencyUiState> = _state.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            (agencyRepo.getAgencies() as? ApiResult.Success)?.let {
                _state.value = _state.value.copy(agencies = it.data)
            }
            when (val mine = agencyRepo.getMyAgency(userId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(myAgency = mine.data)
                    mine.data?.let { loadMembers(it.id) }
                }
                else -> {}
            }
            _state.value = _state.value.copy(loading = false)
        }
    }

    fun loadMembers(agencyId: String) {
        viewModelScope.launch {
            (agencyRepo.getAgencyMembers(agencyId) as? ApiResult.Success)?.let {
                _state.value = _state.value.copy(members = it.data)
            }
        }
    }

    fun createAgency(userId: String, name: String, description: String?, logoUrl: String?) {
        if (name.isBlank()) {
            _state.value = _state.value.copy(error = "أدخل اسم الوكالة")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(creating = true, error = null)
            when (val r = agencyRepo.createAgency(userId, name.trim(), description?.trim(), logoUrl)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(creating = false, myAgency = r.data, message = "تم إنشاء الوكالة")
                    load(userId)
                }
                is ApiResult.Error -> _state.value = _state.value.copy(creating = false, error = r.message)
                else -> {}
            }
        }
    }

    fun searchUsers(query: String) {
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

    fun addMember(userId: String) {
        val agency = _state.value.myAgency ?: return
        viewModelScope.launch {
            when (val r = agencyRepo.addMember(agency.id, userId)) {
                is ApiResult.Error -> _state.value = _state.value.copy(error = r.message)
                else -> {
                    _state.value = _state.value.copy(message = "تمت إضافة العضو")
                    loadMembers(agency.id)
                }
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, message = null)
    }
}
