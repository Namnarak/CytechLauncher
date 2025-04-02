package com.movtery.zalithlauncher.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object MutableStates {
    /**
     * 状态：当前主屏幕的标签
     */
    var mainScreenTag by mutableStateOf<String?>(null)

    /**
     * 状态：当前主屏幕的标签
     */
    var settingsScreenTag by mutableStateOf<String?>(null)
}