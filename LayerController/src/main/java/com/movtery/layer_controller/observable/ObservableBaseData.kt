package com.movtery.layer_controller.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.data.BaseData

/**
 * 可观察的BaseData包装类
 */
open class ObservableBaseData(data: BaseData) {
    val uuid: String = data.uuid
    var position by mutableStateOf(data.position)
    var buttonSize by mutableStateOf(data.buttonSize)
    var buttonStyle by mutableStateOf(data.buttonStyle)
    var visibilityType by mutableStateOf(data.visibilityType)
}