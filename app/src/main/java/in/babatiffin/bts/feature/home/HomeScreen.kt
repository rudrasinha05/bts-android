package com.babatiffin.bts.feature.home

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babatiffin.bts.feature.menu.MealDiscoveryState

@Composable
fun HomeScreen(
    state: MealDiscoveryState,
    onOpenMenu: () -> Unit,
    onOpenMeal: (String) -> Unit,
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
        item { Text("Popular meals", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold) }
        when {
            state.loading -> item { CircularProgressIndicator() }
            state.error != null -> item { Column { Text(state.error); Button(onClick = onRetry) { Text("Retry") } } }
            else -> items(state.meals.take(4).size) { index ->
                val meal = state.meals[index]
                Card(onClick = { onOpenMeal(meal.id) }, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(meal.name, style = MaterialTheme.typography.titleMedium)
                            Text(meal.description, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("₹${meal.price.toInt()}", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
