package com.movtery.layer_controller.utils

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.data.ButtonPosition
import com.movtery.layer_controller.data.ButtonShape.Companion.toAndroidShape
import com.movtery.layer_controller.data.ButtonSize
import com.movtery.layer_controller.observable.ObservableBaseData
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.utils.snap.GuideLine
import com.movtery.layer_controller.utils.snap.LineDirection
import com.movtery.layer_controller.utils.snap.SnapMode
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * 自动处理按钮拖动改变位置
 * @param onTapInEditMode 在编辑模式下点击了按钮
 * @param enableSnap 是否开启吸附功能
 * @param snapMode 吸附模式
 * @param localSnapRange 局部吸附范围（仅在Local模式下有效）
 * @param otherWidgets 其他控件的信息，用于计算吸附位置
 * @param snapThresholdValue 吸附距离阈值
 * @param drawLine 绘制吸附参考线
 * @param onLineCancel 取消吸附参考线
 */
@Composable
internal fun Modifier.editMode(
    isEditMode: Boolean,
    data: ObservableBaseData,
    getSize: () -> IntSize,
    enableSnap: Boolean,
    snapMode: SnapMode,
    localSnapRange: Dp,
    otherWidgets: List<Pair<ObservableBaseData, IntSize>>,
    snapThresholdValue: Dp,
    drawLine: (ObservableBaseData, List<GuideLine>) -> Unit,
    onLineCancel: (ObservableBaseData) -> Unit,
    onTapInEditMode: () -> Unit = {}
): Modifier {
    val screenSize by rememberUpdatedState(LocalWindowInfo.current.containerSize)
    val getSize1 by rememberUpdatedState(getSize)

    val enableSnap1 by rememberUpdatedState(enableSnap)
    val snapMode1 by rememberUpdatedState(snapMode)

    val otherWidgets1 by rememberUpdatedState(otherWidgets)
    val drawLine1 by rememberUpdatedState(drawLine)
    val onLineCancel1 by rememberUpdatedState(onLineCancel)

    val onTapInEditMode1 by rememberUpdatedState(onTapInEditMode)

    val density = LocalDensity.current
    val snapThreshold = with(density) { snapThresholdValue.toPx() }
    val localSnapRangePx = with(density) { localSnapRange.toPx() }

    return this.then(
        if (isEditMode) {
            Modifier
                .pointerInput(data, snapThreshold, localSnapRangePx) {
                    detectDragGestures(
                        onDragStart = {
                            data.isEditingPos = false
                            data.movingOffset = Offset.Zero
                            val currentOffset = getWidgetPosition(data, getSize1(), screenSize)
                            data.movingOffset = currentOffset
                            data.isEditingPos = true
                            onLineCancel1(data)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val currentOffset = getWidgetPosition(data, getSize1(), screenSize)
                            val currentSize = getSize1()

                            var newX = currentOffset.x + dragAmount.x
                            var newY = currentOffset.y + dragAmount.y

                            val maxX = screenSize.width.toFloat() - currentSize.width
                            val maxY = screenSize.height.toFloat() - currentSize.height

                            newX = newX.coerceIn(0f, maxX)
                            newY = newY.coerceIn(0f, maxY)

                            val newPosition = Offset(newX, newY)

                            val finalPosition = if (enableSnap1) {
                                calculateSnapPosition(
                                    currentPosition = newPosition,
                                    currentSize = currentSize,
                                    screenSize = screenSize,
                                    otherWidgets = otherWidgets1,
                                    snapThreshold = snapThreshold,
                                    snapMode = snapMode1,
                                    localSnapRange = localSnapRangePx,
                                    drawLine = { lines ->
                                        drawLine1(data, lines)
                                    },
                                    onLineCancel = {
                                        onLineCancel1(data)
                                    }
                                )
                            } else {
                                onLineCancel(data)
                                newPosition
                            }

                            data.position = finalPosition.toPercentagePosition(
                                widgetSize = currentSize,
                                screenSize = screenSize
                            ).also {
                                data.movingOffset = newPosition
                            }
                        },
                        onDragEnd = {
                            data.isEditingPos = false
                            data.movingOffset = Offset.Zero
                            onLineCancel1(data)
                        },
                        onDragCancel = {
                            data.isEditingPos = false
                            data.movingOffset = Offset.Zero
                            onLineCancel1(data)
                        }
                    )
                }
                .pointerInput(data) {
                    detectTapGestures(
                        onTap = { onTapInEditMode1() }
                    )
                }
        } else Modifier
    )
}

