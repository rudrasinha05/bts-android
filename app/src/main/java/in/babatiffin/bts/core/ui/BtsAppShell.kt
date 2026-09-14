package com.babatiffin.bts.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.babatiffin.bts.core.design.BtsSize
import com.babatiffin.bts.core.design.BtsSpacing
import com.babatiffin.bts.navigation.BtsDestination
import kotlinx.coroutines.launch
import com.babatiffin.bts.core.security.canAccessDeliveryOperations
import com.babatiffin.bts.core.security.canAccessKitchenOperations

private data class ShellItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val primaryItems = listOf(
    ShellItem(BtsDestination.Home.route, "Home", Icons.Default.Home),
    ShellItem(BtsDestination.Menu.route, "Menu", Icons.Default.RestaurantMenu),
    ShellItem(BtsDestination.Dashboard.route, "Dashboard", Icons.Default.Dashboard),
    ShellItem(BtsDestination.Orders.route, "Orders", Icons.Default.ReceiptLong),
    ShellItem(BtsDestination.Profile.route, "You", Icons.Default.AccountCircle),
)

private val publicDrawerItems = listOf(
    ShellItem(BtsDestination.HowItWorks.route, "How it works", Icons.Default.Info),
    ShellItem(BtsDestination.About.route, "About", Icons.Default.Info),
    ShellItem(BtsDestination.NutritionGuide.route, "Nutrition guide", Icons.Default.RestaurantMenu),
    ShellItem(BtsDestination.Faq.route, "FAQ", Icons.Default.Help),
    ShellItem(BtsDestination.Contact.route, "Contact", Icons.Default.ContactSupport),
)

private val accountDrawerItems = listOf(
    ShellItem(BtsDestination.BuildMeal.route, "Build Meal", Icons.Default.Build),
    ShellItem(BtsDestination.Subscription.route, "Subscription", Icons.Default.ReceiptLong),
    ShellItem(BtsDestination.Nutrition.route, "Nutrition", Icons.Default.RestaurantMenu),
    ShellItem(BtsDestination.Support.route, "Support", Icons.Default.SupportAgent),
    ShellItem(BtsDestination.Referrals.route, "Refer & earn", Icons.Default.AccountCircle),
)

private val kitchenItems = listOf(ShellItem(BtsDestination.Kitchen.route,"Kitchen",Icons.Default.Dashboard),ShellItem(BtsDestination.Packing.route,"Packing",Icons.Default.Dashboard),ShellItem(BtsDestination.Inventory.route,"Inventory",Icons.Default.Dashboard))
private val deliveryItems = listOf(ShellItem(BtsDestination.Delivery.route,"Delivery",Icons.Default.Dashboard))

@Composable
fun BtsAppShell(
    currentRoute: String?,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onNavigate: (String) -> Unit,
    isAuthenticated: Boolean,
    roles: Set<String>,
    onBack: () -> Unit,
    onBrowseCategories: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val useNavigationRail = LocalConfiguration.current.screenWidthDp >= 600

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.padding(vertical = BtsSpacing.Lg)) {
                    Text(
                        text = "BTS",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = BtsSpacing.Xl),
                    )
                    Text(
                        text = "Baba Tiffin Services",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = BtsSpacing.Xl),
                    )
                    Spacer(Modifier.height(BtsSpacing.Lg))
                    Divider()
                    Spacer(Modifier.height(BtsSpacing.Sm))
                    val operations=buildList{if(roles.canAccessKitchenOperations())addAll(kitchenItems);if(roles.canAccessDeliveryOperations())addAll(deliveryItems)}
                    val coreItems = buildList {
                        add(primaryItems[0])
                        add(primaryItems[1])
                        if (isAuthenticated) add(primaryItems[2])
                        addAll(accountDrawerItems)
                        if (isAuthenticated) {
                            add(primaryItems[3])
                            add(primaryItems[4])
                        }
                    }.distinctBy(ShellItem::route)
                    (coreItems + publicDrawerItems + operations).forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            icon = { Icon(item.icon, contentDescription = null) },
                            onClick = {
                                scope.launch { drawerState.close() }
                                onNavigate(item.route)
                            },
                            modifier = Modifier.padding(horizontal = BtsSpacing.Md),
                        )
                    }
                }
            }
        },
    ) {
        if (useNavigationRail) {
            Row {
                BtsNavigationRail(
                    currentRoute = currentRoute,
                    onNavigate = onNavigate,
                )
                Scaffold(
                    modifier = Modifier.weight(1f),
                    topBar = {
                        BtsTopBar(
                            currentRoute = currentRoute,
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = onToggleTheme,
                            onOpenDrawer = { scope.launch { drawerState.open() } },
                            onOpenCart = { onNavigate(BtsDestination.Cart.route) },
                            onOpenNotifications = { onNavigate(BtsDestination.Notifications.route) },
                            onOpenAccount = { onNavigate(if (isAuthenticated) BtsDestination.Profile.route else BtsDestination.Auth.route) },
                            onSearch = { onNavigate(BtsDestination.Menu.route) },
                            onBack = onBack,
                        )
                    },
                    floatingActionButton = {
                        if (currentRoute != BtsDestination.Menu.route) {
                            FloatingActionButton(onClick = onBrowseCategories) { Text("Menu") }
                        }
                    },
                    content = content,
                )
            }
        } else {
            Scaffold(
                topBar = {
                    BtsTopBar(
                        currentRoute = currentRoute,
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = onToggleTheme,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onOpenCart = { onNavigate(BtsDestination.Cart.route) },
                        onOpenNotifications = { onNavigate(BtsDestination.Notifications.route) },
                        onOpenAccount = { onNavigate(if (isAuthenticated) BtsDestination.Profile.route else BtsDestination.Auth.route) },
                        onSearch = { onNavigate(BtsDestination.Menu.route) },
                        onBack = onBack,
                    )
                },
                floatingActionButton = {
                    if (currentRoute != BtsDestination.Menu.route) {
                        FloatingActionButton(onClick = onBrowseCategories) { Text("Menu") }
                    }
                },
                bottomBar = {
                    NavigationBar(modifier = Modifier.height(BtsSize.BottomBarHeight)) {
                        primaryItems.forEach { item ->
                            NavigationBarItem(
                                selected = currentRoute == item.route,
                                onClick = { onNavigate(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(24.dp),
                                    )
                                },
                                label = { Text(item.label) },
                            )
                        }
                    }
                },
                content = content,
            )
        }
    }
}

