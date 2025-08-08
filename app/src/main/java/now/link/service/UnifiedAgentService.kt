package now.link.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import now.link.MainActivity
import now.link.R
import now.link.agent.AgentManagerFactory
import now.link.agent.UnifiedConfigurationManager
import now.link.utils.Constants
import now.link.utils.LogManager
import now.link.utils.RootUtils
import now.link.utils.SPUtils
import java.io.BufferedReader
import java.io.InputStreamReader

class UnifiedAgentService : Service() {

    companion object {
        private const val TAG = "UnifiedAgentService"
    }

    private lateinit var configManager: UnifiedConfigurationManager

    private var agentProcess: Process? = null
    private var serviceJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isWakeLockEnabled = false

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        configManager = UnifiedConfigurationManager(this)

        createNotificationChannel()
        LogManager.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.Service.ACTION_STOP -> {
                stopAgent()
                return START_NOT_STICKY
            }

            Constants.Service.ACTION_START, null -> {
                startForeground(Constants.Service.NOTIFICATION_ID, createNotification())

                // Handle wake-lock preference
                isWakeLockEnabled =
                    intent?.getBooleanExtra(Constants.Service.EXTRA_WAKE_LOCK_ENABLED, false)
                        ?: false
                if (isWakeLockEnabled) {
                    acquireWakeLock()
                }

                broadcastServiceStatus(true)
                startAgent()
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopAgent()
        serviceScope.cancel()
        releaseWakeLock()
        LogManager.d(TAG, "Service destroyed")
    }

    private fun startAgent() {
        if (serviceJob?.isActive == true) {
            LogManager.d(TAG, "Agent is already running")
            return
        }

        serviceJob = serviceScope.launch {
            try {
                if (!configManager.isConfigured()) {
                    LogManager.e(TAG, "Agent not configured")
                    withContext(Dispatchers.Main) {
                        updateNotification(getString(R.string.agent_not_configured))
                        broadcastServiceError(
                            Constants.Service.ERROR_NOT_CONFIGURED,
                            getString(R.string.agent_config_incomplete)
                        )
                    }
                    return@launch
                }

                val agentManager = configManager.getCurrentAgentManager()
                val configuration = configManager.loadConfiguration()
                
                LogManager.d(TAG, "Starting ${agentManager.agentType.displayName}")

                // Create command
                val command = try {
                    agentManager.createCommand(this@UnifiedAgentService, configuration)
                } catch (e: Exception) {
                    LogManager.e(TAG, "Failed to create command", e)
                    withContext(Dispatchers.Main) {
                        updateNotification(getString(R.string.failed_create_config))
                        broadcastServiceError(
                            Constants.Service.ERROR_NOT_CONFIGURED,
                            getString(R.string.failed_create_config_file)
                        )
                    }
                    return@launch
                }

                LogManager.d(TAG, "Starting agent with command: ${command.joinToString(" ")}")

                // Try to run with root if available, otherwise run normally
                val hasRoot = RootUtils.isRootAvailable()
                LogManager.d(TAG, "Root access: $hasRoot")

                startAgentProcess(command, hasRoot)
            } catch (e: CancellationException) {
                LogManager.d(TAG, "Service job was cancelled. Agent is stopping.")
                throw e
            } catch (e: Exception) {
                LogManager.e(TAG, "Failed to start agent", e)
                withContext(Dispatchers.Main) {
                    updateNotification(
                        getString(
                            R.string.agent_failed_start,
                            e.message ?: "Unknown error"
                        )
                    )
                    broadcastServiceError(
                        Constants.Service.ERROR_AGENT_START_FAILED,
                        getString(R.string.failed_start_agent, e.message ?: "Unknown error")
                    )
                }
            } finally {
                withContext(NonCancellable) {
                    stopSelf()
                }
            }
        }
    }

    private suspend fun startAgentProcess(command: List<String>, useRoot: Boolean) {
        try {
            val processArgs = if (useRoot) listOf("su", "-c", command.joinToString(" ")) else command
            val processBuilder = ProcessBuilder(processArgs)
                .directory(null)
                .redirectErrorStream(true)

            processBuilder.environment().apply {
                put("SSL_CERT_DIR", "/system/etc/security/cacerts")
            }

            agentProcess = processBuilder.start()

            val modeText = if (useRoot) "with root privileges" else "without root privileges"
            LogManager.d(TAG, "Agent started $modeText using ProcessBuilder")

            withContext(Dispatchers.Main) {
                val agentType = configManager.getCurrentAgentType()
                val notificationText = if (useRoot) {
                    "${agentType.displayName} running (root)"
                } else {
                    "${agentType.displayName} running"
                }
                updateNotification(notificationText)
            }

            // Monitor the process output
            monitorAgentOutput()

            // Wait for the process to exit
            val exitCode = agentProcess?.waitFor()
            LogManager.w(TAG, "Agent process exited with code: $exitCode")
        } catch (e: Exception) {
            if (useRoot) {
                LogManager.e(TAG, "Failed to start agent with root: ${e.message}")
                LogManager.d(TAG, "Falling back to non-root mode")
                startAgentProcess(command, false) // Fallback to non-root
            } else {
                LogManager.e(TAG, "Failed to start agent without root", e)
                throw e // Rethrow to be handled by the outer try-catch
            }
        }
    }

