package com.movtery.zalithlauncher.ui.screens.main.control_editor

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.observable.ObservableBaseData
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableNormalData
import com.movtery.layer_controller.observable.ObservableTextData
import com.movtery.layer_controller.utils.snap.SnapMode
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.components.DraggableBox
import com.movtery.zalithlauncher.ui.components.DualMenuSubscreen
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.MenuListLayout
import com.movtery.zalithlauncher.ui.components.MenuState
import com.movtery.zalithlauncher.ui.components.MenuSwitchButton
import com.movtery.zalithlauncher.ui.components.MenuTextButton
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.itemLayoutColor

/**
 * 控制布局编辑器操作状态
 */
sealed interface EditorOperation {
    data object None : EditorOperation
    /** 选择了一个控件, 附带其所属控件层 */
    data class SelectButton(val data: ObservableBaseData, val layer: ObservableControlLayer) : EditorOperation
    /** 编辑控件的显示文本 */
    data class EditWidgetText(val data: ObservableTextData) : EditorOperation
    /** 编辑控件层属性 */
    data class EditLayer(val layer: ObservableControlLayer) : EditorOperation
    /** 编辑切换控件层可见性事件 */
    data class SwitchLayersVisibility(val data: ObservableNormalData) : EditorOperation
    /** 没有控件层级，提醒用户添加 */
    data object WarningNoLayers : EditorOperation
    /** 没有选择控件层，提醒用户选择 */
    data object WarningNoSelectLayer : EditorOperation
    /** 控制布局正在保存中 */
    data object Saving : EditorOperation
    /** 控制布局保存失败 */
    data class SaveFailed(val error: Throwable) : EditorOperation
}

@Composable
fun VisibilityType.getVisibilityText(): String {
    val textRes = when (this) {
        VisibilityType.ALWAYS -> R.string.control_editor_edit_visibility_always
        VisibilityType.IN_GAME -> R.string.control_editor_edit_visibility_in_game
        VisibilityType.IN_MENU -> R.string.control_editor_edit_visibility_in_menu
    }
    return stringResource(textRes)
}

@Composable
fun MenuBox(
    onClick: () -> Unit
) {
    DraggableBox(
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(all = 2.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(28.dp),
                imageVector = Icons.Default.Settings,
                contentDescription = null
            )
        }
    }
}

@Composable
fun EditorMenu(
    state: MenuState,
    closeScreen: () -> Unit,
    layers: List<ObservableControlLayer>,
    selectedLayer: ObservableControlLayer?,
    onLayerSelected: (ObservableControlLayer) -> Unit,
    createLayer: () -> Unit,
    onAttribute: (ObservableControlLayer) -> Unit,
    addNewButton: () -> Unit,
    addNewText: () -> Unit,
    saveAndExit: () -> Unit
) {
    DualMenuSubscreen(
        state = state,
        closeScreen = closeScreen,
        leftMenuContent = {
            Text(
                modifier = Modifier
                    .padding(all = 8.dp)
                    .align(Alignment.CenterHorizontally),
                text = stringResource(R.string.control_editor_menu_title),
                style = MaterialTheme.typography.titleMedium
            )
            HorizontalDivider(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(all = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                //添加按钮
                item {
                    MenuTextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.control_editor_menu_new_widget_button),
                        onClick = addNewButton
                    )
                }

                //添加文本框
                item {
                    MenuTextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.control_editor_menu_new_widget_text),
                        onClick = addNewText
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                //控件吸附
                item {
                    MenuSwitchButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.control_editor_menu_widget_snap),
                        switch = AllSettings.editorEnableWidgetSnap.state,
                        onSwitch = { AllSettings.editorEnableWidgetSnap.save(it) }
                    )
                }

                //控件吸附模式
                item {
                    MenuListLayout(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(R.string.control_editor_menu_widget_snap_mode),
                        items = SnapMode.entries,
                        currentItem = AllSettings.editorWidgetSnapMode.state,
                        onItemChange = { AllSettings.editorWidgetSnapMode.save(it) },
                        getItemText = { mode ->
                            val textRes = when (mode) {
                                SnapMode.FullScreen -> R.string.control_editor_menu_widget_snap_mode_fullscreen
                                SnapMode.Local -> R.string.control_editor_menu_widget_snap_mode_local
                            }
                            stringResource(textRes)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                //保存并退出
                item {
                    MenuTextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.control_editor_menu_save_and_exit),
                        onClick = saveAndExit
                    )
                }
            }
        },
        rightMenuContent = {
            Text(
                modifier = Modifier
                    .padding(all = 8.dp)
                    .align(Alignment.CenterHorizontally),
                text = stringResource(R.string.control_editor_layers_title),
                style = MaterialTheme.typography.titleMedium
            )
            HorizontalDivider(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(all = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(layers) { layer ->
                    ControlLayerItem(
                        modifier = Modifier.fillMaxWidth(),
                        layer = layer,
                        selected = selectedLayer == layer,
                        onSelected = {
                            onLayerSelected(layer)
                        },
                        onAttribute = {
                            onAttribute(layer)
                        }
                    )
                }
            }
            ScalingActionButton(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 4.dp)
                    .fillMaxWidth(),
                onClick = createLayer
            ) {
                MarqueeText(text = stringResource(R.string.control_editor_layers_create))
            }
        }
    )
}

@Composable
private fun ControlLayerItem(
    modifier: Modifier = Modifier,
    layer: ObservableControlLayer,
    selected: Boolean,
    onSelected: () -> Unit,
    onAttribute: () -> Unit,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = MaterialTheme.shapes.large
) {
    val borderWidth by animateDpAsState(
        if (selected) 4.dp
        else (-1).dp
    )

    Surface(
        modifier = modifier.border(
            width = borderWidth,
            color = borderColor,
            shape = shape
        ),
        color = color,
        contentColor = contentColor,
        shape = shape,
        shadowElevation = 1.dp,
        onClick = {
            if (selected) return@Surface
            onSelected()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = MaterialTheme.shapes.large)
                .padding(all = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    layer.hide = !layer.hide
                }
            ) {
                Crossfade(
                    targetState = layer.hide
                ) { isHide ->
                    Icon(
                        imageVector = if (isHide) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = null
                    )
                }
            }
            MarqueeText(
                modifier = Modifier.weight(1f),
                text = layer.name,
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(
                onClick = onAttribute
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = stringResource(R.string.control_editor_layers_attribute)
                )
            }
        }
    }
}
