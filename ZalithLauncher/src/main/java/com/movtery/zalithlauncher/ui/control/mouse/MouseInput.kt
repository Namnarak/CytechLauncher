package com.movtery.zalithlauncher.ui.control.mouse

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalViewConfiguration
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.unit.StringSettingUnit
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 两种控制模式：
 * SLIDE = “滑动控制”
 * CLICK = “点击控制”
 */
enum class ControlMode(val nameRes: Int) {
    SLIDE(R.string.settings_control_mouse_control_mode_slide),
    CLICK(R.string.settings_control_mouse_control_mode_click)
}

/**
 * 将控制模式设置项转换为枚举对象
 */
fun StringSettingUnit.toControlMode(): ControlMode {
    val name = getValue()
    return ControlMode.entries.find { it.name == name } ?: ControlMode.SLIDE
}

/**
 * 原始触摸控制模拟层
 * @param controlMode               控制模式：SLIDE（滑动控制）、CLICK（点击控制）
 * @param longPressTimeoutMillis    长按触发检测时长
 * @param onTap                     点击回调，参数是触摸点在控件内的绝对坐标
 * @param onLongPress               长按开始回调
 * @param onLongPressEnd            长按结束回调
 * @param onPointerMove             指针移动回调，参数在 SLIDE 模式下是指针位置，CLICK 模式下是手指当前位置
 * @param inputChange               重新启动内部的 pointerInput 块，让触摸逻辑能够实时拿到最新的外部参数
 */
@Composable
fun TouchpadLayout(
    modifier: Modifier = Modifier,
    controlMode: ControlMode = ControlMode.SLIDE,
    longPressTimeoutMillis: Long = -1L,
    onTap: (Offset) -> Unit = {},
    onLongPress: () -> Unit = {},
    onLongPressEnd: () -> Unit = {},
    onPointerMove: (Offset) -> Unit = {},
    inputChange: Array<Any> = arrayOf(Unit)
) {
    val viewConfig = LocalViewConfiguration.current

    Box(
        modifier = modifier
            .pointerInput(*inputChange) {
                coroutineScope {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        var pointer = down
                        var isDragging = false
                        var longPressTriggered = false
                        val startPosition = down.position
                        val longPressJob = if (controlMode == ControlMode.SLIDE) launch {
                            //只在滑动点击模式下进行长按计时
                            val timeout = if (longPressTimeoutMillis > 0) longPressTimeoutMillis else viewConfig.longPressTimeoutMillis
                            delay(timeout)
                            if (!isDragging) {
                                longPressTriggered = true
                                onLongPress()
                            }
                        } else null

                        if (controlMode == ControlMode.CLICK) {
                            //点击模式下，如果触摸，无论如何都应该更新指针位置
                            onPointerMove(pointer.position)
                        }

                        drag(down.id) { change ->
                            isDragging = true
                            val delta = change.positionChange()
                            val distanceFromStart = (change.position - startPosition).getDistance()

                            if (controlMode == ControlMode.SLIDE) {
                                if (distanceFromStart > viewConfig.touchSlop) {
                                    //超出了滑动检测距离，说明是真的在进行滑动
                                    longPressJob?.cancel() //取消长按计时
                                }

                                if (isDragging || longPressTriggered) {
                                    pointer = change
                                    onPointerMove(delta)
                                }
                            } else {
                                if (!longPressTriggered) {
                                    longPressTriggered = true
                                    onLongPress()
                                }
                                pointer = change
                                onPointerMove(change.position)
                            }

                            change.consume()
                        }

                        longPressJob?.cancel()
                        if (longPressTriggered) {
                            onLongPressEnd()
                        } else {
                            when (controlMode) {
                                ControlMode.SLIDE -> {
                                    if (!isDragging && !longPressTriggered) {
                                        onTap(pointer.position)
                                    }
                                }
                                ControlMode.CLICK -> {
                                    //未进入长按，算一次点击事件
                                    onTap(pointer.position)
                                }
                            }
                        }
                    }
                }
            }
    )
}
