package com.babatiffin.bts.feature.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Subscriptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.babatiffin.bts.data.order.OrderDetails
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable fun ProfileScreen(
    state: CustomerState,
    accountEmail: String?,
    accountPhone: String?,
    onSave: (String, String, String) -> Unit,
    onAddNewAddress: () -> Unit,
    onDefault: (com.babatiffin.bts.data.customer.Address) -> Unit,
    onDelete: (com.babatiffin.bts.data.customer.Address) -> Unit,
    onOrders: () -> Unit,
    onSubscription: () -> Unit,
    onNutrition: () -> Unit,
    onReferrals: () -> Unit,
    onSupport: () -> Unit,
    onSignOut: () -> Unit,
) {
    var name by remember(state.profile) { mutableStateOf(state.profile?.fullName ?: "") }
    var phone by remember(state.profile) { mutableStateOf(state.profile?.phone ?: "") }
    var dob by remember(state.profile) { mutableStateOf(state.profile?.dateOfBirth ?: "") }
    var addressMenuExpanded by remember { mutableStateOf(false) }
    var showProfileEditor by rememberSaveable { mutableStateOf(false) }
    var showAddressBook by rememberSaveable { mutableStateOf(false) }
    var selectedAddressId by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(state.addresses) {
        if (state.addresses.none { it.id == selectedAddressId }) {
            selectedAddressId = (state.addresses.firstOrNull { it.isDefault } ?: state.addresses.firstOrNull())?.id
        }
    }
    val selectedAddress = state.addresses.firstOrNull { it.id == selectedAddressId }
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                            Text(name.trim().firstOrNull()?.uppercase() ?: "B", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(name.ifBlank { "BTS customer" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            state.profile?.email ?: accountEmail ?: "Email not added",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            state.profile?.phone ?: accountPhone ?: "Phone number not added",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        TextButton(onClick = { showProfileEditor = !showProfileEditor }, contentPadding = PaddingValues(0.dp)) {
                            Text(if (showProfileEditor) "Close editor" else "Edit profile")
                        }
                    }
                }
            }
        }
        if (showProfileEditor) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Field("Full name", name) { name = it }
                        Field("Phone", phone) { phone = it }
                        Field("Date of birth (YYYY-MM-DD)", dob) { dob = it }
                        Button(
                            onClick = { onSave(name, phone, dob); showProfileEditor = false },
                            enabled = !state.loading,
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Save profile") }
                    }
                }
            }
        }
        state.message?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.primary) } }
        item {
            ProfileSection("Food & membership") {
                ProfileMenuRow(Icons.Outlined.ReceiptLong, "Your orders", onOrders)
                HorizontalDivider()
                ProfileMenuRow(
                    icon = Icons.Outlined.Home,
                    title = "Address book",
                    onClick = { showAddressBook = !showAddressBook },
                )
                HorizontalDivider()
                ProfileMenuRow(Icons.Outlined.Subscriptions, "Subscription plans", onSubscription)
                HorizontalDivider()
                ProfileMenuRow(Icons.Outlined.MonitorHeart, "Nutrition tracker", onNutrition)
            }
        }
        if (showAddressBook) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = onAddNewAddress, modifier = Modifier.fillMaxWidth()) { Text("Add new address") }
                        if (state.addresses.isEmpty()) {
                            Text("No saved address.")
                        } else {
                            Box(Modifier.fillMaxWidth()) {
                                OutlinedButton(onClick = { addressMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(selectedAddress?.let { "${it.label}${if (it.isDefault) " · Default" else ""}" } ?: "Select saved address")
                                }
                                DropdownMenu(expanded = addressMenuExpanded, onDismissRequest = { addressMenuExpanded = false }) {
                                    for (address in state.addresses) {
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text("${address.label}${if (address.isDefault) " · Default" else ""}", fontWeight = FontWeight.Bold)
                                                    Text("${address.line1}, ${address.city} - ${address.pincode}", style = MaterialTheme.typography.bodySmall)
                                                }
                                            },
                                            onClick = { selectedAddressId = address.id; addressMenuExpanded = false },
                                        )
                                    }
                                }
                            }
                            selectedAddress?.let { address ->
                                Text(listOfNotNull(address.line1, address.line2, address.landmark, address.city, address.state, address.pincode).joinToString(", "))
                                Row {
                                    TextButton(onClick = { onDefault(address) }, enabled = !address.isDefault) { Text("Make default") }
                                    TextButton(onClick = { onDelete(address) }) { Text("Remove") }
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            ProfileSection("Rewards & support") {
                ProfileMenuRow(Icons.Outlined.CardGiftcard, "Refer & earn", onReferrals)
                HorizontalDivider()
                ProfileMenuRow(Icons.Outlined.HelpOutline, "Help & support", onSupport)
            }
        }
        item {
            ProfileSection("Account") {
                ProfileMenuRow(Icons.Outlined.Logout, "Log out", onSignOut, showArrow = false)
            }
        }
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Column(content = content) }
    }
}

@Composable
private fun ProfileMenuRow(icon: ImageVector, title: String, onClick: () -> Unit, showArrow: Boolean = true) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { if (showArrow) Icon(Icons.Outlined.ChevronRight, contentDescription = null) },
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    )
}

