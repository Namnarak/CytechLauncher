package com.movtery.layer_controller

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.layout.TextButton
import com.movtery.layer_controller.observable.ObservableBaseData
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.layer_controller.observable.ObservableTextData
import com.movtery.layer_controller.utils.snap.GuideLine
import com.movtery.layer_controller.utils.snap.LineDirection
import com.movtery.layer_controller.utils.getWidgetPosition
import com.movtery.layer_controller.utils.snap.SnapMode

/**
 * 控制布局编辑器渲染层
 * @param enableSnap 是否开启吸附
 * @param snapMode 吸附模式
 * @param localSnapRange 局部吸附范围（仅在Local模式下有效）
 * @param snapThresholdValue 吸附距离阈值
 */
@Composable
fun ControlEditorLayer(
    observedLayout: ObservableControlLayout,
    onButtonTap: (data: ObservableBaseData, layer: ObservableControlLayer) -> Unit,
    enableSnap: Boolean,
    snapMode: SnapMode,
    localSnapRange: Dp = 20.dp,
    snapThresholdValue: Dp = 4.dp
) {
    val layers by observedLayout.layers.collectAsState()
    val styles by observedLayout.styles.collectAsState()

    val guideLines = remember { mutableStateMapOf<ObservableBaseData, List<GuideLine>>() }

    //反转：将最后一层视为底层，逐步向上渲染
    val renderingLayers = layers.reversed()

    Box(modifier = Modifier.fillMaxSize()) {
        ControlWidgetRenderer(
            renderingLayers = renderingLayers,
            styles = styles,
            enableSnap = enableSnap,
            snapMode = snapMode,
            localSnapRange = localSnapRange,
            snapThresholdValue = snapThresholdValue,
            onButtonTap = onButtonTap,
            drawLine = { data, line ->
                guideLines[data] = line
            },
            onLineCancel = { data ->
                guideLines.remove(data)
            }
        )
        //绘制参考线
        Canvas(modifier = Modifier.fillMaxSize()) {
            guideLines.values.forEach { guidelines ->
                guidelines.forEach { guideline ->
                    drawLine(
                        guideline = guideline
                    )
                }
            }
        }
    }
}

/**
 * 根据吸附参考线绘制线条
 */
private fun DrawScope.drawLine(
    guideline: GuideLine,
    color: Color = Color(0xFFFF5252),
    strokeWidth: Float = 2f
) {
    when (guideline.direction) {
        LineDirection.Vertical -> {
            drawLine(
                color = color,
                start = Offset(guideline.coordinate, 0f),
                end = Offset(guideline.coordinate, size.height),
                strokeWidth = strokeWidth
            )
        }
        LineDirection.Horizontal -> {
            drawLine(
                color = color,
                start = Offset(0f, guideline.coordinate),
                end = Offset(size.width, guideline.coordinate),
                strokeWidth = strokeWidth
            )
        }
    }
}

/**
 * @param enableSnap 是否开启吸附功能
 * @param snapMode 吸附模式
 * @param localSnapRange 局部吸附范围（仅在Local模式下有效）
 * @param snapThresholdValue 吸附距离阈值
 * @param drawLine 绘制吸附参考线
 * @param onLineCancel 取消吸附参考线
 */
@Composable
private fun ControlWidgetRenderer(
    renderingLayers: List<ObservableControlLayer>,
    styles: List<ObservableButtonStyle>,
    enableSnap: Boolean,
    snapMode: SnapMode,
    localSnapRange: Dp,
    snapThresholdValue: Dp,
    onButtonTap: (data: ObservableBaseData, layer: ObservableControlLayer) -> Unit,
    drawLine: (ObservableBaseData, List<GuideLine>) -> Unit,
    onLineCancel: (ObservableBaseData) -> Unit
) {
    val sizes = remember { mutableStateMapOf<ObservableTextData, IntSize>() }
    val screenSize by rememberUpdatedState(LocalWindowInfo.current.containerSize)

    Layout(
        content = {
            //按图层顺序渲染所有可见的控件
            renderingLayers.forEach { layer ->
                if (!layer.hide) {
                    val normalButtons by layer.normalButtons.collectAsState()
                    val textBoxes by layer.textBoxes.collectAsState()

                    val allWidgetsInLayer = remember(layer, normalButtons, textBoxes) {
                        normalButtons + textBoxes
                    }

                    normalButtons.forEach { data ->
                        TextButton(
                            isEditMode = true,
                            data = data,
                            getSize = { sizes[data] ?: IntSize.Zero },
                            enableSnap = enableSnap,
                            snapMode = snapMode,
                            localSnapRange = localSnapRange,
                            otherWidgets = allWidgetsInLayer
                                .filter { it != data } //排除自己
                                .map { it to (sizes[it] ?: IntSize.Zero) },
                            snapThresholdValue = snapThresholdValue,
                            drawLine = drawLine,
                            onLineCancel = onLineCancel,
                            getStyle = {
                                styles.takeIf { data.buttonStyle != null }
                                    ?.find { it.uuid == data.buttonStyle }
                            },
                            isPressed = data.isPressed,
                            onTapInEditMode = {
                                onButtonTap(data, layer)
                            }
                        )
                    }

                    textBoxes.forEach { data ->
                        TextButton(
                            isEditMode = true,
                            data = data,
                            getSize = { sizes[data] ?: IntSize.Zero },
                            enableSnap = enableSnap,
                            snapMode = snapMode,
                            localSnapRange = localSnapRange,
                            otherWidgets = allWidgetsInLayer
                                .filter { it != data } //排除自己
                                .map { it to (sizes[it] ?: IntSize.Zero) },
                            snapThresholdValue = snapThresholdValue,
                            drawLine = drawLine,
                            onLineCancel = onLineCancel,
                            getStyle = {
                                styles.takeIf { data.buttonStyle != null }
                                    ?.find { it.uuid == data.buttonStyle }
                            },
                            isPressed = false, //文本框不需要按压状态
                            onTapInEditMode = {
                                onButtonTap(data, layer)
                            }
                        )
                    }
                }
            }
        }
    ) { measurables, constraints ->
        val placeables = measurables.map { measurable ->
            measurable.measure(constraints)
        }

        var index = 0
        renderingLayers.forEach { layer ->
            if (!layer.hide) {
                layer.normalButtons.value.forEach { data ->
                    if (index < placeables.size) {
                        val placeable = placeables[index]
                        sizes[data] = IntSize(placeable.width, placeable.height)
                        index++
                    }
                }

                layer.textBoxes.value.forEach { data ->
                    if (index < placeables.size) {
                        val placeable = placeables[index]
                        sizes[data] = IntSize(placeable.width, placeable.height)
                        index++
                    }
                }
            }
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            var placeableIndex = 0
            renderingLayers.forEach { layer ->
                if (!layer.hide) {
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
                }
            }
        }
    }
}