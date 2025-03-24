package com.movtery.zalithlauncher.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.movtery.zalithlauncher.R

@Composable
fun ZalithLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme(
            primary = colorResource(R.color.color_primary),
            onPrimary = colorResource(R.color.color_on_primary),
            primaryContainer = colorResource(R.color.color_primary_container),
            onPrimaryContainer = colorResource(R.color.color_on_primary_container),

            secondary = colorResource(R.color.color_secondary),
            onSecondary = colorResource(R.color.color_on_secondary),
            secondaryContainer = colorResource(R.color.color_secondary_container),
            onSecondaryContainer = colorResource(R.color.color_on_secondary_container),

            tertiary = colorResource(R.color.color_tertiary),
            onTertiary = colorResource(R.color.color_on_tertiary),
            tertiaryContainer = colorResource(R.color.color_tertiary_container),
            onTertiaryContainer = colorResource(R.color.color_on_tertiary_container),

            background = colorResource(R.color.color_background),
            onBackground = colorResource(R.color.color_on_background),

            surface = colorResource(R.color.color_surface),
            onSurface = colorResource(R.color.color_on_surface),

            outline = colorResource(R.color.color_outline),

            error = colorResource(R.color.color_error),
            onError = colorResource(R.color.color_on_error)
        )

        else -> lightColorScheme(
            primary = colorResource(R.color.color_primary),
            onPrimary = colorResource(R.color.color_on_primary),
            primaryContainer = colorResource(R.color.color_primary_container),
            onPrimaryContainer = colorResource(R.color.color_on_primary_container),

            secondary = colorResource(R.color.color_secondary),
            onSecondary = colorResource(R.color.color_on_secondary),
            secondaryContainer = colorResource(R.color.color_secondary_container),
            onSecondaryContainer = colorResource(R.color.color_on_secondary_container),

            tertiary = colorResource(R.color.color_tertiary),
            onTertiary = colorResource(R.color.color_on_tertiary),
            tertiaryContainer = colorResource(R.color.color_tertiary_container),
            onTertiaryContainer = colorResource(R.color.color_on_tertiary_container),

            background = colorResource(R.color.color_background),
            onBackground = colorResource(R.color.color_on_background),

            surface = colorResource(R.color.color_surface),
            onSurface = colorResource(R.color.color_on_surface),

            outline = colorResource(R.color.color_outline),

            error = colorResource(R.color.color_error),
            onError = colorResource(R.color.color_on_error)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}