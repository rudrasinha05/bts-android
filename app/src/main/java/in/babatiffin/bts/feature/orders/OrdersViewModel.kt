package com.babatiffin.bts.feature.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babatiffin.bts.data.order.OrderDetails
import com.babatiffin.bts.data.order.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrdersState(
    val loading: Boolean = false,
    val userId: String? = null,
    val orders: List<OrderDetails> = emptyList(),
    val selectedOrderId: String? = null,
    val error: String? = null,
) {
    val selected: OrderDetails? get() = orders.firstOrNull { it.order.id == selectedOrderId }
}

class OrdersViewModel(private val repository: OrderRepository?) : ViewModel() {
    private val _state = MutableStateFlow(OrdersState())
    val state: StateFlow<OrdersState> = _state.asStateFlow()

    fun load(userId: String?) {
        if (userId == null || (userId == _state.value.userId && _state.value.orders.isNotEmpty())) return
        if (repository == null) { _state.value = OrdersState(error = "Backend configuration is required."); return }
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, userId = userId, error = null)
            runCatching { repository.orders(userId) }
                .onSuccess { _state.value = _state.value.copy(loading = false, orders = it) }
                .onFailure { _state.value = _state.value.copy(loading = false, error = "Orders could not be loaded. Please retry.") }
        }
    }

    fun select(id: String) { _state.value = _state.value.copy(selectedOrderId = id) }
}
