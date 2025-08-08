package now.link.agent

import android.content.Context
import java.io.File

/**
 * Manager for Komari Agent
 */
class KomariAgentManager : BaseAgentManager() {
    
    override val agentType = AgentType.KOMARI_AGENT
    
    companion object {
        private const val TAG = "KomariAgentManager"
    }
    
    override fun createCommand(context: Context, configuration: AgentConfiguration, hasRoot: Boolean): List<String> {
        require(configuration is KomariAgentConfiguration) {
            "KomariAgentManager requires KomariAgentConfiguration"
        }
        
        val agentPath = getAgentPath(context)
        val commandArgs = configuration.toCommandArgs(hasRoot)

        return listOf(agentPath) + commandArgs
    }
    
    override fun createConfigFile(context: Context, configuration: AgentConfiguration): File? {
        // Komari agent doesn't use config files, it uses command line arguments
        return null
    }
}
