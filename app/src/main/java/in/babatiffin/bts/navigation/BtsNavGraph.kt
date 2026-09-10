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
import androidx.compose.ui.platform.LocalContext
import com.babatiffin.bts.data.cart.DataStoreCartRepository
import com.babatiffin.bts.feature.cart.CartScreen
import com.babatiffin.bts.feature.cart.CartViewModel
import com.babatiffin.bts.feature.buildmeal.BuildMealScreen
import com.babatiffin.bts.data.subscription.SupabaseSubscriptionRepository
import com.babatiffin.bts.feature.subscription.ManageSubscriptionScreen
import com.babatiffin.bts.feature.subscription.PlansScreen
import com.babatiffin.bts.feature.subscription.SubscriptionViewModel
import com.babatiffin.bts.data.order.SupabaseOrderRepository
import com.babatiffin.bts.feature.orders.OrderDetailScreen
import com.babatiffin.bts.feature.orders.OrdersScreen
import com.babatiffin.bts.feature.orders.OrdersViewModel
import com.babatiffin.bts.data.checkout.SupabaseCheckoutRepository
import com.babatiffin.bts.feature.checkout.CheckoutScreen
import com.babatiffin.bts.feature.checkout.CheckoutViewModel
import com.babatiffin.bts.data.customer.SupabaseCustomerRepository
import com.babatiffin.bts.feature.customer.CustomerViewModel
import com.babatiffin.bts.feature.customer.NutritionScreen
import com.babatiffin.bts.feature.customer.ProfileScreen
import com.babatiffin.bts.feature.customer.SupportScreen
import com.babatiffin.bts.data.engagement.SupabaseEngagementRepository
import com.babatiffin.bts.feature.engagement.EngagementViewModel
import com.babatiffin.bts.core.notification.PushRegistration
import com.babatiffin.bts.feature.engagement.NotificationsScreen
import com.babatiffin.bts.feature.engagement.ReferralsScreen
import com.babatiffin.bts.data.operations.SupabaseOperationsRepository
import com.babatiffin.bts.feature.operations.DeliveryScreen
import com.babatiffin.bts.feature.operations.InventoryScreen
import com.babatiffin.bts.feature.operations.KitchenScreen
import com.babatiffin.bts.feature.operations.OperationsViewModel
import com.babatiffin.bts.feature.operations.PackingScreen

