package com.movtery.zalithlauncher.game.control

import com.movtery.layer_controller.layout.ControlLayout
import java.io.File

/**
 * 控件管理数据类
 */
data class ControlData(
    val file: File,
    val controlLayout: ControlLayout,
    val isSupport: Boolean = true
)