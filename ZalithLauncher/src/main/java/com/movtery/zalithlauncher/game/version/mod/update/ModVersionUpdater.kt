package com.movtery.zalithlauncher.game.version.mod.update

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.version.download.DownloadFailedException
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.utils.logging.Logger.lError
import com.movtery.zalithlauncher.utils.network.downloadFromMirrorListSuspend
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
import java.util.concurrent.atomic.AtomicLong

class ModVersionUpdater(
    val mods: List<PlatformVersion>,
    private val targetDir: File,
    private val maxDownloadThreads: Int = 64
) {
    //文件下载进度计数
    private var downloadedFileCount: AtomicLong = AtomicLong(0)
    private var downloadedFileSize: AtomicLong = AtomicLong(0)
    private val downloadFailedTasks = mutableSetOf<PlatformVersion>()

    suspend fun startDownload(task: Task) {
        downloadAll(
            task = task,
            taskMessageRes = R.string.mods_update_updating
        )
        if (downloadFailedTasks.isNotEmpty()) {
            downloadedFileCount.set(0)
            downloadedFileSize.set(0L)
            downloadAll(
                task = task,
                tasks = downloadFailedTasks.toList(),
                taskMessageRes = R.string.mods_update_updating_retry
            )
        }
        if (downloadFailedTasks.isNotEmpty()) throw DownloadFailedException()
        //清除任务信息
        task.updateProgress(1f, null)
    }

    private suspend fun downloadAll(
        task: Task,
        tasks: List<PlatformVersion> = mods,
        taskMessageRes: Int,
        totalFileCount: Int = tasks.size
    ) = coroutineScope {
        downloadFailedTasks.clear()

        val semaphore = Semaphore(maxDownloadThreads)

        val downloadJobs = tasks.map { newVersion ->
            launch {
                semaphore.withPermit {
                    val urls = listOf(newVersion.platformDownloadUrl())
                    val outputFile = File(targetDir, newVersion.platformFileName())

                    runCatching {
                        downloadFromMirrorListSuspend(
                            urls = urls,
                            sha1 = newVersion.platformSha1(),
                            outputFile = outputFile
                        ) { size ->
                            downloadedFileSize.addAndGet(size)
                        }
                        //下载成功
                        downloadedFileCount.incrementAndGet()
                    }.onFailure { e ->
                        if (e is CancellationException) return@onFailure
                        lError("Download failed: ${outputFile.absolutePath}, urls: ${urls.joinToString(", ")}", e)
                        downloadFailedTasks.add(newVersion)
                    }
                }
            }
        }

        val progressJob = launch(Dispatchers.Main) {
            while (isActive) {
                try {
                    ensureActive()
                    val currentFileCount = downloadedFileCount.get()
                    task.updateProgress(
                        (currentFileCount.toFloat() / totalFileCount.toFloat()).coerceIn(0f, 1f),
                        taskMessageRes,
                        downloadedFileCount.get(), totalFileCount,
                        formatFileSize(downloadedFileSize.get())
                    )
                    delay(100)
                } catch (_: CancellationException) {
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
}