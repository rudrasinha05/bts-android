package com.babatiffin.bts.feature.checkout

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.SavedStateHandle
import com.babatiffin.bts.core.payment.RazorpayResult
import com.babatiffin.bts.data.cart.CartLine
import com.babatiffin.bts.data.checkout.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class CheckoutViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private lateinit var repository: FakeCheckout
    private lateinit var vm: CheckoutViewModel
    private val cart = listOf(CartLine("line", "meal", "Meal", 100.0))
    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        repository = FakeCheckout()
        vm = CheckoutViewModel(repository)
        store.put("checkout", vm)
        vm.load("owner")
        dispatcher.scheduler.runCurrent()
    }
    @After fun cleanup() { store.clear(); Dispatchers.resetMain() }

    @Test fun rapidTapsCreateOnlyOneOrderAndStayBusyUntilCallback() = runTest(dispatcher) {
        vm.startPayment(cart, "address") { }
        vm.startPayment(cart, "address") { }
        runCurrent()
        assertEquals(1, repository.created)
        assertTrue(vm.state.value.loading)
    }
    @Test fun cancelRetryReusesOrderAndKeepsAddress() = runTest(dispatcher) {
        vm.startPayment(cart, "address") { }; runCurrent()
        vm.handleResult(RazorpayResult.Failure("cancel"))
        assertFalse(vm.state.value.loading)
        vm.startPayment(cart, "address") { }; runCurrent()
        assertEquals(1, repository.created)
        assertEquals("address", repository.request?.addressId)
    }
    @Test fun verificationRetryNeverCreatesAnotherPayment() = runTest(dispatcher) {
        vm.startPayment(cart, "address") { }; runCurrent()
        repository.failVerification = true
        vm.handleResult(RazorpayResult.Success("provider", "payment", "signature")); runCurrent()
        assertTrue(vm.state.value.verificationPending)
        vm.startPayment(cart, "address") { }; runCurrent()
        assertEquals(1, repository.created)
        repository.failVerification = false
        vm.retryVerification(); runCurrent()
        assertTrue(vm.state.value.paymentComplete)
        assertEquals(2, repository.verified)
        assertEquals("order", vm.state.value.completedOrderId)
    }
    @Test fun unknownNetworkOutcomeBlocksBlindRetry() = runTest(dispatcher) {
        repository.failCreate = true
        vm.startPayment(cart, "address") { }; runCurrent()
        assertTrue(vm.state.value.paymentUncertain)
        vm.startPayment(cart, "address") { }; runCurrent()
        assertEquals(1, repository.created)
    }
    @Test fun missingAddressAndSignedOutAccountCannotPay() = runTest(dispatcher) {
        vm.startPayment(cart, null) { }; runCurrent()
        vm.load(null)
        vm.startPayment(cart, "address") { }; runCurrent()
        assertEquals(0, repository.created)
    }
    @Test fun staleAndRepeatedSuccessCallbacksAreIgnored() = runTest(dispatcher) {
        vm.startPayment(cart, "address") { }; runCurrent()
        vm.handleResult(RazorpayResult.Success("wrong-provider", "payment", "signature")); runCurrent()
        assertEquals(0, repository.verified)
        vm.handleResult(RazorpayResult.Success("provider", "payment", "signature")); runCurrent()
        vm.handleResult(RazorpayResult.Success("provider", "payment", "signature")); runCurrent()
        assertEquals(1, repository.verified)
        vm.load(null)
        vm.handleResult(RazorpayResult.Success("provider", "payment", "signature")); runCurrent()
        assertNull(vm.state.value.userId)
        assertFalse(vm.state.value.paymentComplete)
    }

    @Test fun restoredInterruptedAttemptBlocksNewPaymentForSameOwnerOnly() = runTest(dispatcher) {
        val saved = SavedStateHandle(mapOf("paymentOwner" to "owner", "paymentInFlight" to true))
        val restored = CheckoutViewModel(repository, saved)
        store.put("restored", restored)
        restored.load("owner"); runCurrent()
        assertTrue(restored.state.value.paymentUncertain)
        restored.startPayment(cart, "address") { }; runCurrent()
        assertEquals(0, repository.created)
        restored.load("other-owner"); runCurrent()
        assertFalse(restored.state.value.paymentUncertain)
    }

    @Test fun walletSuccessCompletesExactlyOnce() = runTest(dispatcher) {
        vm.choosePayment(PaymentChoice.Wallet)
        vm.startPayment(cart, "address") { fail("Wallet must not open Razorpay") }
        vm.startPayment(cart, "address") { }
        runCurrent()
        assertEquals(1, repository.walletPaid)
        assertTrue(vm.state.value.paymentComplete)
        assertEquals(400.0, vm.state.value.wallet!!.balance, 0.001)
    }

    private class FakeCheckout : CheckoutRepository {
        var created = 0; var verified = 0
        var walletPaid = 0
        var failCreate = false; var failVerification = false
        var request: PaymentOrderRequest? = null
        override suspend fun wallet(userId: String) = Wallet("wallet", userId, 500.0)
        override suspend fun coupons() = emptyList<Coupon>()
        override suspend fun createPaymentOrder(request: PaymentOrderRequest): PaymentOrder {
            created++; this.request = request
            if (failCreate) error("timeout")
            return PaymentOrder("order", "1", "provider", "test-key", 10000, "INR")
        }
        override suspend fun verifyPayment(request: PaymentVerification): VerificationResult {
            verified++
            if (failVerification) error("offline")
            return VerificationResult(true, "order")
        }
        override suspend fun payWithWallet(request: PaymentOrderRequest): WalletPaymentResult {
            walletPaid++
            return WalletPaymentResult(true, "order", 400.0)
        }
    }
}
