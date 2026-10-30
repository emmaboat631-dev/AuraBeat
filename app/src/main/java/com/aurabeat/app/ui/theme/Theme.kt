package com.aurabeat.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AuraColorScheme = darkColorScheme(
    primary            = AuraPrimary,
    onPrimary          = AuraTextPrimary,
    primaryContainer   = AuraPrimaryLight,
    onPrimaryContainer = AuraTextPrimary,
    secondary          = AuraAccent,
    onSecondary        = AuraTextPrimary,
    background         = AuraBackground,
    onBackground       = AuraTextPrimary,
    surface            = AuraSurface,
    onSurface          = AuraTextPrimary,
    surfaceVariant     = AuraSurfaceElev,
    onSurfaceVariant   = AuraTextSecondary,
    outline            = AuraDivider,
)

@Composable
fun AuraBeatTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AuraColorScheme,
        typography  = AuraTypography,
        shapes      = AuraShapes,
        content     = content,
    )
}
