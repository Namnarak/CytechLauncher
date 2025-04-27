package com.movtery.zalithlauncher.ui.control.mouse

import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalView
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
 * @param onMouseMove               实体鼠标指针移动回调
 * @param onMouseScroll             实体鼠标指针滚轮滑动
 * @param onMouseButton             实体鼠标指针按钮按下反馈
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
    onMouseMove: (Offset) -> Unit = {},
    onMouseScroll: (Offset) -> Unit = {},
    onMouseButton: (button: Int, pressed: Boolean) -> Unit = { _, _ -> },
    inputChange: Array<Any> = arrayOf(Unit)
) {
    val viewConfig = LocalViewConfiguration.current

    Box(
        modifier = modifier
            .pointerInput(*inputChange) {
                coroutineScope {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        if (down.type != PointerType.Touch) {
                            //过滤掉不是触摸的类型
                            return@awaitEachGesture
                        }

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

    SimpleMouseCapture(
        onMouseMove = onMouseMove,
        onMouseScroll = onMouseScroll,
        onMouseButton = onMouseButton
    )
}

@Composable
fun SimpleMouseCapture(
    onMouseMove: (Offset) -> Unit,
    onMouseScroll: (Offset) -> Unit,
    onMouseButton: (button: Int, pressed: Boolean) -> Unit
) {
    val view = LocalView.current

    DisposableEffect(view) {
        fun requestCapture() {
            view.requestFocus()
            view.requestPointerCapture()
        }

        val pointerListener = View.OnCapturedPointerListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_HOVER_MOVE, MotionEvent.ACTION_MOVE -> {
                    val relX = event.getAxisValue(MotionEvent.AXIS_RELATIVE_X)
                    val relY = event.getAxisValue(MotionEvent.AXIS_RELATIVE_Y)
                    val dx = if (relX != 0f) relX else event.x
                    val dy = if (relY != 0f) relY else event.y
                    onMouseMove(Offset(dx, dy))
                    true
                }
                MotionEvent.ACTION_SCROLL -> {
                    onMouseScroll(
                        Offset(
                            event.getAxisValue(MotionEvent.AXIS_HSCROLL),
                            event.getAxisValue(MotionEvent.AXIS_VSCROLL)
                        )
                    )
                    true
                }
                MotionEvent.ACTION_BUTTON_PRESS -> {
                    onMouseButton(event.actionButton, true)
                    true
                }
                MotionEvent.ACTION_BUTTON_RELEASE -> {
                    onMouseButton(event.actionButton, false)
                    true
                }
                else -> false
            }
        }

        //窗口获得焦点时请求指针捕获
        val focusListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (hasFocus) requestCapture()
        }

        view.setOnCapturedPointerListener(pointerListener)
        view.viewTreeObserver.addOnWindowFocusChangeListener(focusListener)

        onDispose {
            view.setOnCapturedPointerListener(null)
            view.viewTreeObserver.removeOnWindowFocusChangeListener(focusListener)
        }
    }
}