package com.voicerooms.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.voicerooms.app.data.model.Profile
import com.voicerooms.app.data.repository.AuthRepository
import com.voicerooms.app.data.repository.ProfileRepository
import com.voicerooms.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val user: Profile? = null,
    val isLoggedIn: Boolean = false,
    val initialized: Boolean = false,
)

class AuthViewModel(
    private val authRepo: AuthRepository,
    private val profileRepo: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun checkSession() {
        viewModelScope.launch {
            if (authRepo.isLoggedIn) {
                loadProfile()
            } else {
                _state.value = _state.value.copy(isLoggedIn = false, initialized = true)
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(error = "أدخل البريد وكلمة المرور")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val r = authRepo.signIn(email.trim(), password)) {
                is ApiResult.Success -> {
                    profileRepo.setOnline(authRepo.currentUserId ?: "", true)
                    loadProfile()
                    onSuccess()
                }
                is ApiResult.Error -> _state.value = _state.value.copy(loading = false, error = r.message)
                else -> {}
            }
        }
    }

    fun register(email: String, password: String, username: String, displayName: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.length < 6 || username.isBlank()) {
            _state.value = _state.value.copy(error = "تأكد من البيانات (كلمة المرور 6 أحرف على الأقل)")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            when (val r = authRepo.signUp(email.trim(), password, username.trim(), displayName.trim())) {
                is ApiResult.Success -> {
                    // محاولة تسجيل دخول مباشرة بعد التسجيل
                    val login = authRepo.signIn(email.trim(), password)
                    if (login is ApiResult.Error) {
                        _state.value = _state.value.copy(loading = false, error = "تم التسجيل، سجّل الدخول الآن")
                    } else {
                        loadProfile()
                        onSuccess()
                    }
                }
                is ApiResult.Error -> _state.value = _state.value.copy(loading = false, error = r.message)
                else -> {}
            }
        }
    }

    fun logout(onDone: () -> Unit = {}) {
        viewModelScope.launch {
            authRepo.currentUserId?.let { profileRepo.setOnline(it, false) }
            authRepo.signOut()
            _state.value = AuthUiState(initialized = true)
            onDone()
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            when (val r = authRepo.getMyProfile()) {
                is ApiResult.Success ->
                    _state.value = _state.value.copy(
                        loading = false,
                        user = r.data,
                        isLoggedIn = true,
                        initialized = true,
                        error = null,
                    )
                is ApiResult.Error ->
                    _state.value = _state.value.copy(loading = false, initialized = true, error = r.message)
                else -> {}
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    val currentUserId: String? get() = authRepo.currentUserId
}
