package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
        onDismissRequest = {
            onDismiss()
        },
        title = { Text(text = title) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(text = text)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                }
            ) {
                Text(text = stringResource(R.string.generic_confirm))
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onDismiss()
                }
            ) {
                Text(text = stringResource(R.string.generic_cancel))
            }
        }
    )
}

