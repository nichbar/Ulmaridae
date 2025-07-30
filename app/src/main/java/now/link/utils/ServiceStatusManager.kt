package now.link.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

object ServiceStatusManager {

    fun observeServiceStatus(context: Context): Flow<Boolean> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Constants.Service.ACTION_SERVICE_STATUS_CHANGED) {
                    val isRunning =
                        intent.getBooleanExtra(Constants.Service.EXTRA_SERVICE_RUNNING, false)
                    trySend(isRunning)
                }
            }
        }

        val filter = IntentFilter(Constants.Service.ACTION_SERVICE_STATUS_CHANGED)
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }.distinctUntilChanged()
}
