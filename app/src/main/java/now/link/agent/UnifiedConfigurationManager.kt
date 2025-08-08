package now.link.agent

import android.content.Context
import now.link.utils.SPUtils

/**
 * Unified configuration manager that handles multiple agent types
 */
class UnifiedConfigurationManager(private val context: Context) {
    
    companion object {
        private const val TAG = "UnifiedConfigurationManager"
        private const val CURRENT_AGENT_TYPE_KEY = "current_agent_type"
        private const val DEFAULT_AGENT_TYPE = "NEZHA_AGENT"
    }
    
    /**
     * Gets the currently selected agent type
     */
    fun getCurrentAgentType(): AgentType {
        val agentTypeName = SPUtils.getString(CURRENT_AGENT_TYPE_KEY, DEFAULT_AGENT_TYPE) ?: DEFAULT_AGENT_TYPE
        return AgentType.fromString(agentTypeName) ?: AgentType.NEZHA_AGENT
    }
    
    /**
     * Sets the current agent type
     */
    fun setCurrentAgentType(agentType: AgentType) {
        SPUtils.setString(CURRENT_AGENT_TYPE_KEY, agentType.name)
    }
    
    /**
     * Gets the current agent manager
     */
    fun getCurrentAgentManager(): AgentManager {
        return AgentManagerFactory.create(getCurrentAgentType())
    }
    
    /**
     * Saves configuration for the current agent type
     */
    fun saveConfiguration(configuration: AgentConfiguration) {
        val agentType = getCurrentAgentType()
        val prefix = getConfigKeyPrefix(agentType)
        
        when (configuration) {
            is NezhaAgentConfiguration -> {
                SPUtils.setString("${prefix}_server", configuration.server)
                SPUtils.setString("${prefix}_secret", configuration.secret)
                SPUtils.setString("${prefix}_uuid", configuration.uuid)
                SPUtils.setBoolean("${prefix}_enable_tls", configuration.enableTLS)
                SPUtils.setBoolean("${prefix}_enable_command_execute", configuration.enableCommandExecute)
            }
            is KomariAgentConfiguration -> {
                SPUtils.setString("${prefix}_server", configuration.server)
                SPUtils.setString("${prefix}_secret", configuration.secret)
                SPUtils.setString("${prefix}_uuid", configuration.uuid)
                SPUtils.setBoolean("${prefix}_enable_tls", configuration.enableTLS)
                SPUtils.setBoolean("${prefix}_enable_command_execute", configuration.enableCommandExecute)
            }
        }
    }
    
    /**
     * Loads configuration for the current agent type
     */
    fun loadConfiguration(): AgentConfiguration {
        val agentType = getCurrentAgentType()
        val prefix = getConfigKeyPrefix(agentType)
        
        return when (agentType) {
            AgentType.NEZHA_AGENT -> {
                NezhaAgentConfiguration(
                    server = SPUtils.getString("${prefix}_server", "") ?: "",
                    secret = SPUtils.getString("${prefix}_secret", "") ?: "",
                    uuid = SPUtils.getString("${prefix}_uuid", "") ?: "",
                    enableTLS = SPUtils.getBoolean("${prefix}_enable_tls", true),
                    enableCommandExecute = SPUtils.getBoolean("${prefix}_enable_command_execute", false)
                )
            }
            AgentType.KOMARI_AGENT -> {
                KomariAgentConfiguration(
                    server = SPUtils.getString("${prefix}_server", "") ?: "",
                    secret = SPUtils.getString("${prefix}_secret", "") ?: "",
                    enableTLS = SPUtils.getBoolean("${prefix}_enable_tls", true),
                    enableCommandExecute = SPUtils.getBoolean("${prefix}_enable_command_execute", false),
                )
            }
        }
    }
    
    /**
     * Checks if the current agent is configured
     */
    fun isConfigured(): Boolean {
        val configuration = loadConfiguration()
        return configuration.isValid()
    }
    
    /**
     * Creates config file for the current agent if needed
     */
    fun createConfigFile(): java.io.File? {
        val agentManager = getCurrentAgentManager()
        val configuration = loadConfiguration()
        
        if (!configuration.isValid()) {
            return null
        }
        
        return agentManager.createConfigFile(context, configuration)
    }
    
    /**
     * Gets the config file path for the current agent
     */
    fun getConfigFilePath(): String {
        val agentManager = getCurrentAgentManager()
        val configDir = agentManager.getConfigDir(context)
        return java.io.File(configDir, getCurrentAgentType().configFileName).absolutePath
    }
    
    private fun getConfigKeyPrefix(agentType: AgentType): String {
        return agentType.name.lowercase()
    }
}
