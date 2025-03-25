package com.movtery.zalithlauncher.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.movtery.zalithlauncher.state.LocalColorThemeState

private val rosewoodEmberLight = lightColorScheme(
    primary = primaryLight.rosewoodEmber,
    onPrimary = onPrimaryLight.rosewoodEmber,
    primaryContainer = primaryContainerLight.rosewoodEmber,
    onPrimaryContainer = onPrimaryContainerLight.rosewoodEmber,
    secondary = secondaryLight.rosewoodEmber,
    onSecondary = onSecondaryLight.rosewoodEmber,
    secondaryContainer = secondaryContainerLight.rosewoodEmber,
    onSecondaryContainer = onSecondaryContainerLight.rosewoodEmber,
    tertiary = tertiaryLight.rosewoodEmber,
    onTertiary = onTertiaryLight.rosewoodEmber,
    tertiaryContainer = tertiaryContainerLight.rosewoodEmber,
    onTertiaryContainer = onTertiaryContainerLight.rosewoodEmber,
    error = errorLight.rosewoodEmber,
    onError = onErrorLight.rosewoodEmber,
    errorContainer = errorContainerLight.rosewoodEmber,
    onErrorContainer = onErrorContainerLight.rosewoodEmber,
    background = backgroundLight.rosewoodEmber,
    onBackground = onBackgroundLight.rosewoodEmber,
    surface = surfaceLight.rosewoodEmber,
    onSurface = onSurfaceLight.rosewoodEmber,
    surfaceVariant = surfaceVariantLight.rosewoodEmber,
    onSurfaceVariant = onSurfaceVariantLight.rosewoodEmber,
    outline = outlineLight.rosewoodEmber,
    outlineVariant = outlineVariantLight.rosewoodEmber,
    scrim = scrimLight.rosewoodEmber,
    inverseSurface = inverseSurfaceLight.rosewoodEmber,
    inverseOnSurface = inverseOnSurfaceLight.rosewoodEmber,
    inversePrimary = inversePrimaryLight.rosewoodEmber,
    surfaceDim = surfaceDimLight.rosewoodEmber,
    surfaceBright = surfaceBrightLight.rosewoodEmber,
    surfaceContainerLowest = surfaceContainerLowestLight.rosewoodEmber,
    surfaceContainerLow = surfaceContainerLowLight.rosewoodEmber,
    surfaceContainer = surfaceContainerLight.rosewoodEmber,
    surfaceContainerHigh = surfaceContainerHighLight.rosewoodEmber,
    surfaceContainerHighest = surfaceContainerHighestLight.rosewoodEmber,
)

private val rosewoodEmberDark = darkColorScheme(
    primary = primaryDark.rosewoodEmber,
    onPrimary = onPrimaryDark.rosewoodEmber,
    primaryContainer = primaryContainerDark.rosewoodEmber,
    onPrimaryContainer = onPrimaryContainerDark.rosewoodEmber,
    secondary = secondaryDark.rosewoodEmber,
    onSecondary = onSecondaryDark.rosewoodEmber,
    secondaryContainer = secondaryContainerDark.rosewoodEmber,
    onSecondaryContainer = onSecondaryContainerDark.rosewoodEmber,
    tertiary = tertiaryDark.rosewoodEmber,
    onTertiary = onTertiaryDark.rosewoodEmber,
    tertiaryContainer = tertiaryContainerDark.rosewoodEmber,
    onTertiaryContainer = onTertiaryContainerDark.rosewoodEmber,
    error = errorDark.rosewoodEmber,
    onError = onErrorDark.rosewoodEmber,
    errorContainer = errorContainerDark.rosewoodEmber,
    onErrorContainer = onErrorContainerDark.rosewoodEmber,
    background = backgroundDark.rosewoodEmber,
    onBackground = onBackgroundDark.rosewoodEmber,
    surface = surfaceDark.rosewoodEmber,
    onSurface = onSurfaceDark.rosewoodEmber,
    surfaceVariant = surfaceVariantDark.rosewoodEmber,
    onSurfaceVariant = onSurfaceVariantDark.rosewoodEmber,
    outline = outlineDark.rosewoodEmber,
    outlineVariant = outlineVariantDark.rosewoodEmber,
    scrim = scrimDark.rosewoodEmber,
    inverseSurface = inverseSurfaceDark.rosewoodEmber,
    inverseOnSurface = inverseOnSurfaceDark.rosewoodEmber,
    inversePrimary = inversePrimaryDark.rosewoodEmber,
    surfaceDim = surfaceDimDark.rosewoodEmber,
    surfaceBright = surfaceBrightDark.rosewoodEmber,
    surfaceContainerLowest = surfaceContainerLowestDark.rosewoodEmber,
    surfaceContainerLow = surfaceContainerLowDark.rosewoodEmber,
    surfaceContainer = surfaceContainerDark.rosewoodEmber,
    surfaceContainerHigh = surfaceContainerHighDark.rosewoodEmber,
    surfaceContainerHighest = surfaceContainerHighestDark.rosewoodEmber,
)

