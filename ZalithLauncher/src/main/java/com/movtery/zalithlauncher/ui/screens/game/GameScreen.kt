package com.movtery.zalithlauncher.ui.screens.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.movtery.zalithlauncher.ZLApplication
import com.movtery.zalithlauncher.bridge.CURSOR_DISABLED
import com.movtery.zalithlauncher.bridge.CURSOR_ENABLED
import com.movtery.zalithlauncher.bridge.Logger
import com.movtery.zalithlauncher.bridge.ZLBridgeStates
import com.movtery.zalithlauncher.game.keycodes.LwjglGlfwKeycode
import com.movtery.zalithlauncher.setting.scaleFactor
import com.movtery.zalithlauncher.ui.control.mouse.TouchpadLayout
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
    val sensitivityFactor = 1.4 * (1080f / ZLApplication.DISPLAY_METRICS.heightPixels)

    val mode = ZLBridgeStates.cursorMode
    if (mode == CURSOR_ENABLED) {
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

    if (mode == CURSOR_DISABLED) {
        TouchpadLayout(
            modifier = modifier,
            onPointerMove = { delta ->
                val deltaX = (delta.x * sensitivityFactor).toFloat()
                val deltaY = (delta.y * sensitivityFactor).toFloat()
                CallbackBridge.sendCursorDelta(deltaX, deltaY)
            }
        )
    }
}

private fun Float.sumPosition(): Float {
    return (this * scaleFactor)
}