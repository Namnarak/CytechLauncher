package com.movtery.layer_controller

import com.movtery.layer_controller.data.NormalData
import com.movtery.layer_controller.data.TextData
import com.movtery.layer_controller.layout.ControlLayout

/**
 * 控件编辑器的版本号
 */
internal const val EDITOR_VERSION = 3

/**
 * 自动处理并逐步更新控制布局到新版编辑器
 */
internal fun updateLayoutToNew(
    layout: ControlLayout
): ControlLayout {
    return when (layout.editorVersion) {
        1 -> updateLayoutToNew(update1To2(layout))
        2 -> updateLayoutToNew(update2To3(layout))
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
                NormalData(
                    text = data.text,
                    uuid = data.uuid,
                    position = data.position.copy(
                        x = data.position.x * 10,
                        y = data.position.y * 10
                    ),
                    buttonSize = data.buttonSize.copy(
                        widthPercentage = data.buttonSize.widthPercentage * 10,
                        heightPercentage = data.buttonSize.heightPercentage * 10
                    ),
                    buttonStyle = data.buttonStyle,
                    visibilityType = data.visibilityType,
                    clickEvents = data.clickEvents,
                    isSwipple = data.isSwipple,
                    isPenetrable = data.isPenetrable,
                    isToggleable = data.isToggleable
                )
            },
            textBoxes = layer.textBoxes.map { data ->
                TextData(
                    text = data.text,
                    uuid = data.uuid,
                    position = data.position.copy(
                        x = data.position.x * 10,
                        y = data.position.y * 10
                    ),
                    buttonSize = data.buttonSize.copy(
                        widthPercentage = data.buttonSize.widthPercentage * 10,
                        heightPercentage = data.buttonSize.heightPercentage * 10
                    ),
                    buttonStyle = data.buttonStyle,
                    visibilityType = data.visibilityType
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