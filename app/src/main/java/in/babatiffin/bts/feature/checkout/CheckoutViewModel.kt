package com.babatiffin.bts.feature.checkout

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.babatiffin.bts.core.payment.RazorpayCoordinator
import com.babatiffin.bts.core.payment.RazorpayResult
import com.babatiffin.bts.data.cart.CartLine
import com.babatiffin.bts.data.checkout.*
import com.babatiffin.bts.domain.AppRules
import com.razorpay.Checkout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
    val completedOrderId: String? = null,
    val error: String? = null,
    val verificationPending: Boolean = false,
    val paymentUncertain: Boolean = false,
)

class CheckoutViewModel(private val repository: CheckoutRepository?, private val savedState: SavedStateHandle = SavedStateHandle()) : ViewModel() {
    private val mutableState = MutableStateFlow(CheckoutState())
    val state = mutableState.asStateFlow()
    private var sessionVersion = 0
    private var cachedRequest: PaymentOrderRequest? = null
    private var cachedOrder: PaymentOrder? = null
    private var pendingProof: RazorpayResult.Success? = null
    private var verifying = false

    init { viewModelScope.launch { RazorpayCoordinator.results.collect(::handleResult) } }

    fun load(userId: String?) {
        if (userId == state.value.userId) return
        val version = ++sessionVersion
        cachedRequest = null; cachedOrder = null; pendingProof = null; verifying = false
        if (userId != null && savedState.get<String>("paymentOwner") == userId) {
            cachedRequest = savedState.get<String>("paymentRequest")?.let { runCatching { Json.decodeFromString<PaymentOrderRequest>(it) }.getOrNull() }
            cachedOrder = savedState.get<String>("paymentOrder")?.let { runCatching { Json.decodeFromString<PaymentOrder>(it) }.getOrNull() }
            savedState.get<ArrayList<String>>("paymentProof")?.takeIf { it.size == 3 }?.let { pendingProof = RazorpayResult.Success(it[0], it[1], it[2]) }
        } else clearAttempt()
        val uncertain = savedState.get<Boolean>("paymentInFlight") == true && pendingProof == null
        mutableState.value = CheckoutState(userId = userId, loading = userId != null && repository != null,
            verificationPending = pendingProof != null, paymentUncertain = uncertain,
            error = if (uncertain) "A payment was interrupted. Check Orders before paying again." else null)
        if (userId == null) return
        if (repository == null) { mutableState.value = state.value.copy(error = "Backend configuration is required."); return }
        viewModelScope.launch {
            val wallet = runCatching { repository.wallet(userId) }.getOrNull()
            val coupons = runCatching { repository.coupons() }
            if (version == sessionVersion) mutableState.value = state.value.copy(loading = false, wallet = wallet,
                coupons = coupons.getOrDefault(emptyList()), error = state.value.error ?: if (coupons.isFailure) "Offers could not be loaded. You can continue without a coupon." else null)
        }
    }

    fun setCouponCode(value: String) { if (!state.value.loading) mutableState.value = state.value.copy(couponCode = value.uppercase(), appliedCoupon = null, error = null) }
    fun applyCoupon() {
        if (state.value.loading) return
        val coupon = state.value.coupons.firstOrNull { it.code.equals(state.value.couponCode.trim(), true) }
        mutableState.value = state.value.copy(appliedCoupon = coupon, error = if (coupon == null) "This coupon is not active or does not exist." else null)
    }
    fun choosePayment(choice: PaymentChoice) { if (!state.value.loading) mutableState.value = state.value.copy(paymentChoice = choice) }
    fun chooseMealType(value: String) { if (!state.value.loading && value in listOf("breakfast", "lunch", "dinner")) mutableState.value = state.value.copy(mealType = value) }

    fun pay(activity: Activity, lines: List<CartLine>, addressId: String?) = startPayment(lines, addressId) { order ->
        val options = JSONObject().put("name", "BTS Baba Tiffin Services")
            .put("description", "Order ${order.orderNumber}").put("order_id", order.razorpayOrderId)
            .put("currency", order.currency).put("amount", order.amount)
            .put("theme", JSONObject().put("color", "#E97800"))
        Checkout().apply { setKeyID(order.keyId) }.open(activity, options)
    }

