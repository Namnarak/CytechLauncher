package com.movtery.layer_controller.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.data.ButtonStyle
import com.movtery.layer_controller.data.ButtonStyle.Companion.cloneNew

/**
 * 可观察的ButtonStyle包装类
 */
class ObservableButtonStyle(style: ButtonStyle): Packable<ButtonStyle> {
    val uuid = style.uuid
    var name by mutableStateOf(style.name)
    var animateSwap by mutableStateOf(style.animateSwap)
    var lightStyle = ObservableStyleConfig(style.lightStyle)
    var darkStyle = ObservableStyleConfig(style.darkStyle)

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

        fun ObservableButtonStyle.cloneNew(): ObservableButtonStyle {
            return ObservableButtonStyle(pack().cloneNew())
        }
    }
}