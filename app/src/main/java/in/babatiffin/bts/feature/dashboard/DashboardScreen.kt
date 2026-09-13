package com.babatiffin.bts.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babatiffin.bts.data.menu.Meal
import com.babatiffin.bts.feature.customer.CustomerState
import com.babatiffin.bts.feature.orders.OrdersState
import com.babatiffin.bts.feature.subscription.SubscriptionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    orders: OrdersState,
    subscriptions: SubscriptionState,
    customer: CustomerState,
    meals: List<Meal>,
    onBuildMeal: () -> Unit,
    onOrders: () -> Unit,
    onPlans: () -> Unit,
    onNutrition: () -> Unit,
    onMeal: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val todayOrder = orders.orders.firstOrNull { it.order.scheduledDate == today }
    val activeSubscription = subscriptions.subscriptions.firstOrNull { it.status == "active" || it.status == "paused" }
    val activePlan = subscriptions.plans.firstOrNull { it.id == activeSubscription?.planId }
    val nutritionByMeal = customer.mealNutrition.associateBy { it.mealId }
    val consumed = todayOrder?.items.orEmpty().fold(NutritionTotal()) { total, item ->
        val nutrition = item.mealId?.let(nutritionByMeal::get)
        total + NutritionTotal(
            calories = (nutrition?.calories ?: 0.0) * item.quantity,
            protein = (nutrition?.protein ?: 0.0) * item.quantity,
            carbs = (nutrition?.carbs ?: 0.0) * item.quantity,
            fat = (nutrition?.fat ?: 0.0) * item.quantity,
            fiber = (nutrition?.fiber ?: 0.0) * item.quantity,
        )
    }
    val recentMealIds = orders.orders.take(5).flatMap { details -> details.items.mapNotNull { it.mealId } }.toSet()
    val recommendations = (meals.filterNot { it.id in recentMealIds } + meals.filter { it.id in recentMealIds }).distinctBy { it.id }.take(4)

    LazyColumn(
        modifier = modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Dashboard", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Your meals, subscription and nutrition today", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            DashboardCard("Today's order") {
                if (todayOrder == null) {
                    Text("No meal scheduled for today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Build a meal to get today's tiffin sorted.")
                    Button(onClick = onBuildMeal, modifier = Modifier.fillMaxWidth()) { Text("Build a meal") }
                } else {
                    Text(todayOrder.order.orderNumber, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${todayOrder.items.sumOf { it.quantity }} item(s) · ${todayOrder.order.status.replace('_', ' ')}")
                    Text("₹${todayOrder.order.total.toInt()}", fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = onOrders, modifier = Modifier.fillMaxWidth()) { Text("Track order") }
                }
            }
        }
        item {
            DashboardCard("Subscription") {
                if (activeSubscription == null) {
                    Text("No active subscription", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Choose a plan for regular fresh tiffins.")
                    Button(onClick = onPlans, modifier = Modifier.fillMaxWidth()) { Text("View plans") }
                } else {
                    Text(activePlan?.name ?: "Active plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Status: ${activeSubscription.status.replaceFirstChar(Char::uppercase)}")
                    activeSubscription.nextBillingDate?.let { Text("Next billing: $it") }
                    OutlinedButton(onClick = onPlans, modifier = Modifier.fillMaxWidth()) { Text("Manage subscription") }
                }
            }
        }
        item {
            DashboardCard("Today's nutrition") {
                NutritionMetric("Calories", consumed.calories, customer.nutrition?.targetCalories?.toDouble(), "kcal")
                NutritionMetric("Protein", consumed.protein, customer.nutrition?.targetProtein?.toDouble(), "g")
                NutritionMetric("Carbs", consumed.carbs, customer.nutrition?.targetCarbs?.toDouble(), "g")
                NutritionMetric("Fat", consumed.fat, customer.nutrition?.targetFat?.toDouble(), "g")
                NutritionMetric("Fibre", consumed.fiber, customer.nutrition?.targetFiber?.toDouble(), "g")
                OutlinedButton(onClick = onNutrition, modifier = Modifier.fillMaxWidth()) { Text("Open nutrition tracker") }
            }
        }
        item {
            DashboardCard("Recommended for you") {
                if (recommendations.isEmpty()) Text("Recommendations will appear when meals are available.")
                recommendations.forEach { meal ->
                    Card(onClick = { onMeal(meal.id) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(meal.name, fontWeight = FontWeight.Bold)
                                Text("₹${meal.price.toInt()}", fontWeight = FontWeight.Bold)
                            }
                            Text("Suggested from today's available menu.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun NutritionMetric(label: String, value: Double, target: Double?, unit: String) {
    val progress = if (target != null && target > 0) (value / target).toFloat().coerceIn(0f, 1f) else 0f
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text("${value.toInt()}$unit${target?.let { " / ${it.toInt()}$unit" }.orEmpty()}")
    }
    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
}

private data class NutritionTotal(
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0,
) {
    operator fun plus(other: NutritionTotal) = NutritionTotal(
        calories + other.calories,
        protein + other.protein,
        carbs + other.carbs,
        fat + other.fat,
        fiber + other.fiber,
    )
}
