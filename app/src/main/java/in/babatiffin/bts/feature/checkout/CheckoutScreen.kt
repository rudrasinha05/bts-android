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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
    var termsAccepted by remember { mutableStateOf(false) }
    val subtotal = lines.sumOf(CartLine::lineTotal)
    val coupon = state.appliedCoupon
    val amountDiscount = coupon?.discountAmount ?: 0.0
    val percentDiscount = subtotal * (coupon?.discountPercent ?: 0.0) / 100.0
    var discount = if (amountDiscount > percentDiscount) amountDiscount else percentDiscount
    if (discount < 0.0) discount = 0.0
    if (discount > subtotal) discount = subtotal
    val total = subtotal - discount
    val locationReady = address?.latitude != null && address.longitude != null

    Column(
        modifier = modifier.padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Secure checkout", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text("Review delivery, items and payment before placing your order.", style = MaterialTheme.typography.bodyMedium)
        if (state.loading) CircularProgressIndicator()
        if (state.error != null) Text(state.error, color = MaterialTheme.colorScheme.error)
        if (lines.isEmpty()) {
            Card(Modifier.fillMaxWidth()) { Text("Your cart is empty. Add at least one meal before checkout.", Modifier.padding(16.dp)) }
        }

        CheckoutSectionTitle("1", "Delivery address")
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
                    Text(if (address.latitude != null && address.longitude != null) "GPS location attached" else "GPS location not attached", color = if (address.latitude != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                    Button(onClick = { locationConfirmed = true }, enabled = locationReady && !locationConfirmed, modifier = Modifier.fillMaxWidth()) {
                        Text(if (locationConfirmed) "Location confirmed" else if (locationReady) "Confirm delivery location" else "GPS location required")
                    }
                    OutlinedButton(onClick = onManageAddress, modifier = Modifier.fillMaxWidth()) { Text("Change address") }
                }
            }
        }

        CheckoutSectionTitle("2", "Delivery meal slot")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Text("Delivery date: Today", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp, bottom = 4.dp))
                PaymentOption("Breakfast", "breakfast", state.mealType, onMealType)
                PaymentOption("Lunch", "lunch", state.mealType, onMealType)
                PaymentOption("Dinner", "dinner", state.mealType, onMealType)
            }
        }

        CheckoutSectionTitle("3", "Order summary")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (line in lines) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(line.mealName, fontWeight = FontWeight.SemiBold)
                            Text("Quantity: ${line.quantity}${if (line.addOns.isNotEmpty()) " · ${line.addOns.size} add-ons" else ""}", style = MaterialTheme.typography.bodySmall)
                        }
                        Text("₹${line.lineTotal.toInt()}")
                    }
                }
                HorizontalDivider()
                SummaryRow("Item total", subtotal)
                if (discount > 0) SummaryRow("Coupon discount", -discount)
                SummaryRow("Delivery fee", 0.0, valueLabel = "FREE")
                HorizontalDivider()
                SummaryRow("Amount payable", total, bold = true)
            }
        }

        CheckoutSectionTitle("4", "Offers and coupon")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.couponCode,
                onValueChange = onCouponCode,
                label = { Text("Coupon code") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(onClick = onApplyCoupon, enabled = state.couponCode.isNotBlank()) { Text("Apply") }
        }
        if (coupon != null) Text("${coupon.code} applied${coupon.description?.let { ": $it" }.orEmpty()}", color = MaterialTheme.colorScheme.primary)

        CheckoutSectionTitle("5", "Payment method")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                PaymentOption("Razorpay · UPI, cards and netbanking", PaymentChoice.Online, state.paymentChoice, onPaymentChoice)
                PaymentOption("BTS Wallet · ₹${state.wallet?.balance?.toInt() ?: 0}", PaymentChoice.Wallet, state.paymentChoice, onPaymentChoice)
            }
        }

        val activity = androidx.compose.ui.platform.LocalContext.current as Activity
        val walletReady = state.paymentChoice != PaymentChoice.Wallet || (state.wallet?.balance ?: 0.0) >= total
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = termsAccepted, onCheckedChange = { termsAccepted = it })
            Text("I confirm the order, delivery location and payable amount.", style = MaterialTheme.typography.bodySmall)
        }
        Button(
            onClick = { onPay(activity) },
            enabled = !state.loading && lines.isNotEmpty() && walletReady && address != null && locationReady && locationConfirmed && termsAccepted,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Place order · Pay ₹${total.toInt()}")
        }
        if (address != null && !locationReady) Text("Attach GPS coordinates to the selected address before checkout.", color = MaterialTheme.colorScheme.error)
        if (locationReady && !locationConfirmed) Text("Confirm delivery location to continue.", color = MaterialTheme.colorScheme.error)
        if (state.paymentChoice == PaymentChoice.Wallet && !walletReady) Text("Insufficient wallet balance", color = MaterialTheme.colorScheme.error)
        Text(
            "Secure payment is created and verified by the BTS server. No Razorpay secret is stored in this app.",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun CheckoutSectionTitle(number: String, title: String) {
    Text("$number. $title", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
private fun SummaryRow(label: String, value: Double, bold: Boolean = false, valueLabel: String? = null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(valueLabel ?: "₹${value.toInt()}", fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}
