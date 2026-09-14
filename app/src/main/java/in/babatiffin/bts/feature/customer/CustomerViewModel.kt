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
    val selectedAddressId: String? = null,
    val addressError: String? = null,
    val addressSaving: Boolean = false,
)

class CustomerViewModel(private val repository: CustomerRepository?) : ViewModel() {
    private val mutableState = MutableStateFlow(CustomerState())
    val state = mutableState.asStateFlow()

    private var sessionVersion = 0

    fun load(userId: String?) {
        if (userId == mutableState.value.userId) return
        val version = ++sessionVersion
        if (userId == null) { mutableState.value = CustomerState(); return }
        if (repository == null) { mutableState.value = CustomerState(userId = userId, message = "Backend configuration required."); return }
        mutableState.value = CustomerState(loading = true, userId = userId)
        viewModelScope.launch {
            val profile = runCatching { repository.profile(userId) }.getOrNull()
            val addressResult = runCatching { repository.addresses(userId) }
            val addresses = addressResult.getOrDefault(emptyList())
            val nutrition = runCatching { repository.nutritionProfile(userId) }.getOrNull()
            val mealNutrition = runCatching { repository.mealNutrition() }.getOrDefault(emptyList())
            val tickets = runCatching { repository.tickets(userId) }.getOrDefault(emptyList())
            if (version == sessionVersion) mutableState.value = CustomerState(false, userId, profile, addresses, nutrition, mealNutrition, tickets,
                addressError = if (addressResult.isFailure) "Addresses could not be loaded. Retry to continue." else null)
        }
    }

    fun selectAddress(id: String) {
        if (mutableState.value.addresses.any { it.id == id }) mutableState.value = mutableState.value.copy(selectedAddressId = id)
    }

    fun clearAddressMessage() { mutableState.value = mutableState.value.copy(message = null) }

    fun reloadAddresses() {
        val userId = mutableState.value.userId ?: return
        val version = sessionVersion
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(loading = true, addressError = null)
            runCatching { checkNotNull(repository).addresses(userId) }
                .onSuccess { if (version == sessionVersion) mutableState.value = mutableState.value.copy(loading = false, addresses = it) }
                .onFailure { if (version == sessionVersion) mutableState.value = mutableState.value.copy(loading = false, addressError = "Addresses could not be loaded. Retry to continue.") }
        }
    }

    fun saveProfile(fullName: String, phone: String, dateOfBirth: String) = mutate("Profile saved.") { userId ->
        repository?.saveProfile(CustomerProfile(userId, fullName.ifBlank { null }, phone.ifBlank { null }, mutableState.value.profile?.email, dateOfBirth.ifBlank { null }))
    }

    fun addAddress(label: String, line1: String, line2: String, landmark: String, city: String, state: String, pincode: String, makeDefault: Boolean) {
        if (line1.isBlank() || pincode.length != 6) { mutableState.value = mutableState.value.copy(message = "Enter address line and valid 6-digit pincode."); return }
        mutate("Address added.", reload = true) { userId -> repository?.addAddress(AddressWrite(userId, label.ifBlank { "Home" }, line1, line2.ifBlank { null }, landmark.ifBlank { null }, city, state, pincode, makeDefault)) }
    }

    fun saveDeliveryAddress(addressId: String?, draft: DeliveryAddressDraft, onSaved: () -> Unit) {
        val userId = mutableState.value.userId ?: return
        if (mutableState.value.addressSaving) return
        draft.validationError()?.let { mutableState.value = mutableState.value.copy(message = it); return }
        val existing = mutableState.value.addresses.firstOrNull { it.id == addressId && it.userId == userId }
        if (addressId != null && existing == null) { mutableState.value = mutableState.value.copy(message = "This address is no longer available."); return }
        val version = sessionVersion
        mutableState.value = mutableState.value.copy(addressSaving = true, message = null)
        viewModelScope.launch {
            runCatching {
                val customerRepository = checkNotNull(repository) { "Backend configuration required." }
                customerRepository.saveProfile(
                    CustomerProfile(userId, draft.name.trim(), draft.phone.trim(), mutableState.value.profile?.email, mutableState.value.profile?.dateOfBirth),
                )
                val write = draft.toAddressWrite(userId, existing?.isDefault ?: mutableState.value.addresses.isEmpty())
                if (existing == null) customerRepository.addAddress(write) else customerRepository.editAddress(existing.id, write)
            }.onSuccess { saved ->
                if (version == sessionVersion) {
                    mutableState.value = mutableState.value.copy(addressSaving = false, message = null, addressError = null,
                        selectedAddressId = saved.id, addresses = mutableState.value.addresses.filterNot { it.id == saved.id } + saved,
                        profile = mutableState.value.profile?.copy(fullName = draft.name.trim(), phone = draft.phone))
                    onSaved()
                }
            }.onFailure {
                if (version == sessionVersion) mutableState.value = mutableState.value.copy(addressSaving = false, message = "Address could not be saved. Your entries are kept; check your connection and retry.")
            }
        }
    }
    fun deleteAddress(address: Address) = mutate("Address removed.", true) { repository?.deleteAddress(address.id, it) }
    fun setDefault(address: Address) = mutate("Default address updated.", true) { repository?.setDefaultAddress(address.id, it) }
    fun updateLocation(address: Address, latitude: Double, longitude: Double) = mutate("Address location updated.", true) { repository?.updateLocation(address.id, it, latitude, longitude) }

    fun saveNutrition(goal: String, calories: String, protein: String, carbs: String, fat: String, fiber: String) = mutate("Nutrition goals saved.", true) { userId ->
        repository?.saveNutrition(NutritionProfile(null, userId, goal.ifBlank { null }, calories.toIntOrNull(), protein.toIntOrNull(), carbs.toIntOrNull(), fat.toIntOrNull(), fiber.toIntOrNull()))
    }

    fun createTicket(subject: String, message: String, category: String) {
        if (subject.isBlank() || message.isBlank()) { mutableState.value = mutableState.value.copy(message = "Subject and message are required."); return }
        mutate("Support ticket created.", true) { repository?.createTicket(it, subject, message, category) }
    }

    private fun mutate(success: String, reload: Boolean = false, action: suspend (String) -> Unit) {
        val userId = mutableState.value.userId ?: return
        val version = sessionVersion
        viewModelScope.launch {
            mutableState.value = mutableState.value.copy(loading = true, message = null)
            runCatching { action(userId) }
                .onSuccess { if (version == sessionVersion) { mutableState.value = mutableState.value.copy(loading = false, message = success); if (reload) { mutableState.value = mutableState.value.copy(userId = null); load(userId) } } }
                .onFailure { if (version == sessionVersion) mutableState.value = mutableState.value.copy(loading = false, message = "Update could not be completed.") }
        }
    }
}
