package com.movtery.layer_controller.data

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName
import com.movtery.layer_controller.utils.randomUUID

/**
 * @param name 样式显示名称
 * @param animateSwap 在切换状态时，是否启用动画过渡
 * @param lightStyle 亮色模式样式
 * @param darkStyle 暗色模式样式
 */
data class ButtonStyle(
    @SerializedName("name")
    val name: String,
    @SerializedName("uuid")
    val uuid: String = randomUUID(),
    @SerializedName("animateSwap")
    val animateSwap: Boolean = false,
    @SerializedName("lightStyle")
    val lightStyle: StyleConfig,
    @SerializedName("darkStyle")
    val darkStyle: StyleConfig
) {
    /**
     * @param alpha 整体不透明度
     * @param backgroundColor 背景颜色
     * @param contentColor 内容颜色
     * @param borderWidth 边框粗细
     * @param borderColor 边框颜色
     * @param borderRadius 圆角尺寸
     * @param pressedAlpha 按下时，整体不透明度
     * @param pressedBackgroundColor 按下时，背景颜色
     * @param pressedContentColor 按下时，内容颜色
     * @param pressedBorderWidth 按下时，边框粗细
     * @param pressedBorderColor 按下时，边框颜色
     * @param pressedBorderRadius 按下时，圆角尺寸
     */
    data class StyleConfig(
        @SerializedName("alpha")
        val alpha: Float,
        @SerializedName("pressedAlpha")
        val pressedAlpha: Float,
        @SerializedName("backgroundColor")
        val backgroundColor: Color,
        @SerializedName("pressedBackgroundColor")
        val pressedBackgroundColor: Color,
        @SerializedName("contentColor")
        val contentColor: Color,
        @SerializedName("pressedContentColor")
        val pressedContentColor: Color,
        @SerializedName("borderWidth")
        val borderWidth: Int,
        @SerializedName("pressedBorderWidth")
        val pressedBorderWidth: Int,
        @SerializedName("borderColor")
        val borderColor: Color,
        @SerializedName("pressedBorderColor")
        val pressedBorderColor: Color,
        @SerializedName("borderRadius")
        val borderRadius: ButtonShape,
        @SerializedName("pressedBorderRadius")
        val pressedBorderRadius: ButtonShape
    ) {
        companion object {
            public val Default = StyleConfig(
                alpha = 1f,
                backgroundColor = Color.Black.copy(alpha = 0.5f),
                contentColor = Color.White,
                borderWidth = -1,
                borderColor = Color.White,
                borderRadius = ButtonShape(0f),
                pressedAlpha = 1f,
                pressedBackgroundColor = Color.Gray.copy(alpha = 0.7f),
                pressedContentColor = Color.White,
                pressedBorderWidth = -1,
                pressedBorderColor = Color.White,
                pressedBorderRadius = ButtonShape(0f)
            )
        }
    }

    companion object {
        public val Default = ButtonStyle(
            name = "Default",
            animateSwap = false,
            lightStyle = StyleConfig.Default,
            darkStyle = StyleConfig.Default
        )
    }
}