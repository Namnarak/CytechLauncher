package com.movtery.zalithlauncher.game.version.download

import android.content.Context
import android.util.Log
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersions
import com.movtery.zalithlauncher.game.versioninfo.models.AssetIndexJson
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import com.movtery.zalithlauncher.game.versioninfo.models.VersionManifest.Version
import com.movtery.zalithlauncher.state.ObjectStates
import com.movtery.zalithlauncher.utils.compareSHA1
import com.movtery.zalithlauncher.utils.formatFileSize
import com.movtery.zalithlauncher.utils.network.NetWorkUtils
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.getMessageOrToString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class MinecraftDownloader(
    private val context: Context,
    minecraftPath: File,
    private val version: String,
    private val verifyIntegrity: Boolean,
    private val onCompletion: () -> Unit = {},
    private val maxDownloadThreads: Int = 64
) {
    companion object {
        private const val MINECRAFT_RES: String = "https://resources.download.minecraft.net/"
        private const val LOG_TAG = "MinecraftDownloader"

        private val sThreadLocalDownloadBuffer: ThreadLocal<ByteArray> = ThreadLocal()

        private fun getLocalBuffer() = lazy<ByteArray> {
            var tlb = sThreadLocalDownloadBuffer.get()
            if (tlb != null) return@lazy tlb
            tlb = ByteArray(32768)
            sThreadLocalDownloadBuffer.set(tlb)
            return@lazy tlb
        }
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
    private var downloadedFileSize: AtomicLong = AtomicLong(0)
    private var downloadedFileCount: AtomicLong = AtomicLong(0)
    private var totalFileSize: AtomicLong = AtomicLong(0)
    private var totalFileCount: AtomicLong = AtomicLong(0)

    //进度刷新频率限制
    private var lastProgressUpdate: Long = 0L

    private var allDownloadTasks = mutableListOf<DownloadTask>()
    private var downloadFailedTasks = mutableListOf<DownloadTask>()

    private fun File.createPath(): File = this.apply { if (!(exists() && isDirectory)) mkdirs() }

    fun getDownloadTask(): Task {
        return Task.runTask(
            id = LOG_TAG,
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

                if (allDownloadTasks.isNotEmpty()) {
                    //使用线程池进行下载
                    downloadAll(task, allDownloadTasks, R.string.minecraft_download_downloading)
                    if (downloadFailedTasks.isNotEmpty()) {
                        downloadedFileCount.set(0)
                        totalFileCount.set(downloadFailedTasks.size.toLong())
                        downloadAll(task, downloadFailedTasks.toList(), R.string.minecraft_download_downloading_retry)
                    }
                    if (downloadFailedTasks.isNotEmpty()) throw DownloadFailedException()
                }

                onCompletion()
            },
            onError = { e ->
                var message = e.getMessageOrToString()
                if (e is DownloadFailedException) {
                    message = "${context.getString(R.string.minecraft_download_failed_retried)}\n$message"
                }
                ObjectStates.updateThrowable(
                    ObjectStates.ThrowableMessage(
                        title = context.getString(R.string.minecraft_download_failed),
                        message = message
                    )
                )
            }
        )
    }

    private suspend fun downloadAll(
        task: Task, tasks: List<DownloadTask>, taskMessageRes: Int
    ) = coroutineScope {
        downloadFailedTasks.clear()

        val executor = ThreadPoolExecutor(
            4,
            maxDownloadThreads,
            500L,
            TimeUnit.MILLISECONDS,
            ArrayBlockingQueue(tasks.size)
        )

        tasks.forEach { downloadTask ->
            withContext(Dispatchers.IO) {
                executor.execute(downloadTask)
            }
        }
        executor.shutdown()

        try {
            while (!executor.awaitTermination(33, TimeUnit.MILLISECONDS)) {
                ensureActive()
                if (System.currentTimeMillis() - lastProgressUpdate > 100) {
                    val current = downloadedFileCount.get()
                    val total = totalFileCount.get()
                    val progress = (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    val currentFileSize = downloadedFileSize.get()
                    val totalFileSize = totalFileSize.get().run { if (this < currentFileSize) currentFileSize else this }
                    task.updateProgress(progress, taskMessageRes, current, total, formatFileSize(currentFileSize), formatFileSize(totalFileSize))
                    lastProgressUpdate = System.currentTimeMillis()
                }
            }
        } catch (_: CancellationException) {
            executor.shutdownNow()
        } catch (_: InterruptedException) {
            executor.shutdownNow()
        }
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
            classOfT = GameManifest::class.java
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
            classOfT = AssetIndexJson::class.java
        )
    }

    /** 计划客户端jar下载 */
    private fun scheduleClientJarDownload(gameManifest: GameManifest) {
        val client = gameManifest.downloads.client
        verifyScheduleDownload(client.url, client.sha1, versionJarTarget, client.size)
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
            verifyScheduleDownload("${MINECRAFT_RES}$hashedPath", objectInfo.hash, targetFile, objectInfo.size)
        }
    }

    /** 计划库文件下载 */
    private fun scheduleLibraryDownloads(gameManifest: GameManifest) {
        gameManifest.libraries?.let { libraries ->
            processLibraries { libraries }
            libraries.forEach { library ->
                if (library.name.startsWith("org.lwjgl")) return@forEach

                val artifactPath: String = artifactToPath(library) ?: return@forEach
                val (sha1, url, size) = if (library.downloads != null && library.downloads.artifact != null) {
                    val artifact = library.downloads.artifact
                    Triple(artifact.sha1, artifact.url, artifact.size)
                } else return@forEach

                val fullUrl = url ?: run {
                    if (library.url == null) "https://libraries.minecraft.net/"
                    else library.url.replace("http://", "https://")
                }.let { "${it}$artifactPath" }

                verifyScheduleDownload(fullUrl, sha1, File(librariesTarget, artifactPath), size)
            }
        }
    }

    /**
     * 验证完整性并提交计划下载
     */
    private fun verifyScheduleDownload(url: String, sha1: String, targetFile: File, size: Long) {
        /** 计划下载 */
        fun scheduleDownload(url: String, targetFile: File, size: Long) {
            totalFileCount.incrementAndGet()
            totalFileSize.addAndGet(size)
            allDownloadTasks.add(DownloadTask(url, targetFile))
        }

        if (targetFile.exists()) {
            if (!verifyIntegrity || compareSHA1(targetFile, sha1)) {
                return
            } else {
                //删除损坏文件
                FileUtils.deleteQuietly(targetFile)
            }
        }
        scheduleDownload(url, targetFile, size)
    }

    inner class DownloadTask(
        private val url: String,
        private val targetPath: File,
    ) : Runnable {
        override fun run() {
            try {
                NetWorkUtils.downloadFile(url, targetPath, bufferSize = getLocalBuffer().value) { size ->
                    downloadedFileSize.addAndGet(size.toLong())
                }
                downloadedFileCount.incrementAndGet()
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Download failed: ${targetPath.absolutePath}, url: $url", e)
                downloadFailedTasks.add(this)
            }
        }
    }
}