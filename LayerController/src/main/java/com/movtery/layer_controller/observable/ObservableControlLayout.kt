package com.movtery.layer_controller.observable

import com.movtery.layer_controller.data.ButtonStyle
import com.movtery.layer_controller.layout.ControlLayer
import com.movtery.layer_controller.layout.ControlLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * 可观察的ControlLayout包装类，用于监听变化
 */
class ObservableControlLayout(private val layout: ControlLayout): Packable<ControlLayout> {
    private val _layers = MutableStateFlow(layout.layers.map { ObservableControlLayer(it) })
    val layers: StateFlow<List<ObservableControlLayer>> = _layers
    
    private val _styles = MutableStateFlow(layout.styles.map { ObservableButtonStyle(it) })
    val styles: StateFlow<List<ObservableButtonStyle>> = _styles

    /**
     * 添加控制层
     */
    fun addLayer(layer: ControlLayer) {
        //               在顶部添加
        _layers.update { listOf(ObservableControlLayer(layer)) + it }
    }

    /**
     * 移除控制层
     */
    fun removeLayer(uuid: String) {
        _layers.update { oldLayers ->
            oldLayers.filterNot { it.uuid == uuid }
        }
    }

    /**
     * 添加新的按钮样式
     */
    fun addStyle(style: ButtonStyle) {
        _styles.update { it + ObservableButtonStyle(style) }
    }

    /**
     * 移除按钮样式
     */
    fun removeStyle(uuid: String) {
        _styles.update { oldStyles ->
            oldStyles.filterNot { it.uuid == uuid }
        }
    }

    override fun pack(): ControlLayout {
        return layout.copy(
            layers = _layers.value.map { it.pack() },
            styles = _styles.value.map { it.pack() }
        )
    }
}