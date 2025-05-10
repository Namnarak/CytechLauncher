package com.movtery.zalithlauncher.game.version.download

import android.content.Context
import android.util.Log
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.path.getAssetsHome
import com.movtery.zalithlauncher.game.path.getLibrariesHome
import com.movtery.zalithlauncher.game.path.getResourcesHome
import com.movtery.zalithlauncher.game.path.getVersionsHome
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersions
import com.movtery.zalithlauncher.game.versioninfo.models.AssetIndexJson
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import com.movtery.zalithlauncher.game.versioninfo.models.VersionManifest.Version
import com.movtery.zalithlauncher.state.ObjectStates
import com.movtery.zalithlauncher.utils.file.compareSHA1
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.utils.network.NetWorkUtils
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.getMessageOrToString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class MinecraftDownloader(
    private val context: Context,
    private val version: String,
    private val customName: String = version,
    private val verifyIntegrity: Boolean,
    private val mode: DownloadMode = DownloadMode.DOWNLOAD,
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
    private val assetsTarget = File(getAssetsHome()).createPath()
    private val resourcesTarget = File(getResourcesHome()).createPath()
    private val versionsTarget = File(getVersionsHome()).createPath()
    private val librariesTarget = File(getLibrariesHome()).createPath()
    private val assetIndexTarget = File(assetsTarget, "indexes").createPath()
    //File
    private val versionJarSource: File = getVersionJarPath(version)
    private val versionJarTarget: File = getVersionJarPath(customName)
//    private val versionLog4jXMLSource: File = getLog4jXMLPath(version)
//    private val versionLog4jXMLTarget: File = getLog4jXMLPath(customName)

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
    private fun File.createParent(): File = this.apply { parentFile!!.createPath() }

    private fun getTaskMessage(download: Int, verify: Int): Int =
        when (mode) {
            DownloadMode.DOWNLOAD -> download
            DownloadMode.VERIFY_AND_REPAIR -> verify
        }

    fun getDownloadTask(): Task {
        return Task.runTask(
            id = LOG_TAG,
            dispatcher = Dispatchers.Default,
            task = { task ->
                task.updateProgress(-1f, R.string.minecraft_getting_version_list)
                val selectedVersion = findVersion(customName)

                task.updateProgress(-1f, getTaskMessage(R.string.minecraft_download_download_version_json, R.string.minecraft_download_progress_version_json))
                val gameManifest = selectedVersion?.let { createVersionJson(it) }

                task.updateProgress(-1f, getTaskMessage(R.string.minecraft_download_stat_download_task, R.string.minecraft_download_stat_verify_task))
                progressDownloadTasks(gameManifest, customName)

                if (allDownloadTasks.isNotEmpty()) {
                    //使用线程池进行下载
                    downloadAll(task, allDownloadTasks, getTaskMessage(R.string.minecraft_download_downloading_game_files, R.string.minecraft_download_verifying_and_repairing_files))
                    if (downloadFailedTasks.isNotEmpty()) {
                        downloadedFileCount.set(0)
                        totalFileCount.set(downloadFailedTasks.size.toLong())
                        downloadAll(task, downloadFailedTasks.toList(), getTaskMessage(R.string.minecraft_download_progress_retry_downloading_files, R.string.minecraft_download_progress_retry_verifying_files))
                    }
                    if (downloadFailedTasks.isNotEmpty()) throw DownloadFailedException()
                }

                onCompletion()
            },
            onError = { e ->
                Log.e("MinecraftDownloader", "Failed to download Minecraft!", e)
                val message = when(e) {
                    is InterruptedException, is InterruptedIOException, is CancellationException -> return@runTask
                    is DownloadFailedException -> {
                        val failedUrls = downloadFailedTasks.map { it.url }
                        "${ context.getString(R.string.minecraft_download_failed_retried) }\r\n${ failedUrls.joinToString("\r\n") } }"
                    }
                    else -> e.getMessageOrToString()
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

        runCatching {
            while (!executor.awaitTermination(33, TimeUnit.MILLISECONDS)) {
                ensureActive()
                if (System.currentTimeMillis() - lastProgressUpdate > 100) {
                    val currentFileSize = downloadedFileSize.get()
                    val totalFileSize = totalFileSize.get().run { if (this < currentFileSize) currentFileSize else this }
                    task.updateProgress(
                        (currentFileSize.toFloat() / totalFileSize.toFloat()).coerceIn(0f, 1f),
                        taskMessageRes,
                        downloadedFileCount.get(), totalFileCount.get(), //文件个数
                        formatFileSize(currentFileSize), formatFileSize(totalFileSize) //文件大小
                    )
                    lastProgressUpdate = System.currentTimeMillis()
                }
            }
            ensureFileCopy(versionJarSource, versionJarTarget)
//            ensureFileCopy(versionLog4jXMLSource, versionLog4jXMLTarget)
        }.onFailure { e ->
            executor.shutdownNow()
            when(e) {
                is CancellationException, is InterruptedException, is InterruptedIOException -> return@onFailure
                else -> throw e
            }
        }
    }

    private suspend fun findVersion(version: String): Version? {
        val versionManifest = MinecraftVersions.getVersionManifest()
        return versionManifest.versions.find { it.id == version }
    }

    private suspend fun progressDownloadTasks(gameManifest: GameManifest?, version: String) {
        val gameManifest1 = gameManifest ?: run {
            val jsonFile = getVersionJsonPath(version).takeIf { it.canRead() }
                ?: throw IOException("Unable to read Version JSON for version $version")
            val jsonText = jsonFile.readText()
            jsonText.parseTo(GameManifest::class.java)
        }
        val assetsIndex = createAssetIndex(gameManifest1)

        scheduleClientJarDownload(gameManifest1, version)
        scheduleAssetDownloads(assetsIndex)
        scheduleLibraryDownloads(gameManifest1)
//        scheduleLog4jXMLDownload(gameManifest1, version)

        if (this.version != version) {
            findVersion(this.version)?.let {
                val gameManifest2 = createVersionJson(it)
                progressDownloadTasks(gameManifest2, this.version)
            }
        }
    }

    private fun getVersionJsonPath(version: String) =
        File(versionsTarget, "$version/$version.json".replace("/", File.separator)).createParent()

    private fun getVersionJarPath(version: String) =
        File(versionsTarget, "$version/$version.jar".replace("/", File.separator)).createParent()

//    private fun getLog4jXMLPath(version: String) =
//        File(versionsTarget, "$version/log4j2.xml".replace("/", File.separator)).createParent()

    /**
     * 创建版本 Json
     */
    private suspend fun createVersionJson(version: Version): GameManifest {
        return downloadAndParseJson(
            targetFile = getVersionJsonPath(version.id),
            url = version.url,
            expectedSHA = version.sha1,
            verifyIntegrity = verifyIntegrity,
            classOfT = GameManifest::class.java
        )
    }

    /**
     * 创建 assets 索引 Json
     */
    private suspend fun createAssetIndex(gameManifest: GameManifest): AssetIndexJson? {
        val indexFile = File(assetIndexTarget, "${gameManifest.assets}.json")
        return gameManifest.assetIndex?.let { assetIndex ->
            downloadAndParseJson(
                targetFile = indexFile,
                url = assetIndex.url,
                expectedSHA = assetIndex.sha1,
                verifyIntegrity = verifyIntegrity,
                classOfT = AssetIndexJson::class.java
            )
        }
    }

    @Throws(IOException::class)
    private fun ensureFileCopy(source: File, target: File) {
        if (source == target) return
        if (target.exists()) return
        Log.i("MinecraftDownloader", "Copying ${source.getName()} to ${target.absolutePath}")
        FileUtils.copyFile(source, target, false)
    }

    /** 计划客户端jar下载 */
    private fun scheduleClientJarDownload(gameManifest: GameManifest, version: String) {
        val clientFile = getVersionJarPath(version)
        gameManifest.downloads?.client?.let { client ->
            scheduleDownload(client.url, client.sha1, clientFile, client.size)
        }
    }

    /** 计划assets资产下载 */
    private fun scheduleAssetDownloads(assetIndex: AssetIndexJson?) {
        assetIndex?.objects?.forEach { (path, objectInfo) ->
            val hashedPath = "${objectInfo.hash.substring(0, 2)}/${objectInfo.hash}"
            val targetPath = if (assetIndex.isMapToResources) resourcesTarget else assetsTarget
            val targetFile = if (assetIndex.isVirtual || assetIndex.isMapToResources) {
                File(targetPath, path)
            } else {
                File(targetPath, "objects/${hashedPath}".replace("/", File.separator))
            }
            scheduleDownload("${MINECRAFT_RES}$hashedPath", objectInfo.hash, targetFile, objectInfo.size)
        }
    }

    /** 计划库文件下载 */
    private fun scheduleLibraryDownloads(gameManifest: GameManifest) {
        gameManifest.libraries?.let { libraries ->
            processLibraries { libraries }
            libraries.forEach { library ->
                if (library.name.startsWith("org.lwjgl")) return@forEach

                val artifactPath: String = artifactToPath(library) ?: return@forEach
                val (sha1, url, size) = library.downloads?.let { downloads ->
                    downloads.artifact?.let { artifact ->
                        Triple(artifact.sha1, artifact.url, artifact.size)
                    } ?: return@forEach
                } ?: run {
                    val u1 = library.url?.replace("http://", "https://") ?: "https://libraries.minecraft.net/"
                    val url = u1.let { "${it}$artifactPath" }
                    Triple(library.sha1, url, library.size)
                }

                scheduleDownload(url, sha1, File(librariesTarget, artifactPath), size)
            }
        }
    }

//    /** 计划日志格式化配置下载 */
//    private fun scheduleLog4jXMLDownload(gameManifest: GameManifest, version: String) {
//        val versionLoggingTarget = getLog4jXMLPath(version)
//        gameManifest.logging?.client?.file?.let { loggingConfig ->
//            scheduleDownload(loggingConfig.url, loggingConfig.sha1, versionLoggingTarget, loggingConfig.size)
//        }
//    }

    /**
     * 提交计划下载
     */
    private fun scheduleDownload(url: String, sha1: String?, targetFile: File, size: Long) {
        totalFileCount.incrementAndGet()
        totalFileSize.addAndGet(size)
        allDownloadTasks.add(DownloadTask(url, targetFile, sha1))
    }

    inner class DownloadTask(
        val url: String,
        private val targetFile: File,
        private val sha1: String?
    ) : Runnable {
        override fun run() {
            //若目标文件存在，验证通过或关闭完整性验证时，跳过此次下载
            if (verifySha1()) {
                downloadedSize(FileUtils.sizeOf(targetFile))
                downloadedFile()
                return
            }

            runCatching {
                NetWorkUtils.downloadFile(url, targetFile, bufferSize = getLocalBuffer().value) { size ->
                    downloadedSize(size.toLong())
                }
                downloadedFile()
            }.onFailure { e ->
                Log.e(LOG_TAG, "Download failed: ${targetFile.absolutePath}, url: $url", e)
                downloadFailedTasks.add(this)
            }
        }

        private fun downloadedSize(size: Long) {
            downloadedFileSize.addAndGet(size)
        }

        private fun downloadedFile() {
            downloadedFileCount.incrementAndGet()
        }

        /**
         * 若目标文件存在，验证完整性
         * @return 是否跳过此次下载
         */
        private fun verifySha1(): Boolean {
            if (targetFile.exists()) {
                sha1 ?: return !verifyIntegrity
                if (!verifyIntegrity || compareSHA1(targetFile, sha1)) {
                    return true
                } else {
                    FileUtils.deleteQuietly(targetFile)
                }
            }
            return false
        }
    }
}