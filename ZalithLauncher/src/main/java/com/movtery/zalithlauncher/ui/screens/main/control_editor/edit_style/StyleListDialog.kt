package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_style

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowRight
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.layer_controller.layout.RendererStyleBox
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutTextItem
import com.movtery.zalithlauncher.utils.string.isNotEmptyOrBlank

@Composable
fun StyleListDialog(
    styles: List<ObservableButtonStyle>,
    onEditStyle: (ObservableButtonStyle) -> Unit,
    onCreate: () -> Unit,
    onClone: (ObservableButtonStyle) -> Unit,
    onDelete: (ObservableButtonStyle) -> Unit,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.padding(all = 3.dp),
                shadowElevation = 3.dp,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier.padding(all = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MarqueeText(
                        text = stringResource(R.string.control_editor_edit_style_config),
                        style = MaterialTheme.typography.titleMedium
                    )

                    val itemModifier = Modifier.padding(horizontal = 2.dp)

                    if (styles.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxWidth()
                                .animateContentSize(),
                            contentPadding = PaddingValues(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(styles) { style ->
                                StyleItem(
                                    modifier = itemModifier,
                                    style = style,
                                    onClick = { onEditStyle(style) },
                                    onClone = { onClone(style) },
                                    onDelete = { onDelete(style) }
                                )
                            }
                        }
                    } else {
                        InfoLayoutTextItem(
                            modifier = Modifier.padding(vertical = 12.dp),
                            title = stringResource(R.string.control_editor_edit_style_config_empty),
                            onClick = onCreate
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            modifier = Modifier.weight(1f, fill = false),
                            onClick = onCreate
                        ) {
                            MarqueeText(text = stringResource(R.string.control_manage_create_new))
                        }
                        Button(
                            modifier = Modifier.weight(1f, fill = false),
                            onClick = onClose
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
private fun StyleItem(
    style: ObservableButtonStyle,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onClone: () -> Unit,
    onDelete: () -> Unit
) {
    InfoLayoutItem(
        modifier = modifier,
        onClick = onClick
    ) {
        RendererStyleBox(
            modifier = Modifier.size(50.dp),
            style = style,
            text = "abc",
            isPressed = false,
            isDark = isSystemInDarkTheme()
        )
        Spacer(modifier = Modifier.width(8.dp))
        MarqueeText(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .weight(1f),
            text = style.name.takeIf { it.isNotEmptyOrBlank() } ?: stringResource(R.string.generic_unspecified),
            style = MaterialTheme.typography.bodyMedium
        )
        IconButton(
            onClick = onClone
        ) {
            Icon(
                imageVector = Icons.Outlined.CopyAll,
                contentDescription = stringResource(R.string.generic_copy)
            )
        }
        IconButton(
            onClick = onDelete
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.generic_delete)
            )
        }
        Icon(
            modifier = Modifier
                .size(28.dp),
            imageVector = Icons.AutoMirrored.Rounded.ArrowRight,
            contentDescription = null
        )
    }
}