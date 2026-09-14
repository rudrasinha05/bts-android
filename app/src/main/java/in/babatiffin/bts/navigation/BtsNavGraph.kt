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
import com.babatiffin.bts.feature.dashboard.DashboardScreen
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
import com.babatiffin.bts.feature.cart.AddOnDialog
import com.babatiffin.bts.feature.buildmeal.BuildMealScreen
import com.babatiffin.bts.data.subscription.SupabaseSubscriptionRepository
import com.babatiffin.bts.feature.subscription.ManageSubscriptionScreen
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
import com.babatiffin.bts.feature.customer.AddAddressScreen
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
import com.babatiffin.bts.core.security.canAccessDeliveryOperations
import com.babatiffin.bts.core.security.canAccessKitchenOperations
import com.babatiffin.bts.core.location.LocationAccessEffect
import com.babatiffin.bts.feature.info.AboutScreen
import com.babatiffin.bts.feature.info.ContactScreen
import com.babatiffin.bts.feature.info.FaqScreen
import com.babatiffin.bts.feature.info.HowItWorksScreen
import com.babatiffin.bts.feature.info.NutritionGuideScreen
import com.babatiffin.bts.feature.orders.OrderConfirmationScreen

private val authRequiredRoutes = setOf(
    BtsDestination.Dashboard.route,
    BtsDestination.BuildMeal.route,
    BtsDestination.Subscription.route,
    BtsDestination.Orders.route,
    BtsDestination.Profile.route,
    BtsDestination.Support.route,
    BtsDestination.Checkout.route,
    BtsDestination.AddAddress.route,
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
    var showMenuCategories by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val cartViewModel: CartViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CartViewModel(DataStoreCartRepository(context)) as T
    })
    val cartLines by cartViewModel.lines.collectAsState()
    var quickAddLineId by rememberSaveable { mutableStateOf<String?>(null) }
    var quickAddMealId by rememberSaveable { mutableStateOf<String?>(null) }
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
    val deliveryAddress = customerState.addresses.firstOrNull { it.isDefault } ?: customerState.addresses.firstOrNull()
    LocationAccessEffect(
        authenticated = authState.authenticated,
        address = deliveryAddress,
        onLocation = customerViewModel::updateLocation,
    )
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
    var hadAuthenticatedSession by rememberSaveable { mutableStateOf(false) }

    fun navigate(route: String) {
        val requiresAuth = route in authRequiredRoutes ||
            route.startsWith("orders/") || route.startsWith("order-confirmation/")
        if (requiresAuth && !authState.authenticated) {
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
            val completedOrderId = checkoutState.completedOrderId
            cartViewModel.clear()
            ordersViewModel.load(authState.userId)
            checkoutViewModel.consumeCompletion()
            navigate(completedOrderId?.let(BtsDestination.OrderConfirmation::createRoute) ?: BtsDestination.Orders.route)
        }
    }

    LaunchedEffect(authState.authenticated, authState.loading) {
        when {
            authState.authenticated -> {
                hadAuthenticatedSession = true
                pendingRoute?.let { destination ->
                    pendingRoute = null
                    navigate(destination)
                }
            }
            !authState.loading && hadAuthenticatedSession -> {
                hadAuthenticatedSession = false
                pendingRoute = null
                navController.navigate(BtsDestination.Home.route) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    BtsAppShell(
        currentRoute = currentRoute,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme,
        onNavigate = ::navigate,
        isAuthenticated = authState.authenticated,
        roles = authState.roles,
        onBack = { navController.popBackStack() },
        onBrowseCategories = { showMenuCategories = true; navigate(BtsDestination.Menu.route) },
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
                    onAddMeal = { meal ->
                        quickAddMealId = meal.id
                        quickAddLineId = cartViewModel.addBaseMeal(meal)
                    },
                    onRetry = mealViewModel::refresh,
                )
            }
            composable(BtsDestination.Menu.route) {
                MenuScreen(
                    state = mealState,
                    showCategories = showMenuCategories,
                    onShowCategories = { showMenuCategories = it },
                    onCategory = mealViewModel::selectCategory,
                    onFoodType = mealViewModel::selectFoodType,
                    onRetry = mealViewModel::refresh,
                    onOpenMeal = { id -> mealViewModel.selectMeal(id); navController.navigate(BtsDestination.MealDetail.createRoute(id)) },
                    onAddMeal = { meal ->
                        quickAddMealId = meal.id
                        quickAddLineId = cartViewModel.addBaseMeal(meal)
                    },
                )
            }
            composable(BtsDestination.HowItWorks.route) { HowItWorksScreen() }
            composable(BtsDestination.About.route) { AboutScreen() }
            composable(BtsDestination.NutritionGuide.route) { NutritionGuideScreen() }
            composable(BtsDestination.Faq.route) { FaqScreen() }
            composable(BtsDestination.Contact.route) { ContactScreen() }
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
            composable(BtsDestination.Dashboard.route) {
                DashboardScreen(
                    orders = ordersState,
                    subscriptions = subscriptionState,
                    customer = customerState,
                    meals = mealState.meals,
                    onBuildMeal = { navigate(BtsDestination.BuildMeal.route) },
                    onOrders = { navigate(BtsDestination.Orders.route) },
                    onPlans = { navigate(BtsDestination.Subscription.route) },
                    onNutrition = { navigate(BtsDestination.Nutrition.route) },
                    onMeal = { id ->
                        mealViewModel.selectMeal(id)
                        navController.navigate(BtsDestination.MealDetail.createRoute(id))
                    },
                )
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
                ManageSubscriptionScreen(
                    state = subscriptionState,
                    mealById = mealState.meals.associateBy { it.id },
                    orderHistory = ordersState.orders,
                    onStatus = subscriptionViewModel::setStatus,
                )
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
                NutritionScreen(customerState, ordersState.orders, customerViewModel::saveNutrition)
            }
            composable(BtsDestination.Profile.route) {
                ProfileScreen(
                    state = customerState,
                    accountEmail = authState.email,
                    accountPhone = authState.phone,
                    onSave = customerViewModel::saveProfile,
                    onAddNewAddress = { navigate(BtsDestination.AddAddress.route) },
                    onDefault = customerViewModel::setDefault,
                    onDelete = customerViewModel::deleteAddress,
                    onOrders = { navigate(BtsDestination.Orders.route) },
                    onSubscription = { navigate(BtsDestination.Subscription.route) },
                    onNutrition = { navigate(BtsDestination.Nutrition.route) },
                    onReferrals = { navigate(BtsDestination.Referrals.route) },
                    onSupport = { navigate(BtsDestination.Support.route) },
                    onSignOut = {
                        PushRegistration.unregisterSignedInUser(context, authState.userId, authViewModel::signOut)
                    },
                )
            }
            composable(BtsDestination.Support.route) {
                SupportScreen(customerState, customerViewModel::createTicket)
            }
            composable(BtsDestination.Cart.route) {
                CartScreen(
                    lines = cartLines,
                    onChangeQuantity = cartViewModel::changeQuantity,
                    onRemove = cartViewModel::remove,
                    onEditAddOns = { line ->
                        quickAddLineId = line.id
                        quickAddMealId = line.mealId
                    },
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
                    address = deliveryAddress,
                    addressesLoading = customerState.loading,
                    onManageAddress = { navigate(BtsDestination.AddAddress.route) },
                    onPay = { activity -> checkoutViewModel.pay(activity, cartLines, deliveryAddress?.id) },
                )
            }
            composable(
                route = BtsDestination.OrderConfirmation.route,
                arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
            ) { entry ->
                OrderConfirmationScreen(
                    orderId = entry.arguments?.getString("orderId").orEmpty(),
                    onViewOrders = { navigate(BtsDestination.Orders.route) },
                    onHome = { navigate(BtsDestination.Home.route) },
                )
            }
            composable(BtsDestination.AddAddress.route) {
                AddAddressScreen(
                    state = customerState,
                    onSave = customerViewModel::addDeliveryAddress,
                    onSaved = { navController.popBackStack() },
                )
            }
            composable(BtsDestination.Auth.route) {
                AuthScreen(state = authState, viewModel = authViewModel)
            }
            composable(BtsDestination.Notifications.route) { NotificationsScreen(engagementState, engagementViewModel::read) }
            composable(BtsDestination.Referrals.route) { ReferralsScreen(engagementState) }
            composable(BtsDestination.Kitchen.route) { if(authState.roles.canAccessKitchenOperations())KitchenScreen(operationsState,{operationsViewModel.load(authState.roles)},operationsViewModel::kitchen)else PlaceholderScreen("Restricted","Kitchen access requires an operations role.") }
            composable(BtsDestination.Packing.route) { if(authState.roles.canAccessKitchenOperations())PackingScreen(operationsState,{operationsViewModel.load(authState.roles)},operationsViewModel::packing)else PlaceholderScreen("Restricted","Packing access requires an operations role.") }
            composable(BtsDestination.Inventory.route) { if(authState.roles.canAccessKitchenOperations())InventoryScreen(operationsState){operationsViewModel.load(authState.roles)}else PlaceholderScreen("Restricted","Inventory access requires an operations role.") }
            composable(BtsDestination.Delivery.route) { if(authState.roles.canAccessDeliveryOperations())DeliveryScreen(operationsState,{operationsViewModel.load(authState.roles)},operationsViewModel::delivery)else PlaceholderScreen("Restricted","Delivery access requires an operations role.") }
        }
        val quickAddMeal = mealState.meals.firstOrNull { it.id == quickAddMealId }
        val quickAddLine = cartLines.firstOrNull { it.id == quickAddLineId }
        if (quickAddMeal != null && quickAddLineId != null) {
            AddOnDialog(
                meal = quickAddMeal,
                line = quickAddLine,
                addOns = mealState.addOns,
                onMealQuantity = { current, delta -> quickAddLineId?.let { cartViewModel.changeQuantity(it, current, delta) } },
                onAddOnQuantity = { addOn, current, delta -> quickAddLineId?.let { cartViewModel.changeAddOn(it, addOn, current, delta) } },
                onDismiss = { quickAddLineId = null; quickAddMealId = null },
            )
        }
    }
}
