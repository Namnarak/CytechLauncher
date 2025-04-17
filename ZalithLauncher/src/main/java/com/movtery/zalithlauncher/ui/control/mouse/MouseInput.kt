package com.movtery.zalithlauncher.ui.control.mouse

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ZLApplication
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.unit.StringSettingUnit
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.hypot

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
 * 虚拟指针模拟层
 * @param controlMode       控制模式：SLIDE（滑动控制）、CLICK（点击控制）
 * @param onTap             点击回调，参数是触摸点在控件内的绝对坐标
 * @param onLongPress       长按开始回调
 * @param onLongPressEnd    长按结束回调
 * @param onPointerMove     指针移动回调，参数在 SLIDE 模式下是指针位置，CLICK 模式下是手指当前位置
 * @param customPointer     自定义指针图标文件
 * @param mouseSize         指针大小
 * @param mouseSpeed        指针移动速度（滑动模式生效）
 */
@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
fun VirtualPointerLayout(
    modifier: Modifier = Modifier,
    controlMode: ControlMode = AllSettings.mouseControlMode.toControlMode(),
    onTap: (Offset) -> Unit = {},
    onLongPress: () -> Unit = {},
    onLongPressEnd: () -> Unit = {},
    onPointerMove: (Offset) -> Unit = {},
    customPointer: File? = null,
    mouseSize: Dp = AllSettings.mouseSize.getValue().dp,
    mouseSpeed: Int = AllSettings.mouseSpeed.getValue()
) {
    val screenWidth = ZLApplication.DISPLAY_METRICS.widthPixels.toFloat()
    val screenHeight = ZLApplication.DISPLAY_METRICS.heightPixels.toFloat()
    var pointerPosition by remember { mutableStateOf(Offset(screenWidth / 2, screenHeight / 2)) }

    val speedFactor = mouseSpeed / 100f

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = customPointer?.let { rememberAsyncImagePainter(it) }
                ?: painterResource(R.drawable.ic_mouse_pointer),
            contentDescription = null,
            alignment = Alignment.TopStart,
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(mouseSize)
                .offset(
                    x = with(LocalDensity.current) { pointerPosition.x.toDp() },
                    y = with(LocalDensity.current) { pointerPosition.y.toDp() }
                )
        )

        TouchpadLayout(
            controlMode = controlMode,
            onTap = { fingerPos ->
                onTap(
                    if (controlMode == ControlMode.CLICK) {
                        //当前手指的绝对坐标
                        pointerPosition = fingerPos
                        fingerPos
                    } else {
                        pointerPosition
                    }
                )
            },
            onLongPress = onLongPress,
            onLongPressEnd = onLongPressEnd,
            onPointerMove = { offset ->
                pointerPosition =  if (controlMode == ControlMode.SLIDE) {
                    Offset(
                        x = (pointerPosition.x + offset.x * speedFactor)
                            .coerceIn(0f, screenWidth),
                        y = (pointerPosition.y + offset.y * speedFactor)
                            .coerceIn(0f, screenHeight)
                    )
                } else {
                    //当前手指的绝对坐标
                    offset
                }
                onPointerMove(pointerPosition)
            },
            inputChange = arrayOf(speedFactor, controlMode)
        )
    }
}

/**
 * 原始触摸控制模拟层
 * @param controlMode       控制模式：SLIDE（滑动控制）、CLICK（点击控制）
 * @param onTap             点击回调，参数是触摸点在控件内的绝对坐标
 * @param onLongPress       长按开始回调
 * @param onLongPressEnd    长按结束回调
 * @param onPointerMove     指针移动回调，参数在 SLIDE 模式下是指针位置，CLICK 模式下是手指当前位置
 * @param inputChange       重新启动内部的 pointerInput 块，让触摸逻辑能够实时拿到最新的外部参数
 */
@Composable
fun TouchpadLayout(
    modifier: Modifier = Modifier,
    controlMode: ControlMode = ControlMode.SLIDE,
    onTap: (Offset) -> Unit = {},
    onLongPress: () -> Unit = {},
    onLongPressEnd: () -> Unit = {},
    onPointerMove: (Offset) -> Unit = {},
    inputChange: Array<Any> = arrayOf(Unit)
) {
    val viewConfig = LocalViewConfiguration.current
    var initialDownPosition by remember { mutableStateOf<Offset?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    var longPressTriggered by remember { mutableStateOf(false) }
    var shouldEndLongPressOnRelease by remember { mutableStateOf(false) }
    val longPressJob = remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(*inputChange) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pos = event.changes.first().position

                        when (event.type) {
                            PointerEventType.Press -> {
                                initialDownPosition = pos
                                if (controlMode == ControlMode.CLICK) {
                                    onPointerMove(pos)
                                }
                                isDragging = false
                                longPressTriggered = false
                                longPressJob.value?.cancel()
                                longPressJob.value = scope.launch {
                                    delay(viewConfig.longPressTimeoutMillis)
                                    if (!isDragging) {
                                        onLongPress()
                                        longPressTriggered = true
                                    }
                                }
                            }

                            PointerEventType.Move -> {
                                if (controlMode == ControlMode.CLICK) {
                                    initialDownPosition?.let { startPos ->
                                        val delta = pos - startPos
                                        if (!isDragging) {
                                            if (hypot(delta.x, delta.y) > viewConfig.touchSlop) {
                                                isDragging = true
                                                longPressJob.value?.cancel()
                                                if (longPressTriggered) {
                                                    shouldEndLongPressOnRelease = true //标记延迟取消
                                                }
                                            }
                                        }
                                        onPointerMove(pos)
                                    }
                                } else {
                                    initialDownPosition?.let { startPos ->
                                        val delta = pos - startPos
                                        if (!isDragging) {
                                            if (hypot(delta.x, delta.y) > viewConfig.touchSlop) {
                                                isDragging = true
                                                longPressJob.value?.cancel()
                                                if (longPressTriggered) {
                                                    shouldEndLongPressOnRelease = true //标记延迟取消
                                                }
                                            }
                                        }
                                        if (isDragging || longPressTriggered) {
                                            onPointerMove(delta)
                                            initialDownPosition = pos
                                        }
                                    }
                                }
                            }

                            PointerEventType.Release -> {
                                initialDownPosition?.let { startPos ->
                                    if (!isDragging && !longPressTriggered) {
                                        if (hypot(pos.x - startPos.x, pos.y - startPos.y) < viewConfig.touchSlop) {
                                            onTap(pos)
                                        }
                                    }
                                }

                                if (longPressTriggered || shouldEndLongPressOnRelease) {
                                    onLongPressEnd()
                                }

                                initialDownPosition = null
                                isDragging = false
                                longPressTriggered = false
                                shouldEndLongPressOnRelease = false
                                longPressJob.value?.cancel()
                            }
                        }
                    }
                }
            }
    )
}