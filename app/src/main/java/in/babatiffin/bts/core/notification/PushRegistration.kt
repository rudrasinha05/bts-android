package com.babatiffin.bts.core.notification

import android.content.Context
import com.babatiffin.bts.data.backend.SupabaseProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
        CoroutineScope(Dispatchers.Main).launch {
            try {
                withTimeoutOrNull(5_000L) {
                    val token = suspendCancellableCoroutine<String> { continuation ->
                        FirebaseMessaging.getInstance().token
                            .addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
                            .addOnFailureListener { if (continuation.isActive) continuation.resumeWithException(it) }
                    }
                    PushTokenRepository(client).unregister(userId, token)
                }
            } catch (_: Exception) {
                // Token cleanup is best-effort; it must not block session sign-out.
            } finally {
                onComplete()
            }
        }
    }
}
