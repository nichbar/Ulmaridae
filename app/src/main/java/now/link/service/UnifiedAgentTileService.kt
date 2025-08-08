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
import now.link.MainActivity
import now.link.R
import now.link.agent.UnifiedConfigurationManager
import now.link.utils.Constants
import now.link.utils.SPUtils

class UnifiedAgentTileService : TileService() {

    companion object {
        private const val TAG = "UnifiedAgentTileService"

        fun requestTileUpdate(context: Context) {
            requestListeningState(
                context,
                ComponentName(context, UnifiedAgentTileService::class.java)
            )
        }
    }

    private lateinit var configManager: UnifiedConfigurationManager

    override fun onStartListening() {
        super.onStartListening()
        Log.d(TAG, "Tile started listening")

        configManager = UnifiedConfigurationManager(this)

        updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        Log.d(TAG, "Tile stopped listening")
    }

    private fun isServiceRunning(): Boolean {
        val serviceRunning = SPUtils.getBoolean(Constants.Service.EXTRA_SERVICE_RUNNING, false)

        var serviceFoundInManager = false
        try {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            @Suppress("DEPRECATION")
            for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (UnifiedAgentService::class.java.name == service.service.className) {
                    serviceFoundInManager = true
                    break
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not check running services via ActivityManager", e)
        }

        Log.d(TAG, "Service status check - Prefs: $serviceRunning, Manager: $serviceFoundInManager")

        return serviceRunning
    }

    override fun onClick() {
        super.onClick()

        val isCurrentlyRunning = isServiceRunning()
        Log.d(TAG, "Tile clicked, current service status: $isCurrentlyRunning")

        if (isCurrentlyRunning) {
            stopService()
        } else {
            validateAndStartService()
        }
    }

    private fun validateAndStartService() {
        if (!configManager.isConfigured()) {
            Log.w(TAG, "Agent not configured")
            showToastAndOpenApp(getString(R.string.agent_not_configured_toast))
            return
        }

        startService()
    }

    private fun startService() {
        val intent = Intent(this, UnifiedAgentService::class.java)
        intent.action = Constants.Service.ACTION_START
        Log.d(TAG, "Starting Unified Agent service")

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
        val intent = Intent(this, UnifiedAgentService::class.java)
        intent.action = Constants.Service.ACTION_STOP
        Log.d(TAG, "Stopping Unified Agent service")

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
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        val mainActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(mainActivityIntent)

        Log.d(TAG, "Showed toast and opened app: $message")
    }

    private fun updateTile() {
        val tile = qsTile ?: return

        val isServiceCurrentlyRunning = isServiceRunning()
        val isConfigured = ::configManager.isInitialized && configManager.isConfigured()

        val agentType = if (::configManager.isInitialized) {
            configManager.getCurrentAgentType()
        } else {
            now.link.agent.AgentType.NEZHA_AGENT
        }

        when {
            isServiceCurrentlyRunning -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = agentType.displayName
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.running)
                }
            }

            !isConfigured -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = agentType.displayName
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.not_configured)
                }
            }

            else -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = agentType.displayName
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = getString(R.string.stopped)
                }
            }
        }

        tile.icon = Icon.createWithResource(this, R.drawable.ic_stat_name)

        tile.updateTile()
        Log.d(
            TAG,
            "Tile updated - service running: $isServiceCurrentlyRunning, configured: $isConfigured, agent: ${agentType.displayName}"
        )
    }
}
