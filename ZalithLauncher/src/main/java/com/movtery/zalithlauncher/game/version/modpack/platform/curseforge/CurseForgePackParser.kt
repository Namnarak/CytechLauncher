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

package com.movtery.zalithlauncher.game.version.modpack.platform.curseforge

import com.movtery.zalithlauncher.game.download.modpack.platform.curseforge.CurseForgeManifest
import com.movtery.zalithlauncher.game.version.modpack.platform.AbstractPack
import com.movtery.zalithlauncher.game.version.modpack.platform.PackParser
import com.movtery.zalithlauncher.utils.GSON
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InterruptedIOException
import java.util.concurrent.CancellationException

/**
 * Modrinth 整合包解析器，用于尝试以 CurseForge 的格式解析整合包
 */
object CurseForgePackParser : PackParser {
    /**
     * 尝试解析为 CurseForge 平台的整合包
     */
    override suspend fun parse(packFolder: File): AbstractPack? {
        //CurseForge 整合包清单文件
        val manifestFile = File(packFolder, "manifest.json")
        return withContext(Dispatchers.IO) {
            if (!manifestFile.exists()) return@withContext null

            try {
                //尝试读取并识别，如果识别成功，则判断其为 CurseForge 整合包
                val rawString = manifestFile.readText()
                val manifest = GSON.fromJson(rawString, CurseForgeManifest::class.java)

                return@withContext CurseForgePack(manifest = manifest)
            } catch (th: Throwable) {
                if (th is CancellationException || th is InterruptedIOException) return@withContext null
                throw th
            }
        }
    }
}