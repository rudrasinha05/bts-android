package com.babatiffin.bts.feature.subscription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babatiffin.bts.data.subscription.Subscription

@Composable
fun PlansScreen(state: SubscriptionState, onManage: () -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Subscription plans", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold) }
        if (state.loading) item { CircularProgressIndicator() }
        state.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        items(state.plans.size) { index ->
            val plan = state.plans[index]
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(plan.name + if (plan.isPopular) " • Popular" else "", style = MaterialTheme.typography.titleLarge)
                    Text(plan.description)
                    Text("${plan.mealsPerCycle} meals • ${plan.mealSchedule.replace('_', ' ')} • ${plan.foodType}")
                    plan.highlights.forEach { Text("• $it") }
                    Text("₹${plan.price.toInt()} / ${plan.billingCycle}", fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            Button(onClick = onManage, modifier = Modifier.fillMaxWidth()) { Text("Manage my subscription") }
            Text("New plan activation will be completed through checkout in M9.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ManageSubscriptionScreen(
    state: SubscriptionState,
    onStatus: (Subscription, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("My subscription", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold) }
        if (state.loading) item { CircularProgressIndicator() }
        state.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        if (!state.loading && state.subscriptions.isEmpty()) item { Text("No subscription is active on this account.") }
        items(state.subscriptions.size) { index ->
            val subscription = state.subscriptions[index]
            val plan = state.plans.firstOrNull { it.id == subscription.planId }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(plan?.name ?: "Subscription", style = MaterialTheme.typography.titleLarge)
                    Text("Status: ${subscription.status.replaceFirstChar(Char::uppercase)}")
                    Text("Started: ${subscription.startDate}")
                    subscription.nextBillingDate?.let { Text("Next billing: $it") }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (subscription.status == "active") OutlinedButton(onClick = { onStatus(subscription, "paused") }) { Text("Pause") }
                        if (subscription.status == "paused") Button(onClick = { onStatus(subscription, "active") }) { Text("Resume") }
                        if (subscription.status != "cancelled") OutlinedButton(onClick = { onStatus(subscription, "cancelled") }) { Text("Cancel") }
                    }
                }
            }
        }
    }
}
