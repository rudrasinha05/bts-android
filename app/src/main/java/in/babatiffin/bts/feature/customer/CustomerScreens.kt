package com.babatiffin.bts.feature.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable fun ProfileScreen(
    state: CustomerState,
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
                        Text(state.profile?.email ?: phone.ifBlank { "Complete your profile" }, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                                    state.addresses.forEach { address ->
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

@Composable fun NutritionScreen(state: CustomerState, mealNames: Map<String,String>, onSave:(String,String,String,String,String,String)->Unit){
    var goal by remember(state.nutrition){mutableStateOf(state.nutrition?.goal?:"")};var calories by remember(state.nutrition){mutableStateOf(state.nutrition?.targetCalories?.toString()?:"")};var protein by remember(state.nutrition){mutableStateOf(state.nutrition?.targetProtein?.toString()?:"")};var carbs by remember(state.nutrition){mutableStateOf(state.nutrition?.targetCarbs?.toString()?:"")};var fat by remember(state.nutrition){mutableStateOf(state.nutrition?.targetFat?.toString()?:"")};var fiber by remember(state.nutrition){mutableStateOf(state.nutrition?.targetFiber?.toString()?:"")}
    LazyColumn(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Text("Nutrition tracker",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("Set daily goals");Field("Goal",goal){goal=it};Field("Calories",calories){calories=it};Field("Protein (g)",protein){protein=it};Field("Carbs (g)",carbs){carbs=it};Field("Fat (g)",fat){fat=it};Field("Fiber (g)",fiber){fiber=it};Button({onSave(goal,calories,protein,carbs,fat,fiber)}){Text("Save goals")};HorizontalDivider();Text("Meal nutrition",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)};items(state.mealNutrition.size){i->val n=state.mealNutrition[i];Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text(mealNames[n.mealId]?:"Meal",fontWeight=FontWeight.Bold);Text("${n.calories.toInt()} kcal · P ${n.protein.toInt()}g · C ${n.carbs.toInt()}g · F ${n.fat.toInt()}g · Fiber ${n.fiber.toInt()}g")}}}}
}

@Composable fun SupportScreen(state: CustomerState,onCreate:(String,String,String)->Unit){var subject by remember{mutableStateOf("")};var message by remember{mutableStateOf("")};var category by remember{mutableStateOf("general")};LazyColumn(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Text("Support",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Field("Subject",subject){subject=it};Field("Message",message){message=it};Field("Category",category){category=it};Button({onCreate(subject,message,category)}){Text("Create ticket")};state.message?.let{Text(it)};HorizontalDivider();Text("Your tickets",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)};items(state.tickets.size){i->val t=state.tickets[i];Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text(t.subject,fontWeight=FontWeight.Bold);Text("${t.category} · ${t.status}");Text(t.message)}}}}}

@Composable private fun Field(label:String,value:String,onValue:(String)->Unit){OutlinedTextField(value,onValue,label={Text(label)},modifier=Modifier.fillMaxWidth(),singleLine=true)}
