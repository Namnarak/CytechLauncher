/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_translatable

import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.layer_controller.observable.ObservableLocalizedString
import com.movtery.layer_controller.observable.ObservableTranslatableString
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.utils.string.isEmptyOrBlank

/**
 * 编辑可翻译文本
 * @param onDismissRequest 由Dialog主动调用的关闭请求回调
 * @param onClose 由用户主动点击关闭按钮调用的关闭请求回调
 * @param title 对话框标题
 * @param take 限制输入文本的字数
 */
@Composable
fun EditTranslatableTextDialog(
    text: ObservableTranslatableString,
    onClose: () -> Unit,
    singleLine: Boolean = true,
    allowEmpty: Boolean = true,
    onDismissRequest: (() -> Unit)? = null,
    title: String? = null,
    dismissText: String? = null,
    closeText: String? = null,
    take: Int? = null
) {
    val blankError = stringResource(R.string.control_manage_create_new_field_blank)

    var fieldError by remember { mutableStateOf<String?>(null) }
    val isFieldError = remember(text.default) {
        fieldError = when {
            !allowEmpty && text.default.isEmptyOrBlank() -> blankError
            else -> null
        }
        fieldError != null
    }

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
                .fillMaxWidth(0.65f)
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
                        text = title ?: stringResource(R.string.control_editor_edit_text),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.size(4.dp))
                    val locale = LocalConfiguration.current.locales[0]
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.control_editor_edit_translatable_other_tip, locale.toLanguageTag()),
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center
                    )

                    val focusManager = LocalFocusManager.current

                    val scrollState = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .animateContentSize(),
                        state = scrollState,
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            //默认文本
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = text.default,
                                onValueChange = { text.default = take.take(it) },
                                label = {
                                    Text(stringResource(R.string.control_editor_edit_translatable_default))
                                },
                                isError = isFieldError,
                                supportingText = {
                                    fieldError?.let { Text(it) } ?: run {
                                        if (take != null) {
                                            Text(stringResource(R.string.generic_input_length, text.default.length, take))
                                        }
                                    }
                                },
                                singleLine = singleLine,
                                keyboardOptions = if (singleLine) {
                                    KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Done
                                    )
                                } else {
                                    KeyboardOptions.Default
                                },
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus(true)
                                    }
                                ),
                                shape = MaterialTheme.shapes.large
                            )
                        }

                        items(text.matchQueue) { string ->
                            LocalizedStringItem(
                                modifier = Modifier.fillMaxWidth(),
                                string = string,
                                onDelete = {
                                    text.deleteLocalizedString(string)
                                },
                                singleLine = singleLine,
                                allowEmpty = allowEmpty,
                                take = take
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            modifier = Modifier.weight(1f, fill = false),
                            onClick = {
                                text.addLocalizedString()
                            }
                        ) {
                            MarqueeText(text = stringResource(R.string.control_editor_edit_translatable_other_add))
                        }
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            onDismissRequest?.let { request ->
                                FilledTonalButton(
                                    modifier = Modifier.weight(1f, fill = false),
                                    onClick = request
                                ) {
                                    MarqueeText(text = dismissText ?: stringResource(R.string.generic_close))
                                }
                            }

                            Button(
                                modifier = Modifier.weight(1f, fill = false),
                                onClick = onClose
                            ) {
                                MarqueeText(text = closeText ?: stringResource(R.string.generic_close))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 可翻译项
 */
@Composable
private fun LocalizedStringItem(
    modifier: Modifier = Modifier,
    string: ObservableLocalizedString,
    onDelete: () -> Unit,
    singleLine: Boolean = true,
    allowEmpty: Boolean = true,
    take: Int? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SimpleEditBox(
                modifier = Modifier.weight(0.3f),
                value = string.languageTag,
                onValueChange = { tag ->
                    string.languageTag = tag
                },
                label = stringResource(R.string.control_editor_edit_translatable_other_tag),
                take = 8
            )
            SimpleEditBox(
                modifier = Modifier.weight(0.7f),
                value = string.value,
                onValueChange = { value ->
                    string.value = value
                },
                label = stringResource(R.string.control_editor_edit_translatable_other_value),
                singleLine = singleLine,
                allowEmpty = allowEmpty,
                take = take
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
    }
}

@Composable
private fun SimpleEditBox(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    allowEmpty: Boolean = true,
    take: Int? = null
) {
    val blankError = stringResource(R.string.control_manage_create_new_field_blank)

    var fieldError by remember { mutableStateOf<String?>(null) }
    val isFieldError = remember(value) {
        fieldError = when {
            !allowEmpty && value.isEmptyOrBlank() -> blankError
            else -> null
        }
        fieldError != null
    }

    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = { onValueChange(take.take(it)) },
        label = {
            Text(text = label)
        },
        isError = isFieldError,
        supportingText = {
            fieldError?.let { Text(it) } ?: run {
                if (take != null) {
                    Text(stringResource(R.string.generic_input_length, value.length, take))
                }
            }
        },
        singleLine = singleLine,
        shape = MaterialTheme.shapes.large
    )
}

private fun Int?.take(value: String) = this?.let { takes -> value.take(takes) } ?: value