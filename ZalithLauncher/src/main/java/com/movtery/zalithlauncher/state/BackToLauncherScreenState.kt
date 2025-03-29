package com.movtery.zalithlauncher.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object BackToLauncherScreenState {
    private val _back = MutableStateFlow(false)
    val backState: StateFlow<Boolean> = _back

    fun back() {
        _back.value = true
    }

    fun reset() {
        _back.value = false
    }
}