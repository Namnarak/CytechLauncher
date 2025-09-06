package com.movtery.layer_controller.data

import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 简易的描述按钮的各角的数据类，单位：Dp
 */
@Serializable
data class ButtonShape public constructor(
    @SerialName("topStart")
    val topStart: Float,
    @SerialName("topEnd")
    val topEnd: Float,
    @SerialName("bottomEnd")
    val bottomEnd: Float,
    @SerialName("bottomStart")
    val bottomStart: Float
) {
    constructor(size: Float) : this(size, size, size, size)

    companion object {
        /**
         * 形状有效值范围
         */
        val valueRange = 0f..100f

        /**
         * 转换为 RoundedCornerShape
         */
        fun ButtonShape.toAndroidShape() = RoundedCornerShape(
            topStart = topStart,
            topEnd = topEnd,
            bottomEnd = bottomEnd,
            bottomStart = bottomStart
        )
    }
}