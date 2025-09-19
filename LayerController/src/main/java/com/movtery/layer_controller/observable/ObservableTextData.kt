package com.movtery.layer_controller.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.data.TextData
import com.movtery.layer_controller.data.cloneNew

/**
 * 可观察的TextData包装类
 */
open class ObservableTextData(data: TextData) : ObservableWidget() {
    val text = ObservableTranslatableString(data.text)
    val uuid: String = data.uuid
    var position by mutableStateOf(data.position)
    var buttonSize by mutableStateOf(data.buttonSize)
    var buttonStyle by mutableStateOf(data.buttonStyle)
    var visibilityType by mutableStateOf(data.visibilityType)

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

public fun ObservableTextData.cloneText(): ObservableTextData {
    return ObservableTextData(packText().cloneNew())
}