package com.babatiffin.bts.data.menu

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class Meal(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val foodType: String,
    val portion: String,
    val price: Double,
    val allergens: List<String>,
    val tags: List<String>,
    val imageUrl: String?,
)

interface MealRepository {
    suspend fun availableMeals(): List<Meal>
    suspend fun availableAddOns(): List<Meal>
}

@Serializable
private data class MealRow(
    val id: String,
    val name: String,
    val description: String = "",
    val category: String,
    @SerialName("food_type") val foodType: String,
    val portion: String = "regular",
    val price: Double,
    val allergens: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    @SerialName("image_url") val imageUrl: String? = null,
)

class SupabaseMealRepository(private val client: SupabaseClient) : MealRepository {
    override suspend fun availableMeals(): List<Meal> = loadMeals(isAddOn = false)

    override suspend fun availableAddOns(): List<Meal> = loadMeals(isAddOn = true)

    private suspend fun loadMeals(isAddOn: Boolean): List<Meal> = client.from("meals")
        .select {
            filter {
                eq("is_active", true)
                eq("is_available", true)
                eq("is_addon", isAddOn)
            }
        }
        .decodeList<MealRow>()
        .map { row ->
            Meal(
                id = row.id,
                name = row.name,
                description = row.description,
                category = row.category,
                foodType = row.foodType,
                portion = row.portion,
                price = row.price,
                allergens = row.allergens,
                tags = row.tags,
                imageUrl = row.imageUrl,
            )
        }
        .sortedWith(compareBy(Meal::category, Meal::name))
}
