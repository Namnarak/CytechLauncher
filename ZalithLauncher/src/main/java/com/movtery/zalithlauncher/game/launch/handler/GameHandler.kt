package com.movtery.zalithlauncher.game.launch.handler

import android.view.InputDevice
import android.view.KeyEvent
import android.view.Surface
import androidx.compose.runtime.Composable
import androidx.lifecycle.LifecycleCoroutineScope
import com.movtery.zalithlauncher.bridge.ZLBridge
import com.movtery.zalithlauncher.game.input.EfficientAndroidLWJGLKeycode
import com.movtery.zalithlauncher.game.input.LWJGLCharSender
import com.movtery.zalithlauncher.game.keycodes.LwjglGlfwKeycode
import com.movtery.zalithlauncher.game.launch.Launcher
import com.movtery.zalithlauncher.ui.screens.game.GameScreen
import org.lwjgl.glfw.CallbackBridge

class GameHandler : AbstractHandler(HandlerType.GAME) {
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

    @Suppress("DEPRECATION")
    override fun shouldIgnoreKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP && (event.flags and KeyEvent.FLAG_CANCELED) != 0) return false
        if ((event.flags and KeyEvent.FLAG_SOFT_KEYBOARD) == KeyEvent.FLAG_SOFT_KEYBOARD) {
            if (event.keyCode == KeyEvent.KEYCODE_ENTER) {
                LWJGLCharSender.sendEnter()
                return false
            }
        }

        event.device?.let {
            val source = event.source
            if (source and InputDevice.SOURCE_MOUSE_RELATIVE == InputDevice.SOURCE_MOUSE_RELATIVE ||
                source and InputDevice.SOURCE_MOUSE == InputDevice.SOURCE_MOUSE) {

                if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                    val isDown = event.action == KeyEvent.ACTION_DOWN
                    CallbackBridge.sendMouseButton(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_RIGHT.toInt(), isDown)
                    return false
                }
            }
        }

        EfficientAndroidLWJGLKeycode.getIndexByKey(event.keyCode).takeIf { it >= 0 }?.let { index ->
            EfficientAndroidLWJGLKeycode.execKey(event, index)
            return false
        }

        return when (event.keyCode) {
            KeyEvent.KEYCODE_UNKNOWN,
            KeyEvent.ACTION_MULTIPLE,
            KeyEvent.ACTION_UP
                 -> false

            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_UP
                 -> true

            else -> (event.flags and KeyEvent.FLAG_FALLBACK) != KeyEvent.FLAG_FALLBACK
        }
    }

    @Composable
    override fun getComposableLayout() = @Composable {
        GameScreen()
    }
}