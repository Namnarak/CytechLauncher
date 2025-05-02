package com.movtery.zalithlauncher.game.support.touch_controller

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import com.movtery.zalithlauncher.game.support.touch_controller.ControllerProxy.proxyClient
import org.lwjgl.glfw.CallbackBridge
import top.fifthlight.touchcontroller.proxy.data.Offset

/**
 * 单独捕获触摸事件，为TouchController模组的控制代理提供信息
 */
@Composable
fun Modifier.touchControllerModifier() = this.pointerInput(Unit) {
    awaitPointerEventScope {
        val activePointers = mutableMapOf<PointerId, Int>()
        var nextPointerId = 1

        fun PointerInputChange.toProxyOffset(): Offset {
            val normalizedX = position.x / CallbackBridge.physicalWidth
            val normalizedY = position.y / CallbackBridge.physicalHeight
            return Offset(normalizedX, normalizedY)
        }

        while (true) {
            val event = awaitPointerEvent()

            when (event.type) {
                PointerEventType.Press -> {
                    event.changes.forEach { change ->
                        if (change.changedToDown() && !activePointers.containsKey(change.id)) {
                            val pointerId = nextPointerId++
                            activePointers[change.id] = pointerId
                            proxyClient?.addPointer(pointerId, change.toProxyOffset())
                        }
                    }
                }

                PointerEventType.Move -> {
                    event.changes.forEach { change ->
                        activePointers[change.id]?.let { pointerId ->
                            proxyClient?.addPointer(pointerId, change.toProxyOffset())
                        }
                    }
                }

                PointerEventType.Release -> {
                    event.changes.forEach { change ->
                        if (change.changedToUp()) {
                            activePointers.remove(change.id)?.let { pointerId ->
                                proxyClient?.removePointer(pointerId)
                            }
                        }
                    }
                }
            }
        }
    }
}
