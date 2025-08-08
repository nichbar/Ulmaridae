package now.link.agent

import android.content.Context
import android.util.Log
import java.io.File

/**
 * Manager for Nezha Agent
 */
class NezhaAgentManager : BaseAgentManager() {
    
    override val agentType = AgentType.NEZHA_AGENT
    
    companion object {
        private const val TAG = "NezhaAgentManager"
    }
    
    override fun createCommand(context: Context, configuration: AgentConfiguration, hasRoot: Boolean): List<String> {
        require(configuration is NezhaAgentConfiguration) {
            "NezhaAgentManager requires NezhaAgentConfiguration"
        }
        
        val agentPath = getAgentPath(context)
        val configFile = createConfigFile(context, configuration)
            ?: throw IllegalStateException("Failed to create config file")
        
        return listOf(agentPath, "-c", configFile.absolutePath)
    }
    
    override fun createConfigFile(context: Context, configuration: AgentConfiguration): File? {
        require(configuration is NezhaAgentConfiguration) {
            "NezhaAgentManager requires NezhaAgentConfiguration"
        }
        
        return try {
            if (!configuration.isValid()) {
                Log.e(TAG, "Configuration is incomplete")
                return null
            }
            
            val configDir = getConfigDir(context)
            if (!configDir.exists()) {
                configDir.mkdirs()
            }
            
            val configFile = File(configDir, agentType.configFileName)
            val configContent = configuration.toConfigFileContent()
                ?: throw IllegalStateException("Failed to generate config content")
            
            configFile.writeText(configContent)
            Log.d(TAG, "Config file created at: ${configFile.absolutePath}")
            Log.d(TAG, "Config content:\n$configContent")
            
            configFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create config file", e)
            null
        }
    }
}
