package com.babatiffin.bts.feature.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babatiffin.bts.data.cart.CartLine
import com.babatiffin.bts.data.menu.Meal

@Composable
fun AddOnDialog(
    meal: Meal,
    line: CartLine?,
    addOns: List<Meal>,
    onMealQuantity: (Int, Int) -> Unit,
    onAddOnQuantity: (Meal, Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val mealQuantity = line?.quantity ?: 1
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Customise ${meal.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                QuantityRow(
                    name = meal.name,
                    price = meal.price,
                    quantity = mealQuantity,
                    onMinus = { onMealQuantity(mealQuantity, -1) },
                    onPlus = { onMealQuantity(mealQuantity, 1) },
                    canRemove = false,
                )
                Text("Add-ons", fontWeight = FontWeight.Bold)
                LazyColumn(Modifier.heightIn(max = 360.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(addOns, key = Meal::id) { addOn ->
                        val quantity = line?.addOns?.firstOrNull { it.id == addOn.id }?.quantity ?: 0
                        QuantityRow(
                            name = addOn.name,
                            price = addOn.price,
                            quantity = quantity,
                            onMinus = { onAddOnQuantity(addOn, quantity, -1) },
                            onPlus = { onAddOnQuantity(addOn, quantity, 1) },
                        )
                    }
                }
                val total = line?.lineTotal ?: meal.price
                Text("Total: ₹${total.toInt()}", fontWeight = FontWeight.Bold)
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Done") } },
    )
}

@Composable
private fun QuantityRow(
    name: String,
    price: Double,
    quantity: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    canRemove: Boolean = true,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.SemiBold)
            Text("₹${price.toInt()}")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = onMinus, enabled = quantity > if (canRemove) 0 else 1) { Text("−") }
            Text(quantity.toString())
            Button(onClick = onPlus, enabled = quantity < 10) { Text("+") }
        }
    }
}
