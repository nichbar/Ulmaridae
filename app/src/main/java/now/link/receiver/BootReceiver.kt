package now.link.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import now.link.service.NezhaAgentService
import now.link.utils.ConfigurationManager

class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.d(TAG, "Boot completed or package replaced")
                
                val configManager = ConfigurationManager(context)
                if (configManager.isConfigured()) {
                    Log.d(TAG, "Starting Nezha Agent service on boot")
                    
                    val serviceIntent = Intent(context, NezhaAgentService::class.java)
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to start service on boot", e)
                    }
                } else {
                    Log.d(TAG, "Agent not configured, skipping auto-start")
                }
            }
        }
    }
}