private val velvetRoseLight = lightColorScheme(
    primary = primaryLight.velvetRose,
    onPrimary = onPrimaryLight.velvetRose,
    primaryContainer = primaryContainerLight.velvetRose,
    onPrimaryContainer = onPrimaryContainerLight.velvetRose,
    secondary = secondaryLight.velvetRose,
    onSecondary = onSecondaryLight.velvetRose,
    secondaryContainer = secondaryContainerLight.velvetRose,
    onSecondaryContainer = onSecondaryContainerLight.velvetRose,
    tertiary = tertiaryLight.velvetRose,
    onTertiary = onTertiaryLight.velvetRose,
    tertiaryContainer = tertiaryContainerLight.velvetRose,
    onTertiaryContainer = onTertiaryContainerLight.velvetRose,
    error = errorLight.velvetRose,
    onError = onErrorLight.velvetRose,
    errorContainer = errorContainerLight.velvetRose,
    onErrorContainer = onErrorContainerLight.velvetRose,
    background = backgroundLight.velvetRose,
    onBackground = onBackgroundLight.velvetRose,
    surface = surfaceLight.velvetRose,
    onSurface = onSurfaceLight.velvetRose,
    surfaceVariant = surfaceVariantLight.velvetRose,
    onSurfaceVariant = onSurfaceVariantLight.velvetRose,
    outline = outlineLight.velvetRose,
    outlineVariant = outlineVariantLight.velvetRose,
    scrim = scrimLight.velvetRose,
    inverseSurface = inverseSurfaceLight.velvetRose,
    inverseOnSurface = inverseOnSurfaceLight.velvetRose,
    inversePrimary = inversePrimaryLight.velvetRose,
    surfaceDim = surfaceDimLight.velvetRose,
    surfaceBright = surfaceBrightLight.velvetRose,
    surfaceContainerLowest = surfaceContainerLowestLight.velvetRose,
    surfaceContainerLow = surfaceContainerLowLight.velvetRose,
    surfaceContainer = surfaceContainerLight.velvetRose,
    surfaceContainerHigh = surfaceContainerHighLight.velvetRose,
    surfaceContainerHighest = surfaceContainerHighestLight.velvetRose,
)

private val velvetRoseDark = darkColorScheme(
    primary = primaryDark.velvetRose,
    onPrimary = onPrimaryDark.velvetRose,
    primaryContainer = primaryContainerDark.velvetRose,
    onPrimaryContainer = onPrimaryContainerDark.velvetRose,
    secondary = secondaryDark.velvetRose,
    onSecondary = onSecondaryDark.velvetRose,
    secondaryContainer = secondaryContainerDark.velvetRose,
    onSecondaryContainer = onSecondaryContainerDark.velvetRose,
    tertiary = tertiaryDark.velvetRose,
    onTertiary = onTertiaryDark.velvetRose,
    tertiaryContainer = tertiaryContainerDark.velvetRose,
    onTertiaryContainer = onTertiaryContainerDark.velvetRose,
    error = errorDark.velvetRose,
    onError = onErrorDark.velvetRose,
    errorContainer = errorContainerDark.velvetRose,
    onErrorContainer = onErrorContainerDark.velvetRose,
    background = backgroundDark.velvetRose,
    onBackground = onBackgroundDark.velvetRose,
    surface = surfaceDark.velvetRose,
    onSurface = onSurfaceDark.velvetRose,
    surfaceVariant = surfaceVariantDark.velvetRose,
    onSurfaceVariant = onSurfaceVariantDark.velvetRose,
    outline = outlineDark.velvetRose,
    outlineVariant = outlineVariantDark.velvetRose,
    scrim = scrimDark.velvetRose,
    inverseSurface = inverseSurfaceDark.velvetRose,
    inverseOnSurface = inverseOnSurfaceDark.velvetRose,
    inversePrimary = inversePrimaryDark.velvetRose,
    surfaceDim = surfaceDimDark.velvetRose,
    surfaceBright = surfaceBrightDark.velvetRose,
    surfaceContainerLowest = surfaceContainerLowestDark.velvetRose,
    surfaceContainerLow = surfaceContainerLowDark.velvetRose,
    surfaceContainer = surfaceContainerDark.velvetRose,
    surfaceContainerHigh = surfaceContainerHighDark.velvetRose,
    surfaceContainerHighest = surfaceContainerHighestDark.velvetRose,
)

