package com.babatiffin.bts.feature.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babatiffin.bts.data.menu.Meal
import com.babatiffin.bts.data.menu.MealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MealDiscoveryState(
    val loading: Boolean = true,
    val meals: List<Meal> = emptyList(),
    val addOns: List<Meal> = emptyList(),
    val category: String = "all",
    val foodType: String = "all",
    val error: String? = null,
    val selectedMealId: String? = null,
    val addOnQuantities: Map<String, Int> = emptyMap(),
) {
    val categories: List<String> get() = listOf("all") + meals.map(Meal::category).distinct()
    val visibleMeals: List<Meal> get() = meals.filter {
        (category == "all" || it.category == category) &&
            (foodType == "all" || it.foodType == foodType)
    }
    val selectedMeal: Meal? get() = meals.firstOrNull { it.id == selectedMealId }
    val configuredTotal: Double get() = (selectedMeal?.price ?: 0.0) + addOns.sumOf {
        it.price * (addOnQuantities[it.id] ?: 0)
    }
}

class MealDiscoveryViewModel(private val repository: MealRepository?) : ViewModel() {
    private val _state = MutableStateFlow(MealDiscoveryState(loading = repository != null))
    val state: StateFlow<MealDiscoveryState> = _state.asStateFlow()

    init { refresh() }

    fun selectCategory(value: String) { _state.value = _state.value.copy(category = value) }
    fun selectFoodType(value: String) { _state.value = _state.value.copy(foodType = value) }
    fun startBuilder() {
        _state.value = _state.value.copy(category = "all", foodType = "all", selectedMealId = null, addOnQuantities = emptyMap())
    }
    fun selectBuilderCategory(value: String) {
        _state.value = _state.value.copy(category = value, foodType = "all", selectedMealId = null, addOnQuantities = emptyMap())
    }
    fun selectBuilderFoodType(value: String) {
        _state.value = _state.value.copy(foodType = value, selectedMealId = null, addOnQuantities = emptyMap())
    }
    fun selectMeal(id: String) {
        if (_state.value.selectedMealId != id) {
            _state.value = _state.value.copy(selectedMealId = id, addOnQuantities = emptyMap())
        }
    }
    fun changeAddOn(id: String, delta: Int) {
        val current = _state.value.addOnQuantities[id] ?: 0
        val next = (current + delta).coerceIn(0, 10)
        _state.value = _state.value.copy(addOnQuantities = _state.value.addOnQuantities + (id to next))
    }

    fun refresh() {
        if (repository == null) {
            _state.value = MealDiscoveryState(loading = false, error = "Backend configuration is required.")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching { repository.availableMeals() to repository.availableAddOns() }
                .onSuccess { (meals, addOns) -> _state.value = _state.value.copy(loading = false, meals = meals, addOns = addOns) }
                .onFailure { _state.value = _state.value.copy(loading = false, error = "Menu could not be loaded. Check your connection and retry.") }
        }
    }
}
