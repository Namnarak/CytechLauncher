package com.movtery.zalithlauncher.ui.screens.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.CURSOR_DISABLED
import com.movtery.zalithlauncher.bridge.CURSOR_ENABLED
import com.movtery.zalithlauncher.bridge.ZLBridgeStates
import com.movtery.zalithlauncher.game.input.LWJGLCharSender
import com.movtery.zalithlauncher.game.keycodes.LwjglGlfwKeycode
import com.movtery.zalithlauncher.game.support.touch_controller.touchControllerInputModifier
import com.movtery.zalithlauncher.game.support.touch_controller.touchControllerTouchModifier
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.toAction
import com.movtery.zalithlauncher.ui.control.mouse.TouchpadLayout
import com.movtery.zalithlauncher.ui.control.mouse.VirtualPointerLayout
import com.movtery.zalithlauncher.ui.screens.game.elements.LogBox
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import org.lwjgl.glfw.CallbackBridge

@Composable
fun GameScreen(
    version: Version,
    isGameRendering: Boolean,
    isTouchProxyEnabled: Boolean,
    onInputAreaRectUpdated: (IntRect?) -> Unit = {},
) {
    var enableLog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        GameInfoBox(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(all = 16.dp),
            version = version,
            isGameRendering = isGameRendering
        )

        MouseControlLayout(
            isTouchProxyEnabled = isTouchProxyEnabled,
            modifier = Modifier.fillMaxSize(),
            onInputAreaRectUpdated = onInputAreaRectUpdated,
        )

        LogBox(
            enableLog = enableLog,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun GameInfoBox(
    modifier: Modifier = Modifier,
    version: Version,
    isGameRendering: Boolean
) {
    AnimatedVisibility(
        visible = !isGameRendering,
        enter = expandVertically(animationSpec = getAnimateTween()),
        exit = shrinkVertically(animationSpec = getAnimateTween()) + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                //提示信息
                Column(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.game_loading),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.game_loading_version_name, version.getVersionName()),
                        style = MaterialTheme.typography.labelLarge
                    )
                    version.getVersionInfo()?.let { info ->
                        Text(
                            text = stringResource(R.string.game_loading_version_info, info.getInfoString()),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MouseControlLayout(
    isTouchProxyEnabled: Boolean,
    modifier: Modifier = Modifier,
    onInputAreaRectUpdated: (IntRect?) -> Unit = {},
) {
    Box(modifier = modifier.then(
            if (isTouchProxyEnabled) {
                Modifier.touchControllerTouchModifier()
                    .touchControllerInputModifier(
                        onInputAreaRectUpdated = onInputAreaRectUpdated,
                    )
            } else Modifier
        )) {
        //上次虚拟鼠标的位置
        val lastVirtualMousePos = remember { object { var value: Offset? = null } }

        val mode = ZLBridgeStates.cursorMode
        if (mode == CURSOR_ENABLED) {
            //非实体鼠标控制 -> 抓取系统指针，使用虚拟鼠标
            val requestPointerCapture = !AllSettings.physicalMouseMode.state

            VirtualPointerLayout(
                modifier = Modifier.fillMaxSize(),
                requestPointerCapture = requestPointerCapture,
                lastMousePosition = lastVirtualMousePos.value,
                onTap = { position ->
                    CallbackBridge.putMouseEventWithCoords(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT.toInt(), position.x.sumPosition(), position.y.sumPosition())
                },
                onPointerMove = { pos ->
                    pos.sendPosition()
                    //更新上次虚拟鼠标指针的位置
                    lastVirtualMousePos.value = pos
                },
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
                controlMode = AllSettings.mouseControlMode.state,
                mouseSize = AllSettings.mouseSize.state.dp,
                cursorSensitivity = AllSettings.cursorSensitivity.state,
                longPressTimeoutMillis = AllSettings.mouseLongPressDelay.state.toLong(),
                requestFocusKey = mode
            )
        }

        if (mode == CURSOR_DISABLED) {
            val speedFactor = AllSettings.mouseCaptureSensitivity.state / 100f
            val tapMouseAction = AllSettings.gestureTapMouseAction.state.toAction()
            val longPressMouseAction = AllSettings.gestureLongPressMouseAction.state.toAction()

            TouchpadLayout(
                modifier = Modifier.fillMaxSize(),
                longPressTimeoutMillis = AllSettings.gestureLongPressDelay.state.toLong(),
                requestPointerCapture = true,
                onTap = {
                    if (AllSettings.gestureControl.state) {
                        CallbackBridge.putMouseEvent(tapMouseAction)
                    }
                },
                onLongPress = {
                    if (AllSettings.gestureControl.state) {
                        CallbackBridge.putMouseEvent(longPressMouseAction, true)
                    }
                },
                onLongPressEnd = {
                    if (AllSettings.gestureControl.state) {
                        CallbackBridge.putMouseEvent(longPressMouseAction, false)
                    }
                },
                onPointerMove = { delta ->
                    CallbackBridge.sendCursorDelta(
                        (delta.x * speedFactor).toFloat(),
                        (delta.y * speedFactor).toFloat()
                    )
                },
                onMouseMove = { delta ->
                    CallbackBridge.sendCursorDelta(
                        delta.x * speedFactor,
                        delta.y * speedFactor
                    )
                },
                onMouseScroll = { scroll ->
                    CallbackBridge.sendScroll(scroll.x.toDouble(), scroll.y.toDouble())
                },
                onMouseButton = { button, pressed ->
                    val code = LWJGLCharSender.getMouseButton(button) ?: return@TouchpadLayout
                    CallbackBridge.sendMouseButton(code.toInt(), pressed)
                },
                requestFocusKey = mode
            )
        }
    }
}

private fun Offset.sendPosition() {
    CallbackBridge.sendCursorPos(x.sumPosition(), y.sumPosition())
}

private fun Float.sumPosition(): Float {
    return (this * (AllSettings.resolutionRatio.getValue() / 100f))
}