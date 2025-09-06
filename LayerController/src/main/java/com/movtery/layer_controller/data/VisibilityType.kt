package com.movtery.layer_controller.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 控件可见场景
 */
@Serializable
enum class VisibilityType {
    /**
     * 始终展示
     */
    @SerialName("always")
    ALWAYS,

    /**
     * 在虚拟鼠标被捕获时展示
     */
    @SerialName("in_game")
    IN_GAME,

    /**
     * 在虚拟鼠标被释放时展示
     */
    @SerialName("in_menu")
    IN_MENU
}