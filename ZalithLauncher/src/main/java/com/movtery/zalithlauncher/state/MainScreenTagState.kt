package com.movtery.zalithlauncher.state

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 状态：当前主屏幕的标签
 */
class MainScreenTagState: AbstractScreenTagState()

val LocalMainScreenTag = staticCompositionLocalOf<AbstractScreenTagState> {
    error("MainScreenTagState not provided!")
}