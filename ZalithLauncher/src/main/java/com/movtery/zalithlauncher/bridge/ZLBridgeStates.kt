package com.movtery.zalithlauncher.bridge

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ZLBridgeStates {
    /**
     * 状态：指针模式（启用、禁用）
     */
    @JvmStatic
    var cursorMode by mutableIntStateOf(CURSOR_ENABLED)

    /**
     * 状态：当前画面帧率
     */
    @JvmStatic
    var currentFPS by mutableIntStateOf(0)

    /**
     * 莊濤：窗口变更刷新key
     */
    @JvmStatic
    var windowChangeKey by mutableStateOf(false)

    fun onWindowChange() {
        this.windowChangeKey = !this.windowChangeKey
    }
}

/** 指针:启用 */
const val CURSOR_ENABLED = 1
/** 指针:禁用 */
const val CURSOR_DISABLED = 0