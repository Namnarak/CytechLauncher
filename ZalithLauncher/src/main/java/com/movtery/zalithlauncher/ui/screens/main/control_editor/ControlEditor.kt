package com.movtery.zalithlauncher.ui.screens.main.control_editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import com.movtery.layer_controller.ControlEditorLayer
import com.movtery.layer_controller.data.ButtonPosition
import com.movtery.layer_controller.data.ButtonSize
import com.movtery.layer_controller.data.NormalData
import com.movtery.layer_controller.data.TextData
import com.movtery.layer_controller.layout.ControlLayer
import com.movtery.layer_controller.observable.ObservableBaseData
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.utils.lang.TranslatableString
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.components.MenuState
import com.movtery.zalithlauncher.ui.components.ProgressDialog
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_layer.EditControlLayerDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_layer.EditSwitchLayersVisibilityDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_translatable.EditTranslatableTextDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget.EditWidgetDialog
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.EditorViewModel
import java.io.File

@Composable
fun ControlEditor(
    viewModel: EditorViewModel,
    targetFile: File,
    exit: () -> Unit
) {
    val layers by viewModel.observableLayout.layers.collectAsState()

    /** 默认新建的控件层的名称 */
    val defaultLayerName = stringResource(R.string.control_editor_edit_layer_default)
    /** 默认新建的按键的名称 */
    val defaultButtonName = stringResource(R.string.control_editor_edit_button_default)
    /** 默认新建的文本框的名称 */
    val defaultTextName = stringResource(R.string.control_editor_edit_text_default)

    val density = LocalDensity.current.density
    val screenSize = LocalWindowInfo.current.containerSize
    val screenHeight = remember(screenSize) { screenSize.height }

    ControlEditorLayer(
        observedLayout = viewModel.observableLayout,
        onButtonTap = { data, layer ->
            viewModel.editorOperation = EditorOperation.SelectButton(data, layer)
        },
        enableSnap = AllSettings.editorEnableWidgetSnap.state,
        snapMode = AllSettings.editorWidgetSnapMode.state
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
        onAttribute = { layer ->
            viewModel.editorOperation = EditorOperation.EditLayer(layer)
        },
        addNewButton = {
            viewModel.addWidget(layers) { layer ->
                layer.addNormalButton(
                    NormalData(
                        text = TranslatableString.create(default = defaultButtonName),
                        position = ButtonPosition.Center,
                        buttonSize = ButtonSize.createAdaptiveButtonSize(
                            referenceLength = screenHeight,
                            density = density
                        )
                    )
                )
            }
        },
        addNewText = {
            viewModel.addWidget(layers) { layer ->
                layer.addTextBox(
                    TextData(
                        text = TranslatableString.create(default = defaultTextName),
                        position = ButtonPosition.Center,
                        buttonSize = ButtonSize.createAdaptiveButtonSize(
                            referenceLength = screenHeight,
                            density = density,
                            type = ButtonSize.Type.WrapContent //文本框默认使用包裹内容
                        )
                    )
                )
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
        changeOperation = { viewModel.editorOperation = it },
        onDeleteWidget = { data, layer ->
            viewModel.removeWidget(layer, data)
        },
        onDeleteLayer = { layer ->
            viewModel.removeLayer(layer)
        },
        controlLayers = layers
    )
}

@Composable
private fun EditorOperation(
    operation: EditorOperation,
    changeOperation: (EditorOperation) -> Unit,
    onDeleteWidget: (ObservableBaseData, ObservableControlLayer) -> Unit,
    onDeleteLayer: (ObservableControlLayer) -> Unit,
    controlLayers: List<ObservableControlLayer>
) {
    when (operation) {
        is EditorOperation.None -> {}
        is EditorOperation.SelectButton -> {
            EditWidgetDialog(
                data = operation.data,
                onDismissRequest = {
                    changeOperation(EditorOperation.None)
                },
                onDelete = {
                    onDeleteWidget(operation.data, operation.layer)
                    changeOperation(EditorOperation.None)
                },
                onEditWidgetText = { textData ->
                    changeOperation(EditorOperation.EditWidgetText(textData))
                },
                switchControlLayers = { data ->
                    changeOperation(EditorOperation.SwitchLayersVisibility(data))
                }
            )
        }
        is EditorOperation.EditWidgetText -> {
            val textData = operation.data
            EditTranslatableTextDialog(
                text = textData.text,
                singleLine = false,
                onClose = {
                    changeOperation(EditorOperation.None)
                }
            )
        }
        is EditorOperation.EditLayer -> {
            val layer = operation.layer
            EditControlLayerDialog(
                layer = layer,
                onDismissRequest = {
                    changeOperation(EditorOperation.None)
                },
                onDelete = {
                    onDeleteLayer(layer)
                    changeOperation(EditorOperation.None)
                }
            )
        }
        is EditorOperation.SwitchLayersVisibility -> {
            val data = operation.data
            EditSwitchLayersVisibilityDialog(
                data = data,
                layers = controlLayers,
                onDismissRequest = {
                    changeOperation(EditorOperation.None)
                }
            )
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