package now.link.utils

/**
 * Centralized constants for the application
 */
object Constants {
    
    // Shared Preferences
    object Preferences {
        const val PREFS_NAME = "ulmaridae_settings"
        const val WAKE_LOCK_ENABLED = "wake_lock_enabled"
        
        // Configuration keys
        const val SERVER = "server"
        const val SECRET = "secret"
        const val CLIENT_ID = "client_id"
        const val UUID = "uuid"
        const val ENABLE_TLS = "enable_tls"
        
        // Theme keys
        const val DYNAMIC_COLOR_ENABLED = "dynamic_color_enabled"
        const val FOLLOW_SYSTEM_THEME = "follow_system_theme"
        const val DARK_MODE_ENABLED = "dark_mode_enabled"
    }
    
    // Service Constants
    object Service {
        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "nezha_agent_channel"
        
        // Actions
        const val ACTION_START = "action_start"
        const val ACTION_STOP = "action_stop"
        const val ACTION_SERVICE_STATUS_CHANGED = "now.link.service.STATUS_CHANGED"
        const val ACTION_SERVICE_ERROR = "now.link.service.ERROR"
        
        // Extras
        const val EXTRA_SERVICE_RUNNING = "service_running"
        const val EXTRA_ERROR_MESSAGE = "error_message"
        const val EXTRA_ERROR_TYPE = "error_type"
        const val EXTRA_WAKE_LOCK_ENABLED = "wake_lock_enabled"
        
        // Error Types
        const val ERROR_NOT_CONFIGURED = "not_configured"
        const val ERROR_AGENT_NOT_INSTALLED = "agent_not_installed"
        const val ERROR_AGENT_START_FAILED = "agent_start_failed"
    }
    
    // Agent Configuration
    object Agent {
        const val VERSION = "v1.13.0"
        const val FILENAME = "nezha-agent"
        const val CONFIG_FILENAME = "config.yml"
        const val CONFIG_DIR = "nezha"
    }
    
    // Intent Data
    object Intent {
        const val PACKAGE_URI_PREFIX = "package:"
    }
}
