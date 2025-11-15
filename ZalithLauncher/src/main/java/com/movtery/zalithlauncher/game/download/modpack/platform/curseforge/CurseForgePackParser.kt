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

package com.movtery.zalithlauncher.game.download.modpack.platform.curseforge

import com.movtery.zalithlauncher.game.download.modpack.platform.PackPlatform
import com.movtery.zalithlauncher.game.download.modpack.platform.SimplePackParser
import com.movtery.zalithlauncher.game.download.modpack.platform.multimc.MultiMCManifest
import com.movtery.zalithlauncher.game.download.modpack.platform.multimc.MultiMCPackParser
import com.movtery.zalithlauncher.utils.GSON
import java.io.File

/**
 * CurseForge 整合包解析器，用于尝试以 CurseForge 的格式解析整合包
 */
object CurseForgePackParser : SimplePackParser<CurseForgeManifest>(
    indexFilePath = "manifest.json",
    manifestClass = CurseForgeManifest::class.java,
    extraProcess = extraProcess@{ root ->
        //排除 MultiMC 整合包误判
        val manifestFile = File(root, MultiMCPackParser.indexFilePath)
        if (manifestFile.exists()) {
            try {
                GSON.fromJson(manifestFile.readText(), MultiMCManifest::class.java)
                //成功识别为 MultiMC 整合包，则说明是误判为 CurseForge 整合包
                false
            } catch (_: Throwable) {
                return@extraProcess true
            }
        } else {
            true
        }
    },
    buildPack = { root, manifest ->
        CurseForgePack(root = root, manifest = manifest)
    }
) {
    override fun getIdentifier(): String = PackPlatform.CurseForge.identifier
}