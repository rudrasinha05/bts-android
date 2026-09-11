package com.babatiffin.bts.feature.operations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babatiffin.bts.data.operations.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.babatiffin.bts.core.security.canAccessDeliveryOperations
import com.babatiffin.bts.core.security.canAccessKitchenOperations

data class OperationsState(val loading:Boolean=false,val roles:Set<String> = emptySet(),val kitchen:List<KitchenOrder> = emptyList(),val packing:List<PackingBatch> = emptyList(),val items:List<InventoryItem> = emptyList(),val stock:List<InventoryStock> = emptyList(),val deliveryBatches:List<DeliveryBatch> = emptyList(),val assignments:List<DeliveryAssignment> = emptyList(),val error:String?=null)
class OperationsViewModel(private val repository:OperationsRepository?):ViewModel(){private val mutable=MutableStateFlow(OperationsState());val state=mutable.asStateFlow();fun load(roles:Set<String>){val repo=repository?:return;viewModelScope.launch{mutable.value=mutable.value.copy(loading=true,roles=roles,error=null);runCatching{val kitchen=roles.canAccessKitchenOperations();val delivery=roles.canAccessDeliveryOperations();mutable.value.copy(loading=false,kitchen=if(kitchen)repo.kitchenOrders()else emptyList(),packing=if(kitchen)repo.packingBatches()else emptyList(),items=if(kitchen)repo.inventoryItems()else emptyList(),stock=if(kitchen)repo.inventoryStock()else emptyList(),deliveryBatches=if(delivery)repo.deliveryBatches()else emptyList(),assignments=if(delivery)repo.deliveryAssignments()else emptyList())}.onSuccess{mutable.value=it}.onFailure{mutable.value=mutable.value.copy(loading=false,error="Operations data could not be loaded.")}}}fun kitchen(id:String,status:String)=change{it.kitchenStatus(id,status)};fun packing(id:String,status:String)=change{it.packingStatus(id,status)};fun delivery(id:String,status:String)=change{it.deliveryStatus(id,status)};private fun change(action:suspend(OperationsRepository)->Unit){val repo=repository?:return;viewModelScope.launch{runCatching{action(repo)};load(mutable.value.roles)}}}

@Composable private fun Header(title:String,state:OperationsState){Column{Text(title,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);if(state.loading)CircularProgressIndicator();state.error?.let{Text(it,color=MaterialTheme.colorScheme.error)}}}
@Composable private fun StatusCard(title:String,status:String,next:String?,onNext:()->Unit){Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text(title,fontWeight=FontWeight.Bold);Text("Status: $status");if(next!=null)Button(onClick=onNext){Text("Mark $next")}}}}
@Composable fun KitchenScreen(state:OperationsState,onStatus:(String,String)->Unit){LazyColumn(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Header("Kitchen orders",state)};if(state.kitchen.isEmpty())item{Text("No kitchen orders assigned.")}else items(state.kitchen){o->val next=when(o.status.lowercase()){"pending"->"preparing";"preparing"->"ready";else->null};StatusCard("Order ${o.orderId.take(8)}",o.status,next){next?.let{onStatus(o.id,it)}}}}}
@Composable fun PackingScreen(state:OperationsState,onStatus:(String,String)->Unit){LazyColumn(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Header("Packing batches",state)};if(state.packing.isEmpty())item{Text("No packing batches available.")}else items(state.packing){b->val next=when(b.status.lowercase()){"pending"->"packing";"packing"->"packed";else->null};StatusCard("${b.batchDate} · ${b.mealType}",b.status,next){next?.let{onStatus(b.id,it)}}}}}
@Composable fun InventoryScreen(state:OperationsState){val stock=state.stock.associateBy{it.inventoryItemId};LazyColumn(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Header("Inventory",state)};if(state.items.isEmpty())item{Text("No inventory items available.")}else items(state.items){i->val qty=stock[i.id]?.quantity?:0.0;Card(Modifier.fillMaxWidth()){Row(Modifier.padding(12.dp).fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Column{Text(i.name,fontWeight=FontWeight.Bold);Text("${qty} ${i.unit}")};Text(if(qty<=i.reorderLevel)"LOW STOCK" else "Available",color=if(qty<=i.reorderLevel)MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)}}}}}
@Composable fun DeliveryScreen(state:OperationsState,onStatus:(String,String)->Unit){LazyColumn(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Header("Delivery operations",state);Text("${state.deliveryBatches.size} active/available batches")};if(state.assignments.isEmpty())item{Text("No delivery assignments available.")}else items(state.assignments){a->val next=when(a.status.lowercase()){"pending"->"out_for_delivery";"out_for_delivery"->"delivered";else->null};StatusCard("Order ${a.orderId.take(8)}",a.status,next){next?.let{onStatus(a.id,it)}}}}}
