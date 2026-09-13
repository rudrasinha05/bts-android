package com.babatiffin.bts.feature.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babatiffin.bts.data.cart.CartLine
import com.babatiffin.bts.data.customer.Address
import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun CheckoutScreen(
    state: CheckoutState,
    lines: List<CartLine>,
    onCouponCode: (String) -> Unit,
    onApplyCoupon: () -> Unit,
    onPaymentChoice: (PaymentChoice) -> Unit,
    onMealType: (String) -> Unit,
    address: Address?,
    addressesLoading: Boolean,
    onManageAddress: () -> Unit,
    onPay: (Activity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var locationConfirmed by remember(address?.id) { mutableStateOf(false) }
    var subtotal = 0.0
    for (line in lines) subtotal += line.lineTotal
    val coupon = state.appliedCoupon
    val amountDiscount = coupon?.discountAmount ?: 0.0
    val percentDiscount = subtotal * (coupon?.discountPercent ?: 0.0) / 100.0
    var discount = if (amountDiscount > percentDiscount) amountDiscount else percentDiscount
    if (discount < 0.0) discount = 0.0
    if (discount > subtotal) discount = subtotal
    val total = subtotal - discount

    Column(
        modifier = modifier.padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Checkout", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        if (state.loading) CircularProgressIndicator()
        if (state.error != null) Text(state.error, color = MaterialTheme.colorScheme.error)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Order summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                for (line in lines) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${line.mealName} × ${line.quantity}")
                        Text("₹${line.lineTotal.toInt()}")
                    }
                }
                SummaryRow("Subtotal", subtotal)
                if (discount > 0) SummaryRow("Coupon discount", -discount)
                SummaryRow("Payable total", total, bold = true)
            }
        }

        Text("Delivery location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        when {
            addressesLoading -> CircularProgressIndicator()
            address == null -> {
                Text("Add at least one delivery address before checkout.", color = MaterialTheme.colorScheme.error)
                Button(onClick = onManageAddress, modifier = Modifier.fillMaxWidth()) { Text("Add delivery address") }
            }
            else -> Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${address.label}${if (address.isDefault) " · Default" else ""}", fontWeight = FontWeight.Bold)
                    Text("${address.line1}, ${address.city} - ${address.pincode}")
                    if (address.latitude != null && address.longitude != null) {
                        Text("Location updated", color = MaterialTheme.colorScheme.primary)
                    }
                    Button(onClick = { locationConfirmed = true }, enabled = !locationConfirmed, modifier = Modifier.fillMaxWidth()) {
                        Text(if (locationConfirmed) "Location confirmed" else "Confirm delivery location")
                    }
                    OutlinedButton(onClick = onManageAddress, modifier = Modifier.fillMaxWidth()) { Text("Change address") }
                }
            }
        }

        Text("Coupon", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = state.couponCode,
            onValueChange = onCouponCode,
            label = { Text("Coupon code") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedButton(onClick = onApplyCoupon, enabled = state.couponCode != "") { Text("Apply") }
        if (coupon != null) {
            val description = if (coupon.description == null) "" else ": ${coupon.description}"
            Text("${coupon.code} applied$description")
        }

        Text("Payment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Meal time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        PaymentOption("Breakfast", "breakfast", state.mealType, onMealType)
        PaymentOption("Lunch", "lunch", state.mealType, onMealType)
        PaymentOption("Dinner", "dinner", state.mealType, onMealType)
        PaymentOption("Pay securely online", PaymentChoice.Online, state.paymentChoice, onPaymentChoice)
        PaymentOption(
            "BTS Wallet · ₹${state.wallet?.balance?.toInt() ?: 0}",
            PaymentChoice.Wallet,
            state.paymentChoice,
            onPaymentChoice,
        )

        val activity = androidx.compose.ui.platform.LocalContext.current as Activity
        val walletReady = state.paymentChoice != PaymentChoice.Wallet || (state.wallet?.balance ?: 0.0) >= total
        Button(onClick = { onPay(activity) }, enabled = !state.loading && lines.isNotEmpty() && walletReady && address != null && locationConfirmed, modifier = Modifier.fillMaxWidth()) {
            Text("Pay ₹${total.toInt()}")
        }
        if (address != null && !locationConfirmed) Text("Confirm delivery location to continue.", color = MaterialTheme.colorScheme.error)
        if (state.paymentChoice == PaymentChoice.Wallet && !walletReady) Text("Insufficient wallet balance", color = MaterialTheme.colorScheme.error)
        Text(
            "Secure payment is created and verified by the BTS server. No Razorpay secret is stored in this app.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun PaymentOption(label: String, value: PaymentChoice, selected: PaymentChoice, onSelect: (PaymentChoice) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = value == selected, onClick = { onSelect(value) })
        Text(label)
    }
}

@Composable
private fun PaymentOption(label: String, value: String, selected: String, onSelect: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = value == selected, onClick = { onSelect(value) })
        Text(label)
    }
}

@Composable
private fun SummaryRow(label: String, value: Double, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text("₹${value.toInt()}", fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}
