package com.movtery.layer_controller

import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAll
import com.movtery.layer_controller.data.ButtonShape
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.event.switchLayer
import com.movtery.layer_controller.layout.TextButton
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.layer_controller.observable.ObservableNormalData
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.layer_controller.utils.getWidgetPosition
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 控制布局画布
 * @param observedLayout 需要监听并绘制的控制布局
 * @param checkOccupiedPointers 检查已占用的指针，防止底层正在被使用的指针仍被控制布局画布处理
 * @param opacity 控制布局画布整体不透明度 0f~1f
 * @param onClickEvent 控制按键点击事件回调（切换层级事件已优先处理）
 * @param markPointerAsMoveOnly 标记指针为仅接受滑动处理
 * @param enabled 是否启用
 */
@Composable
fun ControlBoxLayout(
    modifier: Modifier = Modifier,
    observedLayout: ObservableControlLayout? = null,
    isCursorGrabbing: Boolean,
    checkOccupiedPointers: (PointerId) -> Boolean,
    @FloatRange(0.0, 1.0) opacity: Float = 1f,
    onClickEvent: (event: ClickEvent, pressed: Boolean) -> Unit = { _, _ -> },
    markPointerAsMoveOnly: (PointerId) -> Unit = {},
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    when {
        observedLayout == null -> {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.BottomCenter
            ) {
                //控件处于加载状态
                LinearProgressIndicator(
                    modifier = Modifier.padding(all = 16.dp)
                )
            }
        }
        else -> {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                key(observedLayout.hashCode()) {
                    BaseControlBoxLayout(
                        modifier = modifier,
                        observedLayout = observedLayout,
                        checkOccupiedPointers = checkOccupiedPointers,
                        opacity = opacity,
                        onClickEvent = onClickEvent,
                        markPointerAsMoveOnly = markPointerAsMoveOnly,
                        isCursorGrabbing = isCursorGrabbing,
                        enabled = enabled,
                        content = content
                    )
                }
            }
        }
    }
}

/**
 * 控制布局画布
 */
