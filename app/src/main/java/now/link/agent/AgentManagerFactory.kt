package now.link.agent

import android.content.Context

/**
 * Factory for creating agent managers based on agent type
 */
object AgentManagerFactory {
    
    /**
     * Creates an appropriate agent manager for the given agent type
     */
    fun create(agentType: AgentType): AgentManager {
        return when (agentType) {
            AgentType.NEZHA_AGENT -> NezhaAgentManager()
            AgentType.KOMARI_AGENT -> KomariAgentManager()
        }
    }
    
    /**
     * Gets all available agent types
     */
    fun getAvailableAgentTypes(): List<AgentType> {
        return AgentType.values().toList()
    }
    
    /**
     * Finds installed agents on the device
     */
    fun findInstalledAgents(context: Context): List<AgentType> {
        return AgentType.values().filter { agentType ->
            val manager = create(agentType)
            manager.isAgentInstalled(context)
        }
    }
}
