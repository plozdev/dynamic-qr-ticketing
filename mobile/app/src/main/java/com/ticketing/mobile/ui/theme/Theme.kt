package com.ticketing.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ObsidianDarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = OnEmerald,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldPrimaryFixed,
    secondary = CyanSecondary,
    onSecondary = ObsidianVoid,
    secondaryContainer = CyanContainer,
    onSecondaryContainer = CyanSecondaryFixed,
    tertiary = AmberTertiary,
    onTertiary = ObsidianVoid,
    tertiaryContainer = AmberContainer,
    error = FieryError,
    onError = TextHighEmphasis,
    errorContainer = ErrorContainer,
    background = ObsidianVoid,
    onBackground = TextHighEmphasis,
    surface = ObsidianVoid,
    onSurface = TextHighEmphasis,
    surfaceVariant = SurfaceContainerHigh,
    onSurfaceVariant = TextMediumEmphasis,
    outline = OutlineBorder
)

@Composable
fun DynamicQRTicketingTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ObsidianDarkColorScheme,
        typography = Typography,
        content = content
    )
}