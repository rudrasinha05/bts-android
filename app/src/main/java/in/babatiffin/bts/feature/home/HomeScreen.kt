package com.babatiffin.bts.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babatiffin.bts.feature.menu.MealDiscoveryState
import com.babatiffin.bts.data.menu.Meal
import com.babatiffin.bts.feature.menu.MealImage

@Composable
fun HomeScreen(
    state: MealDiscoveryState,
    onOpenMenu: () -> Unit,
    onOpenMeal: (String) -> Unit,
    onAddMeal: (Meal) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text(
                text = "Ghar jaisa khana, every day",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Fresh tiffins prepared with simple ingredients and delivered with care.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = onOpenMenu) {
                Text("Explore menu")
            }
        }
        when {
            state.loading -> item { CircularProgressIndicator() }
            state.error != null -> item { Column { Text(state.error); Button(onClick = onRetry) { Text("Retry") } } }
            else -> for (rail in homeRails(state.meals)) {
                item { Text(rail.first, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(rail.second, key = Meal::id) { meal ->
                            HomeMealCard(meal, onOpenMeal, onAddMeal)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeMealCard(meal: Meal, onOpenMeal: (String) -> Unit, onAddMeal: (Meal) -> Unit) {
    Card(onClick = { onOpenMeal(meal.id) }, modifier = Modifier.width(180.dp)) {
        Column {
            MealImage(meal, Modifier.fillMaxWidth().height(112.dp))
            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(meal.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("₹${meal.price.toInt()}", fontWeight = FontWeight.Bold)
                    Button(onClick = { onAddMeal(meal) }) { Text("Add") }
                }
            }
        }
    }
}

private fun homeRails(meals: List<Meal>): List<Pair<String, List<Meal>>> = listOf(
    "Popular meals" to meals.take(10),
    "Breakfast" to meals.filter { it.category == "breakfast" },
    "Lunch" to meals.filter { it.category == "lunch" },
    "Dinner" to meals.filter { it.category == "dinner" },
    "Chicken favourites" to meals.filter { it.foodType == "chicken" },
    "Egg meals" to meals.filter { it.foodType == "egg" },
    "Fish meals" to meals.filter { it.foodType == "fish" },
    "Specials" to meals.filter { it.category == "special" || it.foodType == "special" },
).filter { it.second.isNotEmpty() }
