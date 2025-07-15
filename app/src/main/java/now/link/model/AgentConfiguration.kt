package now.link.model

data class AgentConfiguration(
    val server: String = "",
    val secret: String = "",
    val clientId: String = "",
    val enableTLS: Boolean = true
)
