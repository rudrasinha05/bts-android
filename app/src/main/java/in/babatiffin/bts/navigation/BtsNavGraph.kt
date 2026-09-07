package com.babatiffin.bts.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.babatiffin.bts.core.ui.BtsAppShell
import com.babatiffin.bts.feature.common.PlaceholderScreen
import com.babatiffin.bts.feature.home.HomeScreen
import com.babatiffin.bts.feature.menu.MenuScreen

@Composable
fun BtsNavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    fun navigate(route: String) {
        if (route == currentRoute) return
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
        }
    }

    BtsAppShell(
        currentRoute = currentRoute,
        onNavigate = ::navigate,
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BtsDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(BtsDestination.Home.route) {
                HomeScreen(onOpenMenu = { navigate(BtsDestination.Menu.route) })
            }
            composable(BtsDestination.Menu.route) {
                MenuScreen()
            }
            composable(BtsDestination.Plans.route) {
                PlaceholderScreen("Plans", "Subscription plans will be connected to the existing BTS backend in the scheduled milestone.")
            }
            composable(BtsDestination.Dashboard.route) {
                PlaceholderScreen("Dashboard", "Customer dashboard shell is ready for backend-driven widgets.")
            }
            composable(BtsDestination.BuildMeal.route) {
                PlaceholderScreen("Build Meal", "The controlled meal builder will be implemented after menu and add-on data integration.")
            }
            composable(BtsDestination.Subscription.route) {
                PlaceholderScreen("Subscription", "Subscription lifecycle controls will reuse the existing BTS subscriptions data.")
            }
            composable(BtsDestination.Orders.route) {
                PlaceholderScreen("Orders", "Order history and live status timeline will appear here.")
            }
            composable(BtsDestination.Nutrition.route) {
                PlaceholderScreen("Nutrition", "Nutrition tracking will use the existing meal and nutrition records.")
            }
            composable(BtsDestination.Profile.route) {
                PlaceholderScreen("Profile", "Profile, addresses and customer preferences will live here.")
            }
            composable(BtsDestination.Support.route) {
                PlaceholderScreen("Support", "Support tickets and customer help will be available here.")
            }
            composable(BtsDestination.Cart.route) {
                PlaceholderScreen("Cart", "Persistent configured meal lines and editable add-ons will be implemented in the cart milestone.")
            }
        }
    }
}
