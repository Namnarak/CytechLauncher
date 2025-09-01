package com.movtery.layer_controller.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.data.ButtonStyle

/**
 * 可观察的ButtonStyle包装类
 */
class ObservableButtonStyle(style: ButtonStyle): Packable<ButtonStyle> {
    val uuid = style.uuid
    var name by mutableStateOf(style.name)
    var animateSwap by mutableStateOf(style.animateSwap)
    var lightStyle by mutableStateOf(ObservableStyleConfig(style.lightStyle))
    var darkStyle by mutableStateOf(ObservableStyleConfig(style.darkStyle))

    override fun pack(): ButtonStyle {
        return ButtonStyle(
            name = this.name,
            uuid = this.uuid,
            animateSwap = this.animateSwap,
            lightStyle = this.lightStyle.pack(),
            darkStyle = this.darkStyle.pack()
        )
    }

    companion object {
        public val Default = ObservableButtonStyle(ButtonStyle.Default)
    }
}