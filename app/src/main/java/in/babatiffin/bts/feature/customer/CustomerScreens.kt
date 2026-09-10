package com.babatiffin.bts.feature.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable fun ProfileScreen(state: CustomerState, onSave: (String, String, String) -> Unit, onAddAddress: (String,String,String,String,String,String,String,Boolean)->Unit, onDefault:(com.babatiffin.bts.data.customer.Address)->Unit, onDelete:(com.babatiffin.bts.data.customer.Address)->Unit, onSignOut:()->Unit) {
    var name by remember(state.profile) { mutableStateOf(state.profile?.fullName ?: "") }; var phone by remember(state.profile) { mutableStateOf(state.profile?.phone ?: "") }; var dob by remember(state.profile) { mutableStateOf(state.profile?.dateOfBirth ?: "") }
    var label by remember { mutableStateOf("Home") }; var line1 by remember { mutableStateOf("") }; var line2 by remember { mutableStateOf("") }; var landmark by remember { mutableStateOf("") }; var city by remember { mutableStateOf("Greater Noida") }; var region by remember { mutableStateOf("Uttar Pradesh") }; var pincode by remember { mutableStateOf("") }
    LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("Profile", style=MaterialTheme.typography.headlineMedium, fontWeight=FontWeight.Bold); state.message?.let { Text(it) }; Field("Full name",name){name=it}; Field("Phone",phone){phone=it}; Field("Date of birth (YYYY-MM-DD)",dob){dob=it}; Button({onSave(name,phone,dob)}, enabled=!state.loading){Text("Save profile")}; HorizontalDivider(); Text("Saved addresses",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold) }
        items(state.addresses.size) { i -> val a=state.addresses[i]; Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text("${a.label}${if(a.isDefault) " · Default" else ""}",fontWeight=FontWeight.Bold);Text("${a.line1}, ${a.city} - ${a.pincode}");Row{TextButton({onDefault(a)},enabled=!a.isDefault){Text("Make default")};TextButton({onDelete(a)}){Text("Remove")}}}} }
        item { Text("Add address",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Field("Label",label){label=it};Field("Address line 1",line1){line1=it};Field("Address line 2",line2){line2=it};Field("Landmark",landmark){landmark=it};Field("City",city){city=it};Field("State",region){region=it};Field("Pincode",pincode){pincode=it};Button({onAddAddress(label,line1,line2,landmark,city,region,pincode,state.addresses.isEmpty())}){Text("Add address")};OutlinedButton(onSignOut){Text("Sign out")}}
    }
}

@Composable fun NutritionScreen(state: CustomerState, mealNames: Map<String,String>, onSave:(String,String,String,String,String,String)->Unit){
    var goal by remember(state.nutrition){mutableStateOf(state.nutrition?.goal?:"")};var calories by remember(state.nutrition){mutableStateOf(state.nutrition?.targetCalories?.toString()?:"")};var protein by remember(state.nutrition){mutableStateOf(state.nutrition?.targetProtein?.toString()?:"")};var carbs by remember(state.nutrition){mutableStateOf(state.nutrition?.targetCarbs?.toString()?:"")};var fat by remember(state.nutrition){mutableStateOf(state.nutrition?.targetFat?.toString()?:"")};var fiber by remember(state.nutrition){mutableStateOf(state.nutrition?.targetFiber?.toString()?:"")}
    LazyColumn(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Text("Nutrition tracker",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("Set daily goals");Field("Goal",goal){goal=it};Field("Calories",calories){calories=it};Field("Protein (g)",protein){protein=it};Field("Carbs (g)",carbs){carbs=it};Field("Fat (g)",fat){fat=it};Field("Fiber (g)",fiber){fiber=it};Button({onSave(goal,calories,protein,carbs,fat,fiber)}){Text("Save goals")};HorizontalDivider();Text("Meal nutrition",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)};items(state.mealNutrition.size){i->val n=state.mealNutrition[i];Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text(mealNames[n.mealId]?:"Meal",fontWeight=FontWeight.Bold);Text("${n.calories.toInt()} kcal · P ${n.protein.toInt()}g · C ${n.carbs.toInt()}g · F ${n.fat.toInt()}g · Fiber ${n.fiber.toInt()}g")}}}}
}

@Composable fun SupportScreen(state: CustomerState,onCreate:(String,String,String)->Unit){var subject by remember{mutableStateOf("")};var message by remember{mutableStateOf("")};var category by remember{mutableStateOf("general")};LazyColumn(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Text("Support",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Field("Subject",subject){subject=it};Field("Message",message){message=it};Field("Category",category){category=it};Button({onCreate(subject,message,category)}){Text("Create ticket")};state.message?.let{Text(it)};HorizontalDivider();Text("Your tickets",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)};items(state.tickets.size){i->val t=state.tickets[i];Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text(t.subject,fontWeight=FontWeight.Bold);Text("${t.category} · ${t.status}");Text(t.message)}}}}}

@Composable private fun Field(label:String,value:String,onValue:(String)->Unit){OutlinedTextField(value,onValue,label={Text(label)},modifier=Modifier.fillMaxWidth(),singleLine=true)}
