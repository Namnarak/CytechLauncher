package com.movtery.zalithlauncher.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.movtery.zalithlauncher.setting.AllSettings
import kotlin.reflect.KProperty

/**
 * 状态：自定义颜色主题（当前自定义颜色状态）
 */
class CustomColorThemeState {
    private var currentColor by mutableStateOf(getCustomColorFromSettings())

    operator fun getValue(nothing: Nothing?, property: KProperty<*>): Color {
        return currentColor
    }

    fun updateValue(value: Color) {
        currentColor = value
    }

    fun saveValue() {
        AllSettings.launcherCustomColor.save(currentColor.toArgb())
    }
}

val LocalCustomColorThemeState = staticCompositionLocalOf<CustomColorThemeState> {
    error("CustomColorThemeState not provided!")
}

fun getCustomColorFromSettings(): Color = Color(AllSettings.launcherCustomColor.getValue())
