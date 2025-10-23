package com.movtery.zalithlauncher.ui.control.gamepad

import com.movtery.zalithlauncher.R

/**
 * 摇杆控制模式
 */
enum class JoystickMode(val titleRes: Int, val summaryRes: Int) {
    /**
     * 使用左摇杆控制移动，右摇杆控制视角
     */
    LeftMovement(
        titleRes = R.string.settings_gamepad_joystick_mode_left,
        summaryRes = R.string.settings_gamepad_joystick_mode_left_summary
    ),

    /**
     * 使用右摇杆控制移动，左摇杆控制视角
     */
    RightMovement(
        titleRes = R.string.settings_gamepad_joystick_mode_right,
        summaryRes = R.string.settings_gamepad_joystick_mode_right_summary
    )
}