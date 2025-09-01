package com.movtery.layer_controller.data

import androidx.compose.foundation.shape.RoundedCornerShape
import com.google.gson.annotations.SerializedName

/**
 * 简易的描述按钮的各角的数据类，单位：Dp
 */
data class ButtonShape constructor(
    @SerializedName("topStart")
    val topStart: Float,
    @SerializedName("topEnd")
    val topEnd: Float,
    @SerializedName("bottomEnd")
    val bottomEnd: Float,
    @SerializedName("bottomStart")
    val bottomStart: Float
) {
    constructor(size: Float) : this(size, size, size, size)

    companion object {
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