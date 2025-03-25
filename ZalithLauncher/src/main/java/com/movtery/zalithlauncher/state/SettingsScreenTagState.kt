package com.movtery.zalithlauncher.state

import androidx.compose.runtime.compositionLocalOf

/**
 * 状态：当前设置屏幕的标签
 */
class SettingsScreenTagState: AbstractScreenTagState()

val LocalSettingsScreenTag = compositionLocalOf<AbstractScreenTagState> {
    error("SettingsScreenTagState not provided!")
}