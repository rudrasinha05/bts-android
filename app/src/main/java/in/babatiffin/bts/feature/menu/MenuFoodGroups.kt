package com.babatiffin.bts.feature.menu

import com.babatiffin.bts.data.menu.Meal

data class MenuFoodGroup(
    val key: String,
    val title: String,
    val description: String,
    val keywords: List<String>,
)

val menuFoodGroups = listOf(
    MenuFoodGroup("rice", "Rice", "Plain rice, jeera rice, pulao, biryani and khichdi", listOf("rice", "chawal", "pulao", "biryani", "khichdi")),
    MenuFoodGroup("paneer", "Paneer curries", "Paneer butter masala, kadai paneer, shahi paneer and more", listOf("paneer")),
    MenuFoodGroup("chicken", "Chicken curries", "Chicken curry, kadai chicken, butter chicken and more", listOf("chicken", "murgh")),
    MenuFoodGroup("dal", "Dal & legumes", "Dal, rajma, chole, chana and lentil dishes", listOf("dal", "daal", "rajma", "chole", "chana", "lentil")),
    MenuFoodGroup("bread", "Roti & breads", "Roti, chapati, naan, paratha, puri and kulcha", listOf("roti", "chapati", "naan", "paratha", "poori", "puri", "kulcha", "bread")),
    MenuFoodGroup("vegetable", "Vegetable curries", "Seasonal sabzi, kofta and vegetarian curries", listOf("sabzi", "sabji", "vegetable", "veggie", "aloo", "gobhi", "bhindi", "kofta", "matar")),
    MenuFoodGroup("egg", "Egg dishes", "Egg curry, boiled eggs, omelette and more", listOf("egg", "anda", "omelette", "boiled egg")),
    MenuFoodGroup("fish", "Fish dishes", "Fish curry, fried fish and seafood meals", listOf("fish", "machli", "seafood")),
    MenuFoodGroup("breakfast", "Breakfast & snacks", "Poha, upma, chilla, idli and quick bites", listOf("poha", "upma", "chilla", "idli", "dosa", "snack", "pakora", "samosa")),
    MenuFoodGroup("sweet", "Sweets & drinks", "Desserts, sweets, raita and beverages", listOf("sweet", "dessert", "halwa", "kheer", "gulab", "lassi", "juice", "drink", "raita")),
)

// Dish form takes precedence over ingredients; whole words avoid "anda" in "kanda".
fun Meal.menuFoodGroupKey(): String {
    val words = name.lowercase(java.util.Locale.ROOT).split(Regex("[^\\p{L}\\p{N}]+")).toSet()
    fun has(vararg values: String) = values.any { it in words }
    return when {
        has("thali", "combo", "platter") -> "other"
        has("rice", "chawal", "pulao", "pulav", "biryani", "khichdi") -> "rice"
        has("roti", "chapati", "naan", "paratha", "parantha", "poori", "puri", "kulcha") -> "bread"
        has("poha", "upma", "chilla", "cheela", "idli", "dosa", "pakora", "samosa", "sandwich", "toast") -> "breakfast"
        has("halwa", "kheer", "gulab", "lassi", "juice", "raita", "curd") -> "sweet"
        has("paneer") -> "paneer"
        has("chicken", "murgh") -> "chicken"
        has("dal", "daal", "rajma", "chole", "chana", "lentil") -> "dal"
        has("egg", "eggs", "anda", "omelette", "omelet") -> "egg"
        has("fish", "machli", "seafood") -> "fish"
        has("sabzi", "sabji", "vegetable", "aloo", "gobhi", "bhindi", "kofta", "matar") -> "vegetable"
        else -> "other"
    }
}

fun Meal.matchesMenuFoodGroup(groupKey: String): Boolean =
    groupKey == "all" || menuFoodGroupKey() == groupKey
