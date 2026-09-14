package com.babatiffin.bts.feature.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OrderConfirmationScreen(orderId: String, onViewOrders: () -> Unit, onHome: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text("Order confirmed", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Your meal has been placed successfully.", modifier = Modifier.padding(vertical = 8.dp))
        Text("Order ID: ${orderId.take(8).uppercase()}", style = MaterialTheme.typography.labelLarge)
        Button(onClick = onViewOrders, modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) { Text("Track order") }
        OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) { Text("Back to Home") }
    }
}
