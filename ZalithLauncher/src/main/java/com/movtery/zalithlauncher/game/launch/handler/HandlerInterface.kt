package com.movtery.zalithlauncher.game.launch.handler

import android.view.KeyEvent
import androidx.compose.runtime.Composable

interface HandlerInterface {
    fun onPause()
    fun onResume()
    fun onGraphicOutput()
    fun dispatchKeyEvent(event: KeyEvent): Boolean

    @Composable
    fun getComposableLayout(): @Composable () -> Unit
}