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

package com.movtery.zalithlauncher.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.path.GLOBAL_JSON
import com.movtery.zalithlauncher.path.URL_PROJECT_INFO
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.upgrade.UpgradeDialog
import com.movtery.zalithlauncher.ui.upgrade.UpgradeFilesDialog
import com.movtery.zalithlauncher.upgrade.GithubContentApi
import com.movtery.zalithlauncher.upgrade.RemoteData
import com.movtery.zalithlauncher.upgrade.TooFrequentOperationException
import com.movtery.zalithlauncher.utils.logging.Logger.lInfo
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import com.movtery.zalithlauncher.utils.network.safeBodyAsJson
import com.movtery.zalithlauncher.utils.network.withRetry
import com.movtery.zalithlauncher.utils.string.decodeBase64
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

sealed interface LauncherUpgradeOperation {
    data object None : LauncherUpgradeOperation
    /** 已检查到启动器存在新版本，展示更新信息 */
    data class Upgrade(val data: RemoteData) : LauncherUpgradeOperation
    /** 选择要安装的安装包文件 */
    data class SelectApk(val data: RemoteData) : LauncherUpgradeOperation
}

/**
 * 最新版本的信息获取源
 */
private const val LATEST_API_URL = "$URL_PROJECT_INFO/latest_version.json"

/**
 * 用于记录启动器更新 ViewModel
 */
class LauncherUpgradeViewModel: ViewModel() {
    var operation by mutableStateOf<LauncherUpgradeOperation>(LauncherUpgradeOperation.None)

    private val checkMutex = Mutex()

    private fun lastCheck(
        time: Long
    ): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - AllSettings.lastUpgradeCheck.getValue() < time) {
            return false
        }
        AllSettings.lastUpgradeCheck.save(currentTime)
        return true
    }

    /**
     * 在启动时，快速完成所有的检查，含更新检测限频
     */
    fun firstCheck() {
        viewModelScope.launch {
            if (!lastCheck(TimeUnit.HOURS.toMillis(1L))) return@launch

            val data = syncRemote()
            if (data != null) {
                checkUpgrade(
                    data = data,
                    currentVersionCode = BuildConfig.VERSION_CODE,
                    lastIgnored = AllSettings.lastIgnoredVersion.getValue(),
                    onUpgrade = { data ->
                        operation = LauncherUpgradeOperation.Upgrade(data)
                    }
                )
            }
        }
    }

    /**
     * 从远端获取最新的启动器信息
     */
    suspend fun checkRemote(): RemoteData? {
        return checkMutex.withLock {
            if (!lastCheck(TimeUnit.SECONDS.toMillis(5L))) {
                throw TooFrequentOperationException()
            }
            syncRemote()
        }
    }

    private suspend fun syncRemote(): RemoteData? {
        return withContext(Dispatchers.IO) {
            runCatching {
                withRetry(logTag = "LauncherUpgrade", maxRetries = 2) {
                    //获取最新的启动器信息
                    val api = GLOBAL_CLIENT.get(LATEST_API_URL).safeBodyAsJson<GithubContentApi>()
                    //需要Base64解密
                    val contentString = decodeBase64(api.content)
                    GLOBAL_JSON.decodeFromString(RemoteData.serializer(), contentString)
                }
            }.onFailure { e ->
                lWarning("Failed to check for launcher upgrade!", e)
                //如果检查失败了，就不管了，下次启动时继续检查
            }.getOrNull()
        }
    }

    /**
     * 检查启动器是否需要更新
     * @param currentVersionCode 当前软件的版本号，用于比较
     * @param lastIgnored 上次弹出更新弹窗时，用户所忽略的版本号，
     *                    如果为null，则表示不再忽略新的更新弹窗
     * @param onIsLatest 当前已是最新版本时
     */
    suspend fun checkUpgrade(
        data: RemoteData,
        currentVersionCode: Int,
        lastIgnored: Int? = null,
        onUpgrade: (RemoteData) -> Unit,
        onIsLatest: () -> Unit = {}
    ) {
        checkMutex.withLock {
            if (currentVersionCode < data.code) {
                //启动器为旧版本
                when {
                    lastIgnored == data.code -> {
                        //忽略这次更新
                        lInfo("Launcher update detected: $currentVersionCode -> ${data.code}, but ignored by user")
                    }
                    else -> {
                        //弹出更新弹窗
                        lInfo("Launcher update detected: $currentVersionCode -> ${data.code}, dialog shown to user")
                        onUpgrade(data)
                    }
                }
            } else {
                lInfo("Launcher is running the latest version: $currentVersionCode")
                onIsLatest()
            }
        }
    }
}

@Composable
fun LauncherUpgradeOperation(
    operation: LauncherUpgradeOperation,
    onChanged: (LauncherUpgradeOperation) -> Unit,
    onIgnoredClick: (code: Int) -> Unit,
    onLinkClick: (String) -> Unit
) {
    when (operation) {
        is LauncherUpgradeOperation.None -> {}
        is LauncherUpgradeOperation.Upgrade -> {
            UpgradeDialog(
                data = operation.data,
                onDismissRequest = {
                    onChanged(LauncherUpgradeOperation.None)
                },
                onFilesClick = {
                    onChanged(LauncherUpgradeOperation.SelectApk(operation.data))
                },
                onIgnored = {
                    onIgnoredClick(operation.data.code)
                },
                onLinkClick = onLinkClick
            )
        }
        is LauncherUpgradeOperation.SelectApk -> {
            UpgradeFilesDialog(
                data = operation.data,
                onDismissRequest = {
                    onChanged(LauncherUpgradeOperation.None)
                },
                onFileSelected = { file ->
                    onLinkClick(file.uri)
                    onChanged(LauncherUpgradeOperation.None)
                }
            )
        }
    }
}