    internal fun startPayment(lines: List<CartLine>, addressId: String?, open: (PaymentOrder) -> Unit) {
        val snapshot = state.value
        if (snapshot.loading || snapshot.verificationPending || snapshot.paymentUncertain || snapshot.paymentComplete) return
        if (repository == null || snapshot.userId == null || lines.isEmpty() || addressId.isNullOrBlank()) {
            mutableState.value = snapshot.copy(error = "Sign in, add a meal and select a delivery address."); return
        }
        val request = PaymentOrderRequest(lines.flatMap { line ->
            listOf(CheckoutItem(line.mealId, line.quantity)) + line.addOns.map { CheckoutItem(it.id, it.quantity * line.quantity) }
        }, snapshot.appliedCoupon?.code, snapshot.mealType, addressId = addressId)
        val version = sessionVersion
        if (cachedRequest != request) { cachedRequest = null; cachedOrder = null }
        persistAttempt(snapshot.userId, true)
        // Set before launching: two fast taps cannot create two requests.
        mutableState.value = snapshot.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                if (snapshot.paymentChoice == PaymentChoice.Wallet) {
                    val result = repository.payWithWallet(request)
                    if (version != sessionVersion) return@launch
                    mutableState.value = state.value.copy(loading = false, paymentComplete = result.verified,
                        completedOrderId = if (result.verified) result.orderId else null,
                        paymentUncertain = !result.verified, error = if (result.verified) null else "Wallet payment is pending. Check Orders before paying again.",
                        wallet = state.value.wallet?.copy(balance = result.balance))
                    if (result.verified) clearAttempt()
                } else {
                    val order = if (cachedRequest == request) cachedOrder ?: repository.createPaymentOrder(request)
                        else repository.createPaymentOrder(request)
                    if (version != sessionVersion) return@launch
                    cachedRequest = request; cachedOrder = order
                    persistAttempt(snapshot.userId, true)
                    try { open(order) } catch (_: Exception) {
                        persistAttempt(snapshot.userId, false)
                        mutableState.value = state.value.copy(loading = false, error = "Payment window could not open. Retry to reopen the same order.")
                    }
                    // Remain busy until the provider callback arrives.
                }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                if (version == sessionVersion && e is CheckoutRejected) clearAttempt()
                if (version == sessionVersion) mutableState.value = state.value.copy(loading = false,
                    paymentUncertain = e !is CheckoutRejected,
                    error = if (e is CheckoutRejected) e.message else "The payment request outcome is unknown. Check Orders before paying again.")
            }
        }
    }

    internal fun handleResult(result: RazorpayResult) {
        if (state.value.userId == null || state.value.paymentComplete || cachedOrder == null) return
        when (result) {
            is RazorpayResult.Failure -> if (pendingProof == null) {
                persistAttempt(state.value.userId!!, false)
                mutableState.value = state.value.copy(loading = false, paymentUncertain = false,
                    error = "Payment was cancelled or failed. Your cart is kept. Retry uses the same order if checkout details are unchanged.")
            }
            is RazorpayResult.Success -> {
                if (result.orderId != cachedOrder?.razorpayOrderId) return
                pendingProof = result
                persistAttempt(state.value.userId!!, true)
                mutableState.value = state.value.copy(verificationPending = true)
                retryVerification()
            }
        }
    }

    fun retryVerification() {
        val proof = pendingProof ?: return
        if (verifying || repository == null) return
        verifying = true
        val version = sessionVersion
        mutableState.value = state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val result = repository.verifyPayment(PaymentVerification(proof.orderId, proof.paymentId, proof.signature))
                if (version != sessionVersion) return@launch
                if (!result.verified) error("Verification pending")
                pendingProof = null
                clearAttempt()
                mutableState.value = state.value.copy(loading = false, verificationPending = false,
                    paymentUncertain = false, paymentComplete = true, completedOrderId = result.orderId)
            } catch (e: CancellationException) { throw e
            } catch (_: Exception) {
                if (version == sessionVersion) mutableState.value = state.value.copy(loading = false,
                    verificationPending = true, error = "Payment verification is pending. Use Check payment status; do not pay again.")
            } finally { if (version == sessionVersion) verifying = false }
        }
    }

    fun consumeCompletion() {
        cachedRequest = null; cachedOrder = null
        mutableState.value = state.value.copy(paymentComplete = false, completedOrderId = null)
    }

    private fun persistAttempt(userId: String, inFlight: Boolean) {
        savedState["paymentOwner"] = userId
        savedState["paymentInFlight"] = inFlight
        savedState["paymentRequest"] = cachedRequest?.let { Json.encodeToString(it) }
        savedState["paymentOrder"] = cachedOrder?.let { Json.encodeToString(it) }
        savedState["paymentProof"] = pendingProof?.let { arrayListOf(it.orderId, it.paymentId, it.signature) }
    }

    private fun clearAttempt() {
        for (key in listOf("paymentOwner", "paymentInFlight", "paymentRequest", "paymentOrder", "paymentProof")) savedState.remove<Any>(key)
    }

    fun discount(subtotal: Double): Double = AppRules.discount(subtotal, state.value.appliedCoupon?.discountAmount, state.value.appliedCoupon?.discountPercent)
}
