package com.babatiffin.bts.data.checkout

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.babatiffin.bts.BuildConfig
import io.github.jan.supabase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

@Serializable data class CheckoutItem(val mealId: String, val quantity: Int)
@Serializable data class PaymentOrderRequest(val items: List<CheckoutItem>, val couponCode: String? = null, val mealType: String, val method: String = "UPI")
@Serializable data class PaymentOrder(val orderId: String, val orderNumber: String, val razorpayOrderId: String, val keyId: String, val amount: Int, val currency: String)
@Serializable data class PaymentVerification(val razorpayOrderId: String, val razorpayPaymentId: String, val razorpaySignature: String)
@Serializable data class VerificationResult(val verified: Boolean, val orderId: String)
@Serializable data class WalletPaymentResult(val verified: Boolean, val orderId: String, val balance: Double)

interface CheckoutRepository {
    suspend fun wallet(userId: String): Wallet?
    suspend fun coupons(): List<Coupon>
    suspend fun createPaymentOrder(request: PaymentOrderRequest): PaymentOrder
    suspend fun verifyPayment(request: PaymentVerification): VerificationResult
    suspend fun payWithWallet(request: PaymentOrderRequest): WalletPaymentResult
}

class SupabaseCheckoutRepository(private val client: SupabaseClient) : CheckoutRepository {
    private val http = HttpClient(Android)
    private val json = Json { ignoreUnknownKeys = true }
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

    override suspend fun createPaymentOrder(request: PaymentOrderRequest): PaymentOrder = invoke("create-payment-order", json.encodeToString(request))
    override suspend fun verifyPayment(request: PaymentVerification): VerificationResult = invoke("verify-payment", json.encodeToString(request))
    override suspend fun payWithWallet(request: PaymentOrderRequest): WalletPaymentResult = invoke("pay-with-wallet", json.encodeToString(request.copy(method = "WALLET")))

    private suspend inline fun <reified T> invoke(function: String, payload: String): T {
        val token = client.auth.currentSessionOrNull()?.accessToken ?: error("Authentication required")
        val response = http.post("${BuildConfig.SUPABASE_URL}/functions/v1/$function") {
            header("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return json.decodeFromString(response.bodyAsText())
    }
}
