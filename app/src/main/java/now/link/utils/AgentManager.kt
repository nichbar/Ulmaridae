package now.link.utils

import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

class AgentManager(private val context: Context) {
    
    companion object {
        private const val AGENT_VERSION = "v1.13.0"
        private const val AGENT_FILENAME = "nezha-agent"
        private const val GITHUB_RELEASE_URL = "https://github.com/nezhahq/agent/releases/download"
    }
    
    private val agentDir = File(context.filesDir, "agent")
    private val agentFile = File(agentDir, AGENT_FILENAME)
    
    init {
        agentDir.mkdirs()
    }
    
    suspend fun downloadAndInstallAgent(): Boolean = withContext(Dispatchers.IO) {
        try {
            val architecture = getDeviceArchitecture()
            val downloadUrl = "$GITHUB_RELEASE_URL/$AGENT_VERSION/nezha-agent_linux_$architecture.zip"
            
            // Download the agent
            val zipFile = downloadFile(downloadUrl)
            if (zipFile != null) {
                // Extract the agent binary
                extractAgent(zipFile)
                zipFile.delete()
                
                // Make executable
                makeExecutable()
                
                true
            } else {
                false
            }
        } catch (e: Exception) {
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
    
    private fun getDeviceArchitecture(): String {
        return when {
            Build.SUPPORTED_64_BIT_ABIS.isNotEmpty() -> {
                when {
                    Build.SUPPORTED_64_BIT_ABIS.contains("arm64-v8a") -> "arm64"
                    Build.SUPPORTED_64_BIT_ABIS.contains("x86_64") -> "amd64"
                    else -> "arm64" // Default to arm64 for most Android devices
                }
            }
            Build.SUPPORTED_32_BIT_ABIS.isNotEmpty() -> {
                when {
                    Build.SUPPORTED_32_BIT_ABIS.contains("armeabi-v7a") || 
                    Build.SUPPORTED_32_BIT_ABIS.contains("armeabi") -> "arm"
                    Build.SUPPORTED_32_BIT_ABIS.contains("x86") -> "386"
                    else -> "arm"
                }
            }
            else -> "arm64" // Default fallback
        }
    }
    
    private suspend fun downloadFile(url: String): File? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val zipFile = File(agentDir, "agent.zip")
                val inputStream = connection.inputStream
                val outputStream = FileOutputStream(zipFile)
                
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                
                zipFile
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun extractAgent(zipFile: File) {
        ZipInputStream(FileInputStream(zipFile)).use { zipIn ->
            var entry = zipIn.nextEntry
            
            while (entry != null) {
                if (!entry.isDirectory && entry.name.endsWith(AGENT_FILENAME)) {
                    FileOutputStream(agentFile).use { output ->
                        zipIn.copyTo(output)
                    }
                    break
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }
    }
    
    private fun makeExecutable() {
        try {
            agentFile.setExecutable(true, true)
            agentFile.setReadable(true, true)
            agentFile.setWritable(true, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
