package now.link.service

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import now.link.MainActivity
import now.link.R
import now.link.utils.AgentManager
import now.link.utils.ConfigurationManager
import now.link.utils.Constants
import now.link.utils.SPUtils

class NezhaAgentTileService : TileService() {

    companion object {
        private const val TAG = "NezhaAgentTileService"

        /**
         * Request tile to update from external components
         */
        fun requestTileUpdate(context: Context) {
            requestListeningState(
                context,
                ComponentName(context, NezhaAgentTileService::class.java)
            )
        }
    }

    private lateinit var configManager: ConfigurationManager
    private lateinit var agentManager: AgentManager

    override fun onStartListening() {
        super.onStartListening()
        Log.d(TAG, "Tile started listening")

        // Initialize managers
        configManager = ConfigurationManager(this)
        agentManager = AgentManager(this)

        // Update tile with current status
        updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        Log.d(TAG, "Tile stopped listening")
    }

    /**
     * Check if NezhaAgentService is currently running
     * Uses multiple methods for better compatibility across Android versions
     */
    private fun isServiceRunning(): Boolean {
        // Method 1: Check using shared preferences (more reliable)
        val serviceRunning = SPUtils.getBoolean(Constants.Service.EXTRA_SERVICE_RUNNING, false)

        // Method 2: Check using ActivityManager (for additional verification on older versions)
        var serviceFoundInManager = false
        try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            @Suppress("DEPRECATION")
            for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (NezhaAgentService::class.java.name == service.service.className) {
                    serviceFoundInManager = true
                    break
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not check running services via ActivityManager", e)
        }

        Log.d(TAG, "Service status check - Prefs: $serviceRunning, Manager: $serviceFoundInManager")

        // Use shared preferences as primary indicator
        return serviceRunning
    }

    override fun onClick() {
        super.onClick()

        // Check current service status
        val isCurrentlyRunning = isServiceRunning()
        Log.d(TAG, "Tile clicked, current service status: $isCurrentlyRunning")

        if (isCurrentlyRunning) {
            // Stop the service
            stopService()
        } else {
            // Validate configuration and prerequisites before starting
            validateAndStartService()
        }
    }

    private fun validateAndStartService() {
        // Check if configuration is valid
        if (!configManager.isConfigured()) {
            Log.w(TAG, "Agent not configured")
            showToastAndOpenApp(getString(R.string.agent_not_configured_toast))
            return
        }

        // All checks passed, start the service
        startService()
    }

    private fun startService() {
        val intent = Intent(this, NezhaAgentService::class.java)
        intent.action = Constants.Service.ACTION_START
        Log.d(TAG, "Starting Nezha Agent service")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start service", e)
            showToastAndOpenApp(
                getString(
                    R.string.failed_start_service,
                    e.message ?: "Unknown error"
                )
            )
        }
    }

    private fun stopService() {
        val intent = Intent(this, NezhaAgentService::class.java)
        intent.action = Constants.Service.ACTION_STOP
        Log.d(TAG, "Stopping Nezha Agent service")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop service", e)
            showToastAndOpenApp(
                getString(
                    R.string.failed_stop_service,
                    e.message ?: "Unknown error"
                )
            )
        }
    }

    private fun showToastAndOpenApp(message: String) {
        // Show toast message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // Open the main activity
        val mainActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(mainActivityIntent)

        Log.d(TAG, "Showed toast and opened app: $message")
    }

    private fun updateTile() {
        val tile = qsTile ?: return

        // Check current service status
        val isServiceCurrentlyRunning = isServiceRunning()

        // Check configuration status for better tile state
        val isConfigured = ::configManager.isInitialized && configManager.isConfigured()

        when {
            isServiceCurrentlyRunning -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = getString(R.string.nezha_agent)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.running)
                }
            }

            !isConfigured -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = getString(R.string.nezha_agent)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.not_configured)
                }
            }

            else -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = getString(R.string.nezha_agent)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.stopped)
                }
            }
        }

        // Set icon - you can customize this
        tile.icon = Icon.createWithResource(this, R.drawable.ic_stat_name)

        tile.updateTile()
        Log.d(
            TAG,
            "Tile updated - service running: $isServiceCurrentlyRunning, configured: $isConfigured"
        )
    }
}
