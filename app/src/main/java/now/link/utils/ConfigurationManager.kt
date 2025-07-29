package now.link.utils

import android.content.Context
import android.util.Log
import now.link.model.AgentConfiguration
import java.io.File
import java.util.UUID

class ConfigurationManager(private val context: Context) {

    companion object {
        private const val TAG = "ConfigurationManager"
    }

    fun saveConfiguration(config: AgentConfiguration) {
        SPUtils.setString(Constants.Preferences.SERVER, config.server)
        SPUtils.setString(Constants.Preferences.SECRET, config.secret)
        SPUtils.setString(Constants.Preferences.CLIENT_ID, config.clientId)
        SPUtils.setString(Constants.Preferences.UUID, config.uuid)
        SPUtils.setBoolean(Constants.Preferences.ENABLE_TLS, config.enableTLS)
    }

    fun loadConfiguration(): AgentConfiguration {
        return AgentConfiguration(
            server = SPUtils.getString(Constants.Preferences.SERVER, "") ?: "",
            secret = SPUtils.getString(Constants.Preferences.SECRET, "") ?: "",
            clientId = SPUtils.getString(Constants.Preferences.CLIENT_ID, "") ?: "",
            uuid = SPUtils.getString(Constants.Preferences.UUID, "") ?: "",
            enableTLS = SPUtils.getBoolean(Constants.Preferences.ENABLE_TLS, true)
        )
    }

    fun isConfigured(): Boolean {
        val config = loadConfiguration()
        return config.server.isNotEmpty() && config.secret.isNotEmpty()
    }

    fun createConfigFile(): File? {
        return try {
            val config = loadConfiguration()
            if (!isConfigured()) {
                Log.e(TAG, "Configuration is incomplete")
                return null
            }

            val configDir = File(context.filesDir, Constants.Agent.CONFIG_DIR)
            if (!configDir.exists()) {
                configDir.mkdirs()
            }

            val configFile = File(configDir, Constants.Agent.CONFIG_FILENAME)

            val configContent = buildString {
                appendLine("client_secret: \"${config.secret}\"")
                appendLine("debug: false")
                appendLine("disable_auto_update: true")
                appendLine("disable_command_execute: true")
                appendLine("disable_force_update: false")
                appendLine("disable_nat: false")
                appendLine("disable_send_query: false")
                appendLine("gpu: false")
                appendLine("insecure_tls: false")
                appendLine("ip_report_period: 1800")
                appendLine("report_delay: 3")
                appendLine("self_update_period: 0")
                appendLine("server: \"${config.server}\"")
                appendLine("skip_connection_count: false")
                appendLine("skip_procs_count: false")
                appendLine("temperature: false")
                appendLine("tls: ${config.enableTLS}")
                appendLine("use_gitee_to_upgrade: false")
                appendLine("use_ipv6_country_code: false")
                if (config.uuid.isNotEmpty()) {
                    appendLine("uuid: \"${config.uuid}\"")
                } else {
                    // Generate a simple UUID-like identifier if not provided
                    val uuid = UUID.randomUUID().toString()
                    SPUtils.setString(Constants.Preferences.UUID, uuid)
                    appendLine("uuid: \"${uuid}\"")
                }
            }

            configFile.writeText(configContent)
            Log.d(TAG, "Config file created at: ${configFile.absolutePath}")
            Log.d(TAG, "Config content:\n$configContent")

            configFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create config file", e)
            null
        }
    }

    fun getConfigFilePath(): String {
        val configDir = File(context.filesDir, Constants.Agent.CONFIG_DIR)
        return File(configDir, Constants.Agent.CONFIG_FILENAME).absolutePath
    }
}
