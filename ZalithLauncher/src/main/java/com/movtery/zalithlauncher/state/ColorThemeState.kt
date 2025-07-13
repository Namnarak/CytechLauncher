package com.movtery.zalithlauncher.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.theme.ColorThemeType
import kotlin.reflect.KProperty

/**
 * 状态：当前颜色主题状态
 */
class ColorThemeState {
    private var currentType by mutableStateOf(AllSettings.launcherColorTheme.getValue())

    operator fun getValue(nothing: Nothing?, property: KProperty<*>): ColorThemeType {
        return currentType
    }

    fun updateValue(value: ColorThemeType) {
        currentType = value
    }
}

val LocalColorThemeState = staticCompositionLocalOf<ColorThemeState> {
    error("ColorThemeState not provided!")
}