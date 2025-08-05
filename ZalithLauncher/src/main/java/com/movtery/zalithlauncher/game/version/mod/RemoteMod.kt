package com.movtery.zalithlauncher.game.version.mod

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeFile
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.getProjectByVersion
import com.movtery.zalithlauncher.game.download.assets.platform.getVersionByLocalFile
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthModLoaderCategory
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthVersion
import com.movtery.zalithlauncher.game.download.assets.utils.ModTranslations
import com.movtery.zalithlauncher.game.download.assets.utils.getMcMod
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadProjectInfo
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.toInfo
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
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
     * 远端设置的加载器列表
     */
    var remoteLoaders = mutableStateListOf<PlatformDisplayLabel>()

    /**
     * 项目信息
     */
    var projectInfo: DownloadProjectInfo? by mutableStateOf(null)
        private set

    /**
     * 项目翻译信息
     */
    var mcMod: ModTranslations.McMod? by mutableStateOf(null)
        private set

    suspend fun load() {
        if (projectInfo != null) return

        isLoading = true
        try {
            withContext(Dispatchers.IO) {
                runCatching {
                    val version = getVersionByLocalFile(localMod.file)
                    ensureActive()

                    version?.let { ver ->
                        updateLoaders(ver)
                        getProjectByVersion(ver).let { project ->
                            mcMod = project.getMcMod(PlatformClasses.MOD)
                            project.toInfo(PlatformClasses.MOD)
                        }
                    }
                }.onSuccess { info ->
                    projectInfo = info
                }.onFailure { e ->
                    lWarning("Failed to load project info for mod: ${localMod.file.name}", e)
                }
            }
        } finally {
            isLoading = false
        }
    }

    private fun updateLoaders(
        version: PlatformVersion
    ) {
        remoteLoaders.clear()
        when (version) {
            is ModrinthVersion -> {
                remoteLoaders.addAll(
                    version.loaders.mapNotNull { loaderName ->
                        ModrinthModLoaderCategory.entries.find { it.facetValue() == loaderName }
                    }
                )
            }
            is CurseForgeFile -> {
                remoteLoaders.addAll(
                    version.gameVersions.mapNotNull { loaderName ->
                        CurseForgeModLoader.entries.find {
                            it.getDisplayName().equals(loaderName, true)
                        }
                    }
                )
            }
        }
    }
}