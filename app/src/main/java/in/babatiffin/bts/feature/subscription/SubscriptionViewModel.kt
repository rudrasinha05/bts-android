package com.babatiffin.bts.feature.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babatiffin.bts.data.subscription.Subscription
import com.babatiffin.bts.data.subscription.SubscriptionPlan
import com.babatiffin.bts.data.subscription.SubscriptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SubscriptionState(
    val loading: Boolean = true,
    val plans: List<SubscriptionPlan> = emptyList(),
    val subscriptions: List<Subscription> = emptyList(),
    val userId: String? = null,
    val error: String? = null,
)

class SubscriptionViewModel(private val repository: SubscriptionRepository?) : ViewModel() {
    private val _state = MutableStateFlow(SubscriptionState(loading = repository != null))
    val state: StateFlow<SubscriptionState> = _state.asStateFlow()

    init { refreshPlans() }

    fun loadForUser(userId: String?) {
        if (userId == null || userId == _state.value.userId) return
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, userId = userId, error = null)
            runCatching { repository?.subscriptions(userId).orEmpty() }
                .onSuccess { _state.value = _state.value.copy(loading = false, subscriptions = it) }
                .onFailure { _state.value = _state.value.copy(loading = false, error = "Subscriptions could not be loaded.") }
        }
    }

    fun setStatus(subscription: Subscription, status: String) = viewModelScope.launch {
        val userId = _state.value.userId ?: return@launch
        _state.value = _state.value.copy(loading = true, error = null)
        runCatching { repository?.setStatus(subscription.id, userId, status) }
            .onSuccess { _state.value = _state.value.copy(loading = false, subscriptions = _state.value.subscriptions.map { if (it.id == subscription.id) it.copy(status = status) else it }) }
            .onFailure { _state.value = _state.value.copy(loading = false, error = "Subscription update was not allowed. Please retry or contact support.") }
    }

    fun refreshPlans() {
        if (repository == null) { _state.value = SubscriptionState(loading = false, error = "Backend configuration is required."); return }
        viewModelScope.launch {
            runCatching { repository.plans() }
                .onSuccess { _state.value = _state.value.copy(loading = false, plans = it) }
                .onFailure { _state.value = _state.value.copy(loading = false, error = "Plans could not be loaded.") }
        }
    }
}
