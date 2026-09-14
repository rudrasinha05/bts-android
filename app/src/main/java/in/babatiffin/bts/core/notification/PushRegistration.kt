package com.babatiffin.bts.core.notification

import android.content.Context
import com.babatiffin.bts.data.backend.SupabaseProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.os.Handler
import android.os.Looper

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

    fun unregisterSignedInUser(context: Context, userId: String?, onComplete: () -> Unit) {
        val client = SupabaseProvider.client
        if (userId == null || client == null || FirebaseApp.getApps(context).isEmpty()) {
            onComplete()
            return
        }
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                CoroutineScope(Dispatchers.IO).launch {
                    runCatching { PushTokenRepository(client).unregister(userId, token) }
                    Handler(Looper.getMainLooper()).post { onComplete() }
                }
            }
            .addOnFailureListener { onComplete() }
    }
}
