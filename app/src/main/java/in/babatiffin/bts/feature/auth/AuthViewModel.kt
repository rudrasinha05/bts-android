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
    val email: String? = null,
    val phone: String? = null,
    val userId: String? = null,
    val message: String? = null,
    val otpSent: Boolean = false,
    val otpPhone: String? = null,
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
                            email = user?.email,
                            phone = user?.phone,
                            userId = user?.id,
                            roles = roles,
                            message = null,
                        )
                    }
                    is SessionStatus.Initializing -> _state.value.copy(loading = true)
                    else -> _state.value.copy(
                        loading = false,
                        authenticated = false,
                        userLabel = null,
                        email = null,
                        phone = null,
                        userId = null,
                        otpSent = false,
                        otpPhone = null,
                        roles = emptySet(),
                    )
                }
            }
        }
    }

    fun signIn(email: String, password: String) = execute { this.signIn(email, password) }
    fun signUp(email: String, password: String) = execute { this.signUp(email, password) }
    fun sendOtp(phone: String) = execute(success = { it.copy(otpSent = true, otpPhone = phone.trim(), message = "OTP sent. Check your SMS.") }) {
        sendPhoneOtp(phone)
    }
    fun verifyOtp(phone: String, otp: String) {
        val requestedPhone = _state.value.otpPhone ?: phone
        execute(success = { it.copy(otpSent = false, otpPhone = null, message = null) }) {
            verifyPhoneOtp(requestedPhone, otp)
        }
    }
    fun changeOtpNumber() { _state.value = _state.value.copy(otpSent = false, otpPhone = null, message = null) }
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

    private fun friendlyError(error: Throwable): String {
        val detail = generateSequence(error) { it.cause }.mapNotNull(Throwable::message).firstOrNull().orEmpty()
        return when {
            error is IllegalArgumentException -> detail.ifBlank { "Please check the entered details." }
            detail.contains("provider", ignoreCase = true) && detail.contains("phone", ignoreCase = true) ->
                "Phone OTP is not configured on the server. Enable Phone Auth and an SMS provider in Supabase."
            detail.contains("rate", ignoreCase = true) -> "Too many OTP requests. Please wait before trying again."
            detail.contains("expired", ignoreCase = true) -> "OTP expired. Request a new code."
            detail.contains("invalid", ignoreCase = true) -> "Invalid phone number or OTP. Please check and retry."
            else -> detail.ifBlank { "Authentication failed. Please check your details and try again." }
        }
    }
}
