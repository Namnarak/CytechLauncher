package com.movtery.zalithlauncher.game.addons.modloader.fabriclike.quilt

import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.FabricLikeVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.models.FabricLikeLoader

object QuiltVersions : FabricLikeVersions("https://meta.quiltmc.org/v3") {

    /**
     * 获取 Quilt 列表
     */
    suspend fun fetchQuiltLoaderList(mcVersion: String, force: Boolean = false): List<QuiltVersion>? {
        val list: List<FabricLikeLoader> = fetchLoaderList(force, "QuiltVersions", mcVersion) ?: return null

        return list.map { loader ->
            QuiltVersion(
                inherit = mcVersion,
                version = loader.version
            )
        }
    }
}