package com.babatiffin.bts.data.order

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("order_number") val orderNumber: String,
    val status: String,
    val subtotal: Double,
    val discount: Double = 0.0,
    val total: Double,
    val notes: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class OrderItem(
    val id: String,
    @SerialName("order_id") val orderId: String,
    @SerialName("meal_id") val mealId: String? = null,
    val quantity: Int,
    @SerialName("unit_price") val unitPrice: Double,
)

@Serializable
data class OrderStatusEvent(
    val id: String,
    @SerialName("order_id") val orderId: String,
    @SerialName("from_status") val fromStatus: String? = null,
    @SerialName("to_status") val toStatus: String,
    @SerialName("created_at") val createdAt: String,
)

data class OrderDetails(val order: Order, val items: List<OrderItem>, val timeline: List<OrderStatusEvent>)

interface OrderRepository {
    suspend fun orders(userId: String): List<OrderDetails>
}

class SupabaseOrderRepository(private val client: SupabaseClient) : OrderRepository {
    override suspend fun orders(userId: String): List<OrderDetails> = client.from("orders")
        .select { filter { eq("user_id", userId) } }
        .decodeList<Order>()
        .sortedByDescending(Order::createdAt)
        .map { order ->
            val items = client.from("order_items").select { filter { eq("order_id", order.id) } }.decodeList<OrderItem>()
            val timeline = client.from("order_status_history").select { filter { eq("order_id", order.id) } }
                .decodeList<OrderStatusEvent>().sortedBy(OrderStatusEvent::createdAt)
            OrderDetails(order, items, timeline)
        }
}
