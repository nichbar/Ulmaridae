package now.link.utils

import androidx.compose.runtime.mutableStateOf

/**
 * Utility class for managing auto-start preferences and settings
 */
object AutoStartManager {

    // Internal mutable state
    private var _isAutoStartEnabled = mutableStateOf(SPUtils.getBoolean(Constants.Preferences.AUTO_START_ENABLED, false))

    // Public read-only property
    val isAutoStartEnabled: Boolean get() = _isAutoStartEnabled.value

    /**
     * Initialize auto-start manager - call this on app startup to ensure state is loaded
     */
    fun initialize() {
        _isAutoStartEnabled.value = SPUtils.getBoolean(Constants.Preferences.AUTO_START_ENABLED, false)
    }

    /**
     * Set auto-start enabled state
     */
    fun setAutoStartEnabled(enabled: Boolean) {
        _isAutoStartEnabled.value = enabled
        SPUtils.setBoolean(Constants.Preferences.AUTO_START_ENABLED, enabled)
    }
}