@Composable
private fun BaseControlBoxLayout(
    modifier: Modifier = Modifier,
    observedLayout: ObservableControlLayout,
    checkOccupiedPointers: (PointerId) -> Boolean,
    @FloatRange(0.0, 1.0) opacity: Float,
    onClickEvent: (event: ClickEvent, pressed: Boolean) -> Unit,
    markPointerAsMoveOnly: (PointerId) -> Unit,
    isCursorGrabbing: Boolean,
    enabled: Boolean,
    content: @Composable BoxScope.() -> Unit
) {
//    val isDarkMode by rememberUpdatedState(isSystemInDarkTheme())

    val layers1 by observedLayout.layers.collectAsState()
    val layers = remember(layers1) { layers1.reversed() }
    val styles by observedLayout.styles.collectAsState()

    val sizes = remember { mutableStateMapOf<ObservableWidget, IntSize>() }
    val activeButtons = remember { mutableStateMapOf<PointerId, List<ObservableNormalData>>() }
    val onClickEvent1 by rememberUpdatedState(onClickEvent)
    val checkOccupiedPointers1 by rememberUpdatedState(checkOccupiedPointers)
    val isCursorGrabbing1 by rememberUpdatedState(isCursorGrabbing)

    val screenSize by rememberUpdatedState(LocalWindowInfo.current.containerSize)

    fun handleClickEvents(
        data: ObservableNormalData,
        extra: ((event: ClickEvent) -> Unit)? = null
    ) {
        for (event in data.clickEvents) {
            extra?.invoke(event)
            onClickEvent1(event, data.isPressed)
        }
    }

    fun prePressStart(data: ObservableNormalData) {
        if (data.isPressed && !data.isToggleable) return

        data.isPressed = if (data.isToggleable) !data.isPressed else true

        handleClickEvents(data) { event ->
            switchLayer(event, layers) { layer ->
                layer.hide = if (data.isToggleable) data.isPressed else !layer.hide
            }
        }
    }

    fun prePressEnd(data: ObservableNormalData) {
        if (!data.isPressed && !data.isToggleable) return

        //非可开关按钮在松开时复位
        if (!data.isToggleable) data.isPressed = false

        handleClickEvents(data)
    }

    Box(
        modifier = modifier
            .pointerInput(layers, sizes, enabled) {
                awaitPointerEventScope {
                    while (enabled) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)

                        event.changes.forEach { change ->
                            val position = change.position
                            val pointerId = change.id
                            val isPressed = change.pressed

                            //在可见控件层中，收集所有可见的按钮
                            val visibleWidgets = layers1 //使用原始控件层顺序，保证触摸逻辑正常
                                .filter { !it.hide && checkVisibility(isCursorGrabbing1, it.visibilityType) }
                                .flatMap { layer ->
                                    //顶向下的顺序影响控件层的处理优先级
                                    layer.normalButtons.value.reversed()
                                }

                            //查找当前指针在哪些按钮上
                            val targetButtons = visibleWidgets
                                .filter { data ->
                                    if (!checkVisibility(isCursorGrabbing1, data.visibilityType)) {
                                        //隐藏了，不响应事件
                                        return@filter false
                                    }

                                    val size = sizes[data] ?: IntSize.Zero
                                    val offset = getWidgetPosition(data, size, screenSize)

                                    val inBoundingBox = position.x in offset.x..(offset.x + size.width) &&
                                            position.y in offset.y..(offset.y + size.height)

                                    inBoundingBox
//                                    if (!inBoundingBox) {
//                                        false
//                                    } else {
//                                        //获取按钮样式和圆角信息
//                                        val style = styles.takeIf {
//                                            data.buttonStyle != null
//                                        }?.find {
//                                            it.uuid == data.buttonStyle
//                                        } ?: ObservableButtonStyle.Default
//                                        val borderRadius = (if (isDarkMode) style.darkStyle else style.lightStyle).borderRadius
//
//                                        if (borderRadius.topStart == 0f && borderRadius.topEnd == 0f &&
//                                            borderRadius.bottomEnd == 0f && borderRadius.bottomStart == 0f
//                                        ) {
//                                            true //都是直角
//                                        } else {
//                                            //检查触摸点是否在圆角矩形内
//                                            isPointInRoundedRect(
//                                                point = position,
//                                                rectOffset = offset,
//                                                rectSize = size,
//                                                cornerRadius = borderRadius
//                                            )
//                                        }
//                                    }
                                }.let { list ->
                                    val nonSwippleButtons = list.filter { !it.isSwipple || !(it.isSwipple && it.isPenetrable) }

                                    when {
                                        nonSwippleButtons.isEmpty() -> list
                                        //如果有不可穿透按钮，只保留最顶层的一个不可穿透按钮及其上层的所有可穿透按钮
                                        else -> {
                                            val topNonSwipple = nonSwippleButtons.first()
                                            val topNonSwippleIndex = list.indexOf(topNonSwipple)
                                            list.subList(0, topNonSwippleIndex + 1).filter {
                                                //作为特性存在，筛除即可穿透又可滑动的按钮
                                                //因为我发现我怎么都修不好:(
                                                !(it.isSwipple && it.isPenetrable)
                                            }
                                        }
                                    }
                                }

                            val activeButtonList = activeButtons[pointerId] ?: emptyList()

                            if (isPressed) {
                                when {
                                    targetButtons.isEmpty() -> {}
                                    else -> {
                                        var consumeEvent = true
                                        for (targetButton in targetButtons) {
                                            if (
                                                checkOccupiedPointers1(pointerId) &&
                                                !(targetButton.isPenetrable && targetButton.isSwipple)
                                            ) {
                                                return@forEach //拒绝处理该事件
                                            }

                                            if (activeButtonList.isEmpty()) {
                                                //新的按下事件
                                                activeButtons[pointerId] = activeButtonList + listOf(targetButton)
                                                if (!targetButton.isPenetrable) {
                                                    change.consume()
                                                    consumeEvent = false
                                                } else {
                                                    //将指针标记为仅接受滑动处理
                                                    //期望子级不对点击事件等进行处理
                                                    markPointerAsMoveOnly(pointerId)
                                                }
                                                prePressStart(targetButton)
                                            } else if (targetButton !in activeButtonList && targetButton.isSwipple) {
                                                //滑动到其他按钮时的处理
                                                if (activeButtonList.fastAll { it.isSwipple } && targetButton.isSwipple) {
                                                    activeButtons[pointerId] = activeButtonList + listOf(targetButton)
                                                    prePressStart(targetButton)
                                                }
                                            }
                                            if (!consumeEvent) break
                                        }
                                    }
                                }

                                //检查是否移出边界
                                for (button in activeButtonList.filter { it.isSwipple }) {
                                    val size = sizes[button] ?: IntSize.Zero
                                    val offset = getWidgetPosition(button, size, screenSize)
                                    val isOutOfBounds = position.x !in offset.x..(offset.x + size.width) ||
                                            position.y !in offset.y..(offset.y + size.height)

                                    if (isOutOfBounds) {
                                        prePressEnd(button)
                                    } else {
                                        prePressStart(button)
                                    }
                                }
                            } else {
                                activeButtons.remove(pointerId)?.let { buttonList ->
                                    buttonList.forEach { prePressEnd(it) }
                                }
                            }
                        }
                    }
                }
            }
    ) {
        content()

        if (enabled) {
            ControlsRendererLayer(
                opacity = opacity,
                layers = layers,
                styles = styles,
                sizes = sizes,
                applySize = { data, size ->
                    sizes[data] = size
                },
                screenSize = screenSize,
                isCursorGrabbing = isCursorGrabbing1
            )
        }
    }
}

