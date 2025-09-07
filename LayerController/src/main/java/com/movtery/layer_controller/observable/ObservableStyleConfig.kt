package com.movtery.layer_controller.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.data.ButtonStyle

/**
 * 可观察的StyleConfig包装类
 */
class ObservableStyleConfig(config: ButtonStyle.StyleConfig): Packable<ButtonStyle.StyleConfig> {
    var alpha by mutableFloatStateOf(config.alpha)
    var backgroundColor by mutableStateOf(config.backgroundColor)
    var contentColor by mutableStateOf(config.contentColor)
    var borderWidth by mutableIntStateOf(config.borderWidth)
    var borderColor by mutableStateOf(config.borderColor)
    var borderRadius by mutableStateOf(config.borderRadius)
    var pressedAlpha by mutableFloatStateOf(config.pressedAlpha)
    var pressedBackgroundColor by mutableStateOf(config.pressedBackgroundColor)
    var pressedContentColor by mutableStateOf(config.pressedContentColor)
    var pressedBorderWidth by mutableIntStateOf(config.pressedBorderWidth)
    var pressedBorderColor by mutableStateOf(config.pressedBorderColor)
    var pressedBorderRadius by mutableStateOf(config.pressedBorderRadius)

    override fun pack(): ButtonStyle.StyleConfig {
        return ButtonStyle.StyleConfig(
            alpha = this.alpha,
            backgroundColor = this.backgroundColor,
            contentColor = this.contentColor,
            borderWidth = this.borderWidth,
            borderColor = this.borderColor,
            borderRadius = this.borderRadius,
            pressedAlpha = this.pressedAlpha,
            pressedBackgroundColor = this.pressedBackgroundColor,
            pressedContentColor = this.pressedContentColor,
            pressedBorderWidth = this.pressedBorderWidth,
            pressedBorderColor = this.pressedBorderColor,
            pressedBorderRadius = this.pressedBorderRadius
        )
    }
}