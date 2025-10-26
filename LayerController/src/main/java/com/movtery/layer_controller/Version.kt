package com.movtery.layer_controller

import com.movtery.layer_controller.data.TextAlignment
import com.movtery.layer_controller.layout.ControlLayout

/**
 * 控件编辑器的版本号
 */
internal const val EDITOR_VERSION = 4

/**
 * 自动处理并逐步更新控制布局到新版编辑器
 */
internal fun updateLayoutToNew(
    layout: ControlLayout
): ControlLayout {
    return when (layout.editorVersion) {
        1 -> updateLayoutToNew(update1To2(layout))
        2 -> updateLayoutToNew(update2To3(layout))
        3 -> updateLayoutToNew(update3To4(layout))
        else -> layout
    }
}

/**
 * 1 -> 2: 控件位置、控件大小 数值精确到小数点后2位
 */
internal fun update1To2(
    layout: ControlLayout
): ControlLayout = layout.copy(
    editorVersion = 2,
    layers = layout.layers.map { layer ->
        layer.copy(
            normalButtons = layer.normalButtons.map { data ->
                data.copy(
                    position = data.position.copy(
                        x = data.position.x * 10,
                        y = data.position.y * 10
                    ),
                    buttonSize = data.buttonSize.copy(
                        widthPercentage = data.buttonSize.widthPercentage * 10,
                        heightPercentage = data.buttonSize.heightPercentage * 10
                    )
                )
            },
            textBoxes = layer.textBoxes.map { data ->
                data.copy(
                    position = data.position.copy(
                        x = data.position.x * 10,
                        y = data.position.y * 10
                    ),
                    buttonSize = data.buttonSize.copy(
                        widthPercentage = data.buttonSize.widthPercentage * 10,
                        heightPercentage = data.buttonSize.heightPercentage * 10
                    )
                )
            }
        )
    }
)

/**
 * 2 -> 3: 支持在实体鼠标、手柄操控后，隐藏控件层
 */
internal fun update2To3(
    layout: ControlLayout
): ControlLayout = layout.copy(
    editorVersion = 3,
    layers = layout.layers.map { layer ->
        layer.copy(
            hideWhenMouse = true,
            hideWhenGamepad = true
        )
    }
)

/**
 * 3 -> 4: 支持为文本设置文本对齐、粗体、斜体、下划线
 */
internal fun update3To4(
    layout: ControlLayout
): ControlLayout = layout.copy(
    editorVersion = 4,
    layers = layout.layers.map { layer ->
        layer.copy(
            normalButtons = layer.normalButtons.map { data ->
                data.copy(
                    textAlignment = TextAlignment.Left,
                    textBold = false,
                    textItalic = false,
                    textUnderline = false
                )
            },
            textBoxes = layer.textBoxes.map { data ->
                data.copy(
                    textAlignment = TextAlignment.Left,
                    textBold = false,
                    textItalic = false,
                    textUnderline = false
                )
            }
        )
    }
)