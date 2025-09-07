package com.movtery.layer_controller.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 按键点击事件
 * @param type 绑定的点击事件类型
 * @param key 事件唯一标识
 */
@Serializable
data class ClickEvent(
    @SerialName("type")
    val type: Type,
    @SerialName("key")
    val key: String
) {
    @Serializable
    enum class Type {
        /**
         * 点击触发按键
         */
        @SerialName("key")
        Key,

        /**
         * 点击触发启动器事件
         */
        @SerialName("launcher_event")
        LauncherEvent,

        /**
         * 点击开关按键层级
         */
        @SerialName("switch_layer")
        SwitchLayer
    }
}