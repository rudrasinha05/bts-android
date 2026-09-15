package com.babatiffin.bts.feature.checkout

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.babatiffin.bts.data.cart.CartLine
import com.babatiffin.bts.data.customer.Address
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Screen interactions only: no authentication, GPS requests or payment SDK calls. */
class CheckoutScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val home = Address("home", "customer", "Home", "Test house", pincode = "201310", latitude = 28.47, longitude = 77.50)
    private val office = home.copy(id = "office", label = "Office", line1 = "Test office")
    private val selected = mutableStateOf<Address?>(home)
    private val checkout = mutableStateOf(CheckoutState(userId = "customer"))
    private var payments = 0
    private var retries = 0
    private var additions = 0
    private var edits: String? = null
    private var orders = 0

    private fun render(error: String? = null) {
        compose.setContent {
            MaterialTheme {
                CheckoutScreen(
                    state = checkout.value,
                    lines = listOf(CartLine("line", "meal", "Test meal", 100.0)),
                    onCouponCode = {}, onApplyCoupon = {}, onPaymentChoice = {}, onMealType = {},
                    address = selected.value, addresses = listOf(home, office), addressError = error,
                    onSelectAddress = { id -> selected.value = listOf(home, office).first { it.id == id } },
                    onRetryAddresses = { retries++ }, onEditAddress = { edits = it.id },
                    addressesLoading = false, onManageAddress = { additions++ },
                    onRetryVerification = { retries++ }, onViewOrders = { orders++ },
                    onPay = { payments++ },
                )
            }
        }
    }

    private fun pay() = compose.onNodeWithText("Place order · Pay ₹100").performScrollTo()
    private fun confirm() = compose.onNodeWithText("Confirm delivery location").performScrollTo().performClick()
    private fun accept() = compose.onNode(isToggleable()).performScrollTo().performClick()

    @Test fun paymentRequiresBothLocationAndConsent() {
        render()
        pay().assertIsNotEnabled()
        confirm()
        pay().assertIsNotEnabled()
        accept()
        pay().assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(1, payments) }
    }

    @Test fun changingAddressRequiresFreshConfirmationAndConsent() {
        render()
        confirm()
        accept()
        pay().assertIsEnabled()
        compose.onNodeWithText("Change address").performScrollTo().performClick()
        compose.onNodeWithText("Office").performClick()
        pay().assertIsNotEnabled()
        confirm()
        pay().assertIsNotEnabled()
        accept()
        pay().assertIsEnabled()
        compose.runOnIdle { assertEquals("office", selected.value?.id) }
    }

    @Test fun addressPickerRoutesAddAndEdit() {
        render()
        compose.onNodeWithText("Edit this address").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("home", edits) }
        compose.onNodeWithText("Change address").performScrollTo().performClick()
        compose.onNodeWithText("Add new address").performClick()
        compose.runOnIdle { assertEquals(1, additions) }
        compose.onNodeWithText("Close").assertDoesNotExist()
    }

    @Test fun missingAddressRoutesToAddressForm() {
        selected.value = null
        render()
        compose.onNodeWithText("Add delivery address").performScrollTo().performClick()
        pay().assertIsNotEnabled()
        compose.runOnIdle { assertEquals(1, additions) }
    }

    @Test fun addressLoadFailureOffersRetryAndBlocksPayment() {
        render(error = "Unable to load addresses")
        compose.onNodeWithText("Retry addresses").performScrollTo().performClick()
        pay().assertIsNotEnabled()
        compose.runOnIdle { assertEquals(1, retries) }
    }

    @Test fun pendingVerificationAllowsStatusCheckButNeverRepayment() {
        render()
        confirm()
        accept()
        compose.runOnIdle { checkout.value = checkout.value.copy(verificationPending = true) }
        pay().assertIsNotEnabled()
        compose.onNodeWithText("Check payment status").performScrollTo().performClick()
        compose.onNodeWithText("View orders").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(1, retries)
            assertEquals(1, orders)
            assertEquals(0, payments)
        }
    }

    @Test fun uncertainPaymentBlocksPayEvenAfterConfirmation() {
        render()
        confirm()
        accept()
        compose.runOnIdle { checkout.value = checkout.value.copy(paymentUncertain = true) }
        pay().assertIsNotEnabled()
        compose.onNodeWithText("Check payment status").assertDoesNotExist()
        compose.onNodeWithText("View orders").performScrollTo().assertIsEnabled()
    }
}
