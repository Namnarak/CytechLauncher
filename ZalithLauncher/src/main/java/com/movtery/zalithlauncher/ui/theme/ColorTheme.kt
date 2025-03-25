package com.movtery.zalithlauncher.ui.theme

import androidx.compose.ui.graphics.Color

data class ColorTheme(
    val rosewoodEmber: Color,
    val velvetRose: Color,
    val mistwave: Color,
    val glacier: Color,
    val verdantField: Color
)

enum class ColorThemeType {
    DYNAMIC,
    ROSEWOOD_EMBER,
    VELVET_ROSE,
    MISTWAVE,
    GLACIER,
    VERDANTFIELD
}