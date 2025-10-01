package com.movtery.zalithlauncher.game.version.mod

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeFile
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.getProjectByVersion
import com.movtery.zalithlauncher.game.download.assets.platform.getVersionByLocalFile
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthModLoaderCategory
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthVersion
import com.movtery.zalithlauncher.game.download.assets.utils.ModTranslations
import com.movtery.zalithlauncher.game.download.assets.utils.getMcMod
import com.movtery.zalithlauncher.game.download.assets.utils.getTranslations
import com.movtery.zalithlauncher.utils.file.calculateFileSha1
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

class RemoteMod(
    val localMod: LocalMod
) {
    /**
     * 是否正在加载项目信息
     */
    var isLoading by mutableStateOf(false)
        private set

    /**
     * 平台对应的文件
     */
    var remoteFile: ModFile? by mutableStateOf(null)
        private set

    /**
     * 项目信息
     */
    var projectInfo: ModProject? by mutableStateOf(null)
        private set

    /**
     * 项目翻译信息
     */
    var mcMod: ModTranslations.McMod? by mutableStateOf(null)
        private set

    /**
     * 是否已经加载过
     */
    var isLoaded: Boolean = false
        private set

    /**
     * @param loadFromCache 是否从缓存中加载
     */
    suspend fun load(loadFromCache: Boolean) {
        if (loadFromCache && isLoaded) return

        if (!loadFromCache) {
            remoteFile = null
            projectInfo = null
            mcMod = null
        }

        isLoaded = false
        isLoading = true

        try {
            withContext(Dispatchers.IO) {
                val file = localMod.file
                val modProjectCache = modProjectCache()
                val modFileCache = modFileCache()

                runCatching {
                    //获取文件 sha1，作为缓存的键
                    val sha1 = calculateFileSha1(file)

                    //从缓存加载项目信息
                    val cachedProject = if (loadFromCache) {
                        modProjectCache.decodeParcelable(sha1, ModProject::class.java)
                    } else null

                    //从缓存加载文件信息
                    val cachedFile = if (loadFromCache) {
                        modFileCache.decodeParcelable(sha1, ModFile::class.java)
                    } else null

                    if (loadFromCache && cachedFile != null) {
                        remoteFile = cachedFile
                    } else {
                        getVersionByLocalFile(file, sha1)?.let { version ->
                            updateRemoteFile(version)
                            remoteFile?.let { modFile ->
                                modFileCache.encode(sha1, modFile, MMKV.ExpireInDay)
                            }
                        }
                    }

                    if (loadFromCache && cachedProject != null) {
                        projectInfo = cachedProject
                        mcMod = PlatformClasses.MOD.getTranslations().getModBySlugId(cachedProject.slug)
                    } else {
                        ensureActive()
                        remoteFile?.let { modFile ->
                            val project = getProjectByVersion(modFile.projectId, modFile.platform)
                            val newProjectInfo = ModProject(
                                id = project.platformId(),
                                platform = project.platform(),
                                iconUrl = project.platformIconUrl(),
                                title = project.platformTitle(),
                                slug = project.platformSlug()
                            )

                            projectInfo = newProjectInfo
                            mcMod = project.getMcMod(PlatformClasses.MOD)

                            modProjectCache.encode(sha1, newProjectInfo, MMKV.ExpireInDay)
                        }
                    }

                    isLoaded = true
                }.onFailure { e ->
                    if (e is CancellationException) return@onFailure
                    lWarning("Failed to load project info for mod: ${file.name}", e)
                }
            }
        } finally {
            isLoading = false
        }
    }

    private fun updateRemoteFile(
        version: PlatformVersion
    ) {
        remoteFile = when (version) {
            is ModrinthVersion -> {
                ModFile(
                    id = version.id,
                    projectId = version.projectId,
                    platform = Platform.MODRINTH,
                    loaders = version.loaders.mapNotNull { loaderName ->
                        ModrinthModLoaderCategory.entries.find { it.facetValue() == loaderName }
                    }.toTypedArray(),
                    datePublished = version.datePublished
                )
            }
            is CurseForgeFile -> {
                ModFile(
                    id = version.id.toString(),
                    projectId = version.modId.toString(),
                    platform = Platform.CURSEFORGE,
                    loaders = version.gameVersions.mapNotNull { loaderName ->
                        CurseForgeModLoader.entries.find {
                            it.getDisplayName().equals(loaderName, true)
                        }
                    }.toTypedArray(),
                    datePublished = version.fileDate
                )
            }
            else -> error("Unknown version type: $version")
        }
    }
}