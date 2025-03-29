package com.movtery.zalithlauncher.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 状态：需要展示的错误信息
 */
object ShowThrowableState {
    data class ThrowableMessage(val title: String, val message: String)

    private val _throwableFlow = MutableStateFlow<ThrowableMessage?>(null)
    val throwableFlow: StateFlow<ThrowableMessage?> = _throwableFlow

    fun update(tm: ThrowableMessage?) {
        _throwableFlow.value = tm
    }
}