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
    val price: Double,
    val tags: List<String>,
    val imageUrl: String?,
)

interface MealRepository {
    suspend fun availableMeals(): List<Meal>
}

@Serializable
private data class MealRow(
    val id: String,
    val name: String,
    val description: String = "",
    val category: String,
    @SerialName("food_type") val foodType: String,
    val price: Double,
    val tags: List<String> = emptyList(),
    @SerialName("image_url") val imageUrl: String? = null,
)

class SupabaseMealRepository(private val client: SupabaseClient) : MealRepository {
    override suspend fun availableMeals(): List<Meal> = client.from("meals")
        .select {
            filter {
                eq("is_active", true)
                eq("is_available", true)
                eq("is_addon", false)
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
                price = row.price,
                tags = row.tags,
                imageUrl = row.imageUrl,
            )
        }
        .sortedWith(compareBy(Meal::category, Meal::name))
}
