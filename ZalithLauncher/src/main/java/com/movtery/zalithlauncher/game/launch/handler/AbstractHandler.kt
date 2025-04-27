package com.movtery.zalithlauncher.game.launch.handler

import android.view.KeyEvent
import android.view.Surface
import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import androidx.lifecycle.LifecycleCoroutineScope
import com.movtery.zalithlauncher.game.launch.Launcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class AbstractHandler(val type: HandlerType) {
    var mIsSurfaceDestroyed: Boolean = false

    @CallSuper
    open suspend fun execute(
        surface: Surface,
        launcher: Launcher,
        scope: LifecycleCoroutineScope
    ) {
        scope.launch(Dispatchers.Default) {
            launcher.launch()
        }
    }

    abstract fun onPause()
    abstract fun onResume()
    abstract fun onGraphicOutput()
    abstract fun shouldIgnoreKeyEvent(event: KeyEvent): Boolean
    abstract fun sendMouseRight(isPressed: Boolean)

    @Composable
    abstract fun getComposableLayout(): @Composable () -> Unit
}