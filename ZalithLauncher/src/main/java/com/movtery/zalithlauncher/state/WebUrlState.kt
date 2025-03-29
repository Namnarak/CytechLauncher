package com.movtery.zalithlauncher.state

import androidx.compose.runtime.compositionLocalOf

/**
 * 状态：当前WebView想要访问的Url
 */
class WebUrlState: AbstractStringState()

val LocalWebUrlState = compositionLocalOf<AbstractStringState> {
    error("WebUrlState not provided!")
}