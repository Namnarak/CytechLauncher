package com.movtery.layer_controller.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 按钮的位置
 * @param x 0~10000
 * @param y 0~10000
 */
@Serializable
data class ButtonPosition(
    @SerialName("x")
    val x: Int,
    @SerialName("y")
    val y: Int
) {
    /**
     * 计算x坐标百分比
     */
    fun xPercentage(): Float {
        return (x / 10000f).coerceAtMost(1f).coerceAtLeast(0f)
    }

    /**
     * 计算y坐标百分比
     */
    fun yPercentage(): Float {
        return (y / 10000f).coerceAtMost(1f).coerceAtLeast(0f)
    }

    companion object {
        public val Zero = ButtonPosition(0, 0)
        public val Center = ButtonPosition(5000, 5000)
    }
}