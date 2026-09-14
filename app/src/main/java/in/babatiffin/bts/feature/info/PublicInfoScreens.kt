package com.babatiffin.bts.feature.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class InfoBlock(val title: String, val body: String)

@Composable
private fun InfoPage(title: String, subtitle: String, blocks: List<InfoBlock>, modifier: Modifier = Modifier) {
    LazyColumn(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(blocks.size) { index ->
            val block = blocks[index]
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(block.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(block.body)
                }
            }
        }
    }
}

@Composable
fun HowItWorksScreen(modifier: Modifier = Modifier) = InfoPage(
    "How it works",
    "Fresh, customisable tiffins in four simple steps.",
    listOf(
        InfoBlock("1. Choose", "Browse today's menu or choose a subscription plan that fits your routine."),
        InfoBlock("2. Customise", "Select a meal, adjust quantity and add extras before reviewing your cart."),
        InfoBlock("3. Confirm", "Sign in, select a saved GPS delivery address and confirm the meal slot."),
        InfoBlock("4. Delivered", "Pay securely and follow the order status from kitchen preparation to delivery."),
    ),
    modifier,
)

@Composable
fun AboutScreen(modifier: Modifier = Modifier) = InfoPage(
    "About Baba Tiffin Services",
    "Ghar jaisa khana, prepared with care in Greater Noida.",
    listOf(
        InfoBlock("Fresh every day", "Meals are prepared for scheduled service instead of long shelf storage."),
        InfoBlock("Built around your routine", "Order individual meals, build your own combination or choose a recurring plan."),
        InfoBlock("Clear and dependable", "Transparent prices, nutrition estimates and live order progress keep every step understandable."),
    ),
    modifier,
)

@Composable
fun NutritionGuideScreen(modifier: Modifier = Modifier) = InfoPage(
    "Nutrition",
    "Practical nutrition guidance for everyday meals.",
    listOf(
        InfoBlock("Balanced plates", "Prefer a balanced combination of protein, carbohydrates, vegetables and healthy fats."),
        InfoBlock("Know your portions", "Calories and macros in BTS are estimates calculated from ingredients and portion sizes."),
        InfoBlock("Set personal targets", "Signed-in customers can use Nutrition Tracker to set daily targets and review recent intake."),
        InfoBlock("Important", "Nutrition information is guidance, not medical advice. Consult a qualified professional for clinical needs."),
    ),
    modifier,
)

@Composable
fun FaqScreen(modifier: Modifier = Modifier) = InfoPage(
    "Frequently asked questions",
    "Quick answers about meals, subscriptions and delivery.",
    listOf(
        InfoBlock("Can I customise a meal?", "Yes. Open a meal or Build Meal to select available add-ons and quantities."),
        InfoBlock("Where do you deliver?", "Serviceability is checked using your saved address and pincode in the Greater Noida service area."),
        InfoBlock("Can I pause a subscription?", "Eligible active subscriptions can be paused, resumed or cancelled from Subscription."),
        InfoBlock("How do payments work?", "Online and wallet payments are priced and verified by the BTS server before an order is confirmed."),
        InfoBlock("Where can I get help?", "Sign in and open Support to create and track a support ticket."),
    ),
    modifier,
)

@Composable
fun ContactScreen(modifier: Modifier = Modifier) = InfoPage(
    "Contact",
    "We are here to help with meals, subscriptions and delivery.",
    listOf(
        InfoBlock("Customer support", "Sign in and open Support to create a ticket linked to your account and orders."),
        InfoBlock("Kitchen and delivery area", "Baba Tiffin Services, Greater Noida, Uttar Pradesh."),
        InfoBlock("Before contacting us", "Keep your order number ready. Never share an OTP, password or payment PIN with anyone."),
    ),
    modifier,
)
