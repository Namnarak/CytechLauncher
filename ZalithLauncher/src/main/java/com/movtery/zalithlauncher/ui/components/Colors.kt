package com.movtery.zalithlauncher.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * 降低颜色的饱和度
 */
fun Color.desaturate(factor: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    hsv[1] *= factor.coerceIn(0f, 1f)
    return Color(android.graphics.Color.HSVToColor(hsv))
}