package com.babatiffin.bts.feature.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babatiffin.bts.data.customer.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CustomerState(
    val loading: Boolean = false,
    val userId: String? = null,
    val profile: CustomerProfile? = null,
    val addresses: List<Address> = emptyList(),
    val nutrition: NutritionProfile? = null,
    val mealNutrition: List<MealNutrition> = emptyList(),
    val tickets: List<SupportTicket> = emptyList(),
    val message: String? = null,
)

class CustomerViewModel(private val repository: CustomerRepository?) : ViewModel() {
    private val mutableState = MutableStateFlow(CustomerState())
    val state = mutableState.asStateFlow()

    fun load(userId: String?) {
        if (userId == null || userId == mutableState.value.userId) return
        if (repository == null) { mutableState.value = CustomerState(userId = userId, message = "Backend configuration required."); return }
        viewModelScope.launch {
            mutableState.value = CustomerState(loading = true, userId = userId)
            val profile = runCatching { repository.profile(userId) }.getOrNull()
            val addresses = runCatching { repository.addresses(userId) }.getOrDefault(emptyList())
            val nutrition = runCatching { repository.nutritionProfile(userId) }.getOrNull()
            val mealNutrition = runCatching { repository.mealNutrition() }.getOrDefault(emptyList())
            val tickets = runCatching { repository.tickets(userId) }.getOrDefault(emptyList())
            mutableState.value = CustomerState(false, userId, profile, addresses, nutrition, mealNutrition, tickets)
        }
    }

    fun saveProfile(fullName: String, phone: String, dateOfBirth: String) = mutate("Profile saved.") { userId ->
        repository?.saveProfile(CustomerProfile(userId, fullName.ifBlank { null }, phone.ifBlank { null }, mutableState.value.profile?.email, dateOfBirth.ifBlank { null }))
    }

    fun addAddress(label: String, line1: String, line2: String, landmark: String, city: String, state: String, pincode: String, makeDefault: Boolean) {
        if (line1.isBlank() || pincode.length != 6) { mutableState.value = mutableState.value.copy(message = "Enter address line and valid 6-digit pincode."); return }
        mutate("Address added.", reload = true) { userId -> repository?.addAddress(AddressWrite(userId, label.ifBlank { "Home" }, line1, line2.ifBlank { null }, landmark.ifBlank { null }, city, state, pincode, makeDefault)) }
    }
    fun deleteAddress(address: Address) = mutate("Address removed.", true) { repository?.deleteAddress(address.id, it) }
    fun setDefault(address: Address) = mutate("Default address updated.", true) { repository?.setDefaultAddress(address.id, it) }

    fun saveNutrition(goal: String, calories: String, protein: String, carbs: String, fat: String, fiber: String) = mutate("Nutrition goals saved.", true) { userId ->
        repository?.saveNutrition(NutritionProfile(null, userId, goal.ifBlank { null }, calories.toIntOrNull(), protein.toIntOrNull(), carbs.toIntOrNull(), fat.toIntOrNull(), fiber.toIntOrNull()))
    }

    fun createTicket(subject: String, message: String, category: String) {
        if (subject.isBlank() || message.isBlank()) { mutableState.value = mutableState.value.copy(message = "Subject and message are required."); return }
        mutate("Support ticket created.", true) { repository?.createTicket(it, subject, message, category) }
    }

    private fun mutate(success: String, reload: Boolean = false, action: suspend (String) -> Unit) {
        val userId = mutableState.value.userId ?: return
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(loading = true, message = null)
            runCatching { action(userId) }
                .onSuccess { mutableState.value = mutableState.value.copy(loading = false, message = success); if (reload) { mutableState.value = mutableState.value.copy(userId = null); load(userId) } }
                .onFailure { mutableState.value = mutableState.value.copy(loading = false, message = "Update could not be completed.") }
        }
    }
}
