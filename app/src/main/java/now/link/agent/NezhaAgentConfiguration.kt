package now.link.agent

import now.link.model.AgentConfiguration as LegacyAgentConfiguration
import java.util.UUID

/**
 * Configuration for Nezha Agent
 */
data class NezhaAgentConfiguration(
    override val server: String = "",
    override val secret: String = "",
    override val uuid: String = "",
    override val enableTLS: Boolean = true,
    override val enableCommandExecute: Boolean = false,
) : AgentConfiguration {
    
    override fun toCommandArgs(hasRoot: Boolean): List<String> {
        // Nezha agent typically uses config file, not command line args
        return emptyList()
    }
    
    override fun toConfigFileContent(): String {
        return buildString {
            appendLine("client_secret: \"$secret\"")
            appendLine("debug: false")
            appendLine("disable_auto_update: true")
            appendLine("disable_command_execute: ${!enableCommandExecute}")
            appendLine("disable_force_update: true")
            appendLine("disable_nat: false")
            appendLine("disable_send_query: false")
            appendLine("gpu: false")
            appendLine("insecure_tls: false")
            appendLine("ip_report_period: 1800")
            appendLine("report_delay: 3")
            appendLine("self_update_period: 0")
            appendLine("server: \"$server\"")
            appendLine("skip_connection_count: false")
            appendLine("skip_procs_count: false")
            appendLine("temperature: false")
            appendLine("tls: $enableTLS")
            appendLine("use_gitee_to_upgrade: false")
            appendLine("use_ipv6_country_code: false")
            
            val actualUuid = uuid.ifEmpty { UUID.randomUUID().toString() }
            appendLine("uuid: \"$actualUuid\"")
        }
    }
    
    companion object {
        fun fromLegacyConfig(legacy: LegacyAgentConfiguration): NezhaAgentConfiguration {
            return NezhaAgentConfiguration(
                server = legacy.server,
                secret = legacy.secret,
                uuid = legacy.uuid,
                enableTLS = legacy.enableTLS,
                enableCommandExecute = legacy.enableCommandExecute,
            )
        }
    }
}
