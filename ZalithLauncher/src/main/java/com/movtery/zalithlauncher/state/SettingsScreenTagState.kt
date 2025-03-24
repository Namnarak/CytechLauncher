package com.movtery.zalithlauncher.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 状态：当前设置屏幕的标签
 */
class SettingsScreenTagState {
    var currentTag by mutableStateOf<String?>(null)
        private set

    fun updateTag(newTag: String?) {
        currentTag = newTag
    }
}

val LocalSettingsScreenTag = staticCompositionLocalOf<SettingsScreenTagState> {
    error("SettingsScreenTagState not provided!")
}