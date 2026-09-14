package com.babatiffin.bts.feature.subscription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babatiffin.bts.data.menu.Meal
import com.babatiffin.bts.data.order.OrderDetails
import com.babatiffin.bts.data.subscription.Subscription
import com.babatiffin.bts.data.subscription.SubscriptionPlan
import com.babatiffin.bts.domain.AppRules

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
    mealById: Map<String, Meal>,
    orderHistory: List<OrderDetails>,
    onStatus: (Subscription, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedPlanName by rememberSaveable { mutableStateOf<String?>(null) }
    val currentSubscription = state.subscriptions.firstOrNull { it.status == "active" || it.status == "paused" }
    val currentPlan = state.plans.firstOrNull { it.id == currentSubscription?.planId }
    val recommendedIds = recommendedPlanIds(state.plans, mealById, orderHistory)
    val displayedPlans = state.plans.filter { AppRules.isSuccessorPlan(currentPlan?.price, it.price) }
    LazyColumn(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("My subscription", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold) }
        if (state.loading) item { CircularProgressIndicator() }
        state.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        if (!state.loading && currentSubscription == null) {
            item {
                Text("No subscription is active. Choose from all plans below.")
                Text("Recommended plans are based on your order and meal history.", style = MaterialTheme.typography.bodySmall)
            }
        }
        currentSubscription?.let { subscription ->
            item {
                val plan = state.plans.firstOrNull { it.id == subscription.planId }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text((plan?.name ?: "Subscription") + " • Current", style = MaterialTheme.typography.titleLarge)
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
        if (!state.loading) {
            item { Text(if (currentPlan == null) "Available plans" else "Upgrade plans", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            for ((title, plans) in subscriptionRails(displayedPlans)) {
                item { Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                item {
                    if (plans.isEmpty()) {
                        Text("No $title plans available.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(plans, key = SubscriptionPlan::id) { plan ->
                                SubscriptionPlanCard(
                                    plan = plan,
                                    recommended = currentPlan == null && plan.id in recommendedIds,
                                    upgrade = currentPlan != null,
                                    onSelect = { selectedPlanName = plan.name },
                                )
                            }
                        }
                    }
                }
            }
        }
        selectedPlanName?.let { name ->
            item {
                Card(Modifier.fillMaxWidth()) {
                    Text("$name selected. Subscription payment will continue through the secure checkout flow.", Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SubscriptionPlanCard(
    plan: SubscriptionPlan,
    recommended: Boolean,
    upgrade: Boolean,
    onSelect: () -> Unit,
) {
    Card(Modifier.width(292.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(plan.name + if (recommended) " • Recommended" else if (plan.isPopular) " • Popular" else "", style = MaterialTheme.typography.titleLarge)
            Text(plan.description)
            Text("${plan.mealsPerCycle} meals • ${plan.mealSchedule.replace('_', ' ')} • ${plan.foodType}")
            for (highlight in plan.highlights) Text("• $highlight")
            Text("₹${plan.price.toInt()} / ${plan.billingCycle}", fontWeight = FontWeight.Bold)
            Button(onClick = onSelect, modifier = Modifier.fillMaxWidth()) { Text(if (upgrade) "Upgrade" else "Choose plan") }
        }
    }
}

private fun subscriptionRails(plans: List<SubscriptionPlan>): List<Pair<String, List<SubscriptionPlan>>> = listOf(
    "Weekly" to plans.filter { it.billingCycle.contains("week", ignoreCase = true) },
    "Monthly" to plans.filter { it.billingCycle.contains("month", ignoreCase = true) },
    "Quarterly" to plans.filter { it.billingCycle.contains("quarter", ignoreCase = true) },
    "Yearly" to plans.filter { it.billingCycle.contains("year", ignoreCase = true) },
)
