package com.babatiffin.bts.feature.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babatiffin.bts.data.cart.CartAddOn
import com.babatiffin.bts.data.cart.CartLine
import com.babatiffin.bts.data.cart.CartRepository
import com.babatiffin.bts.feature.menu.MealDiscoveryState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(private val repository: CartRepository) : ViewModel() {
    val lines: StateFlow<List<CartLine>> = repository.lines.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addConfiguredMeal(state: MealDiscoveryState) {
        val meal = state.selectedMeal ?: return
        val addOns = state.addOns.mapNotNull { addOn ->
            val quantity = state.addOnQuantities[addOn.id] ?: 0
            if (quantity == 0) null else CartAddOn(addOn.id, addOn.name, addOn.price, quantity)
        }
        val line = CartLine(
            id = "${meal.id}-${System.currentTimeMillis()}",
            mealId = meal.id,
            mealName = meal.name,
            unitPrice = meal.price,
            addOns = addOns,
        )
        viewModelScope.launch { repository.add(line) }
    }

    fun changeQuantity(line: CartLine, delta: Int) = viewModelScope.launch {
        repository.setQuantity(line.id, line.quantity + delta)
    }
    fun remove(lineId: String) = viewModelScope.launch { repository.remove(lineId) }
    fun clear() = viewModelScope.launch { repository.clear() }
}
