package now.link.utils

import android.os.Build
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Utility class for managing theme preferences and settings
 */
object ThemeManager {
    
    // Internal mutable states
    private var _isDynamicColorEnabled by mutableStateOf(SPUtils.getBoolean(Constants.Preferences.DYNAMIC_COLOR_ENABLED, false))
    private var _isFollowSystemTheme by mutableStateOf(SPUtils.getBoolean(Constants.Preferences.FOLLOW_SYSTEM_THEME, true))
    private var _isDarkModeEnabled by mutableStateOf(SPUtils.getBoolean(Constants.Preferences.DARK_MODE_ENABLED, false))
    
    // Public read-only properties
    val isDynamicColorEnabled: Boolean get() = _isDynamicColorEnabled
    val isFollowSystemTheme: Boolean get() = _isFollowSystemTheme
    val isDarkModeEnabled: Boolean get() = _isDarkModeEnabled
    
    /**
     * Initialize theme manager - call this on app startup to ensure states are loaded
     */
    fun initialize() {
        _isDynamicColorEnabled = SPUtils.getBoolean(Constants.Preferences.DYNAMIC_COLOR_ENABLED, false)
        _isFollowSystemTheme = SPUtils.getBoolean(Constants.Preferences.FOLLOW_SYSTEM_THEME, true)
        _isDarkModeEnabled = SPUtils.getBoolean(Constants.Preferences.DARK_MODE_ENABLED, false)
    }
    
    /**
     * Set dynamic color enabled state
     */
    fun setDynamicColorEnabled(enabled: Boolean) {
        _isDynamicColorEnabled = enabled
        SPUtils.setBoolean(Constants.Preferences.DYNAMIC_COLOR_ENABLED, enabled)
    }
    
    /**
     * Set follow system theme state
     */
    fun setFollowSystemTheme(enabled: Boolean) {
        _isFollowSystemTheme = enabled
        SPUtils.setBoolean(Constants.Preferences.FOLLOW_SYSTEM_THEME, enabled)
    }
    
    /**
     * Set dark mode enabled state
     */
    fun setDarkModeEnabled(enabled: Boolean) {
        _isDarkModeEnabled = enabled
        SPUtils.setBoolean(Constants.Preferences.DARK_MODE_ENABLED, enabled)
    }
    
    /**
     * Check if dynamic color is supported on this device
     */
    fun isDynamicColorSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
}
