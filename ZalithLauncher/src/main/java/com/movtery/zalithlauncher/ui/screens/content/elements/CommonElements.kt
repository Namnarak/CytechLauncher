package com.movtery.zalithlauncher.ui.screens.content.elements

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.copyLocalFile
import com.movtery.zalithlauncher.context.getFileName
import com.movtery.zalithlauncher.contract.ExtensionFilteredDocumentPicker
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.getMessageOrToString
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