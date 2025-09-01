package com.movtery.layer_controller.data

/**
 * 按钮的位置
 * @param x 0~1000
 * @param y 0~1000
 */
data class ButtonPosition(
    val x: Int,
    val y: Int
) {
    /**
     * 计算x坐标百分比
     */
    fun xPercentage(): Float {
        return (x / 1000f).coerceAtMost(1f).coerceAtLeast(0f)
    }

    /**
     * 计算y坐标百分比
     */
    fun yPercentage(): Float {
        return (y / 1000f).coerceAtMost(1f).coerceAtLeast(0f)
    }

    companion object {
        public val Zero = ButtonPosition(0, 0)
    }
}