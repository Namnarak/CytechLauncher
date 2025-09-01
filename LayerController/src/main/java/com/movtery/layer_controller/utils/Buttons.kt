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

/**
 * 自动处理按钮拖动改变位置
 * @param onTapInEditMode 在编辑模式下点击了按钮
 */
@Composable
internal fun Modifier.editMode(
    isEditMode: Boolean,
    data: ObservableBaseData,
    getSize: () -> IntSize,
    onTapInEditMode: () -> Unit = {}
): Modifier {
    val screenSize by rememberUpdatedState(LocalWindowInfo.current.containerSize)
    val getSize1 by rememberUpdatedState(getSize)
    val onTapInEditMode1 by rememberUpdatedState(onTapInEditMode)

    return this.then(
        if (isEditMode) {
            Modifier
                .pointerInput(data) {
                    detectDragGestures(
                        onDragStart = {
                            data.isEditingPos = false
                            data.movingOffset = Offset.Zero
                            val currentOffset = getWidgetPosition(data, getSize1(), screenSize)
                            data.movingOffset = currentOffset
                            data.isEditingPos = true
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val currentOffset = getWidgetPosition(data, getSize1(), screenSize)
                            val currentSize = getSize1()

                            val newX = currentOffset.x + dragAmount.x
                            val newY = currentOffset.y + dragAmount.y

                            val maxX = screenSize.width.toFloat() - currentSize.width
                            val maxY = screenSize.height.toFloat() - currentSize.height

                            data.position = Offset(
                                x = newX.coerceIn(0f, maxX),
                                y = newY.coerceIn(0f, maxY)
                            ).also { offset ->
                                data.movingOffset = offset
                            }.toPercentagePosition(
                                widgetSize = currentSize,
                                screenSize = screenSize
                            )
                        },
                        onDragEnd = {
                            data.isEditingPos = false
                            data.movingOffset = Offset.Zero
                        },
                        onDragCancel = {
                            data.isEditingPos = false
                            data.movingOffset = Offset.Zero
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
                    val buttonWidth = (widthReference * (size.widthPercentage / 1000f)).toDp()
                    val buttonHeight = (heightReference * (size.heightPercentage / 1000f)).toDp()
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
    val x = ((100 * x) / (screenSize.width - widgetSize.width) * 10).toInt()
    val y = ((100 * y) / (screenSize.height - widgetSize.height) * 10).toInt()
    return ButtonPosition(x, y)
}