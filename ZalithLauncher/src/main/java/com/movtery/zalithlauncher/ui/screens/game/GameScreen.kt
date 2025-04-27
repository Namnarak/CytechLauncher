package com.movtery.zalithlauncher.ui.screens.game

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.ZLApplication
import com.movtery.zalithlauncher.bridge.CURSOR_DISABLED
import com.movtery.zalithlauncher.bridge.CURSOR_ENABLED
import com.movtery.zalithlauncher.bridge.LoggerBridge
import com.movtery.zalithlauncher.bridge.ZLBridgeStates
import com.movtery.zalithlauncher.game.input.LWJGLCharSender
import com.movtery.zalithlauncher.game.keycodes.LwjglGlfwKeycode
import com.movtery.zalithlauncher.setting.enums.toAction
import com.movtery.zalithlauncher.setting.gestureControl
import com.movtery.zalithlauncher.setting.gestureLongPressDelay
import com.movtery.zalithlauncher.setting.gestureLongPressMouseAction
import com.movtery.zalithlauncher.setting.gestureTapMouseAction
import com.movtery.zalithlauncher.setting.mouseControlMode
import com.movtery.zalithlauncher.setting.mouseLongPressDelay
import com.movtery.zalithlauncher.setting.mouseSize
import com.movtery.zalithlauncher.setting.mouseSpeed
import com.movtery.zalithlauncher.setting.scaleFactor
import com.movtery.zalithlauncher.ui.control.mouse.TouchpadLayout
import com.movtery.zalithlauncher.ui.control.mouse.VirtualPointerLayout
import com.movtery.zalithlauncher.ui.screens.game.elements.LogBox
import org.lwjgl.glfw.CallbackBridge

@Composable
fun GameScreen() {
    var enableLog by remember { mutableStateOf(false) }
    var logText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        MouseControlLayout(modifier = Modifier.fillMaxSize())

        if (enableLog) {
            LoggerBridge.setListener { log ->
                logText += "$log\n"
            }
            LogBox(logText = logText)
        } else {
            logText = ""
            LoggerBridge.setListener(null)
        }
    }
}

@Composable
fun MouseControlLayout(
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        //请求焦点，否则无法正常处理实体鼠标指针数据
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .focusable() //能够获得焦点，便于实体鼠标指针捕获
            .focusRequester(focusRequester)
    ) {
        val sensitivityFactor = 1.4 * (1080f / ZLApplication.DISPLAY_METRICS.heightPixels)

        val mode = ZLBridgeStates.cursorMode
        if (mode == CURSOR_ENABLED) {
            VirtualPointerLayout(
                modifier = Modifier.fillMaxSize(),
                onTap = { position ->
                    CallbackBridge.putMouseEventWithCoords(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT.toInt(), position.x.sumPosition(), position.y.sumPosition())
                },
                onPointerMove = { it.sendPosition() },
                onLongPress = {
                    CallbackBridge.putMouseEvent(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT.toInt(), true)
                },
                onLongPressEnd = {
                    CallbackBridge.putMouseEvent(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT.toInt(), false)
                },
                onMouseScroll = { scroll ->
                    CallbackBridge.sendScroll(scroll.x.toDouble(), scroll.y.toDouble())
                },
                onMouseButton = { button, pressed ->
                    val code = LWJGLCharSender.getMouseButton(button) ?: return@VirtualPointerLayout
                    CallbackBridge.sendMouseButton(code.toInt(), pressed)
                },
                controlMode = mouseControlMode,
                mouseSize = mouseSize.dp,
                mouseSpeed = mouseSpeed,
                longPressTimeoutMillis = mouseLongPressDelay.toLong()
            )
        }

        if (mode == CURSOR_DISABLED) {
            val tapMouseAction = gestureTapMouseAction.toAction()
            val longPressMouseAction = gestureLongPressMouseAction.toAction()

            TouchpadLayout(
                modifier = Modifier.fillMaxSize(),
                longPressTimeoutMillis = gestureLongPressDelay.toLong(),
                onTap = {
                    if (gestureControl) {
                        CallbackBridge.putMouseEvent(tapMouseAction)
                    }
                },
                onLongPress = {
                    if (gestureControl) {
                        CallbackBridge.putMouseEvent(longPressMouseAction, true)
                    }
                },
                onLongPressEnd = {
                    if (gestureControl) {
                        CallbackBridge.putMouseEvent(longPressMouseAction, false)
                    }
                },
                onPointerMove = { delta ->
                    val deltaX = (delta.x * sensitivityFactor).toFloat()
                    val deltaY = (delta.y * sensitivityFactor).toFloat()
                    CallbackBridge.sendCursorDelta(deltaX, deltaY)
                },
                onMouseMove = { delta ->
                    CallbackBridge.sendCursorDelta(delta.x, delta.y)
                },
                onMouseScroll = { scroll ->
                    CallbackBridge.sendScroll(scroll.x.toDouble(), scroll.y.toDouble())
                },
                onMouseButton = { button, pressed ->
                    val code = LWJGLCharSender.getMouseButton(button) ?: return@TouchpadLayout
                    CallbackBridge.sendMouseButton(code.toInt(), pressed)
                }
            )
        }
    }
}

private fun Offset.sendPosition() {
    CallbackBridge.sendCursorPos(x.sumPosition(), y.sumPosition())
}

private fun Float.sumPosition(): Float {
    return (this * scaleFactor)
}