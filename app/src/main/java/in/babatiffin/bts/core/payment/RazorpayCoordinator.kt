package com.babatiffin.bts.core.payment

import com.razorpay.PaymentData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface RazorpayResult {
    data class Success(val orderId: String, val paymentId: String, val signature: String) : RazorpayResult
    data class Failure(val message: String) : RazorpayResult
}

object RazorpayCoordinator {
    private val mutableResults = MutableSharedFlow<RazorpayResult>(extraBufferCapacity = 1)
    val results = mutableResults.asSharedFlow()

    fun success(data: PaymentData?) {
        val orderId = data?.orderId
        val paymentId = data?.paymentId
        val signature = data?.signature
        if (orderId == null || paymentId == null || signature == null) failure("Incomplete payment response")
        else mutableResults.tryEmit(RazorpayResult.Success(orderId, paymentId, signature))
    }

    fun failure(message: String?) {
        mutableResults.tryEmit(RazorpayResult.Failure(message ?: "Payment was cancelled or failed."))
    }
}
