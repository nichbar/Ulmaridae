package now.link.utils

import android.content.Context
import android.os.Build

class AgentManager(private val context: Context) {

    fun getAgentPath(): String {
        return context.applicationInfo.nativeLibraryDir + "/libnezha-agent" + ".so"
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

//    private fun makeExecutable() {
//        try {
//            agentFile.setExecutable(true, true)
//            agentFile.setReadable(true, true)
//            agentFile.setWritable(true, true)
//        } catch (e: Exception) {
//            LogManager.e(TAG, "Failed to make agent executable", e)
//            e.printStackTrace()
//        }
//    }

    companion object {
        private const val TAG = "AgentManager"
    }
}
