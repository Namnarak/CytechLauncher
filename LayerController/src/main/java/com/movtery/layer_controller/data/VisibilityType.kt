package com.movtery.layer_controller.data

import com.google.gson.annotations.SerializedName

/**
 * 控件可见场景
 */
enum class VisibilityType {
    /**
     * 始终展示
     */
    @SerializedName("always")
    ALWAYS,

    /**
     * 在虚拟鼠标被捕获时展示
     */
    @SerializedName("in_game")
    IN_GAME,

    /**
     * 在虚拟鼠标被释放时展示
     */
    @SerializedName("in_menu")
    IN_MENU
}