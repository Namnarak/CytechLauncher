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

package com.movtery.zalithlauncher.game.download.modpack.install

import com.google.gson.JsonParseException
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.fixedFileUrl
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.getPlatformClassesOrNull
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.getSHA1
import com.movtery.zalithlauncher.game.download.assets.platform.getProjectFromCurseForge
import com.movtery.zalithlauncher.game.download.assets.platform.getVersionFromCurseForge
import com.movtery.zalithlauncher.game.download.modpack.platform.curseforge.CurseForgeManifest
import com.movtery.zalithlauncher.game.download.modpack.platform.modrinth.ModrinthManifest
import com.movtery.zalithlauncher.game.download.modpack.platform.modrinth.getGameVersion
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.file.extractFromZip
import com.movtery.zalithlauncher.utils.file.readText
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.zip.ZipFile

/**
 * 根据平台，分别处理不同的整合包
 */
suspend fun parserModPack(
    file: File,
    platform: Platform,
    targetFolder: File,
    task: Task
) = withContext(Dispatchers.IO) {
    when (platform) {
        Platform.CURSEFORGE -> {
            curseforge(
                file = file,
                targetFolder = targetFolder,
                task = task
            )
        }
        Platform.MODRINTH -> {
            modrinth(
                file = file,
                targetFolder = targetFolder,
                task = task
            )
        }
    }
}

private suspend fun curseforge(
    file: File,
    targetFolder: File,
    task: Task
) = withContext(Dispatchers.IO) {
    ZipFile(file).use { zip ->
        task.updateProgress(-1f)

        val manifestString = zip.readText("manifest.json")
        val manifest = GSON.fromJson(manifestString, CurseForgeManifest::class.java)

        val modsFolder = File(targetFolder, VersionFolders.MOD.folderName)

        //获取全部需要下载的模组文件
        val totalCount = manifest.files.size
        val files = manifest.files.mapIndexed { index, manifestFile ->
            val modFile = if (manifestFile.fileName.isNullOrBlank() || manifestFile.getFileUrl() == null) {
                ModFile(
                    getFile = {
                        runCatching {
                            val version = getVersionFromCurseForge(
                                projectID = manifestFile.projectID.toString(),
                                fileID = manifestFile.fileID.toString()
                            ).data
                            val url = version.fixedFileUrl() ?: throw IOException("Can't get the file url")
                            val fileName = version.fileName ?: throw IOException("Can't get the file name")

                            //获取项目
                            val project = getProjectFromCurseForge(
                                projectID = manifestFile.projectID.toString()
                            ).data
                            //通过项目类型指定目标下载目录
                            val folder = project.getPlatformClassesOrNull()
                                ?.versionFolder?.folderName
                                ?.let { folderName ->
                                    File(targetFolder, folderName)
                                } ?: modsFolder

                            ModFile(
                                outputFile = File(folder, fileName),
                                downloadUrls = listOf(url),
                                sha1 = version.getSHA1()
                            )
                        }.onFailure { e ->
                            when (e) {
                                is FileNotFoundException -> lWarning("Could not query api.curseforge.com for deleted mods: ${manifestFile.projectID}, ${manifestFile.fileID}", e)
                                is IOException, is JsonParseException -> lWarning("Unable to fetch the file name projectID=${manifestFile.projectID}, fileID=${manifestFile.fileID}", e)
                                else -> lWarning("Unable to fetch the file name projectID=${manifestFile.projectID}, fileID=${manifestFile.fileID}", e)
                            }
                        }.getOrNull()
                    }
                )
            } else {
                ModFile(
                    outputFile = File(modsFolder, manifestFile.fileName),
                    downloadUrls = listOf(manifestFile.getFileUrl()!!)
                )
            }
            task.updateProgress(
                percentage = index.toFloat() / totalCount.toFloat(),
                message =  R.string.download_modpack_install_get_mod_url,
                index, totalCount
            )
            modFile
        }

        //获取模组加载器信息
        val loaders = manifest.minecraft.modLoaders.mapNotNull { modloader ->
            val id = modloader.id
            if (id.startsWith("forge-")) ModLoader.FORGE to id.removePrefix("forge-")
            else if (id.startsWith("fabric-")) ModLoader.FABRIC to id.removePrefix("fabric-")
            else if (id.startsWith("neoforge-")) ModLoader.NEOFORGE to id.removePrefix("neoforge-")
            else null
        }

        //解压覆盖包到目标目录
        task.updateProgress(-1f, R.string.download_modpack_install_overrides)
        zip.extractFromZip(manifest.overrides ?: "overrides", targetFolder)

        ModPackInfo(
            name = manifest.name,
            files = files,
            loaders = loaders,
            gameVersion = manifest.minecraft.gameVersion
        )
    }
}

private suspend fun modrinth(
    file: File,
    targetFolder: File,
    task: Task
) = withContext(Dispatchers.IO) {
    ZipFile(file).use { zip ->
        task.updateProgress(-1f)

        val indexString = zip.readText("modrinth.index.json")
        val manifest = GSON.fromJson(indexString, ModrinthManifest::class.java)

        //获取所有需要下载的模组文件
        val files = manifest.files.mapNotNull { manifestFile ->
            //客户端不支持
            if (manifestFile.env?.client == "unsupported") return@mapNotNull null
            ModFile(
                outputFile = File(targetFolder, manifestFile.path),
                downloadUrls = manifestFile.downloads.toList(),
                sha1 = manifestFile.hashes.sha1
            )
        }

        //获取加载器信息
        val loaders = manifest.dependencies.entries.mapNotNull { (id, version) ->
            when (id) {
                "forge" -> ModLoader.FORGE to version
                "neoforge" -> ModLoader.NEOFORGE to version
                "fabric-loader" -> ModLoader.FABRIC to version
                "quilt-loader" -> ModLoader.QUILT to version
                else -> null
            }
        }

        //解压覆盖包到目标目录
        task.updateProgress(-1f, R.string.download_modpack_install_overrides)
        zip.extractFromZip("overrides", targetFolder)

        ModPackInfo(
            name = manifest.name,
            summary = manifest.summary,
            files = files,
            loaders = loaders,
            gameVersion = manifest.getGameVersion()
        )
    }
}