package com.movtery.zalithlauncher.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ObjectStates {
    private val _backToLauncherScreen = MutableStateFlow(false)
    private val _throwableFlow = MutableStateFlow<ThrowableMessage?>(null)
    private val _url = MutableStateFlow<String?>(null)

    /**
     * 状态：返回启动器主屏幕
     */
    val backToLauncherScreenState: StateFlow<Boolean> = _backToLauncherScreen

    /**
     * 状态：需要展示的错误信息
     */
    val throwableFlow: StateFlow<ThrowableMessage?> = _throwableFlow

    /**
     * 状态：需要在内置WebView中访问的链接
     */
    val url: StateFlow<String?> = _url

    /**
     * 返回启动器主屏幕
     */
    fun backToLauncherScreen() {
        _backToLauncherScreen.value = true
    }

    /**
     * 重置返回启动器主屏幕状态
     */
    fun resetBackToLauncherScreen() {
        _backToLauncherScreen.value = false
    }

    /**
     * 需要展示的错误信息
     */
    fun updateThrowable(tm: ThrowableMessage?) {
        _throwableFlow.value = tm
    }

    /**
     * 在内置WebView中访问链接
     */
    fun accessUrl(url: String) {
        _url.value = url
    }

    /**
     * 重置链接
     */
    fun clearUrl() {
        _url.value = null
    }

    data class ThrowableMessage(val title: String, val message: String)
}