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
import android.app.Activity
import com.babatiffin.bts.core.payment.RazorpayCoordinator
import com.babatiffin.bts.core.payment.RazorpayResult
import com.babatiffin.bts.data.cart.CartLine
import com.babatiffin.bts.data.checkout.CheckoutItem
import com.babatiffin.bts.data.checkout.PaymentOrderRequest
import com.babatiffin.bts.data.checkout.PaymentVerification
import com.razorpay.Checkout
import org.json.JSONObject

enum class PaymentChoice { Online, Wallet }

data class CheckoutState(
    val loading: Boolean = false,
    val userId: String? = null,
    val wallet: Wallet? = null,
    val coupons: List<Coupon> = emptyList(),
    val couponCode: String = "",
    val appliedCoupon: Coupon? = null,
    val paymentChoice: PaymentChoice = PaymentChoice.Online,
    val mealType: String = "lunch",
    val paymentComplete: Boolean = false,
    val error: String? = null,
)

class CheckoutViewModel(private val repository: CheckoutRepository?) : ViewModel() {
    private val _state = MutableStateFlow(CheckoutState())
    val state: StateFlow<CheckoutState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            RazorpayCoordinator.results.collect { result ->
                when (result) {
                    is RazorpayResult.Failure -> _state.value = _state.value.copy(loading = false, error = result.message)
                    is RazorpayResult.Success -> verify(result)
                }
            }
        }
    }

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

    fun chooseMealType(value: String) { _state.value = _state.value.copy(mealType = value) }

    fun pay(activity: Activity, lines: List<CartLine>) {
        if (repository == null || lines.isEmpty() || _state.value.paymentChoice != PaymentChoice.Online) return
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            runCatching {
                val items = mutableListOf<CheckoutItem>()
                for (line in lines) {
                    items += CheckoutItem(line.mealId, line.quantity)
                    for (addOn in line.addOns) items += CheckoutItem(addOn.id, addOn.quantity * line.quantity)
                }
                repository.createPaymentOrder(PaymentOrderRequest(items, _state.value.appliedCoupon?.code, _state.value.mealType))
            }.onSuccess { order ->
                _state.value = _state.value.copy(loading = false)
                val options = JSONObject()
                    .put("name", "BTS Baba Tiffin Services")
                    .put("description", "Order ${order.orderNumber}")
                    .put("order_id", order.razorpayOrderId)
                    .put("currency", order.currency)
                    .put("amount", order.amount)
                    .put("theme", JSONObject().put("color", "#E97800"))
                Checkout().apply { setKeyID(order.keyId) }.open(activity, options)
            }.onFailure { _state.value = _state.value.copy(loading = false, error = "Payment could not be started. Please retry.") }
        }
    }

    private suspend fun verify(result: RazorpayResult.Success) {
        _state.value = _state.value.copy(loading = true, error = null)
        runCatching { repository?.verifyPayment(PaymentVerification(result.orderId, result.paymentId, result.signature)) }
            .onSuccess { _state.value = _state.value.copy(loading = false, paymentComplete = it?.verified == true) }
            .onFailure { _state.value = _state.value.copy(loading = false, error = "Payment received but verification is pending. Do not pay again; check Orders.") }
    }

    fun consumeCompletion() { _state.value = _state.value.copy(paymentComplete = false) }

    fun discount(subtotal: Double): Double {
        val coupon = _state.value.appliedCoupon ?: return 0.0
        val percentage = coupon.discountPercent?.let { subtotal * it / 100.0 } ?: 0.0
        return maxOf(coupon.discountAmount ?: 0.0, percentage).coerceIn(0.0, subtotal)
    }
}
