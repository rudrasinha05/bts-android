package com.babatiffin.bts.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(state: AuthUiState, viewModel: AuthViewModel, modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
        Text(
            "Mobile OTP will be available after the SMS provider is configured. Use email or Google for now.",
        )
        OutlinedButton(viewModel::google, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) { Text("Continue with Google") }
        if (state.loading) CircularProgressIndicator()
        state.message?.let { Text(it) }
    }
}
