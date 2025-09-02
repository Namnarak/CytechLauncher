package com.movtery.zalithlauncher.ui.screens.main.control_editor

import android.view.WindowManager
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.observable.ObservableBaseData
import com.movtery.layer_controller.observable.ObservableControlLayer
import com.movtery.layer_controller.observable.ObservableNormalData
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.DraggableBox
import com.movtery.zalithlauncher.ui.components.DualMenuSubscreen
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.MenuState
import com.movtery.zalithlauncher.ui.components.MenuTextButton
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.itemLayoutColor
import com.movtery.zalithlauncher.ui.components.itemLayoutColorOnSurface
import com.movtery.zalithlauncher.ui.screens.clearWith
import com.movtery.zalithlauncher.ui.screens.content.elements.CategoryItem

/**
 * 控制布局编辑器操作状态
 */
sealed interface EditorOperation {
    data object None : EditorOperation
    /** 选择了一个控件, 附带其所属控件层 */
    data class SelectButton(val data: ObservableBaseData, val layer: ObservableControlLayer) : EditorOperation
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
    deleteLayer: (ObservableControlLayer) -> Unit,
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
                        deleteLayer = {
                            deleteLayer(layer)
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
    deleteLayer: () -> Unit,
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
            Text(
                modifier = Modifier.weight(1f),
                text = layer.name,
                style = MaterialTheme.typography.bodyMedium
            )
            IconButton(
                onClick = deleteLayer
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.generic_delete)
                )
            }
        }
    }
}

private enum class EditWidgetDialogState(val alpha: Float, val buttonText: Int) {
    /** 完全不透明 */
    OPAQUE(1.0f, R.string.control_editor_edit_dialog_open_preview) {
        override fun nextByUser(): EditWidgetDialogState = SEMI_TRANSPARENT_USER
    },
    /** 半透明 */
    SEMI_TRANSPARENT(0.3f, R.string.control_editor_edit_dialog_close_preview) {
        override fun nextByUser(): EditWidgetDialogState = OPAQUE
    },
    /** 半透明（用户主动选择） */
    SEMI_TRANSPARENT_USER(0.3f, R.string.control_editor_edit_dialog_close_preview){
        override fun nextByUser(): EditWidgetDialogState = OPAQUE
    };

    abstract fun nextByUser(): EditWidgetDialogState
}

/**
 * 控件编辑对话框
 */
