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
import com.babatiffin.bts.data.menu.Meal
import com.babatiffin.bts.data.order.OrderDetails
import com.babatiffin.bts.data.subscription.Subscription
import com.babatiffin.bts.data.subscription.SubscriptionPlan

@Composable
fun PlansScreen(
    state: SubscriptionState,
    mealById: Map<String, Meal>,
    orderHistory: List<OrderDetails>,
    onPlanAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentSubscription = state.subscriptions.firstOrNull { it.status == "active" || it.status == "paused" }
    val currentPlan = state.plans.firstOrNull { it.id == currentSubscription?.planId }
    val recommendedIds = recommendedPlanIds(state.plans, mealById, orderHistory)
    LazyColumn(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Subscription plans", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold) }
        item {
            Text(
                if (currentPlan == null) "Choose from all available plans. Recommendations use your meal and order history."
                else "Current plan: ${currentPlan.name}. Higher plans are available for upgrade.",
            )
        }
        if (state.loading) item { CircularProgressIndicator() }
        state.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        items(state.plans.size) { index ->
            val plan = state.plans[index]
            val isCurrent = plan.id == currentPlan?.id
            val isUpgrade = currentPlan != null && plan.price > currentPlan.price
            val isRecommended = currentPlan == null && plan.id in recommendedIds
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        plan.name + when {
                            isCurrent -> " • Current"
                            isRecommended -> " • Recommended"
                            plan.isPopular -> " • Popular"
                            else -> ""
                        },
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Text(plan.description)
                    Text("${plan.mealsPerCycle} meals • ${plan.mealSchedule.replace('_', ' ')} • ${plan.foodType}")
                    for (highlight in plan.highlights) Text("• $highlight")
                    Text("₹${plan.price.toInt()} / ${plan.billingCycle}", fontWeight = FontWeight.Bold)
                    when {
                        isUpgrade -> Button(onClick = onPlanAction, modifier = Modifier.fillMaxWidth()) { Text("Upgrade") }
                        currentPlan == null -> Button(onClick = onPlanAction, modifier = Modifier.fillMaxWidth()) { Text("Choose plan") }
                    }
                }
            }
        }
        if (currentPlan != null) item { OutlinedButton(onClick = onPlanAction, modifier = Modifier.fillMaxWidth()) { Text("Manage current subscription") } }
    }
}

private fun recommendedPlanIds(
    plans: List<SubscriptionPlan>,
    mealById: Map<String, Meal>,
    orders: List<OrderDetails>,
): Set<String> {
    val meals = orders.flatMap(OrderDetails::items).mapNotNull { it.mealId?.let(mealById::get) }
    if (meals.isEmpty()) return plans.filter(SubscriptionPlan::isPopular).take(2).map(SubscriptionPlan::id).toSet()
    val preferredFood = meals.groupingBy(Meal::foodType).eachCount().maxByOrNull { it.value }?.key
    val preferredSchedule = meals.groupingBy(Meal::category).eachCount().maxByOrNull { it.value }?.key
    return plans.sortedByDescending { plan ->
        (if (plan.foodType.equals(preferredFood, true) || plan.foodType.equals("all", true)) 4 else 0) +
            (if (plan.mealSchedule.contains(preferredSchedule.orEmpty(), true)) 3 else 0) +
            (if (plan.isPopular) 1 else 0)
    }.take(2).map(SubscriptionPlan::id).toSet()
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