/**
 * 计算吸附位置
 * @param snapThreshold 吸附参考距离
 * @param snapMode 吸附模式
 * @param localSnapRange 局部吸附范围（像素）
 * @param drawLine 通知绘制参考线
 * @param onLineCancel 通知取消绘制参考线
 */
private fun calculateSnapPosition(
    currentPosition: Offset,
    currentSize: IntSize,
    screenSize: IntSize,
    otherWidgets: List<Pair<ObservableBaseData, IntSize>>,
    snapThreshold: Float,
    snapMode: SnapMode,
    localSnapRange: Float,
    drawLine: (List<GuideLine>) -> Unit,
    onLineCancel: () -> Unit,
): Offset {
    var newX = currentPosition.x
    var newY = currentPosition.y

    //当前控件的边界
    val currentLeft = newX
    val currentRight = newX + currentSize.width
    val currentTop = newY
    val currentBottom = newY + currentSize.height

    val lines = mutableListOf<GuideLine>()

    for ((otherData, otherSize) in otherWidgets) {
        val otherPosition = getWidgetPosition(otherData, otherSize, screenSize)
        val otherLeft = otherPosition.x
        val otherRight = otherPosition.x + otherSize.width
        val otherTop = otherPosition.y
        val otherBottom = otherPosition.y + otherSize.height

        //在局部模式下，检查是否在吸附范围内
        if (snapMode == SnapMode.Local) {
            val minDistance = calculateMinDistanceBetweenRects(
                currentLeft, currentTop, currentRight, currentBottom,
                otherLeft, otherTop, otherRight, otherBottom
            )

            if (minDistance > localSnapRange) {
                continue
            }
        }

        //左/右
        val rightToLeft = abs(currentRight - otherLeft)
        val leftToRight = abs(currentLeft - otherRight)

        //顶/底
        val bottomToTop = abs(currentBottom - otherTop)
        val topToBottom = abs(currentTop - otherBottom)

        //同侧
        val leftToLeft = abs(currentLeft - otherLeft)
        val rightToRight = abs(currentRight - otherRight)
        val topToTop = abs(currentTop - otherTop)
        val bottomToBottom = abs(currentBottom - otherBottom)

        if (rightToLeft < snapThreshold) {
            newX = otherLeft - currentSize.width
            lines.add(GuideLine(LineDirection.Vertical, otherLeft))
        } else if (leftToRight < snapThreshold) {
            newX = otherRight
            lines.add(GuideLine(LineDirection.Vertical, otherRight))
        }

        if (bottomToTop < snapThreshold) {
            newY = otherTop - currentSize.height
            lines.add(GuideLine(LineDirection.Horizontal, otherTop))
        } else if (topToBottom < snapThreshold) {
            newY = otherBottom
            lines.add(GuideLine(LineDirection.Horizontal, otherBottom))
        }

        if (leftToLeft < snapThreshold) {
            newX = otherLeft
            lines.add(GuideLine(LineDirection.Vertical, otherLeft))
        } else if (rightToRight < snapThreshold) {
            newX = otherRight - currentSize.width
            lines.add(GuideLine(LineDirection.Vertical, otherRight))
        }

        if (topToTop < snapThreshold) {
            newY = otherTop
            lines.add(GuideLine(LineDirection.Horizontal, otherTop))
        } else if (bottomToBottom < snapThreshold) {
            newY = otherBottom - currentSize.height
            lines.add(GuideLine(LineDirection.Horizontal, otherBottom))
        }
    }

    if (lines.isNotEmpty()) {
        drawLine(lines)
    } else {
        onLineCancel()
    }

    return Offset(newX, newY)
}

