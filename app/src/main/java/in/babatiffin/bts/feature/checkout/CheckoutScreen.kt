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

@Composable
fun CheckoutScreen(
    state: CheckoutState,
    lines: List<CartLine>,
    onCouponCode: (String) -> Unit,
    onApplyCoupon: () -> Unit,
    onPaymentChoice: (PaymentChoice) -> Unit,
    modifier: Modifier = Modifier,
) {
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
        PaymentOption("Pay securely online", PaymentChoice.Online, state.paymentChoice, onPaymentChoice)
        PaymentOption(
            "BTS Wallet · ₹${state.wallet?.balance?.toInt() ?: 0}",
            PaymentChoice.Wallet,
            state.paymentChoice,
            onPaymentChoice,
        )

        Button(onClick = {}, enabled = false, modifier = Modifier.fillMaxWidth()) {
            Text("Pay ₹${total.toInt()}")
        }
        Text(
            "Payment remains locked until the existing BTS server checkout endpoint is connected. No Razorpay secret is stored in this app.",
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
private fun SummaryRow(label: String, value: Double, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text("₹${value.toInt()}", fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
    }
}
