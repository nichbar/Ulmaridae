package now.link.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import now.link.utils.ThemeManager

// Neutral slate palette (matching markread / Mastigias)
val PrimaryLight = Color(0xFF5F6368)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFE3E4E6)
val OnPrimaryContainerLight = Color(0xFF1C1D1F)

val SecondaryLight = Color(0xFF5F6368)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFE4E5E7)
val OnSecondaryContainerLight = Color(0xFF1D1E20)

val TertiaryLight = Color(0xFF6B6B74)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFE8E8F0)
val OnTertiaryContainerLight = Color(0xFF25252E)

val BackgroundLight = Color(0xFFFCFCFC)
val OnBackgroundLight = Color(0xFF1A1A1A)
val SurfaceLight = Color(0xFFFCFCFC)
val OnSurfaceLight = Color(0xFF1A1A1A)
val SurfaceVariantLight = Color(0xFFE1E2E4)
val OnSurfaceVariantLight = Color(0xFF44474A)
val OutlineLight = Color(0xFF757780)
val OutlineVariantLight = Color(0xFFC5C6CA)
val ScrimLight = Color(0xFF000000)

val InverseSurfaceLight = Color(0xFF2F2F2F)
val InverseOnSurfaceLight = Color(0xFFF2F2F2)
val InversePrimaryLight = Color(0xFFB8BCC2)
val SurfaceTintLight = Color(0xFF5F6368)

val SurfaceDimLight = Color(0xFFD8D9DB)
val SurfaceBrightLight = Color(0xFFFCFCFC)
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFF6F6F8)
val SurfaceContainerLight = Color(0xFFF0F1F3)
val SurfaceContainerHighLight = Color(0xFFEAEBEE)
val SurfaceContainerHighestLight = Color(0xFFE1E2E4)

val PrimaryFixedLight = Color(0xFFE3E4E6)
val OnPrimaryFixedLight = Color(0xFF1C1D1F)
val PrimaryFixedDimLight = Color(0xFFB8BCC2)
val OnPrimaryFixedVariantLight = Color(0xFF5F6368)

val SecondaryFixedLight = Color(0xFFE4E5E7)
val OnSecondaryFixedLight = Color(0xFF1D1E20)
val SecondaryFixedDimLight = Color(0xFFBCC0C4)
val OnSecondaryFixedVariantLight = Color(0xFF5F6368)

val TertiaryFixedLight = Color(0xFFE8E8F0)
val OnTertiaryFixedLight = Color(0xFF25252E)
val TertiaryFixedDimLight = Color(0xFFCACBD3)
val OnTertiaryFixedVariantLight = Color(0xFF6B6B74)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

// Dark palette
val PrimaryDark = Color(0xFFB8BCC2)
val OnPrimaryDark = Color(0xFF2F3032)
val PrimaryContainerDark = Color(0xFF46494C)
val OnPrimaryContainerDark = Color(0xFFE3E4E6)

val SecondaryDark = Color(0xFFBCC0C4)
val OnSecondaryDark = Color(0xFF303236)
val SecondaryContainerDark = Color(0xFF47494D)
val OnSecondaryContainerDark = Color(0xFFE4E5E7)

val TertiaryDark = Color(0xFFCACBD3)
val OnTertiaryDark = Color(0xFF3B3B44)
val TertiaryContainerDark = Color(0xFF525259)
val OnTertiaryContainerDark = Color(0xFFE8E8F0)

val BackgroundDark = Color(0xFF1C2228)
val OnBackgroundDark = Color(0xFFE4E4E4)
val SurfaceDark = Color(0xFF1C2228)
val OnSurfaceDark = Color(0xFFE4E4E4)
val SurfaceVariantDark = Color(0xFF44474A)
val OnSurfaceVariantDark = Color(0xFFC5C6CA)
val OutlineDark = Color(0xFF8F9195)
val OutlineVariantDark = Color(0xFF44474A)
val ScrimDark = Color(0xFF000000)

val InverseSurfaceDark = Color(0xFFE4E4E4)
val InverseOnSurfaceDark = Color(0xFF1C2228)
val InversePrimaryDark = Color(0xFF5F6368)
val SurfaceTintDark = Color(0xFFB8BCC2)

val SurfaceDimDark = Color(0xFF14181D)
val SurfaceBrightDark = Color(0xFF32383F)
val SurfaceContainerLowestDark = Color(0xFF0F1216)
val SurfaceContainerLowDark = Color(0xFF181E23)
val SurfaceContainerDark = Color(0xFF1C2228)
val SurfaceContainerHighDark = Color(0xFF262D34)
val SurfaceContainerHighestDark = Color(0xFF44474A)

val PrimaryFixedDark = Color(0xFFE3E4E6)
val OnPrimaryFixedDark = Color(0xFF1C1D1F)
val PrimaryFixedDimDark = Color(0xFFB8BCC2)
val OnPrimaryFixedVariantDark = Color(0xFF5F6368)

val SecondaryFixedDark = Color(0xFFE4E5E7)
val OnSecondaryFixedDark = Color(0xFF1D1E20)
val SecondaryFixedDimDark = Color(0xFFBCC0C4)
val OnSecondaryFixedVariantDark = Color(0xFF5F6368)

val TertiaryFixedDark = Color(0xFFE8E8F0)
val OnTertiaryFixedDark = Color(0xFF25252E)
val TertiaryFixedDimDark = Color(0xFFCACBD3)
val OnTertiaryFixedVariantDark = Color(0xFF6B6B74)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    scrim = ScrimLight,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = InversePrimaryLight,
    surfaceTint = SurfaceTintLight,
    surfaceDim = SurfaceDimLight,
    surfaceBright = SurfaceBrightLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    scrim = ScrimDark,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    inversePrimary = InversePrimaryDark,
    surfaceTint = SurfaceTintDark,
    surfaceDim = SurfaceDimDark,
    surfaceBright = SurfaceBrightDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

// Backward compatibility aliases
val LightColors = LightColorScheme
val DarkColors = DarkColorScheme

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
        isSystemInDarkTheme()
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
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
