package com.babatiffin.bts.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(state: AuthUiState, viewModel: AuthViewModel, modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var otp by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(state.otpPhone) {
        state.otpPhone?.let { phone = it.filter(Char::isDigit).takeLast(10) }
        if (!state.otpSent) otp = ""
    }
    Column(modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (state.authenticated) "Welcome to BTS" else "Login or get started")
        if (!state.configured) {
            Text("Backend configuration is required. Add SUPABASE_URL and SUPABASE_PUBLISHABLE_KEY to local.properties.")
            return@Column
        }
        if (state.authenticated) {
            Text(state.userLabel ?: "Signed in")
            Text("Role: ${state.roles.joinToString().ifBlank { "customer" }}")
            Button(onClick = viewModel::signOut, enabled = !state.loading) { Text("Sign out") }
            return@Column
        }
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Button({ viewModel.signIn(email, password) }, enabled = !state.loading && email.isNotBlank() && password.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Login") }
        OutlinedButton({ viewModel.signUp(email, password) }, enabled = !state.loading && email.isNotBlank() && password.length >= 6, modifier = Modifier.fillMaxWidth()) { Text("Create account") }
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it.filter(Char::isDigit).take(10) },
            label = { Text("10-digit mobile number") },
            enabled = !state.otpSent,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
        )
        if (state.otpSent) {
            Text("OTP sent to +91 ${phone.takeLast(10)}")
            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it.filter(Char::isDigit).take(6) },
                label = { Text("6-digit OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        OutlinedButton(
            onClick = { if (state.otpSent) viewModel.verifyOtp(phone, otp) else viewModel.sendOtp(phone) },
            enabled = !state.loading && phone.length == 10 && (!state.otpSent || otp.length == 6),
            modifier = Modifier.fillMaxWidth(),
        ) { Text(if (state.otpSent) "Verify OTP" else "Send OTP") }
        if (state.otpSent) OutlinedButton(viewModel::changeOtpNumber, modifier = Modifier.fillMaxWidth()) { Text("Change mobile number") }
        OutlinedButton(viewModel::google, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) { Text("Continue with Google") }
        if (state.loading) CircularProgressIndicator()
        state.message?.let { Text(it) }
    }
}
