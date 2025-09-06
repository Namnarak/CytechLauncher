package com.movtery.layer_controller.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.data.NormalData
import com.movtery.layer_controller.data.NormalData.Companion.cloneNew
import com.movtery.layer_controller.event.ClickEvent

/**
 * 可观察的NormalData包装类
 */
class ObservableNormalData(data: NormalData) : ObservableWidget() {
    val text = ObservableTranslatableString(data.text)
    val uuid: String = data.uuid
    var position by mutableStateOf(data.position)
    var buttonSize by mutableStateOf(data.buttonSize)
    var buttonStyle by mutableStateOf(data.buttonStyle)
    var visibilityType by mutableStateOf(data.visibilityType)
    var clickEvents by mutableStateOf(data.clickEvents)
    var isSwipple by mutableStateOf(data.isSwipple)
    var isPenetrable by mutableStateOf(data.isPenetrable)
    var isToggleable by mutableStateOf(data.isToggleable)

    /**
     * 当前是否处于按下状态
     */
    var isPressed by mutableStateOf(false)

    fun addEvent(event: ClickEvent) {
        if (clickEvents.none { it.type == event.type && it.key == event.key }) {
            clickEvents = clickEvents + event
        }
    }

    fun removeEvent(event: ClickEvent) {
        removeEvent(event.type, event.key)
    }

    fun removeEvent(eventType: ClickEvent.Type, key: String) {
        clickEvents = clickEvents.filterNot { it.type == eventType && it.key == key }
    }

    fun removeAllEvent(events: Collection<ClickEvent>) {
        val keysToRemove = events.map { it.type to it.key }.toSet()
        clickEvents = clickEvents.filterNot { (it.type to it.key) in keysToRemove }
    }

    fun packNormal(): NormalData {
        return NormalData(
            text = text.pack(),
            uuid = uuid,
            position = position,
            buttonSize = buttonSize,
            buttonStyle = buttonStyle,
            visibilityType = visibilityType,
            clickEvents = clickEvents,
            isSwipple = isSwipple,
            isPenetrable = isPenetrable,
            isToggleable = isToggleable
        )
    }

    companion object {
        fun ObservableNormalData.cloneNormal(): ObservableNormalData {
            return ObservableNormalData(packNormal().cloneNew())
        }
    }
}