/**
 * 自动处理按钮大小
 */
@Composable
internal fun Modifier.buttonSize(
    data: ObservableBaseData
): Modifier {
    val size = data.buttonSize
    return this.then(
        when (size.type) {
            ButtonSize.Type.Dp -> Modifier.size(
                width = size.widthDp.dp,
                height = size.heightDp.dp
            )

            //百分比计算方式，根据屏幕的高宽来计算按钮的大小尺寸
            ButtonSize.Type.Percentage -> {
                val containerSize = LocalWindowInfo.current.containerSize
                val screenWidth = containerSize.width.toFloat()
                val screenHeight = containerSize.height.toFloat()

                val widthReference = when (size.widthReference) {
                    ButtonSize.Reference.ScreenWidth -> screenWidth
                    ButtonSize.Reference.ScreenHeight -> screenHeight
                }
                val heightReference = when (size.heightReference) {
                    ButtonSize.Reference.ScreenWidth -> screenWidth
                    ButtonSize.Reference.ScreenHeight -> screenHeight
                }

                val density = LocalDensity.current
                with(density) {
                    val buttonWidth = (widthReference * (size.widthPercentage / 10000f)).toDp()
                    val buttonHeight = (heightReference * (size.heightPercentage / 10000f)).toDp()
                    Modifier.size(width = buttonWidth, height = buttonHeight)
                }
            }

            ButtonSize.Type.WrapContent -> Modifier.wrapContentSize()
        }
    )
}


/**
 * 自动处理按钮内容颜色
 * @param isPressed 按钮是否处于按下的状态
 */
@Composable
internal fun buttonContentColorAsState(
    style: ObservableButtonStyle,
    isPressed: Boolean
): State<Color> {
    val isDark = isSystemInDarkTheme()
    val themeStyle = if (isDark) style.darkStyle else style.lightStyle

    val targetColor = if (isPressed) themeStyle.pressedContentColor else themeStyle.contentColor

    return if (style.animateSwap) {
        animateColorAsState(targetColor, label = "contentColorAnimation")
    } else {
        remember(targetColor) { mutableStateOf(targetColor) }
    }
}

/**
 * 自动处理按钮样式 - 优化版本
 * @param isPressed 按钮是否处于按下的状态
 */
@Composable
internal fun Modifier.buttonStyle(
    style: ObservableButtonStyle,
    isPressed: Boolean
): Modifier {
    val isDark = isSystemInDarkTheme()
    val themeStyle = if (isDark) style.darkStyle else style.lightStyle

    val alpha = remember(themeStyle, isPressed) {
        if (isPressed) themeStyle.pressedAlpha else themeStyle.alpha
    }
    val backgroundColor = remember(themeStyle, isPressed) {
        if (isPressed) themeStyle.pressedBackgroundColor else themeStyle.backgroundColor
    }
    val borderWidth = remember(themeStyle, isPressed) {
        if (isPressed) themeStyle.pressedBorderWidth.dp else themeStyle.borderWidth.dp
    }
    val borderColor = remember(themeStyle, isPressed) {
        if (isPressed) themeStyle.pressedBorderColor else themeStyle.borderColor
    }
    val borderRadius = remember(themeStyle, isPressed) {
        if (isPressed) themeStyle.pressedBorderRadius.toAndroidShape() else themeStyle.borderRadius.toAndroidShape()
    }

    return if (style.animateSwap) {
        this.animatedButtonModifier(alpha, backgroundColor, borderWidth, borderColor, borderRadius)
    } else {
        this.staticButtonModifier(alpha, backgroundColor, borderWidth, borderColor, borderRadius)
    }
}

