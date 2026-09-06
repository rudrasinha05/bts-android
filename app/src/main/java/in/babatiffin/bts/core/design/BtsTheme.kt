package com.babatiffin.bts.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightScheme = lightColorScheme(
    primary = BtsLightColors.Primary,
    onPrimary = BtsLightColors.PrimaryForeground,
    secondary = BtsLightColors.Secondary,
    onSecondary = BtsLightColors.SecondaryForeground,
    tertiary = BtsLightColors.Accent,
    onTertiary = BtsLightColors.AccentForeground,
    background = BtsLightColors.Background,
    onBackground = BtsLightColors.Foreground,
    surface = BtsLightColors.Card,
    onSurface = BtsLightColors.CardForeground,
    error = BtsLightColors.Destructive,
    outline = BtsLightColors.Border,
)

private val DarkScheme = darkColorScheme(
    primary = BtsDarkColors.Primary,
    onPrimary = BtsDarkColors.PrimaryForeground,
    secondary = BtsDarkColors.Secondary,
    onSecondary = BtsDarkColors.SecondaryForeground,
    tertiary = BtsDarkColors.Accent,
    onTertiary = BtsDarkColors.AccentForeground,
    background = BtsDarkColors.Background,
    onBackground = BtsDarkColors.Foreground,
    surface = BtsDarkColors.Card,
    onSurface = BtsDarkColors.CardForeground,
    error = BtsDarkColors.Destructive,
    outline = BtsDarkColors.Border,
)

@Composable
fun BtsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        content = content,
    )
}
