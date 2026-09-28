package com.fluxboard.app.core.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FluxDarkColorScheme = darkColorScheme(
    primary = FluxColors.Primary,
    onPrimary = FluxColors.TextPrimary,
    secondary = FluxColors.Accent,
    onSecondary = FluxColors.Background,
    tertiary = FluxColors.Success,
    onTertiary = FluxColors.Background,
    background = FluxColors.Background,
    onBackground = FluxColors.TextPrimary,
    surface = FluxColors.Surface,
    onSurface = FluxColors.TextPrimary,
    surfaceVariant = FluxColors.BackgroundAlt,
    onSurfaceVariant = FluxColors.TextSecondary,
    outline = FluxColors.TextSecondary,
    error = Color(0xFFEF4444)
)

private val BaseTypography = Typography()

private val FluxTypography = BaseTypography.copy(
    displayLarge = BaseTypography.displayLarge.copy(fontFamily = InterFontFamily),
    displayMedium = BaseTypography.displayMedium.copy(fontFamily = InterFontFamily),
    displaySmall = BaseTypography.displaySmall.copy(fontFamily = InterFontFamily),
    headlineLarge = BaseTypography.headlineLarge.copy(fontFamily = InterFontFamily),
    headlineMedium = BaseTypography.headlineMedium.copy(fontFamily = InterFontFamily),
    headlineSmall = BaseTypography.headlineSmall.copy(fontFamily = InterFontFamily),
    titleLarge = BaseTypography.titleLarge.copy(fontFamily = InterFontFamily),
    titleMedium = BaseTypography.titleMedium.copy(fontFamily = InterFontFamily),
    titleSmall = BaseTypography.titleSmall.copy(fontFamily = InterFontFamily),
    bodyLarge = BaseTypography.bodyLarge.copy(fontFamily = InterFontFamily),
    bodyMedium = BaseTypography.bodyMedium.copy(fontFamily = InterFontFamily),
    bodySmall = BaseTypography.bodySmall.copy(fontFamily = InterFontFamily),
    labelLarge = BaseTypography.labelLarge.copy(fontFamily = InterFontFamily),
    labelMedium = BaseTypography.labelMedium.copy(fontFamily = InterFontFamily),
    labelSmall = BaseTypography.labelSmall.copy(fontFamily = InterFontFamily)
)

/**
 * Tema global de FluxBoard: fondo #121212, tipografía Inter y acentos de marca.
 */
@Composable
fun FluxBoardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FluxDarkColorScheme,
        typography = FluxTypography,
        content = content
    )
}
