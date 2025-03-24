package com.movtery.zalithlauncher.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 状态：当前主屏幕的标签
 */
class MainScreenTagState {
    var currentTag by mutableStateOf<String?>(null)
        private set

    fun updateTag(newTag: String?) {
        currentTag = newTag
    }
}

val LocalMainScreenTag = staticCompositionLocalOf<MainScreenTagState> {
    error("MainScreenTagState not provided!")
}