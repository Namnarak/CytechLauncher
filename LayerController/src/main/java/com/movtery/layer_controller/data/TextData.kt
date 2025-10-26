package com.movtery.layer_controller.data

import com.movtery.layer_controller.data.lang.TranslatableString
import com.movtery.layer_controller.utils.getAButtonUUID
import kotlinx.serialization.Serializable

/**
 * @param text 按钮显示的文本
 */
@Serializable
data class TextData(
    val text: TranslatableString,
    val uuid: String,
    val position: ButtonPosition,
    val buttonSize: ButtonSize,
    val buttonStyle: String? = null,
    val textAlignment: TextAlignment = TextAlignment.Left,
    val textBold: Boolean = false,
    val textItalic: Boolean = false,
    val textUnderline: Boolean = false,
    val visibilityType: VisibilityType
): Widget

/**
 * 克隆一个新的TextData对象（UUID、位置不同）
 */
public fun TextData.cloneNew(): TextData = TextData(
    text = this.text,
    uuid = getAButtonUUID(),
    position = CenterPosition,
    buttonSize = buttonSize,
    buttonStyle = buttonStyle,
    textAlignment = textAlignment,
    textBold = textBold,
    textItalic = textItalic,
    textUnderline = textUnderline,
    visibilityType = visibilityType
)