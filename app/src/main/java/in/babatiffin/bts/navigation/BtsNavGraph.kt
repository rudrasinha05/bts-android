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
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.babatiffin.bts.core.ui.BtsAppShell
import com.babatiffin.bts.feature.common.PlaceholderScreen
import com.babatiffin.bts.feature.home.HomeScreen
import com.babatiffin.bts.feature.menu.MenuScreen
import com.babatiffin.bts.feature.menu.MealDiscoveryViewModel
import com.babatiffin.bts.feature.menu.MealDetailScreen
import com.babatiffin.bts.data.menu.SupabaseMealRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.babatiffin.bts.data.auth.SupabaseAuthRepository
import com.babatiffin.bts.data.backend.SupabaseProvider
import com.babatiffin.bts.feature.auth.AuthScreen
import com.babatiffin.bts.feature.auth.AuthViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

private val authRequiredRoutes = setOf(
    BtsDestination.Dashboard.route,
    BtsDestination.BuildMeal.route,
    BtsDestination.Subscription.route,
    BtsDestination.Orders.route,
    BtsDestination.Profile.route,
    BtsDestination.Support.route,
)

@Composable
fun BtsNavGraph(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val authViewModel: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(
            repository = SupabaseProvider.client?.let(::SupabaseAuthRepository),
            configured = SupabaseProvider.isConfigured,
        ) as T
    })
    val authState by authViewModel.state.collectAsState()
    val mealViewModel: MealDiscoveryViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MealDiscoveryViewModel(
            SupabaseProvider.client?.let(::SupabaseMealRepository),
        ) as T
    })
    val mealState by mealViewModel.state.collectAsState()
    var pendingRoute by rememberSaveable { mutableStateOf<String?>(null) }

    fun navigate(route: String) {
        if (route in authRequiredRoutes && !authState.authenticated) {
            pendingRoute = route
            if (currentRoute != BtsDestination.Auth.route) navController.navigate(BtsDestination.Auth.route) { launchSingleTop = true }
            return
        }
        if (route == currentRoute) return
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
        }
    }

    LaunchedEffect(authState.authenticated) {
        if (authState.authenticated) pendingRoute?.let { destination ->
            pendingRoute = null
            navigate(destination)
        }
    }

    BtsAppShell(
        currentRoute = currentRoute,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme,
        onNavigate = ::navigate,
        isAuthenticated = authState.authenticated,
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BtsDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(BtsDestination.Home.route) {
                HomeScreen(
                    state = mealState,
                    onOpenMenu = { navigate(BtsDestination.Menu.route) },
                    onOpenMeal = { id -> mealViewModel.selectMeal(id); navController.navigate(BtsDestination.MealDetail.createRoute(id)) },
                    onRetry = mealViewModel::refresh,
                )
            }
            composable(BtsDestination.Menu.route) {
                MenuScreen(
                    state = mealState,
                    onCategory = mealViewModel::selectCategory,
                    onFoodType = mealViewModel::selectFoodType,
                    onRetry = mealViewModel::refresh,
                    onOpenMeal = { id -> mealViewModel.selectMeal(id); navController.navigate(BtsDestination.MealDetail.createRoute(id)) },
                )
            }
            composable(
                route = BtsDestination.MealDetail.route,
                arguments = listOf(navArgument("mealId") { type = NavType.StringType }),
            ) { entry ->
                val mealId = entry.arguments?.getString("mealId")
                LaunchedEffect(mealId) { mealId?.let(mealViewModel::selectMeal) }
                MealDetailScreen(state = mealState, onChangeAddOn = mealViewModel::changeAddOn, onRetry = mealViewModel::refresh)
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
                AuthScreen(state = authState, viewModel = authViewModel)
            }
            composable(BtsDestination.Support.route) {
                PlaceholderScreen("Support", "Support tickets and customer help will be available here.")
            }
            composable(BtsDestination.Cart.route) {
                PlaceholderScreen("Cart", "Persistent configured meal lines and editable add-ons will be implemented in the cart milestone.")
            }
            composable(BtsDestination.Auth.route) {
                AuthScreen(state = authState, viewModel = authViewModel)
            }
        }
    }
}
