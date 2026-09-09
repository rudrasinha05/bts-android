package com.babatiffin.bts.data.cart

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.cartDataStore by preferencesDataStore("bts_cart")

@Serializable
data class CartAddOn(val id: String, val name: String, val unitPrice: Double, val quantity: Int)

@Serializable
data class CartLine(
    val id: String,
    val mealId: String,
    val mealName: String,
    val unitPrice: Double,
    val quantity: Int = 1,
    val addOns: List<CartAddOn> = emptyList(),
) {
    val lineTotal: Double get() = quantity * (unitPrice + addOns.sumOf { it.unitPrice * it.quantity })
}

interface CartRepository {
    val lines: Flow<List<CartLine>>
    suspend fun add(line: CartLine)
    suspend fun setQuantity(lineId: String, quantity: Int)
    suspend fun remove(lineId: String)
    suspend fun clear()
}

class DataStoreCartRepository(context: Context) : CartRepository {
    private val store = context.applicationContext.cartDataStore
    private val key = stringPreferencesKey("configured_lines")
    private val json = Json { ignoreUnknownKeys = true }

    override val lines: Flow<List<CartLine>> = store.data.map { preferences ->
        preferences[key]?.let { runCatching { json.decodeFromString<List<CartLine>>(it) }.getOrDefault(emptyList()) }.orEmpty()
    }

    override suspend fun add(line: CartLine) = update { it + line }
    override suspend fun setQuantity(lineId: String, quantity: Int) = update { lines ->
        if (quantity <= 0) lines.filterNot { it.id == lineId }
        else lines.map { if (it.id == lineId) it.copy(quantity = quantity.coerceAtMost(20)) else it }
    }
    override suspend fun remove(lineId: String) = update { it.filterNot { line -> line.id == lineId } }
    override suspend fun clear() { store.edit { it.remove(key) } }

    private suspend fun update(transform: (List<CartLine>) -> List<CartLine>) {
        store.edit { preferences ->
            val current = preferences[key]?.let { runCatching { json.decodeFromString<List<CartLine>>(it) }.getOrDefault(emptyList()) }.orEmpty()
            preferences[key] = json.encodeToString(transform(current))
        }
    }
}
