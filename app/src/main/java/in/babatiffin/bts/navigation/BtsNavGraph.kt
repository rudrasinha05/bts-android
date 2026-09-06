package in.babatiffin.bts.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import in.babatiffin.bts.feature.home.HomeScreen
import in.babatiffin.bts.feature.menu.MenuScreen

@Composable
fun BtsNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = BtsDestination.Home.route,
    ) {
        composable(BtsDestination.Home.route) {
            HomeScreen(onOpenMenu = { navController.navigate(BtsDestination.Menu.route) })
        }
        composable(BtsDestination.Menu.route) {
            MenuScreen()
        }
    }
}
