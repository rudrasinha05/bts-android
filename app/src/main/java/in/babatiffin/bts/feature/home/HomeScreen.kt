package com.babatiffin.bts.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babatiffin.bts.feature.menu.MealDiscoveryState
import com.babatiffin.bts.data.menu.Meal
import com.babatiffin.bts.feature.menu.MealImage
import kotlinx.coroutines.delay
import java.util.Calendar
import com.babatiffin.bts.domain.AppRules

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
        item { PromotionSlideshow(promotionalMeals(state.meals)) }
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
private fun PromotionSlideshow(promotions: List<Meal>) {
    if (promotions.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { promotions.size })
    LaunchedEffect(promotions.size) {
        if (promotions.size > 1) {
            while (true) {
                delay(4_000)
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % promotions.size)
            }
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(210.dp)) { page ->
            val meal = promotions[page]
            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp))) {
                MealImage(meal, Modifier.fillMaxSize())
                Column(
                    Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color.Black.copy(alpha = 0.62f)).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(promotionTitle(meal), color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(meal.description, color = Color.White, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(promotions.size) { index ->
                Spacer(
                    Modifier.size(if (pagerState.currentPage == index) 18.dp else 7.dp, 7.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                )
            }
        }
    }
}

private fun promotionalMeals(meals: List<Meal>): List<Meal> {
    if (meals.isEmpty()) return fallbackPromotions
    val keywords = listOf("biryani", "paneer", "chicken", "breakfast", "fish")
    val selected = keywords.mapNotNull { keyword ->
        meals.firstOrNull { it.name.contains(keyword, ignoreCase = true) || it.category.equals(keyword, ignoreCase = true) }
    }.distinctBy(Meal::id)
    return (selected + meals).distinctBy(Meal::id).take(6)
}

private val fallbackPromotions = listOf(
    Meal("promo-biryani", "Chicken Biryani", "Slow-cooked biryani with fragrant rice and homestyle spices.", "special", "chicken", "regular", 0.0, emptyList(), emptyList(), null),
    Meal("promo-paneer", "Shahi Paneer", "Creamy paneer favourite for a comforting meal.", "dinner", "veg", "regular", 0.0, emptyList(), emptyList(), null),
    Meal("promo-breakfast", "Idli Sambar", "A light and wholesome start to your day.", "breakfast", "veg", "regular", 0.0, emptyList(), emptyList(), null),
    Meal("promo-fish", "Fish Curry", "Fresh fish cooked in a rich homestyle curry.", "dinner", "fish", "regular", 0.0, emptyList(), emptyList(), null),
)

private fun promotionTitle(meal: Meal): String = when {
    meal.name.contains("biryani", ignoreCase = true) -> "Biryani special"
    meal.foodType.equals("chicken", ignoreCase = true) -> "Chicken favourite"
    meal.foodType.equals("fish", ignoreCase = true) -> "Fresh fish special"
    meal.category.equals("breakfast", ignoreCase = true) -> "Start your day right"
    else -> meal.name
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

private fun homeRails(meals: List<Meal>): List<Pair<String, List<Meal>>> {
    val timelyCategory = currentMealCategory()
    return listOf(
        "Popular meals" to meals.take(10),
        "${timelyCategory.replaceFirstChar(Char::uppercase)} right now" to meals.filter { it.category == timelyCategory },
        "Chicken favourites" to meals.filter { it.foodType == "chicken" },
        "Egg meals" to meals.filter { it.foodType == "egg" },
        "Fish meals" to meals.filter { it.foodType == "fish" },
        "Specials" to meals.filter { it.category == "special" || it.foodType == "special" },
    ).filter { it.second.isNotEmpty() }
}

private fun currentMealCategory(): String = AppRules.mealCategoryForHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
