package now.link.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import now.link.model.LogEntry
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Manages in-memory logging for the application
 * - Wraps Android Log.* calls to capture logs in memory
 * - Maintains a circular buffer of logs
 * - Provides real-time updates via LiveData
 */
object LogManager {

    private const val TAG = "LogManager"
    private const val PREF_LOG_ENABLED = "log_enabled"
    private const val MAX_LOG_ENTRIES = 10000

    private val logEntries = ConcurrentLinkedQueue<LogEntry>()
    private val _logEntriesLiveData = MutableLiveData<List<LogEntry>>()
    val logEntriesLiveData: LiveData<List<LogEntry>> = _logEntriesLiveData

    private var isLogEnabled = false

    /**
     * Initialize the LogManager with context
     */
    fun initialize(context: Context) {
        isLogEnabled = SPUtils.getBoolean(PREF_LOG_ENABLED, false) ?: false

        // Add initialization log
        if (isLogEnabled) {
            addLogEntry(LogEntry.LogLevel.INFO, TAG, "LogManager initialized, logging enabled")
        }
    }

    /**
     * Enable or disable logging
     */
    fun setLogEnabled(enabled: Boolean) {
        isLogEnabled = enabled
        SPUtils.setBoolean(PREF_LOG_ENABLED, enabled)

        if (enabled) {
            addLogEntry(LogEntry.LogLevel.INFO, TAG, "In-memory logging enabled")
        } else {
            addLogEntry(LogEntry.LogLevel.INFO, TAG, "In-memory logging disabled")
        }
    }

    /**
     * Check if logging is enabled
     */
    fun isLogEnabled(): Boolean = isLogEnabled

    /**
     * Add a log entry to the in-memory store
     */
    private fun addLogEntry(
        level: LogEntry.LogLevel,
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        if (!isLogEnabled) return

        val entry = LogEntry(
            level = level,
            tag = tag,
            message = message,
            throwable = throwable
        )

        synchronized(logEntries) {
            // Add new entry
            logEntries.offer(entry)

            // Remove oldest entries if we exceed the limit
            while (logEntries.size > MAX_LOG_ENTRIES) {
                logEntries.poll()
            }

            // Update LiveData with current entries
            _logEntriesLiveData.postValue(logEntries.toList())
        }
    }

    /**
     * Get all current log entries
     */
    fun getAllLogEntries(): List<LogEntry> {
        return synchronized(logEntries) {
            logEntries.toList()
        }
    }

    /**
     * Clear all log entries
     */
    fun clearLogs() {
        synchronized(logEntries) {
            logEntries.clear()
            _logEntriesLiveData.postValue(emptyList())
        }
        if (isLogEnabled) {
            addLogEntry(LogEntry.LogLevel.INFO, TAG, "Log entries cleared")
        }
    }

    /**
     * Get log entries count
     */
    fun getLogCount(): Int = logEntries.size

    // Wrapper methods for Android Log.* calls

    fun v(tag: String, msg: String): Int {
        val result = Log.v(tag, msg)
        addLogEntry(LogEntry.LogLevel.VERBOSE, tag, msg)
        return result
    }

    fun v(tag: String, msg: String, tr: Throwable): Int {
        val result = Log.v(tag, msg, tr)
        addLogEntry(LogEntry.LogLevel.VERBOSE, tag, msg, tr)
        return result
    }

    fun d(tag: String, msg: String): Int {
        val result = Log.d(tag, msg)
        addLogEntry(LogEntry.LogLevel.DEBUG, tag, msg)
        return result
    }

    fun d(tag: String, msg: String, tr: Throwable): Int {
        val result = Log.d(tag, msg, tr)
        addLogEntry(LogEntry.LogLevel.DEBUG, tag, msg, tr)
        return result
    }

    fun i(tag: String, msg: String): Int {
        val result = Log.i(tag, msg)
        addLogEntry(LogEntry.LogLevel.INFO, tag, msg)
        return result
    }

    fun i(tag: String, msg: String, tr: Throwable): Int {
        val result = Log.i(tag, msg, tr)
        addLogEntry(LogEntry.LogLevel.INFO, tag, msg, tr)
        return result
    }

    fun w(tag: String, msg: String): Int {
        val result = Log.w(tag, msg)
        addLogEntry(LogEntry.LogLevel.WARN, tag, msg)
        return result
    }

    fun w(tag: String, msg: String, tr: Throwable): Int {
        val result = Log.w(tag, msg, tr)
        addLogEntry(LogEntry.LogLevel.WARN, tag, msg, tr)
        return result
    }

    fun w(tag: String, tr: Throwable): Int {
        val result = Log.w(tag, tr)
        addLogEntry(LogEntry.LogLevel.WARN, tag, tr.message ?: "Exception", tr)
        return result
    }

    fun e(tag: String, msg: String): Int {
        val result = Log.e(tag, msg)
        addLogEntry(LogEntry.LogLevel.ERROR, tag, msg)
        return result
    }

    fun e(tag: String, msg: String, tr: Throwable): Int {
        val result = Log.e(tag, msg, tr)
        addLogEntry(LogEntry.LogLevel.ERROR, tag, msg, tr)
        return result
    }

    fun wtf(tag: String, msg: String): Int {
        val result = Log.wtf(tag, msg)
        addLogEntry(LogEntry.LogLevel.ASSERT, tag, msg)
        return result
    }

    fun wtf(tag: String, tr: Throwable): Int {
        val result = Log.wtf(tag, tr)
        addLogEntry(LogEntry.LogLevel.ASSERT, tag, tr.message ?: "WTF Exception", tr)
        return result
    }

    fun wtf(tag: String, msg: String, tr: Throwable): Int {
        val result = Log.wtf(tag, msg, tr)
        addLogEntry(LogEntry.LogLevel.ASSERT, tag, msg, tr)
        return result
    }
}
