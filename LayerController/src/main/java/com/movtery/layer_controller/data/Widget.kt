package com.movtery.layer_controller.data

import com.movtery.layer_controller.utils.getAButtonUUID

interface Widget

fun <E: Widget> createWidgetWithUUID(block: (uuid: String) -> E): E {
    return block(getAButtonUUID())
}