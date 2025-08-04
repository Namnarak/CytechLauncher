package com.movtery.zalithlauncher.game.version.mod

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.getProjectByVersion
import com.movtery.zalithlauncher.game.download.assets.platform.getVersionByLocalFile
import com.movtery.zalithlauncher.game.download.assets.utils.ModTranslations
import com.movtery.zalithlauncher.game.download.assets.utils.getMcMod
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadProjectInfo
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.toInfo
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning

class RemoteMod(
    val localMod: LocalMod
) {
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

    suspend fun search() {
        if (projectInfo == null) {
            val version = getVersionByLocalFile(localMod.file)
            projectInfo = if (version != null) {
                runCatching {
                    val project = getProjectByVersion(version)
                    mcMod = project.getMcMod(PlatformClasses.MOD)
                    project.toInfo(PlatformClasses.MOD)
                }.onFailure {
                    lWarning("Failed to get project! mod = ${localMod.file.name}", it)
                }.getOrNull()
            } else {
                null
            }
        }
    }
}