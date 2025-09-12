package com.movtery.layer_controller.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableNormalData
import com.movtery.layer_controller.observable.ObservableTextData
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.layer_controller.utils.buttonContentColorAsState
import com.movtery.layer_controller.utils.buttonSize
import com.movtery.layer_controller.utils.buttonStyle
import com.movtery.layer_controller.utils.editMode
import com.movtery.layer_controller.utils.snap.GuideLine
import com.movtery.layer_controller.utils.snap.SnapMode

/**
 * 基础文本控件
 * @param enableSnap 编辑模式下，是否开启吸附功能
 * @param snapMode 吸附模式
 * @param localSnapRange 局部吸附范围（仅在Local模式下有效）
 * @param getOtherWidgets 获取其他控件的信息，在编辑模式下，用于计算吸附位置
 * @param snapThresholdValue 吸附距离阈值
 * @param drawLine 绘制吸附参考线
 * @param onLineCancel 取消吸附参考线
 */
@Composable
internal fun TextButton(
    isEditMode: Boolean,
    data: ObservableWidget,
    visible: Boolean = true,
    getSize: (ObservableWidget) -> IntSize,
    enableSnap: Boolean = false,
    snapMode: SnapMode = SnapMode.FullScreen,
    localSnapRange: Dp = 50.dp,
    getOtherWidgets: () -> List<ObservableWidget>,
    snapThresholdValue: Dp,
    drawLine: (ObservableWidget, List<GuideLine>) -> Unit = { _, _ -> },
    onLineCancel: (ObservableWidget) -> Unit = {},
    getStyle: () -> ObservableButtonStyle?,
    isPressed: Boolean,
    onTapInEditMode: () -> Unit = {}
) {
    if (visible) {
        val buttonStyle = when (data) {
            is ObservableNormalData -> data.buttonStyle
            is ObservableTextData -> data.buttonStyle
            else -> error("Unknown widget type")
        }

        val style = remember(data, buttonStyle) {
            getStyle() ?: ObservableButtonStyle.Default
        }

        val locale = LocalConfiguration.current.locales[0]

        Box(
            modifier = Modifier
                .buttonSize(data)
                .buttonStyle(style = style, isPressed = isPressed)
                .editMode(
                    isEditMode = isEditMode,
                    data = data,
                    getSize = getSize,
                    enableSnap = enableSnap,
                    snapMode = snapMode,
                    localSnapRange = localSnapRange,
                    getOtherWidgets = getOtherWidgets,
                    snapThresholdValue = snapThresholdValue,
                    drawLine = drawLine,
                    onLineCancel = onLineCancel,
                    onTapInEditMode = onTapInEditMode
                ),
            contentAlignment = Alignment.Center
        ) {
            val color by buttonContentColorAsState(style = style, isPressed = isPressed)
            val text = remember(data) {
                when (data) {
                    is ObservableNormalData -> data.text
                    is ObservableTextData -> data.text
                    else -> error("Unknown widget type")
                }
            }
            RtLText(
                text = text.translate(locale),
                color = color
            )
        }
    } else {
        //虚假的控件，使用一个空的组件，只是让Layout有东西能测
        Spacer(
            modifier = Modifier.buttonSize(data)
        )
    }
}

/**
 * 仅渲染控件外观的组件
 */
@Composable
fun RendererStyleBox(
    style: ObservableButtonStyle,
    modifier: Modifier = Modifier,
    text: String = "",
    isDark: Boolean,
    isPressed: Boolean
) {
    Box(
        modifier = modifier.buttonStyle(style = style, isDark = isDark, isPressed = isPressed),
        contentAlignment = Alignment.Center
    ) {
        val color by buttonContentColorAsState(style = style, isDark = isDark, isPressed = isPressed)
        RtLText(
            text = text,
            color = color
        )
    }
}