private val mistwaveLight = lightColorScheme(
    primary = primaryLight.mistwave,
    onPrimary = onPrimaryLight.mistwave,
    primaryContainer = primaryContainerLight.mistwave,
    onPrimaryContainer = onPrimaryContainerLight.mistwave,
    secondary = secondaryLight.mistwave,
    onSecondary = onSecondaryLight.mistwave,
    secondaryContainer = secondaryContainerLight.mistwave,
    onSecondaryContainer = onSecondaryContainerLight.mistwave,
    tertiary = tertiaryLight.mistwave,
    onTertiary = onTertiaryLight.mistwave,
    tertiaryContainer = tertiaryContainerLight.mistwave,
    onTertiaryContainer = onTertiaryContainerLight.mistwave,
    error = errorLight.mistwave,
    onError = onErrorLight.mistwave,
    errorContainer = errorContainerLight.mistwave,
    onErrorContainer = onErrorContainerLight.mistwave,
    background = backgroundLight.mistwave,
    onBackground = onBackgroundLight.mistwave,
    surface = surfaceLight.mistwave,
    onSurface = onSurfaceLight.mistwave,
    surfaceVariant = surfaceVariantLight.mistwave,
    onSurfaceVariant = onSurfaceVariantLight.mistwave,
    outline = outlineLight.mistwave,
    outlineVariant = outlineVariantLight.mistwave,
    scrim = scrimLight.mistwave,
    inverseSurface = inverseSurfaceLight.mistwave,
    inverseOnSurface = inverseOnSurfaceLight.mistwave,
    inversePrimary = inversePrimaryLight.mistwave,
    surfaceDim = surfaceDimLight.mistwave,
    surfaceBright = surfaceBrightLight.mistwave,
    surfaceContainerLowest = surfaceContainerLowestLight.mistwave,
    surfaceContainerLow = surfaceContainerLowLight.mistwave,
    surfaceContainer = surfaceContainerLight.mistwave,
    surfaceContainerHigh = surfaceContainerHighLight.mistwave,
    surfaceContainerHighest = surfaceContainerHighestLight.mistwave,
)

private val mistwaveDark = darkColorScheme(
    primary = primaryDark.mistwave,
    onPrimary = onPrimaryDark.mistwave,
    primaryContainer = primaryContainerDark.mistwave,
    onPrimaryContainer = onPrimaryContainerDark.mistwave,
    secondary = secondaryDark.mistwave,
    onSecondary = onSecondaryDark.mistwave,
    secondaryContainer = secondaryContainerDark.mistwave,
    onSecondaryContainer = onSecondaryContainerDark.mistwave,
    tertiary = tertiaryDark.mistwave,
    onTertiary = onTertiaryDark.mistwave,
    tertiaryContainer = tertiaryContainerDark.mistwave,
    onTertiaryContainer = onTertiaryContainerDark.mistwave,
    error = errorDark.mistwave,
    onError = onErrorDark.mistwave,
    errorContainer = errorContainerDark.mistwave,
    onErrorContainer = onErrorContainerDark.mistwave,
    background = backgroundDark.mistwave,
    onBackground = onBackgroundDark.mistwave,
    surface = surfaceDark.mistwave,
    onSurface = onSurfaceDark.mistwave,
    surfaceVariant = surfaceVariantDark.mistwave,
    onSurfaceVariant = onSurfaceVariantDark.mistwave,
    outline = outlineDark.mistwave,
    outlineVariant = outlineVariantDark.mistwave,
    scrim = scrimDark.mistwave,
    inverseSurface = inverseSurfaceDark.mistwave,
    inverseOnSurface = inverseOnSurfaceDark.mistwave,
    inversePrimary = inversePrimaryDark.mistwave,
    surfaceDim = surfaceDimDark.mistwave,
    surfaceBright = surfaceBrightDark.mistwave,
    surfaceContainerLowest = surfaceContainerLowestDark.mistwave,
    surfaceContainerLow = surfaceContainerLowDark.mistwave,
    surfaceContainer = surfaceContainerDark.mistwave,
    surfaceContainerHigh = surfaceContainerHighDark.mistwave,
    surfaceContainerHighest = surfaceContainerHighestDark.mistwave,
)

