package now.link.agent

/**
 * Configuration for Komari Agent
 */
data class KomariAgentConfiguration(
    override val server: String = "",
    override val secret: String = "", // In Komari this is called "token"
    override val uuid: String = "",
    override val enableTLS: Boolean = true,
    override val enableCommandExecute: Boolean = false,
) : AgentConfiguration {
    
    override fun toCommandArgs(hasRoot: Boolean): List<String> {
        val args = mutableListOf<String>()
        
        // Required arguments
        args.addAll(listOf("-e", server))
        args.addAll(listOf("-t", secret))
        
        // Optional arguments
        if (!enableTLS) {
            args.add("-u") // ignore-unsafe-cert
        }
        
        if (!enableCommandExecute) {
            args.add("--disable-web-ssh")
        }

        // ignore auto-update
        args.add("--disable-auto-update")

        args.add("--is-android=true")

        args.add("--has-root-privilege=$hasRoot")

        return args
    }
    
    override fun toConfigFileContent(): String? {
        // Komari agent uses command line arguments, not config file
        return null
    }
}
