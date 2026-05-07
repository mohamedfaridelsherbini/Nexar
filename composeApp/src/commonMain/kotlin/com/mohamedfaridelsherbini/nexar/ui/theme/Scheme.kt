package com.mohamedfaridelsherbini.nexar.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class NexarExtraColors(
    val warning: Color = Color.Unspecified,
    val success: Color = Color.Unspecified,
    val borderPrimary: Color = Color.Unspecified,
    val borderSubtle: Color = Color.Unspecified,
    val foregroundSecondary: Color = Color.Unspecified,
    val foregroundMuted: Color = Color.Unspecified
)

val LocalNexarExtraColors = staticCompositionLocalOf { NexarExtraColors() }

object NexarExtraTheme {
    val colors: NexarExtraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalNexarExtraColors.current
}

val NexarLightColorScheme = lightColorScheme(
    primary = NexarAccentPrimary,
    onPrimary = NexarSurfaceSecondary,
    primaryContainer = NexarAccentPrimary,
    onPrimaryContainer = NexarSurfaceSecondary,
    secondary = NexarForegroundSecondary,
    onSecondary = NexarSurfaceSecondary,
    background = NexarSurfacePrimary,
    onBackground = NexarForegroundPrimary,
    surface = NexarSurfacePrimary,
    onSurface = NexarForegroundPrimary,
    surfaceVariant = NexarSurfaceSecondary,
    onSurfaceVariant = NexarForegroundSecondary,
    outline = NexarBorderPrimary,
    outlineVariant = NexarBorderSubtle,
    error = NexarError,
    onError = NexarSurfaceSecondary
)

val NexarLightExtraColors = NexarExtraColors(
    warning = NexarWarning,
    success = NexarSuccess,
    borderPrimary = NexarBorderPrimary,
    borderSubtle = NexarBorderSubtle,
    foregroundSecondary = NexarForegroundSecondary,
    foregroundMuted = NexarForegroundMuted
)

val NexarDarkColorScheme = darkColorScheme(
    primary = NexarDarkAccentPrimary,
    onPrimary = NexarDarkSurfacePrimary,
    primaryContainer = NexarDarkAccentPrimary,
    onPrimaryContainer = NexarDarkSurfacePrimary,
    secondary = NexarDarkForegroundSecondary,
    onSecondary = NexarDarkSurfacePrimary,
    background = NexarDarkSurfacePrimary,
    onBackground = NexarDarkForegroundPrimary,
    surface = NexarDarkSurfacePrimary,
    onSurface = NexarDarkForegroundPrimary,
    surfaceVariant = NexarDarkSurfaceSecondary,
    onSurfaceVariant = NexarDarkForegroundSecondary,
    outline = NexarDarkBorderPrimary,
    outlineVariant = NexarDarkBorderSubtle,
    error = NexarDarkError,
    onError = NexarDarkSurfacePrimary
)

val NexarDarkExtraColors = NexarExtraColors(
    warning = NexarDarkWarning,
    success = NexarDarkSuccess,
    borderPrimary = NexarDarkBorderPrimary,
    borderSubtle = NexarDarkBorderSubtle,
    foregroundSecondary = NexarDarkForegroundSecondary,
    foregroundMuted = NexarDarkForegroundMuted
)
