package now.link.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import now.link.agent.UnifiedConfigurationManager
import now.link.service.UnifiedAgentService
import now.link.utils.Constants.Service

/**
 * Boot receiver for auto-start functionality
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
        private const val AUTO_START_DELAY_MS = 10000L // 10 seconds delay after boot
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == null) {
            return
        }

        val action = intent.action
        LogManager.d(TAG, "Boot event received: $action")

        // Handle boot completed events
        when (action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON"-> {
                handleBootCompleted(context)
            }
        }
    }

    private fun handleBootCompleted(context: Context) {
        LogManager.i(TAG, "Boot completed detected")

        // Initialize managers to ensure they're ready
        AutoStartManager.initialize()

        // Check if auto-start is enabled
        if (!AutoStartManager.isAutoStartEnabled) {
            LogManager.d(TAG, "Auto-start is disabled, skipping automatic service start")
            return
        }

        LogManager.i(TAG, "Auto-start is enabled, performing safety checks before starting service")

        // Delay the auto-start to ensure system is fully ready
        Handler(Looper.getMainLooper()).postDelayed({
            performAutoStart(context)
        }, AUTO_START_DELAY_MS)
    }

    private fun performAutoStart(context: Context) {
        try {
            // Safety Check 1: Verify auto-start is still enabled
            if (!AutoStartManager.isAutoStartEnabled) {
                LogManager.d(TAG, "Auto-start was disabled during delay, cancelling")
                return
            }

            // Safety Check 2: Validate configuration
            val configManager = UnifiedConfigurationManager(context)
            val currentAgentType = configManager.getCurrentAgentType()
            val agentManager = configManager.getCurrentAgentManager()
            val configuration = configManager.loadConfiguration()

            if (!configuration.isValid()) {
                LogManager.w(TAG, "Configuration is not valid, cannot auto-start ${currentAgentType.displayName}")
                return
            }

            // Safety Check 3: Check if service permissions are available
            if (!hasRequiredPermissions(context)) {
                LogManager.w(TAG, "Required permissions not available, cannot auto-start service")
                return
            }

            // Safety Check 4: Check battery optimization status
            if (isBatteryOptimizationActive(context)) {
                LogManager.w(TAG, "Battery optimization is active, auto-start may be unreliable")
                // Note: We still proceed but log the warning
            }

            LogManager.i(TAG, "All safety checks passed, auto-starting ${currentAgentType.displayName}")

            // Start the service
            val serviceIntent = Intent(context, UnifiedAgentService::class.java).apply {
                action = Service.ACTION_START
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

            LogManager.i(TAG, "${currentAgentType.displayName} auto-start initiated successfully")
        } catch (e: Exception) {
            LogManager.e(TAG, "Failed to perform auto-start", e)
        }
    }

    /**
     * Check if required permissions are available
     */
    private fun hasRequiredPermissions(context: Context): Boolean {
        // Check foreground service permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                LogManager.w(TAG, "POST_NOTIFICATIONS permission not granted")
                return false
            }
        }

        return true
    }

    /**
     * Check if battery optimization is active for this app
     */
    private fun isBatteryOptimizationActive(context: Context): Boolean {
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
            return !powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } catch (e: Exception) {
            LogManager.w(TAG, "Failed to check battery optimization status", e)
            return false // Assume not restricted if we can't check
        }
    }
}