package com.movtery.zalithlauncher.game.versioninfo

import android.util.Log
import com.movtery.zalithlauncher.game.versioninfo.models.VersionManifest
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.path.UrlManager
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.network.NetWorkUtils
import com.movtery.zalithlauncher.utils.network.withRetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils

object MinecraftVersions {
    private var manifest: VersionManifest? = null

    /**
     * 获取Minecraft版本信息列表
     */
    suspend fun getVersionManifest(): VersionManifest {
        manifest?.let { return it }

        val localVersions = PathManager.FILE_MINECRAFT_VERSIONS

        manifest = runCatching {
            if (!localVersions.exists() || !localVersions.isFile ||
                //一天更新一次版本信息列表
                localVersions.lastModified() + 1000 * 60 * 60 * 24 < System.currentTimeMillis()
            ) {
                downloadVersionManifest()
            } else {
                runCatching {
                    GSON.fromJson(localVersions.readText(), VersionManifest::class.java)
                }.getOrElse { e ->
                    Log.w("MinecraftVersions", "Failed to parse version manifest, will redownload", e)
                    //读取失败则删除当前的版本信息文件
                    FileUtils.deleteQuietly(localVersions)
                    null
                }
            }
        }.getOrElse {
            Log.e("MinecraftVersions", "Failed to get version manifest", it)
            throw it
        }

        return manifest ?: downloadVersionManifest()
    }

    /**
     * 从官方版本仓库获取版本信息
     */
    private suspend fun downloadVersionManifest(): VersionManifest {
        return withContext(Dispatchers.IO) {
            withRetry("MinecraftVersions", maxRetries = 1) {
                Log.d("MinecraftVersions", "Downloading version manifest")
                val rawJson = NetWorkUtils.fetchStringFromUrl(UrlManager.URL_MINECRAFT_VERSION_REPOS)
                val versionManifest = GSON.fromJson(rawJson, VersionManifest::class.java)
                PathManager.FILE_MINECRAFT_VERSIONS.writeText(rawJson)
                versionManifest
            }
        }
    }
}