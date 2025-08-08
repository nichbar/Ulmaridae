package now.link.utils

import android.content.Context
import android.os.Build
import now.link.agent.AgentManagerFactory
import now.link.agent.AgentType
import now.link.agent.UnifiedConfigurationManager

class UnifiedAgentManager(private val context: Context) {

    private val configManager = UnifiedConfigurationManager(context)

    fun getAgentPath(): String {
        val agentManager = configManager.getCurrentAgentManager()
        return agentManager.getAgentPath(context)
    }

    fun getCurrentAgentType(): AgentType {
        return configManager.getCurrentAgentType()
    }

    fun setCurrentAgentType(agentType: AgentType) {
        configManager.setCurrentAgentType(agentType)
    }

    fun getAvailableAgentTypes(): List<AgentType> {
        return AgentManagerFactory.getAvailableAgentTypes()
    }

    fun findInstalledAgents(): List<AgentType> {
        return AgentManagerFactory.findInstalledAgents(context)
    }

    fun isCurrentAgentInstalled(): Boolean {
        val agentManager = configManager.getCurrentAgentManager()
        return agentManager.isAgentInstalled(context)
    }

    fun getDeviceArchitecture(): String {
        return when {
            Build.SUPPORTED_64_BIT_ABIS.isNotEmpty() -> {
                when {
                    Build.SUPPORTED_64_BIT_ABIS.contains("arm64-v8a") -> "ARM64"
                    Build.SUPPORTED_64_BIT_ABIS.contains("x86_64") -> "x86_64"
                    else -> "ARM64 (default)"
                }
            }

            Build.SUPPORTED_32_BIT_ABIS.isNotEmpty() -> {
                when {
                    Build.SUPPORTED_32_BIT_ABIS.contains("armeabi-v7a") ||
                        Build.SUPPORTED_32_BIT_ABIS.contains("armeabi") -> "ARM (32-bit)"

                    Build.SUPPORTED_32_BIT_ABIS.contains("x86") -> "x86 (32-bit)"
                    else -> "ARM (32-bit)"
                }
            }

            else -> "Unknown"
        }
    }

    companion object {
        private const val TAG = "UnifiedAgentManager"
    }
}
