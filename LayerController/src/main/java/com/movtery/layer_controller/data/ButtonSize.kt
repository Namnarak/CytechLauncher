package com.movtery.layer_controller.data

import com.movtery.layer_controller.data.ButtonSize.Reference
import com.movtery.layer_controller.data.ButtonSize.Type
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 按钮的大小
 * @param widthDp 绝对值宽度 0~设备总Dp
 * @param heightDp 绝对值高度 0~设备总Dp
 * @param widthPercentage 百分比宽度 0~10000
 * @param heightPercentage 百分比高度 0~10000
 */
@Serializable
data class ButtonSize(
    @SerialName("type")
    val type: Type,
    @SerialName("widthDp")
    val widthDp: Float,
    @SerialName("heightDp")
    val heightDp: Float,
    @SerialName("widthPercentage")
    val widthPercentage: Int,
    @SerialName("heightPercentage")
    val heightPercentage: Int,
    @SerialName("widthReference")
    val widthReference: Reference,
    @SerialName("heightReference")
    val heightReference: Reference
) {
    /**
     * 大小计算类型
     */
    @Serializable
    enum class Type {
        /**
         * 以 Dp 绝对值进行存储
         */
        @SerialName("dp") Dp,

        /**
         * 以百分比值进行存储
         */
        @SerialName("percentage") Percentage,

        /**
         * 跟随内容大小变化
         */
        @SerialName("wrap_content") WrapContent
    }

    @Serializable
    enum class Reference {
        /**
         * 参考屏幕宽
         */
        @SerialName("screen_width") ScreenWidth,

        /**
         * 参考屏幕高
         */
        @SerialName("screen_height") ScreenHeight,
    }
}

/**
 * 默认大小：以百分比值进行存储
 */
public val DefaultSize = ButtonSize(
    type = Type.Percentage,
    widthDp = 50f,
    heightDp = 50f,
    widthPercentage = 1400,
    heightPercentage = 1400,
    widthReference = Reference.ScreenHeight,
    heightReference = Reference.ScreenHeight
)

/**
 * 创建一个默认的百分比尺寸，根据参考尺寸计算出合适的值
 */
public fun createAdaptiveButtonSize(
    referenceLength: Int,
    type: Type = Type.Percentage,
    reference: Reference = Reference.ScreenHeight,
    density: Float = 1f,
    targetDpSize: Float = 50f
): ButtonSize {
    val percentage = ((targetDpSize * density) / referenceLength * 10000).toInt()

    return ButtonSize(
        type = type,
        widthDp = targetDpSize,
        heightDp = targetDpSize,
        widthPercentage = percentage,
        heightPercentage = percentage,
        widthReference = reference,
        heightReference = reference
    )
}