@Composable
fun EditWidgetDialog(
    data: ObservableBaseData,
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit,
    switchControlLayers: (ObservableNormalData) -> Unit
) {
    val backStack = rememberNavBackStack(EditWidgetCategory.Info)
    var dialogTransparent by remember { mutableStateOf(EditWidgetDialogState.OPAQUE) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        val window = (LocalView.current.parent as DialogWindowProvider).window
        //清除dim，避免背景变暗
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        val cardAlpha by animateFloatAsState(dialogTransparent.alpha)

        val categories = if (data is ObservableNormalData) {
            editWidgetCategories
        } else {
            editWidgetCategories.filterNot { it.key == EditWidgetCategory.ClickEvent }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .padding(5.dp)
                .alpha(cardAlpha)
        ) {
            Surface(
                modifier = Modifier.padding(all = 3.dp),
                shadowElevation = 3.dp,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        EditWidgetTabLayout(
                            modifier = Modifier.fillMaxHeight(),
                            items = categories,
                            currentKey = backStack.lastOrNull(),
                            navigateTo = { key ->
                                backStack.clearWith(key)
                            }
                        )

                        EditWidgetNavigation(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            backStack = backStack,
                            data = data,
                            switchControlLayers = switchControlLayers,
                            onPreviewRequested = {
                                if (dialogTransparent == EditWidgetDialogState.SEMI_TRANSPARENT_USER) return@EditWidgetNavigation
                                dialogTransparent = EditWidgetDialogState.SEMI_TRANSPARENT
                            },
                            onDismissRequested = {
                                if (dialogTransparent == EditWidgetDialogState.SEMI_TRANSPARENT_USER) return@EditWidgetNavigation
                                dialogTransparent = EditWidgetDialogState.OPAQUE
                            }
                        )
                    }
                    //底部操作栏
                    ConstraintLayout(
                        modifier = Modifier
                            .padding(all = 8.dp)
                            .fillMaxWidth()
                    ) {
                        val (previewButton, deleteButton, closeButton) = createRefs()

                        //非用户决定时，隐藏这个按钮
                        if (dialogTransparent != EditWidgetDialogState.SEMI_TRANSPARENT) {
                            Button(
                                modifier = Modifier
                                    .constrainAs(previewButton) {
                                        start.linkTo(parent.start)
                                    },
                                onClick = {
                                    dialogTransparent = dialogTransparent.nextByUser()
                                }
                            ) {
                                MarqueeText(
                                    text = stringResource(dialogTransparent.buttonText)
                                )
                            }
                        }

                        Button(
                            modifier = Modifier
                                .constrainAs(deleteButton) {
                                    end.linkTo(closeButton.start, 16.dp)
                                },
                            onClick = onDelete
                        ) {
                            MarqueeText(text = stringResource(R.string.generic_delete))
                        }

                        Button(
                            modifier = Modifier
                                .constrainAs(closeButton) {
                                    end.linkTo(parent.end)
                                },
                            onClick = onDismissRequest
                        ) {
                            MarqueeText(text = stringResource(R.string.generic_close))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditWidgetTabLayout(
    modifier: Modifier = Modifier,
    items: List<CategoryItem>,
    currentKey: NavKey?,
    navigateTo: (NavKey) -> Unit
) {
    NavigationRail(
        modifier = modifier
            .width(IntrinsicSize.Min)
            .verticalScroll(rememberScrollState()),
        containerColor = Color.Transparent,
        windowInsets = WindowInsets(0)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        items.forEach { item ->
            if (item.division) {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(all = 12.dp)
                        .fillMaxWidth()
                        .alpha(0.5f),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            NavigationRailItem(
                selected = currentKey == item.key,
                onClick = {
                    navigateTo(item.key)
                },
                icon = {
                    item.icon()
                },
                label = {
                    Text(
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        text = stringResource(item.textRes),
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EditWidgetNavigation(
    modifier: Modifier = Modifier,
    backStack: NavBackStack,
    data: ObservableBaseData,
    switchControlLayers: (ObservableNormalData) -> Unit,
    onPreviewRequested: () -> Unit,
    onDismissRequested: () -> Unit
) {
    if (backStack.isNotEmpty()) {
        NavDisplay(
            modifier = modifier,
            backStack = backStack,
            onBack = { /* 忽略 */ },
            entryProvider = entryProvider {
                entry<EditWidgetCategory.Info> {
                    EditWidgetInfo(data, onPreviewRequested, onDismissRequested)
                }
                entry<EditWidgetCategory.ClickEvent> {
                    EditWidgetClickEvent(data as ObservableNormalData, switchControlLayers)
                }
                entry<EditWidgetCategory.Style> {

                }
            }
        )
    }
}


@Composable
fun EditSwitchLayersVisibilityDialog(
    data: ObservableNormalData,
    layers: List<ObservableControlLayer>,
    onDismissRequest: () -> Unit
) {
    /**
     * 缓存哪些控制层被选中
     */
    val layerSelected = remember { mutableStateListOf<ObservableControlLayer>() }

    LaunchedEffect(data.clickEvents) {
        val layerUuids = layers.map { it.uuid }.toSet()
        val unsafeEvents = data.clickEvents.filter { event ->
            event.type == ClickEvent.Type.SwitchLayer && event.key !in layerUuids //控件层已不存在
        }

        if (unsafeEvents.isNotEmpty()) {
            data.removeAllEvent(unsafeEvents)
            return@LaunchedEffect
        }

        val validLayerEvents = data.clickEvents.filter {
            it.type == ClickEvent.Type.SwitchLayer
        }

        val eventLayerMap = validLayerEvents.associateBy { it.key }
        val selectedLayers = layers.filter { layer ->
            layer.uuid in eventLayerMap
        }

        layerSelected.clear()
        layerSelected.addAll(selectedLayers)
    }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            shadowElevation = 3.dp,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(all = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MarqueeText(
                    text = stringResource(R.string.control_editor_edit_switch_layers),
                    style = MaterialTheme.typography.titleMedium
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(layers) { layer ->
                        LayerVisibilityItem(
                            modifier = Modifier.fillMaxWidth(),
                            layer = layer,
                            selected = layerSelected.contains(layer),
                            onSelectedChange = { selected ->
                                val event = ClickEvent(ClickEvent.Type.SwitchLayer, layer.uuid)
                                if (selected) {
                                    data.addEvent(event)
                                } else {
                                    data.removeEvent(event)
                                }
                            }
                        )
                    }
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDismissRequest
                ) {
                    MarqueeText(
                        text = stringResource(R.string.generic_close)
                    )
                }
            }
        }
    }
}

@Composable
private fun LayerVisibilityItem(
    modifier: Modifier = Modifier,
    layer: ObservableControlLayer,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    color: Color = itemLayoutColorOnSurface(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    InfoLayoutItem(
        modifier = modifier,
        onClick = {
            onSelectedChange(!selected)
        },
        color = color,
        contentColor = contentColor
    ) {
        MarqueeText(
            modifier = Modifier.weight(1f),
            text = layer.name,
            style = MaterialTheme.typography.bodyMedium
        )
        Checkbox(
            checked = selected,
            onCheckedChange = onSelectedChange
        )
    }
}
