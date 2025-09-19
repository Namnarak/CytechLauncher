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
}

/**
 * 位于屏幕左上角
 */
public val TopStartPosition = ButtonPosition(0, 0)

/**
 * 位于屏幕右上角
 */
public val TopEndPosition = ButtonPosition(10000, 0)

/**
 * 位于屏幕中心
 */
public val CenterPosition = ButtonPosition(5000, 5000)

/**
 * 位于屏幕左下角
 */
public val BottomStartPosition = ButtonPosition(0, 10000)

/**
 * 位于屏幕右下角
 */
public val BottomEndPosition = ButtonPosition(10000, 10000)
