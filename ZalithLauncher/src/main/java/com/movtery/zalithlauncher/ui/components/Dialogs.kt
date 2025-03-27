package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.movtery.zalithlauncher.R

/**
 * 展示警告对话框
 *
 * @param title 对话框标题
 * @param text 对话框内容
 * @param onConfirm 点击确认按钮的回调
 * @param onDismiss 点击取消或对话框外部的回调
 */
@Composable
fun SimpleAlertDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(text = text)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(R.string.generic_confirm))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(R.string.generic_cancel))
            }
        }
    )
}

/**
 * 展示警告对话框
 *
 * @param title 对话框标题
 * @param text 对话框内容
 * @param onDismiss 点击确认或对话框外部的回调
 */
@Composable
fun SimpleAlertDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(text = text)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(R.string.generic_confirm))
            }
        }
    )
}

@Composable
fun SimpleEditDialog(
    title: String,
    value: String,
    onValueChange: (newValue: String) -> Unit,
    label: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    onDismissRequest: () -> Unit = {},
    onConfirm: (value: String) -> Unit = {},
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(shape = MaterialTheme.shapes.extraLarge,) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.size(16.dp))
                TextField(
                    value = value,
                    onValueChange = { onValueChange(it) },
                    label = label,
                    isError = isError,
                    supportingText = supportingText
                )
                Spacer(modifier = Modifier.size(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onDismissRequest
                    ) {
                        Text(text = stringResource(R.string.generic_cancel))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onConfirm(value)
                        }
                    ) {
                        Text(text = stringResource(R.string.generic_confirm))
                    }
                }
            }
        }
    }
}

/**
 * 加载中Dialog，但它不会主动关闭
 */
@Composable
fun LoadingDialog(
    title: String,
    text: String? = null
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(text = title) },
        text = {
            Column {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                text?.let {
                    Text(text = it)
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

