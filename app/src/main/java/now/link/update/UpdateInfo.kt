package now.link.update

import kotlinx.serialization.Serializable

/**
 * Data class representing update information
 */
@Serializable
data class UpdateInfo(
    val version: String,
    val versionCode: Int,
    val downloadUrl: String,
    val releaseNotes: String,
    val isRequired: Boolean = false,
    val publishedAt: String
)

/**
 * Data class for GitHub release API response
 */
@Serializable
data class GitHubRelease(
    val tag_name: String,
    val name: String,
    val body: String,
    val html_url: String,
    val published_at: String,
    val prerelease: Boolean,
    val draft: Boolean,
    val assets: List<GitHubAsset> = emptyList()
)

/**
 * Data class for GitHub release assets
 */
@Serializable
data class GitHubAsset(
    val name: String,
    val download_count: Int,
    val browser_download_url: String
)
