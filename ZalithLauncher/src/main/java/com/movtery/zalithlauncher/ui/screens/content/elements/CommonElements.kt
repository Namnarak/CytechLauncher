package com.movtery.zalithlauncher.ui.screens.content.elements

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.copyLocalFile
import com.movtery.zalithlauncher.context.getFileName
import com.movtery.zalithlauncher.contract.ExtensionFilteredDocumentPicker
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskState
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.IOException

@Composable
fun CategoryIcon(iconRes: Int, textRes: Int, iconPadding: PaddingValues = PaddingValues()) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = stringResource(textRes),
        modifier = Modifier
            .size(24.dp)
            .padding(iconPadding)
    )
}

@Composable
fun CategoryIcon(image: ImageVector, textRes: Int) {
    Icon(
        imageVector = image,
        contentDescription = stringResource(textRes),
        modifier = Modifier.size(24.dp)
    )
}

data class CategoryItem(
    val key: NavKey,
    val icon: @Composable () -> Unit,
    val textRes: Int,
    val division: Boolean = false
)

@Composable
fun ImportFileButton(
    extension: String,
    targetDir: File,
    modifier: Modifier = Modifier,
    imageVector: ImageVector = Icons.Default.Add,
    text: String = stringResource(R.string.generic_import),
    allowMultiple: Boolean = true,
    errorTitle: String = stringResource(R.string.generic_error),
    errorMessage: String? = stringResource(R.string.error_import_file),
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit = {},
    onFileCopied: suspend (Task, File) -> Unit = { _, _ -> },
    onImported: () -> Unit = {}
) {
    val context = LocalContext.current

    ImportFileButton(
        modifier = modifier,
        extension = extension,
        imageVector = imageVector,
        text = text,
        allowMultiple = allowMultiple,
        progressUris = { uris ->
            TaskSystem.submitTask(
                Task.runTask(
                    dispatcher = Dispatchers.IO,
                    task = { task ->
                        task.updateProgress(-1f, null)
                        uris.forEach { uri ->
                            try {
                                val fileName = context.getFileName(uri) ?: throw IOException("Failed to get file name")
                                task.updateProgress(-1f, R.string.empty_holder, fileName)
                                val outputFile = File(targetDir, fileName)
                                context.copyLocalFile(uri, outputFile)
                                //成功复制，如调用者有额外操作，可使用回调运行
                                onFileCopied(task, outputFile)
                            } catch (e: Exception) {
                                val eString = e.getMessageOrToString()
                                val messageString = if (errorMessage != null) {
                                    errorMessage + "\n" + eString
                                } else {
                                    eString
                                }

                                submitError(
                                    ErrorViewModel.ThrowableMessage(
                                        title = errorTitle,
                                        message = messageString
                                    )
                                )
                            }
                        }
                        onImported()
                    }
                )
            )
        }
    )
}

@Composable
fun ImportFileButton(
    extension: String,
    progressUris: (uris: List<Uri>) -> Unit,
    modifier: Modifier = Modifier,
    imageVector: ImageVector = Icons.Default.Add,
    text: String = stringResource(R.string.generic_import),
    allowMultiple: Boolean = true
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ExtensionFilteredDocumentPicker(extension = extension, allowMultiple = allowMultiple)
    ) { uris ->
        uris.takeIf { it.isNotEmpty() }?.let { uris1 ->
            progressUris(uris1)
        }
    }

    IconTextButton(
        modifier = modifier,
        onClick = {
            launcher.launch("")
        },
        imageVector = imageVector,
        text = text
    )
}

@Composable
fun TitleTaskFlowDialog(
    title: String,
    tasks: List<TitledTask>,
    onCancel: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier.fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.padding(all = 6.dp),
                shape = MaterialTheme.shapes.extraLarge,
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.size(16.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        items(tasks) { task ->
                            InstallingTaskItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                title = task.title,
                                runningIcon = task.runningIcon,
                                task = task.task
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(16.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onCancel
                    ) {
                        MarqueeText(text = stringResource(R.string.generic_cancel))
                    }
                }
            }
        }
    }
}

@Composable
private fun InstallingTaskItem(
    modifier: Modifier = Modifier,
    title: String,
    runningIcon: ImageVector? = null,
    task: Task
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val icon = when (task.taskState) {
            TaskState.PREPARING -> Icons.Outlined.Schedule
            TaskState.RUNNING -> runningIcon ?: Icons.Outlined.Download
            TaskState.COMPLETED -> Icons.Outlined.Check
        }
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = null
        )

        Column(
            modifier = modifier
                .weight(1f)
                .animateContentSize(animationSpec = getAnimateTween())
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge
            )
            if (task.taskState == TaskState.RUNNING) {
                Spacer(modifier = Modifier.height(4.dp))
                task.currentMessageRes?.let { messageRes ->
                    val args = task.currentMessageArgs
                    Text(
                        text = if (args != null) {
                            stringResource(messageRes, *args)
                        } else {
                            stringResource(messageRes)
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                if (task.currentProgress < 0) { //负数则代表不确定
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = { task.currentProgress },
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(task.currentProgress * 100).toInt()}%",
                            modifier = Modifier.align(Alignment.CenterVertically),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}