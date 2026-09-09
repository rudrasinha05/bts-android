package com.babatiffin.bts.feature.menu

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

@Composable
fun MealDetailScreen(
    state: MealDiscoveryState,
    onChangeAddOn: (String, Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val meal = state.selectedMeal
    when {
        state.loading -> CircularProgressIndicator(modifier.padding(24.dp))
        state.error != null -> Column(modifier.padding(24.dp)) {
            Text(state.error)
            Button(onClick = onRetry) { Text("Retry") }
        }
        meal == null -> Text("Meal is no longer available.", modifier.padding(24.dp))
        else -> LazyColumn(modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Text(meal.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text("${meal.category.title()} • ${meal.foodType.title()} • ${meal.portion}")
                Text(meal.description, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
                Text("₹${meal.price.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                if (meal.tags.isNotEmpty()) Text("Tags: ${meal.tags.joinToString()}")
                if (meal.allergens.isNotEmpty()) Text("Allergens: ${meal.allergens.joinToString()}", color = MaterialTheme.colorScheme.error)
            }
            item { Text("Universal add-ons", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
            items(state.addOns.size) { index ->
                val addOn = state.addOns[index]
                val quantity = state.addOnQuantities[addOn.id] ?: 0
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(addOn.name, fontWeight = FontWeight.SemiBold)
                                Text("₹${addOn.price.toInt()} • ${addOn.portion}")
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onChangeAddOn(addOn.id, -1) }, enabled = quantity > 0) { Text("−") }
                                Text(quantity.toString(), modifier = Modifier.padding(top = 12.dp))
                                Button(onClick = { onChangeAddOn(addOn.id, 1) }, enabled = quantity < 10) { Text("+") }
                            }
                        }
                    }
                }
            }
            item {
                Text("Configured total: ₹${state.configuredTotal.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Cart persistence will be enabled in the next frozen milestone.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun String.title(): String = replaceFirstChar(Char::uppercase)
