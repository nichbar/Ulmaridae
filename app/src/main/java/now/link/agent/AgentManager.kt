package now.link.agent

import android.content.Context
import java.io.File

/**
 * Interface for managing different types of monitoring agents
 */
interface AgentManager {
    val agentType: AgentType
    
    /**
     * Gets the path to the agent binary
     */
    fun getAgentPath(context: Context): String
    
    /**
     * Gets the expected binary filename for this agent type
     */
    fun getBinaryFileName(): String
    
    /**
     * Creates the command to execute the agent
     */
    fun createCommand(context: Context, configuration: AgentConfiguration, hasRoot: Boolean): List<String>
    
    /**
     * Creates configuration file if needed
     */
    fun createConfigFile(context: Context, configuration: AgentConfiguration): File?
    
    /**
     * Gets the configuration directory path
     */
    fun getConfigDir(context: Context): File
    
    /**
     * Validates if the agent binary exists and is executable
     */
    fun isAgentInstalled(context: Context): Boolean
    
    /**
     * Process name pattern for killing the agent process
     */
    fun getProcessKillPattern(): String
}

/**
 * Base implementation of AgentManager with common functionality
 */
abstract class BaseAgentManager : AgentManager {
    
    override fun getAgentPath(context: Context): String {
        return context.applicationInfo.nativeLibraryDir + "/lib${getBinaryFileName()}.so"
    }
    
    override fun getBinaryFileName(): String {
        return agentType.binaryName
    }
    
    override fun getConfigDir(context: Context): File {
        return File(context.filesDir, agentType.configDirName)
    }
    
    override fun isAgentInstalled(context: Context): Boolean {
        val agentFile = File(getAgentPath(context))
        return agentFile.exists() && agentFile.canExecute()
    }
    
    override fun getProcessKillPattern(): String {
        return agentType.binaryName
    }
}
