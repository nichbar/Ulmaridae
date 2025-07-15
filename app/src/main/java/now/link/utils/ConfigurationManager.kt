package now.link.utils

import android.content.Context
import android.content.SharedPreferences
import now.link.model.AgentConfiguration

class ConfigurationManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "nezha_config", Context.MODE_PRIVATE
    )
    
    fun saveConfiguration(config: AgentConfiguration) {
        prefs.edit().apply {
            putString("server", config.server)
            putString("secret", config.secret)
            putString("client_id", config.clientId)
            putBoolean("enable_tls", config.enableTLS)
            apply()
        }
    }
    
    fun loadConfiguration(): AgentConfiguration {
        return AgentConfiguration(
            server = prefs.getString("server", "") ?: "",
            secret = prefs.getString("secret", "") ?: "",
            clientId = prefs.getString("client_id", "") ?: "",
            enableTLS = prefs.getBoolean("enable_tls", true)
        )
    }
    
    fun isConfigured(): Boolean {
        val config = loadConfiguration()
        return config.server.isNotEmpty() && config.secret.isNotEmpty()
    }
}
