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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun EditTranslatableTextDialog(
    text: ObservableTranslatableString,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnClickOutside = false,
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
                        text = stringResource(R.string.control_editor_edit_text),
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

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .animateContentSize(),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            //默认文本
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = text.default,
                                onValueChange = { text.default = it },
                                label = {
                                    Text(stringResource(R.string.control_editor_edit_translatable_default))
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done
                                ),
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
                                }
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
                        Button(
                            modifier = Modifier.weight(1f, fill = false),
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

/**
 * 可翻译项
 */
@Composable
private fun LocalizedStringItem(
    modifier: Modifier = Modifier,
    string: ObservableLocalizedString,
    onDelete: () -> Unit
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
                label = stringResource(R.string.control_editor_edit_translatable_other_tag)
            )
            SimpleEditBox(
                modifier = Modifier.weight(0.7f),
                value = string.value,
                onValueChange = { value ->
                    string.value = value
                },
                label = stringResource(R.string.control_editor_edit_translatable_other_value)
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
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = { onValueChange(it) },
        label = {
            Text(text = label)
        },
        singleLine = true,
        shape = MaterialTheme.shapes.large
    )
}