package now.link.agent

/**
 * Base configuration interface for all monitoring agents
 */
interface AgentConfiguration {
    val server: String
    val secret: String
    val uuid: String
    val enableTLS: Boolean
    val enableCommandExecute: Boolean
    
    /**
     * Validates the configuration
     */
    fun isValid(): Boolean = server.isNotEmpty() && secret.isNotEmpty()
    
    /**
     * Converts configuration to command line arguments
     */
    fun toCommandArgs(): List<String>
    
    /**
     * Converts configuration to config file content
     */
    fun toConfigFileContent(): String?
}
