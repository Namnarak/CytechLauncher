package com.movtery.layer_controller.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.data.TextData

/**
 * 可观察的TextData包装类
 */
open class ObservableTextData(data: TextData) : ObservableBaseData(data) {
    var text by mutableStateOf(data.text)

    fun packText(): TextData {
        return TextData(
            text = text,
            uuid = uuid,
            position = position,
            buttonSize = buttonSize,
            buttonStyle = buttonStyle,
            visibilityType = visibilityType
        )
    }
}