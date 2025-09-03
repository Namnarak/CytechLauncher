package com.movtery.layer_controller.data

import com.google.gson.annotations.SerializedName

data class ButtonSize(
    @SerializedName("type")
    val type: Type,
    @SerializedName("widthDp")
    val widthDp: Float,
    @SerializedName("heightDp")
    val heightDp: Float,
    @SerializedName("widthPercentage")
    val widthPercentage: Int,
    @SerializedName("heightPercentage")
    val heightPercentage: Int,
    @SerializedName("widthReference")
    val widthReference: Reference,
    @SerializedName("heightReference")
    val heightReference: Reference
) {
    /**
     * 大小计算类型
     */
    enum class Type {
        /**
         * 以 Dp 绝对值进行存储
         */
        @SerializedName("dp") Dp,

        /**
         * 以百分比值进行存储
         */
        @SerializedName("percentage") Percentage,

        /**
         * 跟随内容大小变化
         */
        @SerializedName("wrap_content") WrapContent
    }

    enum class Reference {
        /**
         * 参考屏幕宽
         */
        @SerializedName("screen_width") ScreenWidth,

        /**
         * 参考屏幕高
         */
        @SerializedName("screen_height") ScreenHeight,
    }

    companion object {
        /**
         * 默认大小：以百分比值进行存储
         */
        public val Default = ButtonSize(
            type = Type.Percentage,
            widthDp = 50f,
            heightDp = 50f,
            widthPercentage = 140,
            heightPercentage = 140,
            widthReference = Reference.ScreenHeight,
            heightReference = Reference.ScreenHeight
        )

        /**
         * 创建一个默认的百分比尺寸，根据参考尺寸计算出合适的值
         */
        fun createAdaptiveButtonSize(
            referenceLength: Int,
            type: Type = Type.Percentage,
            reference: Reference = Reference.ScreenHeight,
            density: Float = 1f,
            targetDpSize: Float = 50f
        ): ButtonSize {
            val percentage = ((targetDpSize * density) / referenceLength * 1000).toInt()

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
    }
}