package com.movtery.zalithlauncher.ui.screens.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.movtery.zalithlauncher.bridge.Logger
import com.movtery.zalithlauncher.game.keycodes.LwjglGlfwKeycode
import com.movtery.zalithlauncher.setting.scaleFactor
import com.movtery.zalithlauncher.ui.control.mouse.VirtualPointerLayout
import com.movtery.zalithlauncher.ui.control.mouse.getDefaultMousePointer
import com.movtery.zalithlauncher.ui.screens.game.elements.LogBox
import org.lwjgl.glfw.CallbackBridge

@Composable
fun GameScreen() {
    var enableLog by remember { mutableStateOf(false) }
    var logText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        MouseControlLayout(modifier = Modifier.fillMaxSize())

        if (enableLog) {
            Logger.setLogListener { logString ->
                logText += "$logString\n"
            }
            LogBox(logText = logText)
        } else {
            logText = ""
            Logger.setLogListener(null)
        }
    }
}

@Composable
fun MouseControlLayout(
    modifier: Modifier = Modifier
) {
    VirtualPointerLayout(
        modifier = modifier,
        onTap = { position ->
            CallbackBridge.putMouseEventWithCoords(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT.toInt(), position.x.sumPosition(), position.y.sumPosition())
        },
        onPointerMove = { position ->
            CallbackBridge.sendCursorPos(position.x.sumPosition(), position.y.sumPosition())
        },
        onLongPress = {
            CallbackBridge.putMouseEvent(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT.toInt(), true)
        },
        onLongPressEnd = {
            CallbackBridge.putMouseEvent(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT.toInt(), false)
        },
        mousePainter = getDefaultMousePointer()
    )
}

private fun Float.sumPosition(): Float {
    return (this * scaleFactor)
}