package com.babatiffin.bts.data.subscription

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionPlan(
    val id: String,
    val name: String,
    val description: String,
    @SerialName("billing_cycle") val billingCycle: String,
    @SerialName("meal_schedule") val mealSchedule: String,
    @SerialName("meals_per_cycle") val mealsPerCycle: Int,
    val price: Double,
    @SerialName("food_type") val foodType: String,
    val highlights: List<String> = emptyList(),
    @SerialName("is_popular") val isPopular: Boolean = false,
)

@Serializable
data class Subscription(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("plan_id") val planId: String,
    val status: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String? = null,
    @SerialName("next_billing_date") val nextBillingDate: String? = null,
)

interface SubscriptionRepository {
    suspend fun plans(): List<SubscriptionPlan>
    suspend fun subscriptions(userId: String): List<Subscription>
    suspend fun setStatus(id: String, userId: String, status: String)
}

class SupabaseSubscriptionRepository(private val client: SupabaseClient) : SubscriptionRepository {
    override suspend fun plans(): List<SubscriptionPlan> = client.from("subscription_plans")
        .select { filter { eq("is_active", true) } }
        .decodeList<SubscriptionPlan>()
        .sortedWith(compareByDescending<SubscriptionPlan> { it.isPopular }.thenBy { it.price })

    override suspend fun subscriptions(userId: String): List<Subscription> = client.from("subscriptions")
        .select { filter { eq("user_id", userId) } }
        .decodeList<Subscription>()

    override suspend fun setStatus(id: String, userId: String, status: String) {
        require(status in setOf("active", "paused", "cancelled"))
        client.from("subscriptions").update({ set("status", status) }) {
            filter { eq("id", id); eq("user_id", userId) }
        }
    }
}