@Composable
private fun ControlsRendererLayer(
    @FloatRange(0.0, 1.0) opacity: Float,
    layers: List<ObservableControlLayer>,
    styles: List<ObservableButtonStyle>,
    sizes: Map<ObservableWidget, IntSize>,
    applySize: (ObservableWidget, IntSize) -> Unit,
    screenSize: IntSize,
    isCursorGrabbing: Boolean
) {
    Layout(
        modifier = Modifier.alpha(alpha = opacity),
        content = {
            //按图层顺序渲染所有可见的控件
            layers.forEach { layer ->
                val layerVisibility = !layer.hide && checkVisibility(isCursorGrabbing, layer.visibilityType)
                val normalButtons by layer.normalButtons.collectAsState()
                val textBoxes by layer.textBoxes.collectAsState()

                textBoxes.forEach { data ->
                    TextButton(
                        isEditMode = false,
                        data = data,
                        visible = layerVisibility && checkVisibility(isCursorGrabbing, data.visibilityType),
                        getSize = { d1 -> sizes[d1] ?: IntSize.Zero },
                        getOtherWidgets = { emptyList() }, //不需要计算吸附
                        snapThresholdValue = 4.dp,
                        getStyle = { styles.takeIf { data.buttonStyle != null }?.find { it.uuid == data.buttonStyle } },
                        isPressed = false //文本框不需要按压状态
                    )
                }

                normalButtons.forEach { data ->
                    TextButton(
                        isEditMode = false,
                        data = data,
                        visible = layerVisibility && checkVisibility(isCursorGrabbing, data.visibilityType),
                        getOtherWidgets = { emptyList() }, //不需要计算吸附
                        snapThresholdValue = 4.dp,
                        getSize = { d1 -> sizes[d1] ?: IntSize.Zero },
                        getStyle = { styles.takeIf { data.buttonStyle != null }?.find { it.uuid == data.buttonStyle } },
                        isPressed = data.isPressed
                    )
                }
            }
        }
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        var index = 0
        layers.forEach { layer ->
            layer.textBoxes.value.forEach { data ->
                if (index < placeables.size) {
                    val placeable = placeables[index]
                    applySize(
                        data,
                        IntSize(placeable.width, placeable.height)
                    )
                    index++
                }
            }

            layer.normalButtons.value.forEach { data ->
                if (index < placeables.size) {
                    val placeable = placeables[index]
                    applySize(
                        data,
                        IntSize(placeable.width, placeable.height)
                    )
                    index++
                }
            }
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            var placeableIndex = 0
            layers.forEach { layer ->
                layer.textBoxes.value.forEach { data ->
                    if (placeableIndex < placeables.size) {
                        val placeable = placeables[placeableIndex]
                        val position = getWidgetPosition(
                            data = data,
                            widgetSize = IntSize(placeable.width, placeable.height),
                            screenSize = screenSize
                        )
                        placeable.place(position.x.toInt(), position.y.toInt())
                        placeableIndex++
                    }
                }

                layer.normalButtons.value.forEach { data ->
                    if (placeableIndex < placeables.size) {
                        val placeable = placeables[placeableIndex]
                        val position = getWidgetPosition(
                            data = data,
                            widgetSize = IntSize(placeable.width, placeable.height),
                            screenSize = screenSize
                        )
                        placeable.place(position.x.toInt(), position.y.toInt())
                        placeableIndex++
                    }
                }
            }
        }
    }
}

