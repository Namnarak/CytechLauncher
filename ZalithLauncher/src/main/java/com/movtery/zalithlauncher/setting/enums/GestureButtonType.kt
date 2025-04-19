package com.movtery.zalithlauncher.setting.enums

import com.movtery.zalithlauncher.R

/**
 * 手势控制点击时触发的按钮
 */
enum class GestureButtonType(val nameRes: Int) {
    MOUSE_RIGHT(R.string.settings_control_gesture_control_trigger_mouse_right),
    MOUSE_LEFT(R.string.settings_control_gesture_control_trigger_mouse_left)
}

fun String.toGestureButtonType(): GestureButtonType =
    GestureButtonType.entries.find { it.name == this } ?: GestureButtonType.MOUSE_RIGHT
