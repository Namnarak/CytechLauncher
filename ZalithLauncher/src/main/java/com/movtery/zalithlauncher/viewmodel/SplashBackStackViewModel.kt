package com.movtery.zalithlauncher.viewmodel

import androidx.lifecycle.ViewModel
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.addIfEmpty

class SplashBackStackViewModel(): ViewModel() {
    /** 启动屏幕 */
    val splashScreen = NestedNavKey.Splash()

    init {
        splashScreen.backStack.addIfEmpty(NormalNavKey.UnpackDeps)
    }
}