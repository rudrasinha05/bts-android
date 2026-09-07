package com.babatiffin.bts.data.auth

import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val sessionStatus: Flow<SessionStatus>
    suspend fun signIn(email: String, password: String)
    suspend fun signUp(email: String, password: String)
    suspend fun sendPhoneOtp(phone: String)
    suspend fun verifyPhoneOtp(phone: String, token: String)
    suspend fun signInWithGoogle()
    suspend fun signOut()
}

class SupabaseAuthRepository(private val client: io.github.jan.supabase.SupabaseClient) : AuthRepository {
    override val sessionStatus = client.auth.sessionStatus

    override suspend fun signIn(email: String, password: String) = client.auth.signInWith(Email) {
        this.email = email.trim()
        this.password = password
    }

    override suspend fun signUp(email: String, password: String) { client.auth.signUpWith(Email) {
        this.email = email.trim()
        this.password = password
    } }

    override suspend fun sendPhoneOtp(phone: String) = client.auth.signInWith(OTP) {
        this.phone = normalizeIndianPhone(phone)
        createUser = true
    }

    override suspend fun verifyPhoneOtp(phone: String, token: String) {
        client.auth.verifyPhoneOtp(OtpType.Phone.SMS, normalizeIndianPhone(phone), token.trim())
    }

    override suspend fun signInWithGoogle() { client.auth.signInWith(Google, redirectUrl = "bts://auth") }
    override suspend fun signOut() { client.auth.signOut() }

    private fun normalizeIndianPhone(value: String): String {
        val digits = value.filter(Char::isDigit).removePrefix("91").takeLast(10)
        require(digits.length == 10) { "Enter a valid 10-digit Indian mobile number." }
        return "+91$digits"
    }
}
