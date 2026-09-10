package com.babatiffin.bts.data.customer

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class CustomerProfile(
    val id: String,
    @SerialName("full_name") val fullName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    @SerialName("date_of_birth") val dateOfBirth: String? = null,
)

@Serializable data class Address(
    val id: String,
    @SerialName("user_id") val userId: String,
    val label: String,
    val line1: String,
    val line2: String? = null,
    val landmark: String? = null,
    val city: String = "Greater Noida",
    val state: String = "Uttar Pradesh",
    val pincode: String,
    @SerialName("is_default") val isDefault: Boolean = false,
)

@Serializable data class AddressWrite(
    @SerialName("user_id") val userId: String,
    val label: String,
    val line1: String,
    val line2: String? = null,
    val landmark: String? = null,
    val city: String,
    val state: String,
    val pincode: String,
    @SerialName("is_default") val isDefault: Boolean,
)

@Serializable data class NutritionProfile(
    val id: String? = null,
    @SerialName("user_id") val userId: String,
    val goal: String? = null,
    @SerialName("target_calories") val targetCalories: Int? = null,
    @SerialName("target_protein") val targetProtein: Int? = null,
    @SerialName("target_carbs") val targetCarbs: Int? = null,
    @SerialName("target_fat") val targetFat: Int? = null,
    @SerialName("target_fiber") val targetFiber: Int? = null,
)

@Serializable data class MealNutrition(
    @SerialName("meal_id") val mealId: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    @SerialName("is_estimate") val isEstimate: Boolean,
)

@Serializable data class SupportTicket(
    val id: String,
    @SerialName("user_id") val userId: String,
    val subject: String,
    val message: String,
    val category: String,
    val status: String,
    @SerialName("created_at") val createdAt: String,
)

interface CustomerRepository {
    suspend fun profile(userId: String): CustomerProfile?
    suspend fun saveProfile(profile: CustomerProfile)
    suspend fun addresses(userId: String): List<Address>
    suspend fun addAddress(address: AddressWrite)
    suspend fun deleteAddress(id: String, userId: String)
    suspend fun setDefaultAddress(id: String, userId: String)
    suspend fun nutritionProfile(userId: String): NutritionProfile?
    suspend fun saveNutrition(profile: NutritionProfile)
    suspend fun mealNutrition(): List<MealNutrition>
    suspend fun tickets(userId: String): List<SupportTicket>
    suspend fun createTicket(userId: String, subject: String, message: String, category: String)
}

class SupabaseCustomerRepository(private val client: SupabaseClient) : CustomerRepository {
    override suspend fun profile(userId: String) = client.from("profiles").select { filter { eq("id", userId) } }.decodeList<CustomerProfile>().firstOrNull()
    override suspend fun saveProfile(profile: CustomerProfile) {
        if (this.profile(profile.id) == null) client.from("profiles").insert(profile)
        else client.from("profiles").update({ set("full_name", profile.fullName); set("phone", profile.phone); set("date_of_birth", profile.dateOfBirth) }) { filter { eq("id", profile.id) } }
    }
    override suspend fun addresses(userId: String) = client.from("addresses").select { filter { eq("user_id", userId); eq("is_active", true) } }.decodeList<Address>().sortedByDescending(Address::isDefault)
    override suspend fun addAddress(address: AddressWrite) {
        if (address.isDefault) client.from("addresses").update({ set("is_default", false) }) { filter { eq("user_id", address.userId) } }
        client.from("addresses").insert(address)
    }
    override suspend fun deleteAddress(id: String, userId: String) { client.from("addresses").update({ set("is_active", false) }) { filter { eq("id", id); eq("user_id", userId) } } }
    override suspend fun setDefaultAddress(id: String, userId: String) {
        client.from("addresses").update({ set("is_default", false) }) { filter { eq("user_id", userId) } }
        client.from("addresses").update({ set("is_default", true) }) { filter { eq("id", id); eq("user_id", userId) } }
    }
    override suspend fun nutritionProfile(userId: String) = client.from("nutrition_profiles").select { filter { eq("user_id", userId) } }.decodeList<NutritionProfile>().firstOrNull()
    override suspend fun saveNutrition(profile: NutritionProfile) {
        if (nutritionProfile(profile.userId) == null) client.from("nutrition_profiles").insert(profile)
        else client.from("nutrition_profiles").update({
            set("goal", profile.goal); set("target_calories", profile.targetCalories); set("target_protein", profile.targetProtein)
            set("target_carbs", profile.targetCarbs); set("target_fat", profile.targetFat); set("target_fiber", profile.targetFiber)
        }) { filter { eq("user_id", profile.userId) } }
    }
    override suspend fun mealNutrition() = client.from("meal_nutrition").select().decodeList<MealNutrition>()
    override suspend fun tickets(userId: String) = client.from("support_tickets").select { filter { eq("user_id", userId) } }.decodeList<SupportTicket>().sortedByDescending(SupportTicket::createdAt)
    override suspend fun createTicket(userId: String, subject: String, message: String, category: String) {
        client.from("support_tickets").insert(mapOf("user_id" to userId, "subject" to subject, "message" to message, "category" to category))
    }
}
