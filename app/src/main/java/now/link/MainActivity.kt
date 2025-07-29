package now.link

import android.content.ComponentName
import android.os.Bundle
import android.service.quicksettings.TileService
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import now.link.navigation.AppNavigation
import now.link.service.NezhaAgentTileService
import now.link.ui.theme.UlmaridaeTheme
import now.link.utils.LogManager
import now.link.utils.ThemeManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize LogManager
        LogManager.initialize(this)
        
        // Initialize ThemeManager
        ThemeManager.initialize()

        setContent {
            UlmaridaeTheme {
                // Determine if we're in dark mode based on all theme settings
                val isDarkMode = when {
                    // If dynamic color is enabled and supported, follow system theme
                    ThemeManager.isDynamicColorEnabled && ThemeManager.isDynamicColorSupported() -> {
                        isSystemInDarkTheme()
                    }
                    // If following system theme, use system dark mode
                    ThemeManager.isFollowSystemTheme -> {
                        isSystemInDarkTheme()
                    }
                    // Otherwise use manual dark mode setting
                    else -> {
                        ThemeManager.isDarkModeEnabled
                    }
                }
                
                // Update system bars based on theme
                LaunchedEffect(isDarkMode) {
                    enableEdgeToEdge(
                        statusBarStyle = if (isDarkMode) {
                            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                        } else {
                            SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                        },
                        navigationBarStyle = if (isDarkMode) {
                            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                        } else {
                            SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                        }
                    )
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp()
                }
            }
        }

        TileService.requestListeningState(
            this,
            ComponentName(this, NezhaAgentTileService::class.java)
        )
    }

    @Composable
    private fun MainApp() {
        val navController = rememberNavController()

        AppNavigation(navController = navController)
    }

    companion object {
        private const val TAG = "MainActivity"
    }

}
