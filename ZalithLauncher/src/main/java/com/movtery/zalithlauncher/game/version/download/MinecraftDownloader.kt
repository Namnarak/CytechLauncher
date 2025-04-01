package com.movtery.zalithlauncher.game.version.download

import android.util.Log
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersions
import com.movtery.zalithlauncher.game.versioninfo.models.AssetIndexJson
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import com.movtery.zalithlauncher.game.versioninfo.models.VersionManifest.Version
import com.movtery.zalithlauncher.utils.compareSHA1
import kotlinx.coroutines.Dispatchers
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.atomic.AtomicLong

class MinecraftDownloader(
    private val minecraftPath: File,
    private val version: String,
    private val verifyIntegrity: Boolean,
    private val onCompletion: () -> Unit
) {
    companion object {
        private const val MINECRAFT_RES: String = "https://resources.download.minecraft.net/"
    }
    //Dir
    private val assetsTarget = File("$minecraftPath/assets".replace("/", File.separator)).createPath()
    private val resourcesTarget = File("$minecraftPath/resources".replace("/", File.separator)).createPath()
    private val versionsTarget = File("$minecraftPath/versions".replace("/", File.separator)).createPath()
    private val librariesTarget = File("$minecraftPath/libraries".replace("/", File.separator)).createPath()
    private val assetIndexTarget = File(assetsTarget, "indexes").createPath()
    private val gameVersionsTarget = File(versionsTarget, version).createPath()
    //File
    private val versionJsonTarget = File(gameVersionsTarget, "$version.json".replace("/", File.separator))
    private val versionJarTarget = File(gameVersionsTarget, "$version.jar".replace("/", File.separator))

    //已下载文件计数器
    private var downloadedFileCount: AtomicLong = AtomicLong(0)
    private var totalFileCount: AtomicLong = AtomicLong(0)

    private var allDownloadTasks = mutableListOf<DownloadTask>()

    private fun File.createPath(): File = this.apply { if (!(exists() && isDirectory)) mkdirs() }

    fun getDownloadTask(): Task {
        return Task.runTask(
            dispatcher = Dispatchers.Default,
            task = { task ->
                if (!versionsTarget.exists()) versionsTarget.mkdirs()
                if (!gameVersionsTarget.exists()) gameVersionsTarget.mkdirs()

                task.updateProgress(-1f, R.string.minecraft_getting_version_list)

                val versionManifest = MinecraftVersions.getVersionManifest()
                val selectedVersion = versionManifest.versions.find { it.id == version } ?: throw IllegalArgumentException("Version not found")

                task.updateProgress(-1f, R.string.minecraft_download_progress_version_json)
                val gameManifest = createVersionJson(selectedVersion)
                val assetsIndex = createAssetIndex(gameManifest)

                task.updateProgress(-1f, R.string.minecraft_download_task_analysis)
                scheduleClientJarDownload(gameManifest)
                scheduleAssetDownloads(assetsIndex)
                scheduleLibraryDownloads(gameManifest)

                //TODO 下载

                onCompletion()
            },
            onError = {
                it.printStackTrace()
            }
        )
    }

    /**
     * 创建版本 Json
     */
    private suspend fun createVersionJson(version: Version): GameManifest {
        return downloadAndParseJson(
            targetFile = versionJsonTarget,
            url = version.url,
            expectedSHA = version.sha1,
            verifyIntegrity = verifyIntegrity,
            classOfT = GameManifest::class.java,
            logTag = "MinecraftDownloader.createVersionJson"
        )
    }

    /**
     * 创建 assets 索引 Json
     */
    private suspend fun createAssetIndex(gameManifest: GameManifest): AssetIndexJson {
        val indexFile = File(assetIndexTarget, "${gameManifest.assets}.json")
        return downloadAndParseJson(
            targetFile = indexFile,
            url = gameManifest.assetIndex.url,
            expectedSHA = gameManifest.assetIndex.sha1,
            verifyIntegrity = verifyIntegrity,
            classOfT = AssetIndexJson::class.java,
            logTag = "MinecraftDownloader.createAssetIndex"
        )
    }

    /** 计划客户端jar下载 */
    private fun scheduleClientJarDownload(gameManifest: GameManifest) {
        val client = gameManifest.downloads.client
        verifyScheduleDownload("MinecraftDownloader.scheduleClientJarDownload", client.url, client.sha1, versionJarTarget)
    }

    /** 计划assets资产下载 */
    private fun scheduleAssetDownloads(assetIndex: AssetIndexJson) {
        assetIndex.objects?.forEach { (path, objectInfo) ->
            val hashedPath = "${objectInfo.hash.substring(0, 2)}/${objectInfo.hash}"
            val targetPath = if (assetIndex.isMapToResources) resourcesTarget else assetsTarget
            val targetFile = if (assetIndex.isVirtual || assetIndex.isMapToResources) {
                File(targetPath, path)
            } else {
                File(targetPath, "objects/${hashedPath}".replace("/", File.separator))
            }
            verifyScheduleDownload("MinecraftDownloader.scheduleAssetDownloads", "${MINECRAFT_RES}$hashedPath", objectInfo.hash, targetFile)
        }
    }

    /** 计划库文件下载 */
    private fun scheduleLibraryDownloads(gameManifest: GameManifest) {
        gameManifest.libraries?.let { libraries ->
            processLibraries { libraries }
            libraries.forEach { library ->
                if (library.name.startsWith("org.lwjgl")) return@forEach

                val artifactPath: String = artifactToPath(library) ?: return@forEach
                val (sha1, url) = if (library.downloads != null && library.downloads.artifact != null) {
                    Pair(library.downloads.artifact.sha1, library.downloads.artifact.url)
                } else return@forEach

                val fullUrl = url ?: run {
                    if (library.url == null) "https://libraries.minecraft.net/"
                    else library.url.replace("http://", "https://")
                }.let { "${it}$artifactPath" }

                verifyScheduleDownload("MinecraftDownloader.scheduleLibraryDownloads", fullUrl, sha1, File(librariesTarget, artifactPath))
            }
        }
    }

    /**
     * 验证完整性并提交计划下载
     */
    private fun verifyScheduleDownload(logTag: String, url: String, sha1: String, targetFile: File) {
        /** 计划下载 */
        fun scheduleDownload(url: String, targetFile: File) {
            totalFileCount.incrementAndGet()
            allDownloadTasks.add(DownloadTask(url, targetFile))
        }

        if (!verifyIntegrity || !targetFile.exists()) {
            scheduleDownload(url, targetFile)
        } else if (!compareSHA1(targetFile, sha1)) { //通过检查sha1，验证目标完整性
            Log.w(logTag, "SHA-1 mismatch for ${targetFile.absolutePath}, schedule re-downloading...")
            FileUtils.deleteQuietly(targetFile)
            scheduleDownload(url, targetFile)
        }
    }
}

data class DownloadTask(
    val url: String,
    val targetPath: File
)