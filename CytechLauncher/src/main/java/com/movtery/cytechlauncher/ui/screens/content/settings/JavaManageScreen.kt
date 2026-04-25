/*
 * Cytech Launcher
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

package com.movtery.cytechlauncher.ui.screens.content.settings

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.cytechlauncher.R
import com.movtery.cytechlauncher.CytechApplication
import com.movtery.cytechlauncher.context.getFileName
import com.movtery.cytechlauncher.coroutine.Task
import com.movtery.cytechlauncher.coroutine.TaskSystem
import com.movtery.cytechlauncher.game.launch.executeJarWithUri
import com.movtery.cytechlauncher.game.multirt.Runtime
import com.movtery.cytechlauncher.game.multirt.RuntimesManager
import com.movtery.cytechlauncher.path.PathManager
import com.movtery.cytechlauncher.setting.AllSettings
import com.movtery.cytechlauncher.ui.base.BaseScreen
import com.movtery.cytechlauncher.ui.components.CardTitleLayout
import com.movtery.cytechlauncher.ui.components.IconTextButton
import com.movtery.cytechlauncher.ui.components.SimpleAlertDialog
import com.movtery.cytechlauncher.ui.screens.NestedNavKey
import com.movtery.cytechlauncher.ui.screens.NormalNavKey
import com.movtery.cytechlauncher.ui.screens.TitledNavKey
import com.movtery.cytechlauncher.ui.screens.content.elements.ImportMultipleFileButton
import com.movtery.cytechlauncher.ui.screens.content.elements.ImportSingleFileButton
import com.movtery.cytechlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.cytechlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.cytechlauncher.ui.theme.itemColor
import com.movtery.cytechlauncher.ui.theme.onItemColor
import com.movtery.cytechlauncher.utils.animation.getAnimateTween
import com.movtery.cytechlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.cytechlauncher.utils.device.Architecture
import com.movtery.cytechlauncher.utils.file.checkExtensionOrThrow
import com.movtery.cytechlauncher.utils.string.getMessageOrToString
import com.movtery.cytechlauncher.utils.string.throwableToString
import com.movtery.cytechlauncher.viewmodel.ErrorViewModel
import kotlinx.coroutines.Dispatchers

private sealed interface RuntimeOperation {
    data object None: RuntimeOperation
    data class PreDelete(val runtime: Runtime): RuntimeOperation
    data class Delete(val runtime: Runtime): RuntimeOperation
}

@Composable
fun JavaManageScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val context = LocalContext.current

    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.JavaManager, settingsScreenKey, false)
    ) { isVisible ->
        val yOffset by swapAnimateDpAsState(
            targetValue = (-40).dp,
            swapIn = isVisible
        )

        var runtimes by remember { mutableStateOf(getRuntimes()) }
        var runtimeOperation by remember { mutableStateOf<RuntimeOperation>(RuntimeOperation.None) }
        RuntimeOperation(
            runtimeOperation = runtimeOperation,
            updateOperation = { runtimeOperation = it },
            callRefresh = { runtimes = getRuntimes(true) },
            submitError = submitError
        )

        SettingsCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 12.dp)
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
            position = CardPosition.Single
        ) {
            CardTitleLayout {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconTextButton(
                        onClick = { runtimes = getRuntimes(true) },
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = stringResource(R.string.generic_refresh),
                        text = stringResource(R.string.generic_refresh),
                    )
                    
                    var showDownloadDialog by remember { mutableStateOf(false) }
                    IconTextButton(
                        onClick = { showDownloadDialog = true },
                        imageVector = Icons.Outlined.Download,
                        contentDescription = "Download Java",
                        text = "Download",
                    )
                    
                    if (showDownloadDialog) {
                        SimpleAlertDialog(
                            title = "Download Java",
                            text = "Select a Java version to download and install.",
                            onConfirm = { /* จะให้เลือก version ในลำดับถัดไป */ },
                            onDismiss = { showDownloadDialog = false }
                        )
                    }
                    ImportMultipleFileButton(
                        extension = "xz",
                        progressUris = { uris ->
                            uris.forEach { uri ->
                                progressRuntimeUri(
                                    context = context,
                                    uri = uri,
                                    callRefresh = { runtimes = getRuntimes(true) },
                                    submitError = submitError
                                )
                            }
                        }
                    )
                    ImportSingleFileButton(
                        extension = "jar",
                        progressUris = { uris ->
                            uris[0].let { uri ->
                                RuntimesManager.getExactJreName(8) ?: run {
                                    Toast.makeText(context, R.string.multirt_no_java_8, Toast.LENGTH_LONG).show()
                                    return@ImportSingleFileButton
                                }
                                (context as? Activity)?.let { activity ->
                                    val jreName = AllSettings.javaRuntime.takeIf { AllSettings.autoPickJavaRuntime.getValue() }?.getValue()
                                    executeJarWithUri(activity, uri, jreName)
                                }
                            }
                        },
                        imageVector = Icons.Default.Terminal,
                        text = stringResource(R.string.execute_jar_title)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                items(runtimes) { runtime ->
                    JavaRuntimeItem(
                        runtime = runtime,
                        modifier = Modifier
                            .padding(vertical = 6.dp),
                        onDeleteClick = {
                            runtimeOperation = RuntimeOperation.PreDelete(runtime)
                        }
                    )
                }
            }
        }
    }
}

