package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget

import android.view.WindowManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableNormalData
import com.movtery.layer_controller.observable.ObservableTranslatableString
import com.movtery.layer_controller.observable.ObservableWidget
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.rememberAutoScrollToEndState
import com.movtery.zalithlauncher.ui.screens.clearWith
import com.movtery.zalithlauncher.ui.screens.content.elements.CategoryItem

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
    data: ObservableWidget,
    styles: List<ObservableButtonStyle>,
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit,
    onClone: () -> Unit,
    onEditWidgetText: (ObservableTranslatableString) -> Unit,
    switchControlLayers: (ObservableNormalData) -> Unit,
    openStyleList: () -> Unit
) {
    val backStack = rememberNavBackStack(EditWidgetCategory.Info)
    var dialogTransparent by remember { mutableStateOf(EditWidgetDialogState.OPAQUE) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false,
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
                .fillMaxHeight()
                .alpha(cardAlpha),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 16.dp),
                shadowElevation = 3.dp,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
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
                            styles = styles,
                            switchControlLayers = switchControlLayers,
                            openStyleList = openStyleList,
                            onEditWidgetText = onEditWidgetText,
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
                    Row(
                        modifier = Modifier
                            .padding(all = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (dialogTransparent != EditWidgetDialogState.SEMI_TRANSPARENT) {
                            Button(
                                onClick = {
                                    dialogTransparent = dialogTransparent.nextByUser()
                                }
                            ) {
                                MarqueeText(text = stringResource(dialogTransparent.buttonText))
                            }
                            Spacer(Modifier.width(16.dp))
                        }

                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberAutoScrollToEndState()),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = onDelete
                            ) {
                                MarqueeText(text = stringResource(R.string.generic_delete))
                            }

                            Button(
                                onClick = onClone
                            ) {
                                MarqueeText(text = stringResource(R.string.control_editor_edit_dialog_clone_widget))
                            }

                            Button(
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
    backStack: NavBackStack<NavKey>,
    data: ObservableWidget,
    styles: List<ObservableButtonStyle>,
    onEditWidgetText: (ObservableTranslatableString) -> Unit,
    switchControlLayers: (ObservableNormalData) -> Unit,
    openStyleList: () -> Unit,
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
                    EditWidgetInfo(data, onEditWidgetText, onPreviewRequested, onDismissRequested)
                }
                entry<EditWidgetCategory.ClickEvent> {
                    EditWidgetClickEvent(data as ObservableNormalData, switchControlLayers)
                }
                entry<EditWidgetCategory.Style> {
                    EditWidgetStyle(data, styles, openStyleList)
                }
            }
        )
    }
}