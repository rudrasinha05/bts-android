package com.babatiffin.bts.navigation

sealed class BtsDestination(val route: String) {
    data object Home : BtsDestination("home")
    data object Menu : BtsDestination("menu")
    data object Plans : BtsDestination("plans")
    data object Dashboard : BtsDestination("dashboard")
    data object BuildMeal : BtsDestination("build-meal")
    data object Subscription : BtsDestination("subscription")
    data object Orders : BtsDestination("orders")
    data object Nutrition : BtsDestination("nutrition")
    data object Profile : BtsDestination("profile")
    data object Support : BtsDestination("support")
}
