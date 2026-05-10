package com.mohamedfaridelsherbini.nexar.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
actual fun NexarTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) NexarDarkColorScheme else NexarLightColorScheme
    val extraColors = if (darkTheme) NexarDarkExtraColors else NexarLightExtraColors

    CompositionLocalProvider(LocalNexarExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = NexarTypography,
            content = content,
        )
    }
}
