package com.babatiffin.bts.core.notification

import com.babatiffin.bts.data.backend.SupabaseProvider
import com.google.firebase.messaging.FirebaseMessagingService
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BtsFirebaseMessagingService : FirebaseMessagingService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        val client = SupabaseProvider.client ?: return
        val userId = client.auth.currentSessionOrNull()?.user?.id ?: return
        scope.launch { runCatching { PushTokenRepository(client).register(userId, token) } }
    }
}
