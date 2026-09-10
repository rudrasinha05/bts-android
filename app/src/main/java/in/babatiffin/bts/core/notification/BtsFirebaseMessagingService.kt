package com.babatiffin.bts.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.babatiffin.bts.data.backend.SupabaseProvider
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
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

    override fun onMessageReceived(message: RemoteMessage) {
        val channelId = "bts_updates"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(channelId, "BTS updates", NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(message.notification?.title ?: message.data["title"] ?: "BTS")
            .setContentText(message.notification?.body ?: message.data["body"].orEmpty())
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(this).notify(message.messageId?.hashCode() ?: 1, notification)
    }
}
