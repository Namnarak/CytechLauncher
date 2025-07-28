package com.movtery.zalithlauncher.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.ui.screens.clearWith
import com.movtery.zalithlauncher.ui.screens.content.LauncherScreenKey

class MainScreenViewModel: ViewModel() {
    /**
     * 主屏幕堆栈
     */
    val backStack: NavBackStack = mutableStateListOf()

    init {
        if (backStack.isEmpty()) {
            backStack.add(LauncherScreenKey)
        }
    }

    /**
     * 状态：当前主屏幕的标签
     */
    var screenKey by mutableStateOf<NavKey?>(null)

    /**
     * 返回主页面
     */
    fun backToMainScreen() {
        backStack.clearWith(LauncherScreenKey)
    }
}