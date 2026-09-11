package com.babatiffin.bts.feature.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OrdersScreen(state: OrdersState, onOpen: (String) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("My orders", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold) }
        if (state.loading) item { CircularProgressIndicator() }
        state.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        if (!state.loading && state.orders.isEmpty()) item { Text("No orders found for this account.") }
        items(state.orders.size) { index ->
            val details = state.orders[index]
            Card(onClick = { onOpen(details.order.id) }, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("#${details.order.orderNumber}", fontWeight = FontWeight.SemiBold)
                        Text("₹${details.order.total.toInt()}", fontWeight = FontWeight.Bold)
                    }
                    Text("Status: ${details.order.status.title()}")
                    Text(details.order.createdAt.displayDate(), style = MaterialTheme.typography.bodySmall)
                    Text("${details.items.sumOf { it.quantity }} item(s)")
                }
            }
        }
    }
}

@Composable
fun OrderDetailScreen(state: OrdersState, mealNames: Map<String, String>, modifier: Modifier = Modifier) {
    val details = state.selected
    LazyColumn(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Order details", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold) }
        if (details == null) item { Text("Order details are unavailable.") }
        else {
            item {
                Text("#${details.order.orderNumber}", style = MaterialTheme.typography.titleLarge)
                Text("${details.order.status.title()} • ${details.order.createdAt.displayDate()}")
            }
            item { Text("Items", style = MaterialTheme.typography.titleLarge) }
            items(details.items.size) { index ->
                val item = details.items[index]
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.mealId?.let(mealNames::get) ?: "Meal"} × ${item.quantity}")
                        Text("₹${(item.unitPrice * item.quantity).toInt()}")
                    }
                }
            }
            item {
                Text("Status timeline", style = MaterialTheme.typography.titleLarge)
                if (details.timeline.isEmpty()) Text("● ${details.order.status.title()} — current status")
                else for (event in details.timeline) {
                    Text("● ${event.toStatus.title()} — ${event.createdAt.displayDate()}", modifier = Modifier.padding(vertical = 4.dp))
                }
            }
            item {
                Text("Subtotal: ₹${details.order.subtotal.toInt()}")
                if (details.order.discount > 0) Text("Discount: −₹${details.order.discount.toInt()}")
                Text("Total: ₹${details.order.total.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                details.order.notes?.takeIf(String::isNotBlank)?.let { Text("Notes: $it") }
            }
        }
    }
}

private fun String.title() = replace('_', ' ').replaceFirstChar(Char::uppercase)
private fun String.displayDate() = replace('T', ' ').take(16)
