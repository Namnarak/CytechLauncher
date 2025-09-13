package com.movtery.layer_controller.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.data.NormalData
import com.movtery.layer_controller.data.TextData
import com.movtery.layer_controller.layout.ControlLayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * 可观察的ControlLayer包装类
 */
class ObservableControlLayer(layer: ControlLayer): Packable<ControlLayer> {
    val uuid: String = layer.uuid

    var name by mutableStateOf(layer.name)
    var hide by mutableStateOf(layer.hide)
    var visibilityType by mutableStateOf(layer.visibilityType)
    
    private val _normalButtons = MutableStateFlow(layer.normalButtons.map { ObservableNormalData(it) })
    val normalButtons: StateFlow<List<ObservableNormalData>> = _normalButtons
    
    private val _textBoxes = MutableStateFlow(layer.textBoxes.map { ObservableTextData(it) })
    val textBoxes: StateFlow<List<ObservableTextData>> = _textBoxes

    /**
     * 添加一个普通的按钮
     */
    fun addNormalButton(button: NormalData) {
        addNormalButton(ObservableNormalData(button))
    }

    /**
     * 添加一个普通的按钮
     */
    fun addNormalButton(button: ObservableNormalData) {
        _normalButtons.update { it + button }
    }

    /**
     * 批量添加普通的按钮
     */
    fun addAllNormalButton(buttons: List<ObservableNormalData>) {
        _normalButtons.update { it + buttons }
    }

    /**
     * 移除一个普通的按钮
     */
    fun removeNormalButton(uuid: String) {
        _normalButtons.update { oldList ->
            oldList.filterNot { it.uuid == uuid }
        }
    }

    /**
     * 添加文本展示框
     */
    fun addTextBox(textBox: TextData) {
        addTextBox(ObservableTextData(textBox))
    }

    /**
     * 添加文本展示框
     */
    fun addTextBox(textBox: ObservableTextData) {
        _textBoxes.update { it + textBox }
    }

    /**
     * 批量添加文本展示框
     */
    fun addAllTextBox(textBoxes: List<ObservableTextData>) {
        _textBoxes.update { it + textBoxes }
    }

    /**
     * 移除文本展示框
     */
    fun removeTextBox(uuid: String) {
        _textBoxes.update { oldList ->
            oldList.filterNot { it.uuid == uuid }
        }
    }

    override fun pack(): ControlLayer {
        return ControlLayer(
            name = name,
            uuid = uuid,
            hide = hide,
            visibilityType = visibilityType,
            normalButtons = _normalButtons.value.map { it.packNormal() },
            textBoxes = _textBoxes.value.map { it.packText() }
        )
    }
}