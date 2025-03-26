package com.movtery.zalithlauncher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * 启动器页面部分全屏变动
 */
class LauncherFullScreenViewModel : ViewModel() {
    private val _refreshEvent = MutableSharedFlow<Unit>()
    val refreshEvent = _refreshEvent.asSharedFlow()

    fun triggerRefresh() {
        viewModelScope.launch {
            _refreshEvent.emit(Unit)
        }
    }
}