/**
 * 通过虚拟鼠标抓获情况，判断当前是否应当展示控件
 */
private fun checkVisibility(
    isCursorGrabbing: Boolean,
    visibilityType: VisibilityType
): Boolean {
    return when (visibilityType) {
        VisibilityType.ALWAYS -> true
        VisibilityType.IN_GAME -> isCursorGrabbing
        VisibilityType.IN_MENU -> !isCursorGrabbing
    }
}

/**
 * 检查触点是否在按钮的圆角矩形内
 */
@Suppress("unused")
private fun isPointInRoundedRect(
    point: Offset,
    rectOffset: Offset,
    rectSize: IntSize,
    cornerRadius: ButtonShape
): Boolean {
    val left = rectOffset.x
    val top = rectOffset.y
    val right = left + rectSize.width
    val bottom = top + rectSize.height

    //检查是否在中心矩形区域内
    val centerRect = Rect(
        left + cornerRadius.topStart,
        top + max(cornerRadius.topStart, cornerRadius.topEnd),
        right - cornerRadius.topEnd,
        bottom - max(cornerRadius.bottomStart, cornerRadius.bottomEnd)
    )

    if (point.x in centerRect.left..centerRect.right &&
        point.y in centerRect.top..centerRect.bottom) {
        return true
    }

    //左上角
    if (point.x in left..(left + cornerRadius.topStart) &&
        point.y in top..(top + cornerRadius.topStart)) {
        val distance = sqrt(
            (point.x - (left + cornerRadius.topStart)).pow(2) +
                    (point.y - (top + cornerRadius.topStart)).pow(2)
        )
        return distance <= cornerRadius.topStart
    }

    //右上角
    if (point.x in (right - cornerRadius.topEnd)..right &&
        point.y in top..(top + cornerRadius.topEnd)) {
        val distance = sqrt(
            (point.x - (right - cornerRadius.topEnd)).pow(2) +
                    (point.y - (top + cornerRadius.topEnd)).pow(2)
        )
        return distance <= cornerRadius.topEnd
    }

    //右下角
    if (point.x in (right - cornerRadius.bottomEnd)..right &&
        point.y in (bottom - cornerRadius.bottomEnd)..bottom) {
        val distance = sqrt(
            (point.x - (right - cornerRadius.bottomEnd)).pow(2) +
                    (point.y - (bottom - cornerRadius.bottomEnd)).pow(2)
        )
        return distance <= cornerRadius.bottomEnd
    }

    //左下角
    if (point.x in left..(left + cornerRadius.bottomStart) &&
        point.y in (bottom - cornerRadius.bottomStart)..bottom) {
        val distance = sqrt(
            (point.x - (left + cornerRadius.bottomStart)).pow(2) +
                    (point.y - (bottom - cornerRadius.bottomStart)).pow(2)
        )
        return distance <= cornerRadius.bottomStart
    }

    return false
}