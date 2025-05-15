package com.movtery.zalithlauncher.ui.screens.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.bridge.CURSOR_DISABLED
import com.movtery.zalithlauncher.bridge.CURSOR_ENABLED
import com.movtery.zalithlauncher.bridge.ZLBridgeStates
import com.movtery.zalithlauncher.game.input.LWJGLCharSender
import com.movtery.zalithlauncher.game.keycodes.LwjglGlfwKeycode
import com.movtery.zalithlauncher.game.support.touch_controller.touchControllerModifier
import com.movtery.zalithlauncher.setting.enums.toAction
import com.movtery.zalithlauncher.setting.gestureControl
import com.movtery.zalithlauncher.setting.gestureLongPressDelay
import com.movtery.zalithlauncher.setting.gestureLongPressMouseAction
import com.movtery.zalithlauncher.setting.gestureTapMouseAction
import com.movtery.zalithlauncher.setting.mouseControlMode
import com.movtery.zalithlauncher.setting.mouseLongPressDelay
import com.movtery.zalithlauncher.setting.mouseSize
import com.movtery.zalithlauncher.setting.mouseSpeed
import com.movtery.zalithlauncher.setting.physicalMouseMode
import com.movtery.zalithlauncher.setting.scaleFactor
import com.movtery.zalithlauncher.ui.control.mouse.TouchpadLayout
import com.movtery.zalithlauncher.ui.control.mouse.VirtualPointerLayout
import com.movtery.zalithlauncher.ui.screens.game.elements.LogBox
import org.lwjgl.glfw.CallbackBridge

@Composable
fun GameScreen(
    isTouchProxyEnabled: Boolean,
    getWindowSize: () -> IntSize
) {
    var enableLog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        MouseControlLayout(
            isTouchProxyEnabled = isTouchProxyEnabled,
            getWindowSize = getWindowSize,
            modifier = Modifier.fillMaxSize()
        )

        LogBox(
            enableLog = enableLog,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun MouseControlLayout(
    isTouchProxyEnabled: Boolean,
    getWindowSize: () -> IntSize,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        val sensitivityFactor = 1.4 * (1080f / getWindowSize().height)

        val mode = ZLBridgeStates.cursorMode
        if (mode == CURSOR_ENABLED) {
            //非实体鼠标控制 -> 抓取系统指针，使用虚拟鼠标
            val requestPointerCapture = !physicalMouseMode

            VirtualPointerLayout(
                modifier = Modifier.fillMaxSize(),
                requestPointerCapture = requestPointerCapture,
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
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isTouchProxyEnabled) {
                            Modifier.touchControllerModifier()
                        } else Modifier
                    ),
                longPressTimeoutMillis = gestureLongPressDelay.toLong(),
                requestPointerCapture = true,
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