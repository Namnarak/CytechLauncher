package com.movtery.layer_controller.event

import com.movtery.layer_controller.observable.ObservableControlLayer

/**
 * 处理切换布局隐藏显示
 */
internal fun switchLayer(
    clickEvent: ClickEvent,
    layers: List<ObservableControlLayer>,
    switch: (ObservableControlLayer) -> Unit
) {
    if (clickEvent.type == ClickEvent.Type.SwitchLayer) {
        val uuid = clickEvent.key
        layers.find { it.uuid == uuid }?.let { switch(it) }
    }
}