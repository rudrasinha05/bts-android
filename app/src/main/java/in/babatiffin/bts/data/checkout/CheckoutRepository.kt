package com.babatiffin.bts.data.checkout

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Wallet(
    val id: String,
    @SerialName("user_id") val userId: String,
    val balance: Double = 0.0,
)

@Serializable
data class Coupon(
    val id: String,
    val code: String,
    val description: String? = null,
    @SerialName("discount_percent") val discountPercent: Double? = null,
    @SerialName("discount_amount") val discountAmount: Double? = null,
    @SerialName("valid_from") val validFrom: String? = null,
    @SerialName("valid_to") val validTo: String? = null,
)

interface CheckoutRepository {
    suspend fun wallet(userId: String): Wallet?
    suspend fun coupons(): List<Coupon>
}

class SupabaseCheckoutRepository(private val client: SupabaseClient) : CheckoutRepository {
    override suspend fun wallet(userId: String): Wallet? = client.from("wallets")
        .select {
            filter { eq("user_id", userId) }
            limit(1)
        }
        .decodeList<Wallet>()
        .firstOrNull()

    override suspend fun coupons(): List<Coupon> = client.from("coupons")
        .select { filter { eq("is_active", true) } }
        .decodeList()
}
