package com.movtery.layer_controller.data

import com.movtery.layer_controller.utils.getAButtonUUID

interface Widget {
    companion object {
        fun <E: Widget> createWithUUID(block: (uuid: String) -> E): E {
            return block(getAButtonUUID())
        }
    }
}