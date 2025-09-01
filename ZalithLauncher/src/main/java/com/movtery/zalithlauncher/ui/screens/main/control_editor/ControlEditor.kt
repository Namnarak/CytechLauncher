package com.movtery.zalithlauncher.ui.screens.main.control_editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.layer_controller.data.NormalData
import com.movtery.layer_controller.data.TextData
import com.movtery.layer_controller.layout.ControlEditorLayer
import com.movtery.layer_controller.layout.ControlLayer
import com.movtery.layer_controller.layout.ControlLayout
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.layer_controller.utils.lang.TranslatableString
import com.movtery.layer_controller.utils.saveToFile
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.MenuState
import com.movtery.zalithlauncher.ui.components.ProgressDialog
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.getMessageOrToString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private class EditorViewModel(layout: ControlLayout) : ViewModel() {
    val observableLayout = ObservableControlLayout(layout)

    /**
     * 当前选中的控制层级
     */
    var selectedLayer by mutableStateOf<ObservableControlLayer?>(null)

    /**
     * 编辑器菜单状态
     */
    var editorMenu by mutableStateOf(MenuState.HIDE)

    /**
     * 编辑器各种操作项
     */
    var editorOperation by mutableStateOf<EditorOperation>(EditorOperation.None)

    fun switchMenu() {
        editorMenu = editorMenu.next()
    }

    fun addWidget(layers: List<ObservableControlLayer>, addToLayer: (ObservableControlLayer) -> Unit) {
        val layer = selectedLayer
        if (layers.isEmpty()) {
            editorOperation = EditorOperation.WarningNoLayers
        } else if (layer == null) {
            editorOperation = EditorOperation.WarningNoSelectLayer
        } else {
            addToLayer(layer)
        }
    }

    /**
     * 保存控制布局
     */
    fun save(
        targetFile: File,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            editorOperation = EditorOperation.Saving
            val layout = observableLayout.pack()
            runCatching {
                layout.saveToFile(targetFile)
            }.onFailure { e ->
                editorOperation = EditorOperation.SaveFailed(e)
            }.onSuccess {
                onSaved()
            }
            editorOperation = EditorOperation.None
        }
    }
}

@Composable
private fun rememberEditorViewModel(layout: ControlLayout) = viewModel(
    key = layout.toString()
) {
    EditorViewModel(layout)
}

@Composable
fun ControlEditor(
    layout: ControlLayout,
    targetFile: File,
    exit: () -> Unit
) {
    val viewModel = rememberEditorViewModel(layout)
    val layers by viewModel.observableLayout.layers.collectAsState()

    /** 默认新建的控件层的名称 */
    val defaultLayerName = stringResource(R.string.control_editor_layers_title)
    /** 默认新建的按键的名称 */
    val defaultButtonName = stringResource(R.string.control_editor_edit_button_default)
    /** 默认新建的文本框的名称 */
    val defaultTextName = stringResource(R.string.control_editor_edit_text_default)

    ControlEditorLayer(
        observedLayout = viewModel.observableLayout,
        onButtonTap = { data ->
            viewModel.editorOperation = EditorOperation.SelectButton(data)
        }
    )

    EditorMenu(
        state = viewModel.editorMenu,
        closeScreen = { viewModel.editorMenu = MenuState.HIDE },
        layers = layers,
        selectedLayer = viewModel.selectedLayer,
        onLayerSelected = { layer ->
            viewModel.selectedLayer = layer
        },
        createLayer = {
            viewModel.observableLayout.addLayer(ControlLayer(name = defaultLayerName))
        },
        deleteLayer = { layer ->
            if (layer == viewModel.selectedLayer) viewModel.selectedLayer = null
            viewModel.observableLayout.removeLayer(layer.uuid)
        },
        addNewButton = {
            viewModel.addWidget(layers) { layer ->
                layer.addNormalButton(NormalData(text = TranslatableString.create(default = defaultButtonName)))
            }
        },
        addNewText = {
            viewModel.addWidget(layers) { layer ->
                layer.addTextBox(TextData(text = TranslatableString.create(default = defaultTextName)))
            }
        },
        saveAndExit = {
            viewModel.save(targetFile, onSaved = exit)
        }
    )

    MenuBox {
        viewModel.switchMenu()
    }

    EditorOperation(
        operation = viewModel.editorOperation,
        changeOperation = { viewModel.editorOperation = it }
    )
}

@Composable
private fun EditorOperation(
    operation: EditorOperation,
    changeOperation: (EditorOperation) -> Unit
) {
    when (operation) {
        is EditorOperation.None -> {}
        is EditorOperation.SelectButton -> {
            EditWidgetDialog(
                data = operation.data
            ) {
                changeOperation(EditorOperation.None)
            }
        }
        is EditorOperation.WarningNoLayers -> {
            SimpleAlertDialog(
                title = stringResource(R.string.control_editor_menu_no_layers_title),
                text = stringResource(R.string.control_editor_menu_no_layers_message)
            ) {
                changeOperation(EditorOperation.None)
            }
        }
        is EditorOperation.WarningNoSelectLayer -> {
            SimpleAlertDialog(
                title = stringResource(R.string.control_editor_menu_no_selected_layer_title),
                text = stringResource(R.string.control_editor_menu_no_selected_layer_message)
            ) {
                changeOperation(EditorOperation.None)
            }
        }
        is EditorOperation.Saving -> {
            ProgressDialog(
                title = stringResource(R.string.control_manage_saving)
            )
        }
        is EditorOperation.SaveFailed -> {
            SimpleAlertDialog(
                title = stringResource(R.string.control_manage_failed_to_save),
                text = operation.error.getMessageOrToString()
            ) {
                changeOperation(EditorOperation.None)
            }
        }
    }
}