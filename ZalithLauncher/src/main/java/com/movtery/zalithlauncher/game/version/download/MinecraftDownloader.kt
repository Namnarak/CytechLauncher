package com.movtery.zalithlauncher.game.version.download

import android.content.Context
import android.util.Log
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.versioninfo.models.GameManifest
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.getMessageOrToString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.io.File
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.atomic.AtomicLong

class MinecraftDownloader(
    private val context: Context,
    private val version: String,
    private val customName: String = version,
    private val verifyIntegrity: Boolean,
    private val downloader: BaseMinecraftDownloader = BaseMinecraftDownloader(verifyIntegrity = verifyIntegrity),
    private val mode: DownloadMode = DownloadMode.DOWNLOAD,
    private val onCompletion: () -> Unit = {},
    private val onError: (message: String) -> Unit = {},
    private val maxDownloadThreads: Int = 64
) {
    //已下载文件计数器
    private var downloadedFileSize: AtomicLong = AtomicLong(0)
    private var downloadedFileCount: AtomicLong = AtomicLong(0)
    private var totalFileSize: AtomicLong = AtomicLong(0)
    private var totalFileCount: AtomicLong = AtomicLong(0)

    private var allDownloadTasks = mutableListOf<DownloadTask>()
    private var downloadFailedTasks = mutableListOf<DownloadTask>()

    private fun getTaskMessage(download: Int, verify: Int): Int =
        when (mode) {
            DownloadMode.DOWNLOAD -> download
            DownloadMode.VERIFY_AND_REPAIR -> verify
        }

    fun getDownloadTask(): Task {
        return Task.runTask(
            id = DOWNLOADER_TAG,
            dispatcher = Dispatchers.Default,
            task = { task ->
                task.updateProgress(-1f, getTaskMessage(R.string.minecraft_download_stat_download_task, R.string.minecraft_download_stat_verify_task))
                if (mode == DownloadMode.DOWNLOAD) {
                    progressNewDownloadTasks()
                } else {
                    val jsonFile = downloader.getVersionJsonPath(customName).takeIf { it.canRead() } ?: throw IOException("Version $customName JSON file is unreadable.")
                    val jsonText = jsonFile.readText()
                    val gameManifest = jsonText.parseTo(GameManifest::class.java)
                    progressDownloadTasks(gameManifest, customName)
                }

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
                //清除任务信息
                task.updateProgress(1f, null)

                onCompletion()
            },
            onError = { e ->
                Log.e(DOWNLOADER_TAG, "Failed to download Minecraft!", e)
                val message = when(e) {
                    is InterruptedException, is InterruptedIOException, is CancellationException -> return@runTask
                    is DownloadFailedException -> {
                        val failedUrls = downloadFailedTasks.map { it.url }
                        "${ context.getString(R.string.minecraft_download_failed_retried) }\r\n${ failedUrls.joinToString("\r\n") } }"
                    }
                    else -> e.getMessageOrToString()
                }
                onError(message)
            }
        )
    }

    private suspend fun downloadAll(
        task: Task,
        tasks: List<DownloadTask>,
        taskMessageRes: Int
    ) = coroutineScope {
        downloadFailedTasks.clear()

        val semaphore = Semaphore(maxDownloadThreads)

        val downloadJobs = tasks.map { downloadTask ->
            launch {
                semaphore.withPermit {
                    downloadTask.download()
                }
            }
        }

        val progressJob = launch(Dispatchers.Main) {
            while (isActive) {
                try {
                    ensureActive()
                    val currentFileSize = downloadedFileSize.get()
                    val totalFileSize = totalFileSize.get().run { if (this < currentFileSize) currentFileSize else this }
                    task.updateProgress(
                        (currentFileSize.toFloat() / totalFileSize.toFloat()).coerceIn(0f, 1f),
                        taskMessageRes,
                        downloadedFileCount.get(), totalFileCount.get(), //文件个数
                        formatFileSize(currentFileSize), formatFileSize(totalFileSize) //文件大小
                    )
                    delay(100)
                } catch (e: CancellationException) {
                    break //取消
                }
            }
        }

        try {
            downloadJobs.joinAll()
        } catch (e: CancellationException) {
            downloadJobs.forEach { it.cancel("Parent cancelled", e) }
        } finally {
            progressJob.cancel()
        }
    }

    /**
     * 仅将 Jar、Json 文件安装到自定义版本目录中
     */
    private suspend fun progressNewDownloadTasks() {
        val gameManifest = downloader.findVersion(this.version)?.let {
            downloader.createVersionJson(it, this.customName)
        } ?: throw IllegalArgumentException("Version not found: $version")

        commonScheduleDownloads(gameManifest, this.customName)
    }

    private suspend fun progressDownloadTasks(gameManifest: GameManifest, version: String) {
        if (gameManifest.inheritsFrom != null) { //优先尝试解析原版
            val selectedVersion = downloader.findVersion(gameManifest.inheritsFrom)
            selectedVersion?.let {
                downloader.createVersionJson(it)
            }?.let { gameManifest1 ->
                progressDownloadTasks(gameManifest1, gameManifest.inheritsFrom)
            }
        }

        commonScheduleDownloads(gameManifest, version)
    }

    private suspend fun commonScheduleDownloads(gameManifest: GameManifest, version: String) {
        val assetsIndex = downloader.createAssetIndex(downloader.assetIndexTarget, gameManifest)

        downloader.loadClientJarDownload(gameManifest, version) { url, hash, targetFile, size ->
            scheduleDownload(url, hash, targetFile, size)
        }
        downloader.loadAssetsDownload(assetsIndex) { url, hash, targetFile, size ->
            scheduleDownload(url, hash, targetFile, size)
        }
        downloader.loadLibraryDownloads(gameManifest) { url, hash, targetFile, size ->
            scheduleDownload(url, hash, targetFile, size)
        }
    }

    /**
     * 提交计划下载
     */
    private fun scheduleDownload(url: String, sha1: String?, targetFile: File, size: Long) {
        totalFileCount.incrementAndGet()
        totalFileSize.addAndGet(size)
        allDownloadTasks.add(
            DownloadTask(
                url = url,
                verifyIntegrity = verifyIntegrity,
                targetFile = targetFile,
                sha1 = sha1,
                onDownloadFailed = { task ->
                    downloadFailedTasks.add(task)
                },
                onFileDownloadedSize = { downloadedSize ->
                    downloadedFileSize.addAndGet(downloadedSize)
                },
                onFileDownloaded = {
                    downloadedFileCount.incrementAndGet()
                }
            )
        )
    }
}