    private fun monitorAgentOutput() {
        agentProcess?.let { process ->
            serviceScope.launch {
                try {
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    var line: String? = null
                    while (isActive && reader.readLine().also { line = it } != null) {
                        LogManager.w(TAG, "Agent output: $line")
                    }
                } catch (e: Exception) {
                    LogManager.d(TAG, "Agent output stream closed.")
                }
            }
        }
    }

    private fun stopAgent() {
        LogManager.d(TAG, "stopAgent() called.")
        serviceJob?.cancel()

        // Wait a bit for graceful shutdown
        serviceScope.launch {
            delay(1000)
        }

        serviceJob = null

        // Destroy the process
        agentProcess?.let { process ->
            try {
                process.destroy()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    process.destroyForcibly()
                }
                LogManager.d(TAG, "Agent process terminated")
            } catch (e: Exception) {
                LogManager.e(TAG, "Error stopping agent process", e)
            }
        }
        agentProcess = null

        // Also try to kill with root if available
        serviceScope.launch {
            if (RootUtils.isRootAvailable()) {
                try {
                    val agentManager = configManager.getCurrentAgentManager()
                    val killPattern = agentManager.getProcessKillPattern()
                    RootUtils.executeRootCommand("pkill $killPattern")
                    LogManager.d(TAG, "Attempted to kill agent with root")
                } catch (e: Exception) {
                    LogManager.e(TAG, "Error killing agent with root", e)
                }
            }
        }

        // Clean up resources and stop the service
        releaseWakeLock()
        broadcastServiceStatus(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.Service.CHANNEL_ID,
                "Agent Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitoring agent background service"
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(message: String = getDefaultNotificationMessage()): Notification {
        val stopIntent = Intent(this, UnifiedAgentService::class.java).apply {
            action = Constants.Service.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val activityIntent = Intent(this, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, Constants.Service.CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentIntent(activityPendingIntent)
            .addAction(R.drawable.ic_stat_name, getString(R.string.stop), stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun getDefaultNotificationMessage(): String {
        val agentType = configManager.getCurrentAgentType()
        return "${agentType.displayName} running"
    }

    private fun updateNotification(message: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Constants.Service.NOTIFICATION_ID, createNotification(message))
    }

    private fun broadcastServiceStatus(isRunning: Boolean) {
        SPUtils.setBoolean(Constants.Service.EXTRA_SERVICE_RUNNING, isRunning)

        val intent = Intent(Constants.Service.ACTION_SERVICE_STATUS_CHANGED)
        intent.setPackage(packageName)
        intent.putExtra(Constants.Service.EXTRA_SERVICE_RUNNING, isRunning)
        sendBroadcast(intent)

        UnifiedAgentTileService.requestTileUpdate(this)

        LogManager.d(
            TAG,
            "Updated service status: $isRunning, broadcast sent, and requested tile update"
        )
    }

    private fun broadcastServiceError(errorType: String, errorMessage: String) {
        val intent = Intent(Constants.Service.ACTION_SERVICE_ERROR)
        intent.putExtra(Constants.Service.EXTRA_ERROR_TYPE, errorType)
        intent.putExtra(Constants.Service.EXTRA_ERROR_MESSAGE, errorMessage)
        sendBroadcast(intent)

        UnifiedAgentTileService.requestTileUpdate(this)

        LogManager.d(
            TAG,
            "Broadcast service error: $errorType - $errorMessage and requested tile update"
        )
    }

    private fun acquireWakeLock() {
        try {
            if (wakeLock?.isHeld != true) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "UnifiedAgent::ServiceWakeLock"
                )
                wakeLock?.acquire()
                LogManager.d(TAG, "Wake lock acquired")
            }
        } catch (e: Exception) {
            LogManager.e(TAG, "Failed to acquire wake lock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            wakeLock?.let { wl ->
                if (wl.isHeld) {
                    wl.release()
                    LogManager.d(TAG, "Wake lock released")
                }
            }
            wakeLock = null
        } catch (e: Exception) {
            LogManager.e(TAG, "Failed to release wake lock", e)
        }
    }
}