private val glacierLight = lightColorScheme(
    primary = primaryLight.glacier,
    onPrimary = onPrimaryLight.glacier,
    primaryContainer = primaryContainerLight.glacier,
    onPrimaryContainer = onPrimaryContainerLight.glacier,
    secondary = secondaryLight.glacier,
    onSecondary = onSecondaryLight.glacier,
    secondaryContainer = secondaryContainerLight.glacier,
    onSecondaryContainer = onSecondaryContainerLight.glacier,
    tertiary = tertiaryLight.glacier,
    onTertiary = onTertiaryLight.glacier,
    tertiaryContainer = tertiaryContainerLight.glacier,
    onTertiaryContainer = onTertiaryContainerLight.glacier,
    error = errorLight.glacier,
    onError = onErrorLight.glacier,
    errorContainer = errorContainerLight.glacier,
    onErrorContainer = onErrorContainerLight.glacier,
    background = backgroundLight.glacier,
    onBackground = onBackgroundLight.glacier,
    surface = surfaceLight.glacier,
    onSurface = onSurfaceLight.glacier,
    surfaceVariant = surfaceVariantLight.glacier,
    onSurfaceVariant = onSurfaceVariantLight.glacier,
    outline = outlineLight.glacier,
    outlineVariant = outlineVariantLight.glacier,
    scrim = scrimLight.glacier,
    inverseSurface = inverseSurfaceLight.glacier,
    inverseOnSurface = inverseOnSurfaceLight.glacier,
    inversePrimary = inversePrimaryLight.glacier,
    surfaceDim = surfaceDimLight.glacier,
    surfaceBright = surfaceBrightLight.glacier,
    surfaceContainerLowest = surfaceContainerLowestLight.glacier,
    surfaceContainerLow = surfaceContainerLowLight.glacier,
    surfaceContainer = surfaceContainerLight.glacier,
    surfaceContainerHigh = surfaceContainerHighLight.glacier,
    surfaceContainerHighest = surfaceContainerHighestLight.glacier,
)

private val glacierDark = darkColorScheme(
    primary = primaryDark.glacier,
    onPrimary = onPrimaryDark.glacier,
    primaryContainer = primaryContainerDark.glacier,
    onPrimaryContainer = onPrimaryContainerDark.glacier,
    secondary = secondaryDark.glacier,
    onSecondary = onSecondaryDark.glacier,
    secondaryContainer = secondaryContainerDark.glacier,
    onSecondaryContainer = onSecondaryContainerDark.glacier,
    tertiary = tertiaryDark.glacier,
    onTertiary = onTertiaryDark.glacier,
    tertiaryContainer = tertiaryContainerDark.glacier,
    onTertiaryContainer = onTertiaryContainerDark.glacier,
    error = errorDark.glacier,
    onError = onErrorDark.glacier,
    errorContainer = errorContainerDark.glacier,
    onErrorContainer = onErrorContainerDark.glacier,
    background = backgroundDark.glacier,
    onBackground = onBackgroundDark.glacier,
    surface = surfaceDark.glacier,
    onSurface = onSurfaceDark.glacier,
    surfaceVariant = surfaceVariantDark.glacier,
    onSurfaceVariant = onSurfaceVariantDark.glacier,
    outline = outlineDark.glacier,
    outlineVariant = outlineVariantDark.glacier,
    scrim = scrimDark.glacier,
    inverseSurface = inverseSurfaceDark.glacier,
    inverseOnSurface = inverseOnSurfaceDark.glacier,
    inversePrimary = inversePrimaryDark.glacier,
    surfaceDim = surfaceDimDark.glacier,
    surfaceBright = surfaceBrightDark.glacier,
    surfaceContainerLowest = surfaceContainerLowestDark.glacier,
    surfaceContainerLow = surfaceContainerLowDark.glacier,
    surfaceContainer = surfaceContainerDark.glacier,
    surfaceContainerHigh = surfaceContainerHighDark.glacier,
    surfaceContainerHighest = surfaceContainerHighestDark.glacier,
)