private val authRequiredRoutes = setOf(
    BtsDestination.Dashboard.route,
    BtsDestination.BuildMeal.route,
    BtsDestination.Subscription.route,
    BtsDestination.Orders.route,
    BtsDestination.Profile.route,
    BtsDestination.Support.route,
    BtsDestination.Checkout.route,
    BtsDestination.Notifications.route,
    BtsDestination.Referrals.route,
    BtsDestination.Kitchen.route,
    BtsDestination.Packing.route,
    BtsDestination.Inventory.route,
    BtsDestination.Delivery.route,
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
    val context = LocalContext.current
    val cartViewModel: CartViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CartViewModel(DataStoreCartRepository(context)) as T
    })
    val cartLines by cartViewModel.lines.collectAsState()
    val subscriptionViewModel: SubscriptionViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SubscriptionViewModel(
            SupabaseProvider.client?.let(::SupabaseSubscriptionRepository),
        ) as T
    })
    val subscriptionState by subscriptionViewModel.state.collectAsState()
    LaunchedEffect(authState.userId) { subscriptionViewModel.loadForUser(authState.userId) }
    val ordersViewModel: OrdersViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = OrdersViewModel(
            SupabaseProvider.client?.let(::SupabaseOrderRepository),
        ) as T
    })
    val ordersState by ordersViewModel.state.collectAsState()
    LaunchedEffect(authState.userId) { ordersViewModel.load(authState.userId) }
    val checkoutViewModel: CheckoutViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CheckoutViewModel(
            SupabaseProvider.client?.let(::SupabaseCheckoutRepository),
        ) as T
    })
    val checkoutState by checkoutViewModel.state.collectAsState()
    LaunchedEffect(authState.userId) { checkoutViewModel.load(authState.userId) }
    val customerViewModel: CustomerViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CustomerViewModel(
            SupabaseProvider.client?.let(::SupabaseCustomerRepository),
        ) as T
    })
    val customerState by customerViewModel.state.collectAsState()
    LaunchedEffect(authState.userId) { customerViewModel.load(authState.userId) }
    val engagementViewModel: EngagementViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = EngagementViewModel(SupabaseProvider.client?.let(::SupabaseEngagementRepository)) as T
    })
    val engagementState by engagementViewModel.state.collectAsState()
    LaunchedEffect(authState.userId) { engagementViewModel.load(authState.userId) }
    LaunchedEffect(authState.userId) {
        authState.userId?.let { PushRegistration.registerSignedInUser(context, it) }
    }
    val operationsViewModel:OperationsViewModel=viewModel(factory=object:ViewModelProvider.Factory{@Suppress("UNCHECKED_CAST") override fun<T:ViewModel>create(modelClass:Class<T>):T=OperationsViewModel(SupabaseProvider.client?.let(::SupabaseOperationsRepository)) as T})
    val operationsState by operationsViewModel.state.collectAsState()
    LaunchedEffect(authState.roles){operationsViewModel.load(authState.roles)}
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

    LaunchedEffect(checkoutState.paymentComplete) {
        if (checkoutState.paymentComplete) {
            cartViewModel.clear()
            ordersViewModel.load(authState.userId)
            checkoutViewModel.consumeCompletion()
            navigate(BtsDestination.Orders.route)
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
        roles = authState.roles,
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
                MealDetailScreen(
                    state = mealState,
                    onChangeAddOn = mealViewModel::changeAddOn,
                    onRetry = mealViewModel::refresh,
                    onAddToCart = {
                        cartViewModel.addConfiguredMeal(mealState)
                        navigate(BtsDestination.Cart.route)
                    },
                )
            }
            composable(BtsDestination.Plans.route) {
                PlansScreen(state = subscriptionState, onManage = { navigate(BtsDestination.Subscription.route) })
            }
            composable(BtsDestination.Dashboard.route) {
                PlaceholderScreen("Dashboard", "Customer dashboard shell is ready for backend-driven widgets.")
            }
            composable(BtsDestination.BuildMeal.route) {
                LaunchedEffect(Unit) { mealViewModel.startBuilder() }
                BuildMealScreen(
                    state = mealState,
                    onCategory = mealViewModel::selectBuilderCategory,
                    onFoodType = mealViewModel::selectBuilderFoodType,
                    onMeal = mealViewModel::selectMeal,
                    onAddOn = mealViewModel::changeAddOn,
                    onRetry = mealViewModel::refresh,
                    onAddToCart = {
                        cartViewModel.addConfiguredMeal(mealState)
                        navigate(BtsDestination.Cart.route)
                    },
                )
            }
            composable(BtsDestination.Subscription.route) {
                ManageSubscriptionScreen(state = subscriptionState, onStatus = subscriptionViewModel::setStatus)
            }
            composable(BtsDestination.Orders.route) {
                OrdersScreen(state = ordersState, onOpen = { id ->
                    ordersViewModel.select(id)
                    navController.navigate(BtsDestination.OrderDetail.createRoute(id))
                })
            }
            composable(
                route = BtsDestination.OrderDetail.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
            ) { entry ->
                val orderId = entry.arguments?.getString("orderId")
                LaunchedEffect(orderId) { orderId?.let(ordersViewModel::select) }
                OrderDetailScreen(state = ordersState, mealNames = mealState.meals.associate { it.id to it.name })
            }
            composable(BtsDestination.Nutrition.route) {
                NutritionScreen(customerState, mealState.meals.associate { it.id to it.name }, customerViewModel::saveNutrition)
            }
            composable(BtsDestination.Profile.route) {
                ProfileScreen(customerState, customerViewModel::saveProfile, customerViewModel::addAddress, customerViewModel::setDefault, customerViewModel::deleteAddress, customerViewModel::updateLocation, authViewModel::signOut)
            }
            composable(BtsDestination.Support.route) {
                SupportScreen(customerState, customerViewModel::createTicket)
            }
            composable(BtsDestination.Cart.route) {
                CartScreen(
                    lines = cartLines,
                    onChangeQuantity = cartViewModel::changeQuantity,
                    onRemove = cartViewModel::remove,
                    onClear = cartViewModel::clear,
                    onCheckout = { navigate(BtsDestination.Checkout.route) },
                )
            }
            composable(BtsDestination.Checkout.route) {
                CheckoutScreen(
                    state = checkoutState,
                    lines = cartLines,
                    onCouponCode = checkoutViewModel::setCouponCode,
                    onApplyCoupon = checkoutViewModel::applyCoupon,
                    onPaymentChoice = checkoutViewModel::choosePayment,
                    onMealType = checkoutViewModel::chooseMealType,
                    onPay = { activity -> checkoutViewModel.pay(activity, cartLines) },
                )
            }
            composable(BtsDestination.Auth.route) {
                AuthScreen(state = authState, viewModel = authViewModel)
            }
            composable(BtsDestination.Notifications.route) { NotificationsScreen(engagementState, engagementViewModel::read) }
            composable(BtsDestination.Referrals.route) { ReferralsScreen(engagementState) }
            composable(BtsDestination.Kitchen.route) { if(authState.roles.any{it in setOf("admin","kitchen_manager","kitchen_staff")})KitchenScreen(operationsState,operationsViewModel::kitchen)else PlaceholderScreen("Restricted","Kitchen access requires an operations role.") }
            composable(BtsDestination.Packing.route) { if(authState.roles.any{it in setOf("admin","kitchen_manager","kitchen_staff")})PackingScreen(operationsState,operationsViewModel::packing)else PlaceholderScreen("Restricted","Packing access requires an operations role.") }
            composable(BtsDestination.Inventory.route) { if(authState.roles.any{it in setOf("admin","kitchen_manager","kitchen_staff")})InventoryScreen(operationsState)else PlaceholderScreen("Restricted","Inventory access requires an operations role.") }
            composable(BtsDestination.Delivery.route) { if(authState.roles.any{it in setOf("admin","delivery_manager","delivery_agent")})DeliveryScreen(operationsState,operationsViewModel::delivery)else PlaceholderScreen("Restricted","Delivery access requires an operations role.") }
        }
    }
}
