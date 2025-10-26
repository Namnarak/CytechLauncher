package com.movtery.layer_controller.data

enum class HideLayerWhen {
    /**
     * 实体鼠标操作时
     */
    WhenMouse,

    /**
     * 手柄操作时
     */
    WhenGamepad,

    /**
     * 触摸操作时不隐藏
     */
    None
}