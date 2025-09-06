package com.movtery.zalithlauncher.ui.screens.main.control_editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import com.movtery.layer_controller.ControlEditorLayer
import com.movtery.layer_controller.data.ButtonPosition
import com.movtery.layer_controller.data.ButtonSize
import com.movtery.layer_controller.data.NormalData
import com.movtery.layer_controller.data.TextData
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.data.Widget.Companion.createWithUUID
import com.movtery.layer_controller.data.lang.TranslatableString
import com.movtery.layer_controller.layout.ControlLayer
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.components.MenuState
import com.movtery.zalithlauncher.ui.components.ProgressDialog
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_layer.EditControlLayerDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_layer.EditSwitchLayersVisibilityDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_style.EditStyleDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_style.StyleListDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_translatable.EditTranslatableTextDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget.EditWidgetDialog
import com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget.SelectLayers
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
    val styles by viewModel.observableLayout.styles.collectAsState()

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
        snapInAllLayers = AllSettings.editorSnapInAllLayers.state,
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
            viewModel.observableLayout.addLayer(ControlLayer.createNew(defaultLayerName = defaultLayerName))
        },
        onAttribute = { layer ->
            viewModel.editorOperation = EditorOperation.EditLayer(layer)
        },
        addNewButton = {
            viewModel.addWidget(layers) { layer ->
                layer.addNormalButton(
                    createWithUUID { uuid ->
                        NormalData(
                            text = TranslatableString.create(default = defaultButtonName),
                            uuid = uuid,
                            position = ButtonPosition.Center,
                            buttonSize = ButtonSize.createAdaptiveButtonSize(
                                referenceLength = screenHeight,
                                density = density
                            ),
                            visibilityType = VisibilityType.ALWAYS,
                            isSwipple = false,
                            isPenetrable = false,
                            isToggleable = false
                        )
                    }
                )
            }
        },
        addNewText = {
            viewModel.addWidget(layers) { layer ->
                layer.addTextBox(
                    createWithUUID { uuid ->
                        TextData(
                            text = TranslatableString.create(default = defaultTextName),
                            uuid = uuid,
                            position = ButtonPosition.Center,
                            buttonSize = ButtonSize.createAdaptiveButtonSize(
                                referenceLength = screenHeight,
                                density = density,
                                type = ButtonSize.Type.WrapContent //文本框默认使用包裹内容
                            ),
                            visibilityType = VisibilityType.ALWAYS
                        )
                    }
                )
            }
        },
        openStyleList = {
            viewModel.editorOperation = EditorOperation.OpenStyleList
        },
        onSave = {
            viewModel.save(targetFile, onSaved = {})
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
        onCloneWidgets = { widget, layers ->
            viewModel.cloneWidgetToLayers(widget, layers)
        },
        onCreateStyle = { name ->
            viewModel.createNewStyle(name)
        },
        onCloneStyle = { style ->
            viewModel.cloneStyle(style)
        },
        onDeleteStyle = { style ->
            viewModel.removeStyle(style)
        },
        controlLayers = layers,
        styles = styles
    )
}

@Composable
private fun EditorOperation(
    operation: EditorOperation,
    changeOperation: (EditorOperation) -> Unit,
    onDeleteWidget: (ObservableWidget, ObservableControlLayer) -> Unit,
    onDeleteLayer: (ObservableControlLayer) -> Unit,
    onCloneWidgets: (ObservableWidget, List<ObservableControlLayer>) -> Unit,
    onCreateStyle: (name: String) -> Unit,
    onCloneStyle: (ObservableButtonStyle) -> Unit,
    onDeleteStyle: (ObservableButtonStyle) -> Unit,
    controlLayers: List<ObservableControlLayer>,
    styles: List<ObservableButtonStyle>
) {
    when (operation) {
        is EditorOperation.None -> {}
        is EditorOperation.SelectButton -> {
            val data = operation.data
            val layer = operation.layer
            EditWidgetDialog(
                data = data,
                styles = styles,
                onDismissRequest = {
                    changeOperation(EditorOperation.None)
                },
                onDelete = {
                    onDeleteWidget(data, layer)
                    changeOperation(EditorOperation.None)
                },
                onClone = {
                    changeOperation(EditorOperation.CloneButton(data, layer))
                },
                onEditWidgetText = { string ->
                    changeOperation(EditorOperation.EditWidgetText(string))
                },
                switchControlLayers = { data ->
                    changeOperation(EditorOperation.SwitchLayersVisibility(data))
                },
                openStyleList = {
                    changeOperation(EditorOperation.OpenStyleList)
                }
            )
        }
        is EditorOperation.CloneButton -> {
            val data = operation.data
            val layer = operation.layer
            SelectLayers(
                layers= controlLayers,
                initLayer = layer,
                onDismissRequest = {
                    changeOperation(EditorOperation.None)
                },
                title = stringResource(R.string.control_editor_edit_dialog_clone_widget_title),
                confirmText = stringResource(R.string.control_editor_edit_dialog_clone_widget),
                onConfirm = { layers ->
                    onCloneWidgets(data, layers)
                    changeOperation(EditorOperation.None)
                }
            )
        }
        is EditorOperation.EditWidgetText -> {
            EditTranslatableTextDialog(
                text = operation.string,
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
        is EditorOperation.OpenStyleList -> {
            StyleListDialog(
                styles = styles,
                onEditStyle = { style ->
                    changeOperation(EditorOperation.EditStyle(style))
                },
                onCreate = {
                    changeOperation(EditorOperation.CreateStyle)
                },
                onClone = { style ->
                    onCloneStyle(style)
                },
                onDelete = { style ->
                    onDeleteStyle(style)
                },
                onClose = {
                    changeOperation(EditorOperation.None)
                }
            )
        }
        is EditorOperation.CreateStyle -> {
            var name by remember { mutableStateOf("") }
            SimpleEditDialog(
                title = stringResource(R.string.control_editor_edit_style_config_name),
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                onDismissRequest = {
                    changeOperation(EditorOperation.None)
                },
                onConfirm = {
                    onCreateStyle(name)
                    changeOperation(EditorOperation.OpenStyleList)
                }
            )
        }
        is EditorOperation.EditStyle -> {
            EditStyleDialog(
                style = operation.style,
                onClose = {
                    changeOperation(EditorOperation.None)
                }
            )
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