private val verdantFieldLight = lightColorScheme(
    primary = primaryLight.verdantField,
    onPrimary = onPrimaryLight.verdantField,
    primaryContainer = primaryContainerLight.verdantField,
    onPrimaryContainer = onPrimaryContainerLight.verdantField,
    secondary = secondaryLight.verdantField,
    onSecondary = onSecondaryLight.verdantField,
    secondaryContainer = secondaryContainerLight.verdantField,
    onSecondaryContainer = onSecondaryContainerLight.verdantField,
    tertiary = tertiaryLight.verdantField,
    onTertiary = onTertiaryLight.verdantField,
    tertiaryContainer = tertiaryContainerLight.verdantField,
    onTertiaryContainer = onTertiaryContainerLight.verdantField,
    error = errorLight.verdantField,
    onError = onErrorLight.verdantField,
    errorContainer = errorContainerLight.verdantField,
    onErrorContainer = onErrorContainerLight.verdantField,
    background = backgroundLight.verdantField,
    onBackground = onBackgroundLight.verdantField,
    surface = surfaceLight.verdantField,
    onSurface = onSurfaceLight.verdantField,
    surfaceVariant = surfaceVariantLight.verdantField,
    onSurfaceVariant = onSurfaceVariantLight.verdantField,
    outline = outlineLight.verdantField,
    outlineVariant = outlineVariantLight.verdantField,
    scrim = scrimLight.verdantField,
    inverseSurface = inverseSurfaceLight.verdantField,
    inverseOnSurface = inverseOnSurfaceLight.verdantField,
    inversePrimary = inversePrimaryLight.verdantField,
    surfaceDim = surfaceDimLight.verdantField,
    surfaceBright = surfaceBrightLight.verdantField,
    surfaceContainerLowest = surfaceContainerLowestLight.verdantField,
    surfaceContainerLow = surfaceContainerLowLight.verdantField,
    surfaceContainer = surfaceContainerLight.verdantField,
    surfaceContainerHigh = surfaceContainerHighLight.verdantField,
    surfaceContainerHighest = surfaceContainerHighestLight.verdantField,
)

private val verdantFieldDark = darkColorScheme(
    primary = primaryDark.verdantField,
    onPrimary = onPrimaryDark.verdantField,
    primaryContainer = primaryContainerDark.verdantField,
    onPrimaryContainer = onPrimaryContainerDark.verdantField,
    secondary = secondaryDark.verdantField,
    onSecondary = onSecondaryDark.verdantField,
    secondaryContainer = secondaryContainerDark.verdantField,
    onSecondaryContainer = onSecondaryContainerDark.verdantField,
    tertiary = tertiaryDark.verdantField,
    onTertiary = onTertiaryDark.verdantField,
    tertiaryContainer = tertiaryContainerDark.verdantField,
    onTertiaryContainer = onTertiaryContainerDark.verdantField,
    error = errorDark.verdantField,
    onError = onErrorDark.verdantField,
    errorContainer = errorContainerDark.verdantField,
    onErrorContainer = onErrorContainerDark.verdantField,
    background = backgroundDark.verdantField,
    onBackground = onBackgroundDark.verdantField,
    surface = surfaceDark.verdantField,
    onSurface = onSurfaceDark.verdantField,
    surfaceVariant = surfaceVariantDark.verdantField,
    onSurfaceVariant = onSurfaceVariantDark.verdantField,
    outline = outlineDark.verdantField,
    outlineVariant = outlineVariantDark.verdantField,
    scrim = scrimDark.verdantField,
    inverseSurface = inverseSurfaceDark.verdantField,
    inverseOnSurface = inverseOnSurfaceDark.verdantField,
    inversePrimary = inversePrimaryDark.verdantField,
    surfaceDim = surfaceDimDark.verdantField,
    surfaceBright = surfaceBrightDark.verdantField,
    surfaceContainerLowest = surfaceContainerLowestDark.verdantField,
    surfaceContainerLow = surfaceContainerLowDark.verdantField,
    surfaceContainer = surfaceContainerDark.verdantField,
    surfaceContainerHigh = surfaceContainerHighDark.verdantField,
    surfaceContainerHighest = surfaceContainerHighestDark.verdantField,
)

@Composable
fun ZalithLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val currentColorTheme by LocalColorThemeState.current

    val colorScheme = when {
        dynamicColor && currentColorTheme == ColorThemeType.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> when (currentColorTheme) {
            ColorThemeType.ROSEWOOD_EMBER -> rosewoodEmberDark
            ColorThemeType.VELVET_ROSE -> velvetRoseDark
            ColorThemeType.MISTWAVE -> mistwaveDark
            ColorThemeType.GLACIER -> glacierDark
            ColorThemeType.VERDANTFIELD -> verdantFieldDark
            else -> rosewoodEmberDark
        }
        else -> when (currentColorTheme) {
            ColorThemeType.ROSEWOOD_EMBER -> rosewoodEmberLight
            ColorThemeType.VELVET_ROSE -> velvetRoseLight
            ColorThemeType.MISTWAVE -> mistwaveLight
            ColorThemeType.GLACIER -> glacierLight
            ColorThemeType.VERDANTFIELD -> verdantFieldLight
            else -> rosewoodEmberLight
        }
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}