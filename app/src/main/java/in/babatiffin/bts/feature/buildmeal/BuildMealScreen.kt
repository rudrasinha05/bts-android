package com.babatiffin.bts.feature.buildmeal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babatiffin.bts.feature.menu.MealDiscoveryState

@Composable
fun BuildMealScreen(
    state: MealDiscoveryState,
    onCategory: (String) -> Unit,
    onFoodType: (String) -> Unit,
    onMeal: (String) -> Unit,
    onAddOn: (String, Int) -> Unit,
    onAddToCart: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var step by remember { mutableIntStateOf(0) }
    val categoryMeals = state.meals.filter { state.category == "all" || it.category == state.category }
    val foodTypes = categoryMeals.map { it.foodType }.distinct()

    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Build your meal", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        Text("Step ${step + 1} of 4")
        if (state.loading) CircularProgressIndicator()
        else if (state.error != null) {
            Text(state.error)
            Button(onClick = onRetry) { Text("Retry") }
        } else when (step) {
            0 -> ChoiceList(
                title = "Choose meal time",
                values = state.categories.filterNot { it == "all" },
                selected = state.category,
                onSelect = onCategory,
            )
            1 -> ChoiceList(
                title = "Choose food preference",
                values = foodTypes,
                selected = state.foodType,
                onSelect = onFoodType,
            )
            2 -> {
                Text("Choose one main meal", style = MaterialTheme.typography.titleLarge)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(state.visibleMeals.size) { index ->
                        val meal = state.visibleMeals[index]
                        Card(onClick = { onMeal(meal.id) }, modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(meal.name, fontWeight = FontWeight.SemiBold)
                                    Text(meal.portion)
                                }
                                Text("₹${meal.price.toInt()}")
                            }
                        }
                    }
                }
                state.selectedMeal?.let { Text("Selected: ${it.name}", fontWeight = FontWeight.SemiBold) }
            }
            else -> {
                Text("Add sides and extras", style = MaterialTheme.typography.titleLarge)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                    items(state.addOns.size) { index ->
                        val addOn = state.addOns[index]
                        val quantity = state.addOnQuantities[addOn.id] ?: 0
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column { Text(addOn.name); Text("₹${addOn.price.toInt()} • ${addOn.portion}") }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(onClick = { onAddOn(addOn.id, -1) }, enabled = quantity > 0) { Text("−") }
                                    Text(quantity.toString(), modifier = Modifier.padding(top = 12.dp))
                                    Button(onClick = { onAddOn(addOn.id, 1) }, enabled = quantity < 10) { Text("+") }
                                }
                            }
                        }
                    }
                }
                Text("Total: ₹${state.configuredTotal.toInt()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }

        if (!state.loading && state.error == null) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = { step = (step - 1).coerceAtLeast(0) }, enabled = step > 0) { Text("Back") }
            if (step < 3) {
                val canContinue = when (step) { 0 -> state.category != "all"; 1 -> state.foodType != "all"; else -> state.selectedMeal != null }
                Button(onClick = { step++ }, enabled = canContinue) { Text("Continue") }
            } else Button(onClick = onAddToCart, enabled = state.selectedMeal != null) { Text("Add to cart") }
        }
    }
}

@Composable
private fun ChoiceList(title: String, values: List<String>, selected: String, onSelect: (String) -> Unit) {
    Text(title, style = MaterialTheme.typography.titleLarge)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (value in values) {
            FilterChip(
                selected = selected == value,
                onClick = { onSelect(value) },
                label = { Text(value.replaceFirstChar(Char::uppercase)) },
            )
        }
    }
}
