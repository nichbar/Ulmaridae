package now.link.utils

import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*

class AgentManager(private val context: Context) {

    companion object {
        private const val TAG = "AgentManager"
    }

    private val agentDir = File(context.filesDir, "agent")
    private val agentFile = File(agentDir, Constants.Agent.FILENAME)

    init {
        agentDir.mkdirs()
    }

    suspend fun extractAndInstallAgent(): Boolean = withContext(Dispatchers.IO) {
        try {
            LogManager.d(TAG, "Installing bundled Nezha agent binary")

            // Extract the agent from bundled assets
            val success = extractAgentFromAssets()
            if (success) {
                // Make executable
                makeExecutable()
                LogManager.d(TAG, "Agent installation completed successfully")
                true
            } else {
                LogManager.e(TAG, "Failed to extract agent from assets")
                false
            }
        } catch (e: Exception) {
            LogManager.e(TAG, "Error installing agent", e)
            e.printStackTrace()
            false
        }
    }

    fun isAgentInstalled(): Boolean {
        return agentFile.exists() && agentFile.canExecute()
    }

    fun getAgentPath(): String {
        return agentFile.absolutePath
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

    private fun extractAgentFromAssets(): Boolean {
        return try {
            val assetManager = context.assets
            val inputStream = assetManager.open("binaries/${Constants.Agent.FILENAME}")

            inputStream.use { input ->
                FileOutputStream(agentFile).use { output ->
                    input.copyTo(output)
                }
            }

            LogManager.d(TAG, "Extracted agent binary: ${agentFile.length()} bytes")
            true
        } catch (e: IOException) {
            LogManager.e(TAG, "Failed to extract agent from assets", e)
            false
        }
    }

    private fun makeExecutable() {
        try {
            agentFile.setExecutable(true, true)
            agentFile.setReadable(true, true)
            agentFile.setWritable(true, true)
        } catch (e: Exception) {
            LogManager.e(TAG, "Failed to make agent executable", e)
            e.printStackTrace()
        }
    }
}
