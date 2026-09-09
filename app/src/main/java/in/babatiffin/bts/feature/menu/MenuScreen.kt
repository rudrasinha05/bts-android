package com.babatiffin.bts.feature.menu

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MenuScreen(
    state: MealDiscoveryState,
    onCategory: (String) -> Unit,
    onFoodType: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(horizontal = 16.dp)) {
        Text("Our menu", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 16.dp))
        Text("Choose a category and discover today's available meals.", modifier = Modifier.padding(bottom = 8.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            state.categories.forEach { value ->
                FilterChip(selected = state.category == value, onClick = { onCategory(value) }, label = { Text(value.replaceFirstChar(Char::uppercase)) })
            }
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("all", "veg", "egg", "chicken", "fish", "special").forEach { value ->
                FilterChip(selected = state.foodType == value, onClick = { onFoodType(value) }, label = { Text(value.replaceFirstChar(Char::uppercase)) })
            }
        }
        when {
            state.loading -> CircularProgressIndicator(Modifier.padding(24.dp))
            state.error != null -> Column(Modifier.padding(vertical = 24.dp)) { Text(state.error); Button(onClick = onRetry) { Text("Retry") } }
            state.visibleMeals.isEmpty() -> Text("No meals available for this selection.", modifier = Modifier.padding(24.dp))
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f).padding(top = 8.dp),
            ) {
                items(state.visibleMeals.size) { index ->
                    val meal = state.visibleMeals[index]
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(meal.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text("₹${meal.price.toInt()}", fontWeight = FontWeight.SemiBold)
                            }
                            Text(meal.description)
                            Text("${meal.category.replaceFirstChar(Char::uppercase)} • ${meal.foodType.replaceFirstChar(Char::uppercase)}", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
