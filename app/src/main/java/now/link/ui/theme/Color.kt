package now.link.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import now.link.utils.ThemeManager

// Material 3 Color Scheme (Grey Theme) - Updated for grey primary
private val md_theme_light_primary = Color(0xFF5F6368)
private val md_theme_light_onPrimary = Color(0xFFFFFFFF)
private val md_theme_light_primaryContainer = Color(0xFFE3E4E6)
private val md_theme_light_onPrimaryContainer = Color(0xFF1C1D1F)
private val md_theme_light_secondary = Color(0xFF5F6368)
private val md_theme_light_onSecondary = Color(0xFFFFFFFF)
private val md_theme_light_secondaryContainer = Color(0xFFE4E5E7)
private val md_theme_light_onSecondaryContainer = Color(0xFF1D1E20)
private val md_theme_light_tertiary = Color(0xFF6B6B74)
private val md_theme_light_onTertiary = Color(0xFFFFFFFF)
private val md_theme_light_tertiaryContainer = Color(0xFFE8E8F0)
private val md_theme_light_onTertiaryContainer = Color(0xFF25252E)
private val md_theme_light_error = Color(0xFFBA1A1A)
private val md_theme_light_errorContainer = Color(0xFFFFDAD6)
private val md_theme_light_onError = Color(0xFFFFFFFF)
private val md_theme_light_onErrorContainer = Color(0xFF410002)
private val md_theme_light_background = Color(0xFFFCFCFC)
private val md_theme_light_onBackground = Color(0xFF1A1A1A)
private val md_theme_light_surface = Color(0xFFFCFCFC)
private val md_theme_light_onSurface = Color(0xFF1A1A1A)
private val md_theme_light_surfaceVariant = Color(0xFFE1E2E4)
private val md_theme_light_onSurfaceVariant = Color(0xFF44474A)
private val md_theme_light_outline = Color(0xFF757780)
private val md_theme_light_inverseOnSurface = Color(0xFFF2F2F2)
private val md_theme_light_inverseSurface = Color(0xFF2F2F2F)
private val md_theme_light_inversePrimary = Color(0xFFB8BCC2)
private val md_theme_light_surfaceTint = Color(0xFF5F6368)
private val md_theme_light_outlineVariant = Color(0xFFC5C6CA)
private val md_theme_light_scrim = Color(0xFF000000)

private val md_theme_dark_primary = Color(0xFFB8BCC2)
private val md_theme_dark_onPrimary = Color(0xFF2F3032)
private val md_theme_dark_primaryContainer = Color(0xFF46494C)
private val md_theme_dark_onPrimaryContainer = Color(0xFFE3E4E6)
private val md_theme_dark_secondary = Color(0xFFBCC0C4)
private val md_theme_dark_onSecondary = Color(0xFF303236)
private val md_theme_dark_secondaryContainer = Color(0xFF47494D)
private val md_theme_dark_onSecondaryContainer = Color(0xFFE4E5E7)
private val md_theme_dark_tertiary = Color(0xFFCACBD3)
private val md_theme_dark_onTertiary = Color(0xFF3B3B44)
private val md_theme_dark_tertiaryContainer = Color(0xFF525259)
private val md_theme_dark_onTertiaryContainer = Color(0xFFE8E8F0)
private val md_theme_dark_error = Color(0xFFFFB4AB)
private val md_theme_dark_errorContainer = Color(0xFF93000A)
private val md_theme_dark_onError = Color(0xFF690005)
private val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
private val md_theme_dark_background = Color(0xFF111111)
private val md_theme_dark_onBackground = Color(0xFFE4E4E4)
private val md_theme_dark_surface = Color(0xFF111111)
private val md_theme_dark_onSurface = Color(0xFFE4E4E4)
private val md_theme_dark_surfaceVariant = Color(0xFF44474A)
private val md_theme_dark_onSurfaceVariant = Color(0xFFC5C6CA)
private val md_theme_dark_outline = Color(0xFF8F9195)
private val md_theme_dark_inverseOnSurface = Color(0xFF111111)
private val md_theme_dark_inverseSurface = Color(0xFFE4E4E4)
private val md_theme_dark_inversePrimary = Color(0xFF5F6368)
private val md_theme_dark_surfaceTint = Color(0xFFB8BCC2)
private val md_theme_dark_outlineVariant = Color(0xFF44474A)
private val md_theme_dark_scrim = Color(0xFF000000)

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)

private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)

@Composable
fun UlmaridaeTheme(
    content: @Composable () -> Unit
) {
    // Get theme preferences (these are now observable states)
    val dynamicColorEnabled = ThemeManager.isDynamicColorEnabled
    val followSystemTheme = ThemeManager.isFollowSystemTheme
    val darkModeEnabled = ThemeManager.isDarkModeEnabled
    
    // Determine dark theme state
    val darkTheme = if (followSystemTheme) {
        androidx.compose.foundation.isSystemInDarkTheme()
    } else {
        darkModeEnabled
    }
    
    val colorScheme = when {
        // If dynamic color is enabled and supported on Android 12+
        dynamicColorEnabled && ThemeManager.isDynamicColorSupported() -> {
            val context = LocalContext.current
            // Use the dynamic scheme based on light/dark mode
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Otherwise, use the predefined fallback scheme
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
