package com.babatiffin.bts.core.notification

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class PushTokenUpsert(
    @SerialName("user_id") val userId: String,
    val token: String,
    val platform: String = "android",
    @SerialName("is_active") val isActive: Boolean = true,
)

class PushTokenRepository(private val client: SupabaseClient) {
    suspend fun register(userId: String, token: String) {
        client.from("push_device_tokens").upsert(
            PushTokenUpsert(userId = userId, token = token),
            onConflict = "token",
        )
    }
}
