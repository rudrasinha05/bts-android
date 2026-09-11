package com.babatiffin.bts.feature.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babatiffin.bts.data.cart.CartLine

@Composable
fun CartScreen(
    lines: List<CartLine>,
    onChangeQuantity: (CartLine, Int) -> Unit,
    onRemove: (String) -> Unit,
    onClear: () -> Unit,
    onCheckout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Your cart", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            if (lines.isNotEmpty()) TextButton(onClick = onClear) { Text("Clear") }
        }
        if (lines.isEmpty()) {
            Text("Your cart is empty. Configure a meal from the menu to get started.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                items(lines.size) { index ->
                    val line = lines[index]
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(line.mealName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("₹${line.lineTotal.toInt()}", fontWeight = FontWeight.SemiBold)
                            }
                            for (addOn in line.addOns) Text("+ ${addOn.name} × ${addOn.quantity}", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onChangeQuantity(line, -1) }) { Text("−") }
                                Text(line.quantity.toString(), modifier = Modifier.padding(top = 12.dp))
                                Button(onClick = { onChangeQuantity(line, 1) }, enabled = line.quantity < 20) { Text("+") }
                                TextButton(onClick = { onRemove(line.id) }) { Text("Remove") }
                            }
                        }
                    }
                }
                item {
                    Text(
                        "Subtotal: ₹${lines.sumOf(CartLine::lineTotal).toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    Text("Delivery charges, taxes and checkout are calculated in the payment milestone.")
                    Button(onClick = onCheckout, modifier = Modifier.fillMaxWidth()) { Text("Continue to checkout") }
                }
            }
        }
    }
}
