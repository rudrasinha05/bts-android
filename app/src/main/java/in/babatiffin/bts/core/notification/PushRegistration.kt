package com.babatiffin.bts.core.notification

import android.content.Context
import com.babatiffin.bts.data.backend.SupabaseProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PushRegistration {
    fun registerSignedInUser(context: Context, userId: String) {
        val client = SupabaseProvider.client ?: return
        if (FirebaseApp.getApps(context).isEmpty()) return
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            CoroutineScope(Dispatchers.IO).launch {
                runCatching { PushTokenRepository(client).register(userId, token) }
            }
        }
    }
}
