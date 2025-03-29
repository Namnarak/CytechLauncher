package com.movtery.zalithlauncher.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 状态：想要在启动器内置浏览器访问的网页地址
 */
object WebUrlState {
    private val _url = MutableStateFlow<String?>(null)
    val url: StateFlow<String?> = _url

    fun access(url: String) {
        _url.value = url
    }

    fun clear() {
        _url.value = null
    }
}