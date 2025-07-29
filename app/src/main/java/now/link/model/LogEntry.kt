package now.link.model

import java.text.SimpleDateFormat
import java.util.*

/**
 * Represents a single log entry with timestamp, level, tag, and message
 */
data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwable: Throwable? = null
) {
    
    enum class LogLevel(val priority: Int, val shortName: String) {
        VERBOSE(2, "V"),
        DEBUG(3, "D"),
        INFO(4, "I"),
        WARN(5, "W"),
        ERROR(6, "E"),
        ASSERT(7, "A")
    }
    
    /**
     * Format timestamp to readable string
     */
    fun getFormattedTime(): String {
        val formatter = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
    
    /**
     * Get the full formatted log line
     */
    fun getFormattedMessage(): String {
        val baseMessage = "${getFormattedTime()} ${level.shortName}/$tag: $message"
        return if (throwable != null) {
            "$baseMessage\n${android.util.Log.getStackTraceString(throwable)}"
        } else {
            baseMessage
        }
    }
}
