package com.movtery.zalithlauncher.state

import androidx.compose.runtime.compositionLocalOf

/**
 * 状态：当前设置屏幕的标签
 */
class SettingsScreenTagState: AbstractStringState()

val LocalSettingsScreenTag = compositionLocalOf<AbstractStringState> {
    error("SettingsScreenTagState not provided!")
}