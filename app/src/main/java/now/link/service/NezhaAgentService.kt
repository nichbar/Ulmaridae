package now.link.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import now.link.MainActivity
import now.link.R
import now.link.utils.AgentManager
import now.link.utils.ConfigurationManager
import now.link.utils.RootUtils
import java.io.BufferedReader
import java.io.InputStreamReader

class NezhaAgentService : Service() {
    
    companion object {
        private const val TAG = "NezhaAgentService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "nezha_agent_channel"
        const val ACTION_START = "action_start"
        const val ACTION_STOP = "action_stop"
    }
    
    private lateinit var agentManager: AgentManager
    private lateinit var configManager: ConfigurationManager
    
    private var agentProcess: Process? = null
    private var serviceJob: Job? = null
    
    override fun onCreate() {
        super.onCreate()
        agentManager = AgentManager(this)
        configManager = ConfigurationManager(this)
        
        createNotificationChannel()
        Log.d(TAG, "Service created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopAgent()
                stopForeground(true)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                startForeground(NOTIFICATION_ID, createNotification())
                startAgent()
            }
        }
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        stopAgent()
        Log.d(TAG, "Service destroyed")
    }
    
    private fun startAgent() {
        if (serviceJob?.isActive == true) {
            Log.d(TAG, "Agent is already running")
            return
        }
        
        serviceJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!agentManager.isAgentInstalled()) {
                    Log.e(TAG, "Agent not installed")
                    return@launch
                }
                
                if (!configManager.isConfigured()) {
                    Log.e(TAG, "Agent not configured")
                    return@launch
                }
                
                val config = configManager.loadConfiguration()
                val agentPath = agentManager.getAgentPath()
                
                // Create the command
                val command = buildString {
                    append(agentPath)
                    append(" -s ${config.server}")
                    append(" -p ${config.secret}")
                    if (config.clientId.isNotEmpty()) {
                        append(" --client-id ${config.clientId}")
                    }
                    if (!config.enableTLS) {
                        append(" --disable-tls")
                    }
                }
                
                Log.d(TAG, "Starting agent with command: $command")
                
                // Try to run with root if available, otherwise run normally
                val hasRoot = RootUtils.isRootAvailable()
                Log.d(TAG, "Root access: $hasRoot")
                
                if (hasRoot) {
                    startWithRoot(command)
                } else {
                    startWithoutRoot(command)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start agent", e)
                withContext(Dispatchers.Main) {
                    updateNotification("Agent failed to start: ${e.message}")
                }
            }
        }
    }
    
    private suspend fun startWithRoot(command: String) {
        val (success, output) = RootUtils.executeRootCommand(command)
        
        if (success) {
            Log.d(TAG, "Agent started with root privileges")
            withContext(Dispatchers.Main) {
                updateNotification("Nezha Agent running (root)")
            }
            
            // Monitor the process
            monitorAgent(true)
        } else {
            Log.e(TAG, "Failed to start agent with root: $output")
            // Fallback to non-root
            startWithoutRoot(command)
        }
    }
    
    private suspend fun startWithoutRoot(command: String) {
        try {
            agentProcess = Runtime.getRuntime().exec(command)
            
            Log.d(TAG, "Agent started without root privileges")
            withContext(Dispatchers.Main) {
                updateNotification("Nezha Agent running")
            }
            
            // Monitor the process
            monitorAgent(false)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start agent without root", e)
            withContext(Dispatchers.Main) {
                updateNotification("Agent failed to start")
            }
        }
    }
    
    private suspend fun monitorAgent(isRoot: Boolean) {
        try {
            if (isRoot) {
                // For root processes, we need to check if the process is still running differently
                while (serviceJob?.isActive == true) {
                    delay(5000) // Check every 5 seconds
                    val (isRunning, _) = RootUtils.executeRootCommand("pgrep nezha-agent")
                    if (!isRunning) {
                        Log.w(TAG, "Agent process died (root)")
                        break
                    }
                }
            } else {
                // For non-root processes, we can wait for the process directly
                agentProcess?.let { process ->
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    val errorReader = BufferedReader(InputStreamReader(process.errorStream))
                    
                    // Read output in background
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                Log.d(TAG, "Agent output: $line")
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "Agent output stream closed")
                        }
                    }
                    
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            var line: String?
                            while (errorReader.readLine().also { line = it } != null) {
                                Log.w(TAG, "Agent error: $line")
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "Agent error stream closed")
                        }
                    }
                    
                    val exitCode = process.waitFor()
                    Log.w(TAG, "Agent process exited with code: $exitCode")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error monitoring agent", e)
        } finally {
            withContext(Dispatchers.Main) {
                updateNotification("Agent stopped")
            }
        }
    }
    
    private fun stopAgent() {
        serviceJob?.cancel()
        
        // Stop the process
        agentProcess?.let { process ->
            try {
                process.destroy()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    process.destroyForcibly()
                }
                Log.d(TAG, "Agent process terminated")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping agent process", e)
            }
        }
        
        // Also try to kill with root if available
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val hasRoot = RootUtils.isRootAvailable()
                if (hasRoot) {
                    RootUtils.executeRootCommand("pkill nezha-agent")
                    Log.d(TAG, "Killed agent with root")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error killing agent with root", e)
            }
        }
        
        agentProcess = null
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nezha Agent Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Nezha monitoring agent background service"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(message: String = "Nezha Agent running"): Notification {
        val stopIntent = Intent(this, NezhaAgentService::class.java).apply {
            action = ACTION_STOP
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
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Nezha Agent")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(activityPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification(message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(message))
    }
}