@Composable
fun NutritionScreen(
    state: CustomerState,
    orderHistory: List<OrderDetails>,
    onSave: (String, String, String, String, String, String) -> Unit,
) {
    var goal by remember(state.nutrition) { mutableStateOf(state.nutrition?.goal.orEmpty()) }
    var calories by remember(state.nutrition) { mutableStateOf(state.nutrition?.targetCalories?.toString().orEmpty()) }
    var protein by remember(state.nutrition) { mutableStateOf(state.nutrition?.targetProtein?.toString().orEmpty()) }
    var carbs by remember(state.nutrition) { mutableStateOf(state.nutrition?.targetCarbs?.toString().orEmpty()) }
    var fat by remember(state.nutrition) { mutableStateOf(state.nutrition?.targetFat?.toString().orEmpty()) }
    var fiber by remember(state.nutrition) { mutableStateOf(state.nutrition?.targetFiber?.toString().orEmpty()) }
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val dates = remember(orderHistory) {
        val calendar = Calendar.getInstance()
        (0..13).map {
            formatter.format(calendar.time).also { calendar.add(Calendar.DAY_OF_YEAR, -1) }
        }.reversed()
    }
    val nutritionByMeal = state.mealNutrition.associateBy { it.mealId }
    val daily = dates.associateWith { date ->
        calculateNutrition(orderHistory.filter { it.order.scheduledDate == date }, nutritionByMeal)
    }
    val today = daily[dates.last()] ?: NutritionSummary()
    val target = state.nutrition

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Nutrition tracker", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Daily nutrition versus your targets", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            NutritionCard("Today's progress") {
                NutritionProgress("Calories", today.calories, target?.targetCalories?.toDouble(), "kcal")
                NutritionProgress("Protein", today.protein, target?.targetProtein?.toDouble(), "g")
                NutritionProgress("Carbs", today.carbs, target?.targetCarbs?.toDouble(), "g")
                NutritionProgress("Fat", today.fat, target?.targetFat?.toDouble(), "g")
                NutritionProgress("Fibre", today.fiber, target?.targetFiber?.toDouble(), "g")
                Text(
                    "Nutrition values are estimates calculated from meal portions. This is not medical advice.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            NutritionCard("Your targets") {
                Field("Goal", goal) { goal = it }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Field("Calories", calories, Modifier.weight(1f), KeyboardType.Number) { calories = it.filter(Char::isDigit) }
                    Field("Protein (g)", protein, Modifier.weight(1f), KeyboardType.Number) { protein = it.filter(Char::isDigit) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Field("Carbs (g)", carbs, Modifier.weight(1f), KeyboardType.Number) { carbs = it.filter(Char::isDigit) }
                    Field("Fat (g)", fat, Modifier.weight(1f), KeyboardType.Number) { fat = it.filter(Char::isDigit) }
                }
                Field("Fibre (g)", fiber, keyboardType = KeyboardType.Number) { fiber = it.filter(Char::isDigit) }
                Button(
                    onClick = { onSave(goal, calories, protein, carbs, fat, fiber) },
                    enabled = !state.loading,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Save targets") }
            }
        }
        item {
            NutritionCard("History (14 days)") {
                if (daily.values.none { it.hasData }) {
                    Text("No history yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Order meals to see your nutrition trend.")
                } else {
                    val maxCalories = daily.values.maxOf { it.calories }.coerceAtLeast(1.0)
                    daily.filterValues { it.hasData }.forEach { (date, value) ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(date)
                            Text("${value.calories.toInt()} kcal · ${value.protein.toInt()}g protein")
                        }
                        LinearProgressIndicator(
                            progress = { (value.calories / maxCalories).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            content()
        }
    }
}

@Composable
private fun NutritionProgress(label: String, value: Double, target: Double?, unit: String) {
    val progress = if (target != null && target > 0) (value / target).toFloat().coerceIn(0f, 1f) else 0f
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text("${value.toInt()}$unit${target?.let { " / ${it.toInt()}$unit" }.orEmpty()}")
    }
    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
}

private data class NutritionSummary(
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0,
) {
    val hasData get() = calories > 0 || protein > 0 || carbs > 0 || fat > 0 || fiber > 0
}

private fun calculateNutrition(
    orders: List<OrderDetails>,
    nutritionByMeal: Map<String, com.babatiffin.bts.data.customer.MealNutrition>,
): NutritionSummary = orders.flatMap(OrderDetails::items).fold(NutritionSummary()) { total, item ->
    val nutrition = item.mealId?.let(nutritionByMeal::get)
    NutritionSummary(
        total.calories + (nutrition?.calories ?: 0.0) * item.quantity,
        total.protein + (nutrition?.protein ?: 0.0) * item.quantity,
        total.carbs + (nutrition?.carbs ?: 0.0) * item.quantity,
        total.fat + (nutrition?.fat ?: 0.0) * item.quantity,
        total.fiber + (nutrition?.fiber ?: 0.0) * item.quantity,
    )
}

@Composable fun SupportScreen(state: CustomerState,onCreate:(String,String,String)->Unit){var subject by remember{mutableStateOf("")};var message by remember{mutableStateOf("")};var category by remember{mutableStateOf("general")};LazyColumn(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Text("Support",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Field("Subject",subject){subject=it};Field("Message",message){message=it};Field("Category",category){category=it};Button({onCreate(subject,message,category)}){Text("Create ticket")};state.message?.let{Text(it)};HorizontalDivider();Text("Your tickets",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)};items(state.tickets.size){i->val t=state.tickets[i];Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text(t.subject,fontWeight=FontWeight.Bold);Text("${t.category} · ${t.status}");Text(t.message)}}}}}

@Composable
private fun Field(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValue: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
    )
}
