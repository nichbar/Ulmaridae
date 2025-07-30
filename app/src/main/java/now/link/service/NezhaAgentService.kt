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
import now.link.utils.AgentManager
import now.link.utils.ConfigurationManager
import now.link.utils.Constants
import now.link.utils.LogManager
import now.link.utils.RootUtils
import now.link.utils.SPUtils
import java.io.BufferedReader
import java.io.InputStreamReader

class NezhaAgentService : Service() {

    companion object {
        private const val TAG = "NezhaAgentService"
    }

    private lateinit var agentManager: AgentManager
    private lateinit var configManager: ConfigurationManager

    private var agentProcess: Process? = null
    private var serviceJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isWakeLockEnabled = false

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        agentManager = AgentManager(this)
        configManager = ConfigurationManager(this)

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

                // Create config file
                val configFile = configManager.createConfigFile()
                if (configFile == null) {
                    LogManager.e(TAG, "Failed to create config file")
                    withContext(Dispatchers.Main) {
                        updateNotification(getString(R.string.failed_create_config))
                        broadcastServiceError(
                            Constants.Service.ERROR_NOT_CONFIGURED,
                            getString(R.string.failed_create_config_file)
                        )
                    }
                    return@launch
                }

                val agentPath = agentManager.getAgentPath()

                // Create the command using config file
                val command = "$agentPath -c ${configFile.absolutePath}"

                LogManager.d(TAG, "Starting agent with command: $command")

                // Try to run with root if available, otherwise run normally
                val hasRoot = RootUtils.isRootAvailable()
                LogManager.d(TAG, "Root access: $hasRoot")

                startAgentProcess(command, hasRoot)
            } catch (e: CancellationException) {
                // ✅ **FIX:** This is expected on cancellation (stopping the service).
                // Do not log it as an error. Just rethrow to propagate the cancellation.
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
                // This block will run after the try-catch, including after cancellation.
                // We stop the service here to ensure cleanup.
                withContext(NonCancellable) {
                    stopSelf()
                }
            }
        }
    }

    private suspend fun startAgentProcess(command: String, useRoot: Boolean) {
        try {
            // Parse command into arguments
            val commandParts = command.split(" ")
            val processArgs = if (useRoot) listOf("su", "-c", command) else commandParts
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
                val notificationText =
                    if (useRoot) getString(R.string.nezha_agent_running_root) else getString(R.string.nezha_agent_running)
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
                // ✅ **FIX:** This is the log message you were seeing.
                // We rethrow the exception to be caught by the main catch block.
                LogManager.e(TAG, "Failed to start agent without root", e)
                throw e // Rethrow to be handled by the outer try-catch
            }
        }
    }

    private fun monitorAgentOutput() {
        agentProcess?.let { process ->
            // Use a separate coroutine to read the output stream so it doesn't block
            // the main service job from being cancelled.
            serviceScope.launch {
                try {
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    var line: String? = null
                    while (isActive && reader.readLine().also { line = it } != null) {
                        LogManager.w(TAG, "Agent output: $line")
                    }
                } catch (e: Exception) {
                    // This is expected when the process is destroyed
                    LogManager.d(TAG, "Agent output stream closed.")
                }
            }
        }
    }

    private fun stopAgent() {
        LogManager.d(TAG, "stopAgent() called.")
        // Cancel the main coroutine. This will interrupt the process monitoring.
        serviceJob?.cancel()
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
        // Clear the reference to the process
        agentProcess = null

        // Also try to kill with root if available
        serviceScope.launch {
            if (RootUtils.isRootAvailable()) {
                try {
                    RootUtils.executeRootCommand("pkill nezha-agent")
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
        stopSelf() // Ensure the service itself stops
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.Service.CHANNEL_ID,
                getString(R.string.nezha_agent_service),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.nezha_agent_service_description)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(message: String = getString(R.string.nezha_agent_running)): Notification {
        val stopIntent = Intent(this, NezhaAgentService::class.java).apply {
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

    private fun updateNotification(message: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Constants.Service.NOTIFICATION_ID, createNotification(message))
    }

    private fun broadcastServiceStatus(isRunning: Boolean) {
        // Update shared preferences for tile to check
        SPUtils.setBoolean(Constants.Service.EXTRA_SERVICE_RUNNING, isRunning)

        val intent = Intent(Constants.Service.ACTION_SERVICE_STATUS_CHANGED)
        intent.putExtra(Constants.Service.EXTRA_SERVICE_RUNNING, isRunning)
        sendBroadcast(intent)

        // Request tile update using the proper API
        NezhaAgentTileService.requestTileUpdate(this)

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

        // Request tile update to reflect error state
        NezhaAgentTileService.requestTileUpdate(this)

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
                    "NezhaAgent::ServiceWakeLock"
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
