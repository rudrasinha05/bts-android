package com.babatiffin.bts.feature.menu

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babatiffin.bts.data.menu.Meal

@Composable
fun MenuScreen(
    state: MealDiscoveryState,
    onCategory: (String) -> Unit,
    onFoodType: (String) -> Unit,
    onRetry: () -> Unit,
    onOpenMeal: (String) -> Unit,
    onAddMeal: (Meal) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCategories by rememberSaveable { mutableStateOf(false) }
    var foodGroup by rememberSaveable { mutableStateOf("all") }
    val visibleMeals = state.visibleMeals.filter { it.matchesMenuFoodGroup(foodGroup) }

    Box(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Text(
                "Our menu",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text("Browse by meal time, food preference or dish category.", modifier = Modifier.padding(bottom = 8.dp))
            if (foodGroup != "all") {
                val selectedGroup = menuFoodGroups.firstOrNull { it.key == foodGroup }
                FilterChip(
                    selected = true,
                    onClick = { foodGroup = "all" },
                    label = { Text((selectedGroup?.title ?: "Category") + " ×") },
                )
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (value in state.categories) {
                    FilterChip(
                        selected = state.category == value,
                        onClick = { foodGroup = "all"; onCategory(value) },
                        label = { Text(value.replaceFirstChar(Char::uppercase)) },
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (value in menuFoodTypes) {
                    FilterChip(
                        selected = state.foodType == value,
                        onClick = { foodGroup = "all"; onFoodType(value) },
                        label = { Text(value.replaceFirstChar(Char::uppercase)) },
                    )
                }
            }
            when {
                state.loading -> CircularProgressIndicator(Modifier.padding(24.dp))
                state.error != null -> Column(Modifier.padding(vertical = 24.dp)) {
                    Text(state.error)
                    Button(onClick = onRetry) { Text("Retry") }
                }
                visibleMeals.isEmpty() -> Text("No meals available in this category.", modifier = Modifier.padding(24.dp))
                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f).padding(top = 8.dp, bottom = 88.dp),
                ) {
                    items(visibleMeals.size) { index ->
                        val meal = visibleMeals[index]
                        Card(onClick = { onOpenMeal(meal.id) }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                MealImage(meal, Modifier.fillMaxWidth().height(180.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(meal.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text("₹${meal.price.toInt()}", fontWeight = FontWeight.SemiBold)
                                }
                                Text(meal.description)
                                Text(
                                    "${meal.category.replaceFirstChar(Char::uppercase)} • ${meal.foodType.replaceFirstChar(Char::uppercase)}",
                                    style = MaterialTheme.typography.labelMedium,
                                )
                                Button(onClick = { onAddMeal(meal) }, modifier = Modifier.fillMaxWidth()) { Text("Add") }
                            }
                        }
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = { showCategories = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ) { Text("Menu", fontWeight = FontWeight.Bold) }
    }

    if (showCategories) {
        AlertDialog(
            onDismissRequest = { showCategories = false },
            title = { Text("Browse food categories") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { Text("Dish categories", fontWeight = FontWeight.Bold) }
                    items(menuFoodGroups.size) { index ->
                        val group = menuFoodGroups[index]
                        val available = state.meals.count { it.matchesMenuFoodGroup(group.key) }
                        OutlinedButton(
                            onClick = {
                                onCategory("all")
                                onFoodType("all")
                                foodGroup = group.key
                                showCategories = false
                            },
                            enabled = available > 0,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                Text("${group.title} ($available)", fontWeight = FontWeight.SemiBold)
                                Text(group.description, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    item {
                        val otherCount = state.meals.count { it.menuFoodGroupKey() == "other" }
                        OutlinedButton(
                            onClick = {
                                onCategory("all")
                                onFoodType("all")
                                foodGroup = "other"
                                showCategories = false
                            },
                            enabled = otherCount > 0,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.fillMaxWidth()) {
                                Text("Other meals ($otherCount)", fontWeight = FontWeight.SemiBold)
                                Text("Thalis, combos and remaining dishes", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                    item { Text("Meal time", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }
                    items(state.categories.size) { index ->
                        val category = state.categories[index]
                        OutlinedButton(
                            onClick = {
                                foodGroup = "all"
                                onFoodType("all")
                                onCategory(category)
                                showCategories = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(category.replaceFirstChar(Char::uppercase)) }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    foodGroup = "all"
                    onCategory("all")
                    onFoodType("all")
                    showCategories = false
                }) { Text("Show all") }
            },
        )
    }
}

private val menuFoodTypes = listOf("all", "veg", "egg", "chicken", "fish", "special")
