package com.babatiffin.bts.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babatiffin.bts.data.auth.AuthRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AuthUiState(
    val configured: Boolean,
    val loading: Boolean = true,
    val authenticated: Boolean = false,
    val userLabel: String? = null,
    val userId: String? = null,
    val message: String? = null,
    val otpSent: Boolean = false,
    val roles: Set<String> = emptySet(),
)

class AuthViewModel(private val repository: AuthRepository?, configured: Boolean) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState(configured = configured, loading = configured))
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        if (repository == null) _state.value = _state.value.copy(loading = false)
        else viewModelScope.launch {
            repository.sessionStatus.collectLatest { status ->
                _state.value = when (status) {
                    is SessionStatus.Authenticated -> {
                        val user = status.session.user
                        val roles = user?.id?.let { runCatching { repository.loadRoles(it) }.getOrDefault(emptySet()) }.orEmpty()
                        _state.value.copy(
                            loading = false, authenticated = true,
                            userLabel = user?.email ?: user?.phone,
                            userId = user?.id,
                            roles = roles,
                            message = null,
                        )
                    }
                    is SessionStatus.Initializing -> _state.value.copy(loading = true)
                    else -> _state.value.copy(loading = false, authenticated = false, userLabel = null, userId = null, roles = emptySet())
                }
            }
        }
    }

    fun signIn(email: String, password: String) = execute { this.signIn(email, password) }
    fun signUp(email: String, password: String) = execute { this.signUp(email, password) }
    fun sendOtp(phone: String) = execute(success = { it.copy(otpSent = true, message = "OTP sent.") }) { this.sendPhoneOtp(phone) }
    fun verifyOtp(phone: String, otp: String) = execute { this.verifyPhoneOtp(phone, otp) }
    fun google() = execute { this.signInWithGoogle() }
    fun signOut() = execute { this.signOut() }

    private fun execute(success: (AuthUiState) -> AuthUiState = { it.copy(message = null) }, action: suspend AuthRepository.() -> Unit) {
        val repo = repository ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, message = null)
            runCatching { repo.action() }
                .onSuccess { _state.value = success(_state.value).copy(loading = false) }
                .onFailure { _state.value = _state.value.copy(loading = false, message = friendlyError(it)) }
        }
    }

    private fun friendlyError(error: Throwable): String = when (error) {
        is IllegalArgumentException -> error.message ?: "Please check the entered details."
        else -> "Authentication failed. Please check your details and try again."
    }
}
