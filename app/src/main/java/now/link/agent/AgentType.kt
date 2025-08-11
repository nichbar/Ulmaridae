package now.link.agent

/**
 * Enum representing different types of monitoring agents supported by the application
 */
enum class AgentType(
    val displayName: String,
    val binaryName: String,
    val configFileName: String,
    val configDirName: String,
    val downloadRepo: String
) {
    NEZHA_AGENT(
        displayName = "Nezha Agent",
        binaryName = "nezha-agent",
        configFileName = "config.yml",
        configDirName = "nezha",
        downloadRepo = "nichbar/agent"
    ),
    KOMARI_AGENT(
        displayName = "Komari Agent",
        binaryName = "komari-agent",
        configFileName = "config.yml",
        configDirName = "komari",
        downloadRepo = "nichbar/komari-agent"
    );
    
    companion object {
        fun fromString(name: String): AgentType? {
            return values().find { 
                it.name.equals(name, ignoreCase = true) || 
                it.displayName.equals(name, ignoreCase = true) 
            }
        }
    }
}
