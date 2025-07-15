package now.link.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object RootUtils {
    
    private const val TAG = "RootUtils"
    
    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("su -c 'id'")
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            Log.d(TAG, "Root check failed: ${e.message}")
            false
        }
    }
    
    suspend fun executeRootCommand(command: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("su -c '$command'")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            
            val exitCode = process.waitFor()
            val success = exitCode == 0
            
            Pair(success, output.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Root command execution failed", e)
            Pair(false, e.message ?: "Unknown error")
        }
    }
    
    suspend fun executeCommand(command: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(command)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val output = StringBuilder()
            val error = StringBuilder()
            
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            
            while (errorReader.readLine().also { line = it } != null) {
                error.append(line).append("\n")
            }
            
            val exitCode = process.waitFor()
            val success = exitCode == 0
            val result = if (output.isNotEmpty()) output.toString() else error.toString()
            
            Pair(success, result)
        } catch (e: Exception) {
            Log.e(TAG, "Command execution failed", e)
            Pair(false, e.message ?: "Unknown error")
        }
    }
}