private fun getRuntimes(forceLoad: Boolean = false): List<Runtime> =
    RuntimesManager.getRuntimes(forceLoad = forceLoad)

@Composable
private fun RuntimeOperation(
    runtimeOperation: RuntimeOperation,
    updateOperation: (RuntimeOperation) -> Unit,
    callRefresh: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    when(runtimeOperation) {
        is RuntimeOperation.None -> {}
        is RuntimeOperation.PreDelete -> {
            val runtime = runtimeOperation.runtime
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.multirt_runtime_delete_message, runtime.name),
                onConfirm = { updateOperation(RuntimeOperation.Delete(runtime)) },
                onDismiss = { updateOperation(RuntimeOperation.None) }
            )
        }
        is RuntimeOperation.Delete -> {
            val failedMessage = stringResource(R.string.multirt_runtime_delete_failed)
            val runtime = runtimeOperation.runtime
            TaskSystem.submitTask(
                Task.runTask(
                    id = runtime.name,
                    dispatcher = Dispatchers.IO,
                    task = { task ->
                        task.updateMessage(R.string.multirt_runtime_deleting, runtime.name)
                        RuntimesManager.removeRuntime(runtime.name)
                    },
                    onError = {
                        submitError(
                            ErrorViewModel.ThrowableMessage(
                                title = failedMessage,
                                message = it.getMessageOrToString()
                            )
                        )
                    },
                    onFinally = callRefresh
                )
            )
            updateOperation(RuntimeOperation.None)
        }
    }
}

private fun progressRuntimeUri(
    context: Context,
    uri: Uri,
    callRefresh: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    fun showError(
        title: String = context.getString(R.string.multirt_runtime_import_failed),
        message: String
    ) {
        submitError(
            ErrorViewModel.ThrowableMessage(
                title = title,
                message = message
            )
        )
    }

    val name = context.getFileName(uri) ?: run {
        showError(message = context.getString(R.string.multirt_runtime_import_failed_file_name))
        return
    }
    TaskSystem.submitTask(
        Task.runTask(
            id = name,
            dispatcher = Dispatchers.IO,
            task = { task ->
                name.checkExtensionOrThrow(listOf("xz"))

                val inputStream = context.contentResolver.openInputStream(uri) ?: run {
                    showError(message = context.getString(R.string.multirt_runtime_import_failed_input_stream))
                    return@runTask
                }
                RuntimesManager.installRuntime(
                    nativeLibDir = PathManager.DIR_NATIVE_LIB,
                    inputStream = inputStream,
                    name = name,
                    updateProgress = { textRes, textArg ->
                        task.updateMessage(textRes, *textArg)
                    }
                )
            },
            onError = {
                showError(message = throwableToString(it))
            },
            onFinally = callRefresh,
            onCancel = {
                runCatching {
                    RuntimesManager.removeRuntime(name)
                    callRefresh()
                }.onFailure { t ->
                    showError(
                        title = context.getString(R.string.multirt_runtime_delete_failed),
                        message = t.getMessageOrToString()
                    )
                }
            }
        )
    )
}

@Composable
private fun JavaRuntimeItem(
    runtime: Runtime,
    modifier: Modifier = Modifier,
    color: Color = itemColor(),
    contentColor: Color = onItemColor(),
    onClick: () -> Unit = {},
    onDeleteClick: () -> Unit
) {
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }
    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = runtime.name,
                    style = MaterialTheme.typography.titleSmall
                )
                //环境标签
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (runtime.isProvidedByLauncher) {
                        Text(
                            text = stringResource(R.string.multirt_runtime_provided_by_launcher),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text =  runtime.versionString?.let {
                            stringResource(R.string.multirt_runtime_version_name, it)
                        } ?: stringResource(R.string.multirt_runtime_corrupt),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (runtime.versionString != null) contentColor else MaterialTheme.colorScheme.error
                    )
                    runtime.javaVersion.takeIf { it != 0 }?.let { javaVersion ->
                        Text(
                            text = stringResource(R.string.multirt_runtime_version_code, javaVersion),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    runtime.arch?.let { arch ->
                        val compatible = CytechApplication.DEVICE_ARCHITECTURE == Architecture.archAsInt(arch)
                        Text(
                            text = arch.takeIf {
                                compatible
                            }?.let {
                                stringResource(R.string.multirt_runtime_version_arch, it)
                            } ?: stringResource(R.string.multirt_runtime_incompatible_arch, arch),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (compatible) contentColor else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            IconButton(
                //内置环境（未损坏）无法删除
                enabled = !runtime.isProvidedByLauncher || !runtime.isCompatible(),
                onClick = onDeleteClick
            ) {
                Icon(
                    modifier = Modifier.padding(all = 8.dp),
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.generic_delete)
                )
            }
        }
    }
}