@Composable
private fun Modifier.animatedButtonModifier(
    alpha: Float,
    backgroundColor: Color,
    borderWidth: Dp,
    borderColor: Color,
    borderRadius: RoundedCornerShape
): Modifier {
    val alphaA by animateFloatAsState(alpha, label = "alphaAnimation")
    val backgroundColorA by animateColorAsState(backgroundColor, label = "bgAnimation")
    val borderWidthA by animateDpAsState(borderWidth, label = "borderWidthAnimation")
    val borderColorA by animateColorAsState(borderColor, label = "borderColorAnimation")
    val borderRadiusA by animateShapeAsState(borderRadius, label = "borderRadiusAnimation")

    return this.then(
        Modifier
            .alpha(alphaA)
            .clip(borderRadiusA)
            .background(backgroundColorA)
            .border(
                width = borderWidthA,
                color = borderColorA,
                shape = borderRadiusA
            )
    )
}

private fun Modifier.staticButtonModifier(
    alpha: Float,
    backgroundColor: Color,
    borderWidth: Dp,
    borderColor: Color,
    borderRadius: RoundedCornerShape
) = this.then(
    Modifier
        .alpha(alpha)
        .clip(borderRadius)
        .background(backgroundColor)
        .border(
            width = borderWidth,
            color = borderColor,
            shape = borderRadius
        )
)

/**
 * 根据控件的位置百分比值，计算其在屏幕上的真实位置
 */
internal fun getWidgetPosition(
    data: ObservableBaseData,
    widgetSize: IntSize,
    screenSize: IntSize
): Offset {
    if (data.isEditingPos) return data.movingOffset
    val x = (screenSize.width - widgetSize.width) * (data.position.xPercentage())
    val y = (screenSize.height - widgetSize.height) * (data.position.yPercentage())
    return Offset(x, y)
}

/**
 * 转换为百分比位置值
 */
internal fun Offset.toPercentagePosition(
    widgetSize: IntSize,
    screenSize: IntSize
): ButtonPosition {
    val x = ((100 * x) / (screenSize.width - widgetSize.width) * 100).toInt()
    val y = ((100 * y) / (screenSize.height - widgetSize.height) * 100).toInt()
    return ButtonPosition(x, y)
}

/**
 * 计算两个矩形之间的最小距离（边缘到边缘）
 */
internal fun calculateMinDistanceBetweenRects(
    rect1Left: Float, rect1Top: Float, rect1Right: Float, rect1Bottom: Float,
    rect2Left: Float, rect2Top: Float, rect2Right: Float, rect2Bottom: Float
): Float {
    // 检查是否有重叠
    if (rect1Right >= rect2Left && rect1Left <= rect2Right &&
        rect1Bottom >= rect2Top && rect1Top <= rect2Bottom) {
        return 0f // 有重叠，距离为0
    }

    // 计算水平距离
    val horizontalDistance = if (rect1Right < rect2Left) {
        rect2Left - rect1Right // 矩形1在矩形2左侧
    } else if (rect1Left > rect2Right) {
        rect1Left - rect2Right // 矩形1在矩形2右侧
    } else {
        0f // 水平方向有重叠
    }

    // 计算垂直距离
    val verticalDistance = if (rect1Bottom < rect2Top) {
        rect2Top - rect1Bottom // 矩形1在矩形2上方
    } else if (rect1Top > rect2Bottom) {
        rect1Top - rect2Bottom // 矩形1在矩形2下方
    } else {
        0f // 垂直方向有重叠
    }

    // 返回最小距离
    return if (horizontalDistance > 0 && verticalDistance > 0) {
        // 两个矩形在对角位置，使用欧几里得距离
        sqrt(horizontalDistance * horizontalDistance + verticalDistance * verticalDistance)
    } else {
        // 至少一个方向的距离为0，返回另一个方向的距离
        max(horizontalDistance, verticalDistance)
    }
}