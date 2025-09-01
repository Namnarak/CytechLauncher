package com.movtery.layer_controller.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import com.movtery.layer_controller.observable.ObservableBaseData
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.layer_controller.observable.ObservableTextData
import com.movtery.layer_controller.utils.getWidgetPosition

@Composable
fun ControlEditorLayer(
    observedLayout: ObservableControlLayout,
    onButtonTap: (ObservableBaseData) -> Unit
) {
    val layers by observedLayout.layers.collectAsState()
    val styles by observedLayout.styles.collectAsState()

    val sizes = remember { mutableStateMapOf<ObservableTextData, IntSize>() }
    val screenSize by rememberUpdatedState(LocalWindowInfo.current.containerSize)

    Layout(
        content = {
            //按图层顺序渲染所有可见的控件
            layers.forEach { layer ->
                if (!layer.hide) {
                    val normalButtons by layer.normalButtons.collectAsState()
                    normalButtons.forEach { data ->
                        TextButton(
                            isEditMode = true,
                            data = data,
                            getSize = { sizes[data] ?: IntSize.Zero },
                            getStyle = { styles.takeIf { data.buttonStyle != null }?.find { it.uuid == data.buttonStyle } },
                            isPressed = data.isPressed,
                            onTapInEditMode = {
                                onButtonTap(data)
                            }
                        )
                    }

                    val textBoxes by layer.textBoxes.collectAsState()
                    textBoxes.forEach { data ->
                        TextButton(
                            isEditMode = true,
                            data = data,
                            getSize = { sizes[data] ?: IntSize.Zero },
                            getStyle = { styles.takeIf { data.buttonStyle != null }?.find { it.uuid == data.buttonStyle } },
                            isPressed = false, //文本框不需要按压状态
                            onTapInEditMode = {
                                onButtonTap(data)
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
        layers.forEach { layer ->
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
            layers.forEach { layer ->
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