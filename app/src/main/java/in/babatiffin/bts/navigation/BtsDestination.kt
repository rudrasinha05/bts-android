package com.babatiffin.bts.navigation

sealed class BtsDestination(val route: String) {
    data object Home : BtsDestination("home")
    data object Menu : BtsDestination("menu")
    data object MealDetail : BtsDestination("meal/{mealId}") {
        fun createRoute(mealId: String) = "meal/$mealId"
    }
    data object Plans : BtsDestination("plans")
    data object Dashboard : BtsDestination("dashboard")
    data object BuildMeal : BtsDestination("build-meal")
    data object Subscription : BtsDestination("subscription")
    data object Orders : BtsDestination("orders")
    data object OrderDetail : BtsDestination("orders/{orderId}") {
        fun createRoute(orderId: String) = "orders/$orderId"
    }
    data object Nutrition : BtsDestination("nutrition")
    data object Profile : BtsDestination("profile")
    data object Support : BtsDestination("support")
    data object Cart : BtsDestination("cart")
    data object Checkout : BtsDestination("checkout")
    data object Notifications : BtsDestination("notifications")
    data object Referrals : BtsDestination("referrals")
    data object Kitchen : BtsDestination("operations/kitchen")
    data object Packing : BtsDestination("operations/packing")
    data object Inventory : BtsDestination("operations/inventory")
    data object Delivery : BtsDestination("operations/delivery")
    data object Auth : BtsDestination("auth")
}
