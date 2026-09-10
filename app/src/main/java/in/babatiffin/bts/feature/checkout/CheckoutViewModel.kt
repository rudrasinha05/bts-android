package com.babatiffin.bts.feature.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babatiffin.bts.data.checkout.CheckoutRepository
import com.babatiffin.bts.data.checkout.Coupon
import com.babatiffin.bts.data.checkout.Wallet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PaymentChoice { Online, Wallet }

data class CheckoutState(
    val loading: Boolean = false,
    val userId: String? = null,
    val wallet: Wallet? = null,
    val coupons: List<Coupon> = emptyList(),
    val couponCode: String = "",
    val appliedCoupon: Coupon? = null,
    val paymentChoice: PaymentChoice = PaymentChoice.Online,
    val error: String? = null,
)

class CheckoutViewModel(private val repository: CheckoutRepository?) : ViewModel() {
    private val _state = MutableStateFlow(CheckoutState())
    val state: StateFlow<CheckoutState> = _state.asStateFlow()

    fun load(userId: String?) {
        if (userId == null || (userId == _state.value.userId && _state.value.coupons.isNotEmpty())) return
        if (repository == null) {
            _state.value = CheckoutState(userId = userId, error = "Backend configuration is required.")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, userId = userId, error = null)
            val wallet = runCatching { repository.wallet(userId) }.getOrNull()
            runCatching { repository.coupons() }
                .onSuccess { _state.value = _state.value.copy(loading = false, wallet = wallet, coupons = it) }
                .onFailure { _state.value = _state.value.copy(loading = false, wallet = wallet, error = "Checkout offers could not be loaded.") }
        }
    }

    fun setCouponCode(value: String) {
        _state.value = _state.value.copy(couponCode = value.uppercase(), appliedCoupon = null, error = null)
    }

    fun applyCoupon() {
        val coupon = _state.value.coupons.firstOrNull { it.code.equals(_state.value.couponCode.trim(), true) }
        _state.value = if (coupon == null) _state.value.copy(error = "This coupon is not active or does not exist.")
        else _state.value.copy(appliedCoupon = coupon, couponCode = coupon.code, error = null)
    }

    fun choosePayment(choice: PaymentChoice) {
        _state.value = _state.value.copy(paymentChoice = choice, error = null)
    }

    fun discount(subtotal: Double): Double {
        val coupon = _state.value.appliedCoupon ?: return 0.0
        val percentage = coupon.discountPercent?.let { subtotal * it / 100.0 } ?: 0.0
        return maxOf(coupon.discountAmount ?: 0.0, percentage).coerceIn(0.0, subtotal)
    }
}
