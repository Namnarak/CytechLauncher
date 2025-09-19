package com.movtery.layer_controller.data

import androidx.compose.ui.graphics.Color
import com.movtery.layer_controller.data.ButtonStyle.StyleConfig
import com.movtery.layer_controller.utils.randomUUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param name 样式显示名称
 * @param animateSwap 在切换状态时，是否启用动画过渡
 * @param lightStyle 亮色模式样式
 * @param darkStyle 暗色模式样式
 */
@Serializable
data class ButtonStyle(
    @SerialName("name")
    val name: String,
    @SerialName("uuid")
    val uuid: String,
    @SerialName("animateSwap")
    val animateSwap: Boolean,
    @SerialName("lightStyle")
    val lightStyle: StyleConfig,
    @SerialName("darkStyle")
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
    @Serializable
    data class StyleConfig(
        @SerialName("alpha")
        val alpha: Float,
        @SerialName("pressedAlpha")
        val pressedAlpha: Float,
        @SerialName("backgroundColor")
        @Contextual val backgroundColor: Color,
        @SerialName("pressedBackgroundColor")
        @Contextual val pressedBackgroundColor: Color,
        @SerialName("contentColor")
        @Contextual val contentColor: Color,
        @SerialName("pressedContentColor")
        @Contextual val pressedContentColor: Color,
        @SerialName("borderWidth")
        val borderWidth: Int,
        @SerialName("pressedBorderWidth")
        val pressedBorderWidth: Int,
        @SerialName("borderColor")
        @Contextual val borderColor: Color,
        @SerialName("pressedBorderColor")
        @Contextual val pressedBorderColor: Color,
        @SerialName("borderRadius")
        val borderRadius: ButtonShape,
        @SerialName("pressedBorderRadius")
        val pressedBorderRadius: ButtonShape
    )
}

public val DefaultStyleConfig = StyleConfig(
    alpha = 1f,
    backgroundColor = Color.Black.copy(alpha = 0.5f),
    contentColor = Color.White,
    borderWidth = 0,
    borderColor = Color.White,
    borderRadius = ButtonShape(0f),
    pressedAlpha = 1f,
    pressedBackgroundColor = Color.Gray.copy(alpha = 0.7f),
    pressedContentColor = Color.White,
    pressedBorderWidth = 0,
    pressedBorderColor = Color.White,
    pressedBorderRadius = ButtonShape(0f)
)

public val DefaultStyle = ButtonStyle(
    name = "Default",
    uuid = randomUUID(),
    animateSwap = false,
    lightStyle = DefaultStyleConfig,
    darkStyle = DefaultStyleConfig
)

public fun createNewStyle(name: String): ButtonStyle = DefaultStyle.copy(name = name, uuid = randomUUID())

/**
 * 克隆一个新的ButtonStyle对象（UUID不同）
 */
public fun ButtonStyle.cloneNew(): ButtonStyle = this.copy(
    name = name,
    uuid = randomUUID(),
    animateSwap = animateSwap,
    lightStyle = lightStyle,
    darkStyle = darkStyle
)