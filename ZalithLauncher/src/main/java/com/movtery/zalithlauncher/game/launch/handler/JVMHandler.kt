package com.movtery.zalithlauncher.game.launch.handler

import android.view.KeyEvent
import androidx.compose.runtime.Composable
import com.movtery.zalithlauncher.ui.screens.game.JVMScreen

class JVMHandler: HandlerInterface {
    override fun onPause() {
    }

    override fun onResume() {
    }

    override fun onGraphicOutput() {
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean = true

    @Composable
    override fun getComposableLayout() = @Composable {
        JVMScreen()
    }
}