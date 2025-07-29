package now.link.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.navOptions

/**
 * Navigation utility functions and constants for the app
 */

// Navigation options for different scenarios
object NavigationOptions {
    
    /**
     * Standard navigation with no special behavior
     */
    val standard: NavOptions = navOptions { }
    
    /**
     * Navigation that clears the back stack to the start destination
     */
    fun clearBackStack(startDestination: String): NavOptions = navOptions {
        popUpTo(startDestination) {
            inclusive = true
        }
    }
    
    /**
     * Navigation that clears the current screen from back stack
     */
    val replaceTop: NavOptions = navOptions {
        popUpTo(0) { inclusive = true }
    }
    
    /**
     * Single top navigation (prevents multiple instances of the same screen)
     */
    val singleTop: NavOptions = navOptions {
        launchSingleTop = true
    }
}

/**
 * Safe navigation extension to prevent crashes from rapid clicks
 */
fun NavController.safeNavigate(
    route: String,
    navOptions: NavOptions? = null
) {
    try {
        navigate(route, navOptions)
    } catch (e: Exception) {
        // Log the exception if needed
        // LogManager.w("Navigation", "Failed to navigate to $route", e)
    }
}

/**
 * Navigate with pop behavior - useful for replacing screens
 */
fun NavController.navigateAndPop(
    route: String,
    popUpToRoute: String,
    inclusive: Boolean = false
) {
    navigate(route) {
        popUpTo(popUpToRoute) { this.inclusive = inclusive }
    }
}
