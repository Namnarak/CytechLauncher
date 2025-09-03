package com.movtery.layer_controller.observable

import com.movtery.layer_controller.data.TextData

/**
 * 可观察的TextData包装类
 */
open class ObservableTextData(data: TextData) : ObservableBaseData(data) {
    val text = ObservableTranslatableString(data.text)

    fun packText(): TextData {
        return TextData(
            text = text.pack(),
            uuid = uuid,
            position = position,
            buttonSize = buttonSize,
            buttonStyle = buttonStyle,
            visibilityType = visibilityType
        )
    }
}