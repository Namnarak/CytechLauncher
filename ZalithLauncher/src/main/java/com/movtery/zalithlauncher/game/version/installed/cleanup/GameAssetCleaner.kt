package com.movtery.zalithlauncher.game.version.installed.cleanup

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CleaningServices
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskState
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.game.path.getAssetsHome
import com.movtery.zalithlauncher.game.path.getLibrariesHome
import com.movtery.zalithlauncher.game.version.download.BaseMinecraftDownloader
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.version.installed.getGameManifest
import com.movtery.zalithlauncher.utils.file.collectFiles
import com.movtery.zalithlauncher.utils.file.findRedundantFiles
import com.movtery.zalithlauncher.utils.file.formatFileSize
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File

class GameAssetCleaner(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _tasksFlow: MutableStateFlow<List<TitledTask>> = MutableStateFlow(emptyList())
    val tasksFlow: StateFlow<List<TitledTask>> = _tasksFlow

    /**
     * 当前清理任务
     */
    private var job: Job? = null

    /**
     * 基础下载器
     */
    private val downloader = BaseMinecraftDownloader(verifyIntegrity = true)

    /**
     * 已安装的全部的文件
     */
    private val allFiles = mutableListOf<File>()

    /**
     * 所有游戏所需的文件
     */
    private val allGameFiles = mutableListOf<File>()

    /**
     * 所有冗余文件
     */
    private lateinit var allRedundantFiles: List<File>

    /**
     * 已被清理的文件数量
     */
    private var cleanedFileCount = 0

    /**
     * 已清理的文件总大小
     */
    private var cleanedSize: Long = 0L

    /**
     * 清理失败的文件
     */
    private val failedFiles = mutableListOf<File>()

    fun start(
        onEnd: (count: Int, size: String) -> Unit = { _, _ -> },
        onThrowable: (Throwable) -> Unit = {}
    ) {
        _tasksFlow.update { emptyList() }

        job = scope.launch(Dispatchers.IO) {
            val libraryFolder = File(getLibrariesHome())
            val assetsFolder = File(getAssetsHome())

            allFiles.clear()
            allGameFiles.clear()
            failedFiles.clear()
            cleanedFileCount = 0
            cleanedSize = 0L

            val tasks: MutableList<TitledTask> = mutableListOf()

            //获取全部文件
            tasks.add(
                TitledTask(
                    title = context.getString(R.string.versions_manage_cleanup_collect_files),
                    runningIcon = Icons.AutoMirrored.Outlined.Article,
                    task = Task.runTask(
                        id = "GameAssetCleaner.CollectFiles",
                        task = { task ->
                            task.updateProgress(-1f)

                            collectFiles(libraryFolder) { allFiles.add(it.alsoProgress(task)) }
                            collectFiles(assetsFolder) { allFiles.add(it.alsoProgress(task)) }
                        }
                    )
                )
            )

            //收集所有版本所需的游戏文件
            tasks.add(
                TitledTask(
                    title = context.getString(R.string.versions_manage_cleanup_collect_game_files),
                    runningIcon = Icons.AutoMirrored.Outlined.Article,
                    task = Task.runTask(
                        id = "GameAssetCleaner.CollectGameFiles",
                        task = { task ->
                            task.updateProgress(-1f)

                            val allVersions = VersionsManager.versions.value.toList()

                            allVersions.forEach { version ->
                                ensureActive()

                                task.updateMessage(R.string.versions_manage_cleanup_progress_next_version, version.getVersionName())

                                val gameManifest = getGameManifest(version) //已启动游戏时所需的依赖为准
                                val index = downloader.createAssetIndex(downloader.assetIndexTarget, gameManifest)

                                fun addGameFile(file: File) {
                                    if (allGameFiles.addIfNotContains(file)) {
                                        file.alsoProgress(task)
                                    } else {
                                        task.updateMessage(R.string.versions_manage_cleanup_progress_collected)
                                    }
                                }

                                downloader.loadLibraryDownloads(gameManifest) { _, _, targetFile, _, _ ->
                                    addGameFile(targetFile)
                                }
                                downloader.loadAssetsDownload(index) { _, _, targetFile, _ ->
                                    addGameFile(targetFile)
                                }
                            }
                        }
                    )
                )
            )

            //对比出无用的文件
            tasks.add(
                TitledTask(
                    title = context.getString(R.string.versions_manage_cleanup_compare_files),
                    runningIcon = Icons.Outlined.Build,
                    task = Task.runTask(
                        id = "GameAssetCleaner.CompareFiles",
                        task = { task ->
                            task.updateProgress(-1f)

                            allRedundantFiles = findRedundantFiles(
                                sourceFiles = allFiles,
                                targetFiles = allGameFiles,
                            ).filter { it.exists() }
                        }
                    )
                )
            )

            //清理文件
            tasks.add(
                TitledTask(
                    title = context.getString(R.string.versions_manage_cleanup_cleanup),
                    runningIcon = Icons.Outlined.CleaningServices,
                    task = Task.runTask(
                        id = "GameAssetsCleaner.Cleanup",
                        task = { task ->
                            withContext(Dispatchers.IO) {
                                task.updateProgress(-1f)

                                val totalSize = allRedundantFiles.size
                                allRedundantFiles.forEachIndexed { index, file ->
                                    ensureActive()
                                    val size = FileUtils.sizeOf(file)
                                    if (!FileUtils.deleteQuietly(file)) {
                                        failedFiles.add(file)
                                    } else {
                                        cleanedFileCount++
                                        cleanedSize += size
                                    }
                                    task.updateProgress(
                                        percentage = index.toFloat() / totalSize.toFloat(),
                                        message = R.string.versions_manage_cleanup_progress,
                                        file.name
                                    )
                                }

                                task.updateProgress(-1f)

                                if (failedFiles.isNotEmpty()) {
                                    throw CleanFailedException(failedFiles)
                                }
                            }
                        }
                    )
                )
            )

            _tasksFlow.update { tasks }

            startTasks(
                tasks = tasks,
                onEnd = onEnd,
                onThrowable = onThrowable
            )
        }
    }

    private suspend fun startTasks(
        tasks: List<TitledTask>,
        onEnd: (count: Int, size: String) -> Unit = { _, _ -> },
        onThrowable: (th: Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        for (task in tasks) {
            try {
                ensureActive()
                task.task.taskState = TaskState.RUNNING
                withContext(task.task.dispatcher) {
                    task.task.task(this, task.task)
                }
                task.task.taskState = TaskState.COMPLETED
            } catch (th: Throwable) {
                if (th is CancellationException) return@withContext
                task.task.onError(th)
                onThrowable(th)
                //有任务出现异常，终止所有安装任务
                return@withContext
            } finally {
                task.task.onFinally()
            }
        }
        try {
            ensureActive()
            onEnd(cleanedFileCount, formatFileSize(cleanedSize))
        } catch (_: CancellationException) {
        }
    }

    fun cancel() {
        job?.cancel()
        _tasksFlow.update { emptyList() }
    }

    private fun File.alsoProgress(task: Task) = this.also {
        task.updateProgress(-1f, R.string.versions_manage_cleanup_progress, it.name)
    }

    /**
     * 如果集合内不存在该路径的文件，则添加
     * @return 是否添加
     */
    private fun MutableList<File>.addIfNotContains(file: File): Boolean {
        return if (!any { it.absolutePath == file.absolutePath }) {
            add(file)
            true
        } else {
            false
        }
    }
}