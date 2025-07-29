package now.link.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Utility class for handling Material You dynamic colors
 */
object DynamicColorUtils {

    /**
     * Check if dynamic colors are supported on this device
     */
    fun isDynamicColorSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    /**
     * Apply dynamic colors to the app theme if supported
     * This method can be called from Application.onCreate() or Activity.onCreate()
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun applyDynamicColors(context: Context) {
        if (!isDynamicColorSupported()) return

        try {
            // Dynamic colors are automatically applied when using system color resources
            // in values-v31/ resource directories

            // Note: For full dynamic color support, you could also use:
            // DynamicColors.applyToActivitiesIfAvailable(application)
            // But that requires adding the Material Components dependency

        } catch (e: Exception) {
            // Fallback to static colors if dynamic colors fail
            e.printStackTrace()
        }
    }

    /**
     * Get a description of the current color scheme
     */
    fun getColorSchemeDescription(context: Context): String {
        return if (isDynamicColorSupported()) {
            "Material You dynamic colors (adapts to wallpaper)"
        } else {
            "Static blue Material Design colors"
        }
    }
}
