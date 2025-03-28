package com.movtery.zalithlauncher.ui.theme

import androidx.compose.ui.graphics.Color

data class ColorTheme(
    val embermire: Color,
    val velvetRose: Color,
    val mistwave: Color,
    val glacier: Color,
    val verdantField: Color,
    val ashveil: Color,
    val urbanAsh: Color,
    val verdantDawn: Color,
    val celestineVeil: Color
)

enum class ColorThemeType {
    DYNAMIC,
    EMBERMIRE,
    VELVET_ROSE,
    MISTWAVE,
    GLACIER,
    VERDANTFIELD,
    ASHVEIL,
    URBAN_ASH,
    VERDANT_DAWN,
    CELESTINE_VEIL
}