package now.link.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import now.link.ui.screens.LogsScreen
import now.link.ui.screens.MainScreen
import now.link.viewmodel.MainViewModel

// Define navigation routes as a sealed class for better type safety
sealed class Screen(val route: String) {
    data object Main : Screen("main")
    data object Logs : Screen("logs")
    // Add more screens here as needed
    // data object Settings : Screen("settings")
    // data object About : Screen("about")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    // Hoist the scroll state to preserve it across navigation
    val mainScreenScrollState = rememberScrollState()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
        modifier = modifier
    ) {
        composable(Screen.Main.route) {
            val viewModel = viewModel<MainViewModel>()

            MainScreen(
                viewModel = viewModel,
                scrollState = mainScreenScrollState,
                onLogsClick = {
                    navController.navigate(Screen.Logs.route)
                }
            )
        }

        composable(
            Screen.Logs.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth: Int -> fullWidth },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth: Int -> -fullWidth },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth: Int -> -fullWidth },
                    animationSpec = tween(durationMillis = 300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { fullWidth: Int -> fullWidth },
                    animationSpec = tween(durationMillis = 300)
                )
            }
        ) {
            LogsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Add more composable screens here as needed
        // composable(Screen.Settings.route) {
        //     SettingsScreen(
        //         onBackClick = { navController.popBackStack() }
        //     )
        // }
    }
}

// Extension functions for easier navigation
fun NavHostController.navigateToLogs() {
    this.navigate(Screen.Logs.route)
}

fun NavHostController.navigateToMain() {
    this.navigate(Screen.Main.route) {
        popUpTo(Screen.Main.route) { inclusive = true }
    }
}
