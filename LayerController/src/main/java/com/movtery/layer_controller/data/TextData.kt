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
    val visibilityType: VisibilityType
): Widget {
    companion object {
        /**
         * 克隆一个新的TextData对象（UUID、位置不同）
         */
        fun TextData.cloneNew(): TextData = TextData(
            text = this.text,
            uuid = getAButtonUUID(),
            position = ButtonPosition.Center,
            buttonSize = buttonSize,
            buttonStyle = buttonStyle,
            visibilityType = visibilityType
        )
    }
}