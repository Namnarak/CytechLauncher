package com.movtery.zalithlauncher.game.launch.handler

import android.view.KeyEvent
import android.view.Surface
import androidx.compose.runtime.Composable
import androidx.lifecycle.LifecycleCoroutineScope
import com.movtery.zalithlauncher.bridge.ZLBridge
import com.movtery.zalithlauncher.game.launch.Launcher
import com.movtery.zalithlauncher.ui.screens.game.GameScreen

class GameHandler : AbstractHandler() {
    override fun execute(surface: Surface, launcher: Launcher, scope: LifecycleCoroutineScope) {
        ZLBridge.setupBridgeWindow(surface)
        super.execute(surface, launcher, scope)
    }

    override fun onPause() {
    }

    override fun onResume() {
    }

    override fun onGraphicOutput() {
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return true
    }

    @Composable
    override fun getComposableLayout() = @Composable {
        GameScreen()
    }
}