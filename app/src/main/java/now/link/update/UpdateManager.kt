package now.link.update

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import now.link.BuildConfig
import now.link.utils.LogManager
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(private val context: Context) {
    
    companion object {
        private const val TAG = "UpdateManager"
        private const val GITHUB_API_URL = "https://api.github.com/repos/nichbar/Ulmaridae/releases/latest"
        private const val PREFS_NAME = "update_preferences"
        private const val KEY_LAST_IGNORED_VERSION = "last_ignored_version"
        private const val KEY_LAST_CHECK_TIME = "last_check_time"
        private const val KEY_CHECK_INTERVAL = 24 * 60 * 60 * 1000L // 24 hours
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { 
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    /**
     * Check if an update check should be performed
     */
    fun shouldCheckForUpdates(): Boolean {
        val lastCheckTime = prefs.getLong(KEY_LAST_CHECK_TIME, 0)
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastCheckTime) > KEY_CHECK_INTERVAL
    }
    
    /**
     * Check for app updates from GitHub releases
     */
    suspend fun checkForUpdates(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            LogManager.d(TAG, "Checking for updates...")
            
            val url = URL(GITHUB_API_URL)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "Ulmaridae/${BuildConfig.VERSION_NAME}")
                connectTimeout = 10000
                readTimeout = 10000
            }
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val release = json.decodeFromString<GitHubRelease>(response)
                
                // Update last check time
                prefs.edit().putLong(KEY_LAST_CHECK_TIME, System.currentTimeMillis()).apply()
                
                return@withContext parseReleaseInfo(release)
            } else {
                LogManager.w(TAG, "Failed to check for updates: HTTP $responseCode")
                return@withContext null
            }
            
        } catch (e: IOException) {
            LogManager.e(TAG, "Network error checking for updates", e)
            return@withContext null
        } catch (e: Exception) {
            LogManager.e(TAG, "Error checking for updates", e)
            return@withContext null
        }
    }
    
    /**
     * Parse GitHub release information into UpdateInfo
     */
    private fun parseReleaseInfo(release: GitHubRelease): UpdateInfo? {
        try {
            // Skip draft and prerelease versions
            if (release.draft || release.prerelease) {
                LogManager.d(TAG, "Skipping draft/prerelease version: ${release.tag_name}")
                return null
            }
            
            // Extract version code from tag (assuming format like "v1.0.0" or "1.0.0")
            val versionTag = release.tag_name.removePrefix("v")
            val versionParts = versionTag.split(".")
            
            if (versionParts.size < 2) {
                LogManager.w(TAG, "Invalid version format: ${release.tag_name}")
                return null
            }
            
            // Calculate version code from version string (e.g., "1.2.3" -> 10203)
            val versionCode = try {
                val major = versionParts[0].toInt()
                val minor = versionParts.getOrNull(1)?.toInt() ?: 0
                val patch = versionParts.getOrNull(2)?.toInt() ?: 0
                major * 10000 + minor * 100 + patch
            } catch (e: NumberFormatException) {
                LogManager.w(TAG, "Could not parse version code from: $versionTag")
                return null
            }
            
            // Check if this is a newer version
            if (versionCode <= BuildConfig.VERSION_CODE) {
                LogManager.d(TAG, "No newer version available. Current: ${BuildConfig.VERSION_CODE}, Latest: $versionCode")
                return null
            }
            
            // Check if user has already ignored this version
            val lastIgnoredVersion = prefs.getString(KEY_LAST_IGNORED_VERSION, "")
            if (lastIgnoredVersion == versionTag) {
                LogManager.d(TAG, "User has ignored version: $versionTag")
                return null
            }
            
            return UpdateInfo(
                version = versionTag,
                versionCode = versionCode,
                downloadUrl = release.html_url,
                releaseNotes = release.body,
                publishedAt = release.published_at
            )
            
        } catch (e: Exception) {
            LogManager.e(TAG, "Error parsing release info", e)
            return null
        }
    }
    
    /**
     * Mark a version as ignored by the user
     */
    fun ignoreVersion(version: String) {
        LogManager.d(TAG, "Ignoring version: $version")
        prefs.edit().putString(KEY_LAST_IGNORED_VERSION, version).apply()
    }
    
    /**
     * Clear the ignored version (for testing or reset)
     */
    fun clearIgnoredVersion() {
        prefs.edit().remove(KEY_LAST_IGNORED_VERSION).apply()
    }
    
    /**
     * Force an update check regardless of timing
     */
    suspend fun forceCheckForUpdates(): UpdateInfo? {
        // Reset last check time to force check
        prefs.edit().putLong(KEY_LAST_CHECK_TIME, 0).apply()
        return checkForUpdates()
    }
}
