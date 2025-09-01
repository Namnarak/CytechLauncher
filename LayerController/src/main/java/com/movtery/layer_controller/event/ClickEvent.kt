package com.movtery.layer_controller.event

import com.google.gson.annotations.SerializedName

/**
 * 按键点击事件
 * @param type 绑定的点击事件类型
 * @param key 事件唯一标识
 */
data class ClickEvent(
    @SerializedName("type")
    val type: Type,
    @SerializedName("key")
    val key: String
) {
    enum class Type {
        /**
         * 点击触发按键
         */
        @SerializedName("key")
        Key,

        /**
         * 点击触发启动器事件
         */
        @SerializedName("launcher_event")
        LauncherEvent,

        /**
         * 点击开关按键层级
         */
        @SerializedName("switch_layer")
        SwitchLayer
    }
}