@Composable
private fun BtsNavigationRail(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    NavigationRail(modifier = Modifier.width(BtsSize.RailWidth)) {
        Spacer(Modifier.height(BtsSpacing.Sm))
        primaryItems.forEach { item ->
            NavigationRailItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}

@Composable
private fun BtsTopBar(
    currentRoute: String?,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenCart: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAccount: () -> Unit,
    onSearch: () -> Unit,
    onBack: () -> Unit,
) {
    Surface(shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(BtsSize.TopBarHeight)
                .padding(horizontal = BtsSpacing.Sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val isHome = currentRoute == BtsDestination.Home.route
            IconButton(onClick = if (isHome) onOpenDrawer else onBack) {
                Icon(
                    if (isHome) Icons.Default.Menu else Icons.Default.ArrowBack,
                    contentDescription = if (isHome) "Open navigation" else "Back",
                )
            }
            Spacer(Modifier.width(BtsSpacing.Sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routeTitle(currentRoute),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "Baba Tiffin Services",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, contentDescription = "Search menu")
            }
            IconButton(onClick = onToggleTheme) {
                Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = if (isDarkTheme) "Use light mode" else "Use dark mode",
                )
            }
            IconButton(onClick = onOpenNotifications) {
                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
            }
            IconButton(onClick = onOpenCart) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
            }
            IconButton(onClick = onOpenAccount) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Account")
            }
        }
    }
}

private fun routeTitle(route: String?): String = when (route) {
    BtsDestination.Home.route -> "Home"
    BtsDestination.Menu.route -> "Menu"
    BtsDestination.HowItWorks.route -> "How it works"
    BtsDestination.About.route -> "About"
    BtsDestination.NutritionGuide.route -> "Nutrition"
    BtsDestination.Faq.route -> "FAQ"
    BtsDestination.Contact.route -> "Contact"
    BtsDestination.Dashboard.route -> "Dashboard"
    BtsDestination.BuildMeal.route -> "Build Meal"
    BtsDestination.Subscription.route -> "Subscription"
    BtsDestination.Orders.route -> "Orders"
    BtsDestination.Nutrition.route -> "Nutrition"
    BtsDestination.Profile.route -> "Profile"
    BtsDestination.Support.route -> "Support"
    BtsDestination.Kitchen.route -> "Kitchen"
    BtsDestination.Packing.route -> "Packing"
    BtsDestination.Inventory.route -> "Inventory"
    BtsDestination.Delivery.route -> "Delivery"
    BtsDestination.Cart.route -> "Cart"
    BtsDestination.Checkout.route -> "Checkout"
    BtsDestination.OrderConfirmation.route -> "Order confirmed"
    BtsDestination.AddAddress.route -> "Delivery address"
    BtsDestination.Auth.route -> "Login"
    BtsDestination.Notifications.route -> "Notifications"
    BtsDestination.Referrals.route -> "Refer & earn"
    else -> "BTS"
}
