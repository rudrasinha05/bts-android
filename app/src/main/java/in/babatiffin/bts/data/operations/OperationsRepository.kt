package com.babatiffin.bts.data.operations

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Serializable data class KitchenOrder(val id:String,@SerialName("order_id")val orderId:String,val status:String)
@Serializable data class PackingBatch(val id:String,@SerialName("batch_date")val batchDate:String,@SerialName("meal_type")val mealType:String,val status:String)
@Serializable data class InventoryItem(val id:String,val name:String,val unit:String,@SerialName("reorder_level")val reorderLevel:Double)
@Serializable data class InventoryStock(@SerialName("inventory_item_id")val inventoryItemId:String,val quantity:Double)
@Serializable data class DeliveryBatch(val id:String,@SerialName("batch_date")val batchDate:String,@SerialName("meal_type")val mealType:String,val status:String)
@Serializable data class DeliveryAssignment(val id:String,@SerialName("order_id")val orderId:String,val status:String,@SerialName("delivered_at")val deliveredAt:String?=null)

interface OperationsRepository {
    suspend fun kitchenOrders():List<KitchenOrder>;suspend fun packingBatches():List<PackingBatch>
    suspend fun inventoryItems():List<InventoryItem>;suspend fun inventoryStock():List<InventoryStock>
    suspend fun deliveryBatches():List<DeliveryBatch>;suspend fun deliveryAssignments():List<DeliveryAssignment>
    suspend fun kitchenStatus(id:String,status:String);suspend fun packingStatus(id:String,status:String);suspend fun deliveryStatus(id:String,status:String)
}

class SupabaseOperationsRepository(private val client:SupabaseClient):OperationsRepository{
    override suspend fun kitchenOrders()=client.from("kitchen_orders").select().decodeList<KitchenOrder>()
    override suspend fun packingBatches()=client.from("packing_batches").select().decodeList<PackingBatch>()
    override suspend fun inventoryItems()=client.from("inventory_items").select{filter{eq("is_active",true)}}.decodeList<InventoryItem>()
    override suspend fun inventoryStock()=client.from("inventory_stock").select().decodeList<InventoryStock>()
    override suspend fun deliveryBatches()=client.from("delivery_batches").select().decodeList<DeliveryBatch>()
    override suspend fun deliveryAssignments()=client.from("delivery_assignments").select().decodeList<DeliveryAssignment>()
    override suspend fun kitchenStatus(id:String,status:String){client.from("kitchen_orders").update({set("status",status)}){filter{eq("id",id)}}}
    override suspend fun packingStatus(id:String,status:String){client.from("packing_batches").update({set("status",status)}){filter{eq("id",id)}}}
    override suspend fun deliveryStatus(id:String,status:String){client.from("delivery_assignments").update({set("status",status);if(status=="delivered")set("delivered_at",utcNow())}){filter{eq("id",id)}}}
    private fun utcNow()=SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US).apply{timeZone=TimeZone.getTimeZone("UTC")}.format(Date())
}
