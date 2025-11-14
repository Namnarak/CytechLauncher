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

package com.movtery.zalithlauncher.components.jre

import android.content.Context
import android.content.res.AssetManager
import com.movtery.zalithlauncher.ZLApplication
import com.movtery.zalithlauncher.components.AbstractUnpackTask
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.utils.device.Architecture
import com.movtery.zalithlauncher.utils.file.readString
import com.movtery.zalithlauncher.utils.logging.Logger.lError

class UnpackJreTask(
    private val context: Context,
    private val jre: Jre
) : AbstractUnpackTask() {
    private lateinit var assetManager: AssetManager
    private lateinit var launcherRuntimeVersion: String
    private var isCheckFailed: Boolean = false

    init {
        runCatching {
            assetManager = context.assets
            launcherRuntimeVersion = assetManager.open(jre.jrePath + "/version").readString()
        }.getOrElse {
            isCheckFailed = true
        }
    }

    fun isCheckFailed() = isCheckFailed

    override fun isNeedUnpack(): Boolean {
        if (isCheckFailed) return false

        return runCatching {
            val installedRuntimeVersion = RuntimesManager.loadInternalRuntimeVersion(jre.jreName)
            return launcherRuntimeVersion != installedRuntimeVersion
        }.onFailure { e ->
            lError("An exception occurred while detecting the Java Runtime.", e)
        }.getOrElse { false }
    }

    override suspend fun run() {
        runCatching {
            RuntimesManager.installRuntimeBinPack(
                universalFileInputStream = assetManager.open(jre.jrePath + "/universal.tar.xz"),
                platformBinsInputStream = assetManager.open(
                    jre.jrePath + "/bin-" + Architecture.archAsString(ZLApplication.DEVICE_ARCHITECTURE) + ".tar.xz"
                ),
                name = jre.jreName,
                binPackVersion = launcherRuntimeVersion,
                updateProgress = { textRes, textArgs ->
                    taskMessage = context.getString(textRes, *textArgs)
                }
            )
            RuntimesManager.postPrepare(jre.jreName)
        }.onFailure {
            lError("Internal JRE unpack failed", it)
        }.getOrThrow()
    }
}