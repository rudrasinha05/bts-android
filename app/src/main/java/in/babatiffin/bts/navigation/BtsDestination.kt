package com.babatiffin.bts.navigation

sealed class BtsDestination(val route: String) {
    data object Home : BtsDestination("home")
    data object Menu : BtsDestination("menu")
    data object HowItWorks : BtsDestination("how-it-works")
    data object About : BtsDestination("about")
    data object NutritionGuide : BtsDestination("nutrition")
    data object Faq : BtsDestination("faq")
    data object Contact : BtsDestination("contact")
    data object MealDetail : BtsDestination("meal/{mealId}") {
        fun createRoute(mealId: String) = "meal/$mealId"
    }
    data object Dashboard : BtsDestination("dashboard")
    data object BuildMeal : BtsDestination("build-meal")
    data object Subscription : BtsDestination("subscription")
    data object Orders : BtsDestination("orders")
    data object OrderDetail : BtsDestination("orders/{orderId}") {
        fun createRoute(orderId: String) = "orders/$orderId"
    }
    data object Nutrition : BtsDestination("nutrition-tracker")
    data object OrderConfirmation : BtsDestination("order-confirmation/{orderId}") {
        fun createRoute(orderId: String) = "order-confirmation/$orderId"
    }
    data object Profile : BtsDestination("profile")
    data object Support : BtsDestination("support")
    data object Cart : BtsDestination("cart")
    data object Checkout : BtsDestination("checkout")
    data object AddAddress : BtsDestination("address/add")
    data object Notifications : BtsDestination("notifications")
    data object Referrals : BtsDestination("referrals")
    data object Kitchen : BtsDestination("operations/kitchen")
    data object Packing : BtsDestination("operations/packing")
    data object Inventory : BtsDestination("operations/inventory")
    data object Delivery : BtsDestination("operations/delivery")
    data object Auth : BtsDestination("auth")
}
