package com.example.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = androidx.compose.material3.lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer
)

private val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer
)

private val AmoledColorScheme = androidx.compose.material3.darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF93C5FD), // Light Blue
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF1E293B),
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondary = androidx.compose.ui.graphics.Color(0xFF38BDF8),
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF0F172A),
    onSecondaryContainer = androidx.compose.ui.graphics.Color.White,
    tertiary = androidx.compose.ui.graphics.Color(0xFF34D399),
    onTertiary = androidx.compose.ui.graphics.Color.Black,
    background = androidx.compose.ui.graphics.Color.Black,
    surface = androidx.compose.ui.graphics.Color(0xFF0A0A0A),
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF121212),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF94A3B8),
    outline = androidx.compose.ui.graphics.Color(0xFF222222)
)

private val PurpleColorScheme = androidx.compose.material3.darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFC084FC), // Pastel Purple
    onPrimary = androidx.compose.ui.graphics.Color(0xFF3B0764),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF5B21B6),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFF3E8FF),
    secondary = androidx.compose.ui.graphics.Color(0xFFF472B6), // Pink
    onSecondary = androidx.compose.ui.graphics.Color(0xFF500724),
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF1E1B4B),
    onSecondaryContainer = androidx.compose.ui.graphics.Color(0xFFFDF2F8),
    tertiary = androidx.compose.ui.graphics.Color(0xFF38BDF8),
    background = androidx.compose.ui.graphics.Color(0xFF0F0A1A), // Deep Purple Background
    surface = androidx.compose.ui.graphics.Color(0xFF1E152A), // Dark Purple Surface
    onBackground = androidx.compose.ui.graphics.Color(0xFFF5F3FF),
    onSurface = androidx.compose.ui.graphics.Color(0xFFF5F3FF),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF2E2240),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFD8B4FE),
    outline = androidx.compose.ui.graphics.Color(0xFF4C1D95)
)

private val BlueColorScheme = androidx.compose.material3.darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF60A5FA),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF1E3A8A),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF1D4ED8),
    secondary = androidx.compose.ui.graphics.Color(0xFF38BDF8),
    background = androidx.compose.ui.graphics.Color(0xFF0B132B),
    surface = androidx.compose.ui.graphics.Color(0xFF1C2541),
    onBackground = androidx.compose.ui.graphics.Color(0xFFE2E8F0),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE2E8F0),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF3A506B),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF94A3B8),
    outline = androidx.compose.ui.graphics.Color(0xFF415A77)
)

private val GreenColorScheme = androidx.compose.material3.darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF34D399),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF064E3B),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF047857),
    secondary = androidx.compose.ui.graphics.Color(0xFF6EE7B7),
    background = androidx.compose.ui.graphics.Color(0xFF06100E),
    surface = androidx.compose.ui.graphics.Color(0xFF0F201C),
    onBackground = androidx.compose.ui.graphics.Color(0xFFECFDF5),
    onSurface = androidx.compose.ui.graphics.Color(0xFFECFDF5),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF1F3A34),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFA7F3D0),
    outline = androidx.compose.ui.graphics.Color(0xFF10B981)
)

private val OrangeColorScheme = androidx.compose.material3.darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFFDBA74), // Orange Pastel
    onPrimary = androidx.compose.ui.graphics.Color(0xFF7C2D12),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFC2410C),
    secondary = androidx.compose.ui.graphics.Color(0xFFF97316),
    background = androidx.compose.ui.graphics.Color(0xFF18100C),
    surface = androidx.compose.ui.graphics.Color(0xFF2A1B14),
    onBackground = androidx.compose.ui.graphics.Color(0xFFFFF7ED),
    onSurface = androidx.compose.ui.graphics.Color(0xFFFFF7ED),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF3E281C),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFFFEDD5),
    outline = androidx.compose.ui.graphics.Color(0xFFEA580C)
)

@Composable
fun SmartSchedulerTheme(
    themeMode: String = "system", // "light", "dark", "system", "amoled", "purple", "blue", "green", "orange"
    dynamicColorEnabled: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "dark", "amoled", "purple", "blue", "green", "orange" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColorEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeMode == "amoled" -> AmoledColorScheme
        themeMode == "purple" -> PurpleColorScheme
        themeMode == "blue" -> BlueColorScheme
        themeMode == "green" -> GreenColorScheme
        themeMode == "orange" -> OrangeColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // --- NOTE: These need to be imported or refactored to be in the same package ---
    // Assuming they are in the same directory (core/designsystem/theme/), 
    // imports might be needed if they were in different files or packages.
    // For now, I will assume they are in the same package.
    
    val customGradients = if (darkTheme) {
        CustomGradients(
            primary = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(
                if (themeMode == "purple") androidx.compose.ui.graphics.Color(0xFF8B5CF6) else if (themeMode == "orange") androidx.compose.ui.graphics.Color(0xFFF97316) else DarkPrimary,
                if (themeMode == "purple") androidx.compose.ui.graphics.Color(0xFFD946EF) else if (themeMode == "orange") androidx.compose.ui.graphics.Color(0xFFF59E0B) else DarkSecondary
            )),
            secondary = androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(DarkSecondary, DarkTertiary)),
            cardBackground = androidx.compose.ui.graphics.Brush.verticalGradient(listOf(
                if (themeMode == "amoled") androidx.compose.ui.graphics.Color.Black else DarkSurface,
                if (themeMode == "amoled") androidx.compose.ui.graphics.Color.Black else DarkBackground
            ))
        )
    } else {
        CustomGradients()
    }

    val semanticColors = if (darkTheme) {
        SemanticColors(
            success = DarkTertiary,
            completed = DarkTertiary,
            active = DarkTertiary
        )
    } else {
        SemanticColors()
    }

    CompositionLocalProvider(
        LocalDimens provides Dimens,
        LocalSpacing provides Spacing,
        LocalCustomGradients provides customGradients,
        LocalSemanticColors provides semanticColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
