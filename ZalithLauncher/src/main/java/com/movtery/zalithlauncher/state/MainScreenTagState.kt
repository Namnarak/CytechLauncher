package com.movtery.zalithlauncher.state

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 状态：当前主屏幕的标签
 */
class MainScreenTagState: AbstractStringState()

val LocalMainScreenTag = staticCompositionLocalOf<AbstractStringState> {
    error("MainScreenTagState not provided!")
}