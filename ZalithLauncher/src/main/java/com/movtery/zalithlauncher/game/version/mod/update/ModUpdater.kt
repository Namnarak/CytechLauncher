package com.movtery.zalithlauncher.game.version.mod.update

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Schedule
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskState
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.version.mod.RemoteMod
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger.lDebug
import com.movtery.zalithlauncher.utils.logging.Logger.lInfo
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

/**
 * 全自动模组检查更新，自动检查传入的模组列表，检查并获取模组最新版本，匹配现有MC版本、现有模组加载器
 * @param mods                  需要检查并更新的模组列表
 * @param modsDir               当前模组文件夹
 * @param minecraft             MC主版本号，用于版本匹配
 * @param modLoader             模组加载器信息，用于版本匹配
 * @param waitForUserConfirm    等待用户确认更新模组的信息
 *                              如果用户觉得没有问题，须返回`true`；否则返回`false`，安装会取消
 */
class ModUpdater(
    private val context: Context,
    private val mods: List<RemoteMod>,
    private val modsDir: File,
    private val minecraft: String,
    private val modLoader: ModLoader,
    private val scope: CoroutineScope,
    private val waitForUserConfirm: suspend (Map<ModData, PlatformVersion>) -> Boolean
) {
    private val _tasksFlow: MutableStateFlow<List<TitledTask>> = MutableStateFlow(emptyList())
    val tasksFlow: StateFlow<List<TitledTask>> = _tasksFlow

    /**
     * 当前模组更新任务
     */
    var job: Job? = null

    /**
     * 需要检查新版本的模组列表
     */
    val dataList: MutableList<ModData> = mutableListOf()

    /**
     * 需要更新的模组列表
     */
    val allModsUpdate: MutableMap<ModData, PlatformVersion> = mutableMapOf()

    fun updateAll(
        onUpdated: () -> Unit = {},
        onNoModUpdates: () -> Unit = {},
        onCancelled: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        job = scope.launch(Dispatchers.IO) {
            dataList.clear()
            allModsUpdate.clear()
            val tempModUpdaterDir = PathManager.DIR_CACHE_MOD_UPDATER

            val tasks = mutableListOf<TitledTask>()

            //清理缓存
            tasks.add(
                TitledTask(
                    title = context.getString(R.string.download_install_clear_temp),
                    runningIcon = Icons.Outlined.CleaningServices,
                    task = Task.runTask(
                        id = "ModUpdater.ClearTemp",
                        task = {
                            clearTempModUpdaterDir()
                            //清理后，重新创建缓存目录
                            tempModUpdaterDir.createDirAndLog()
                        }
                    )
                )
            )

            //过滤模组数据
            tasks.add(
                TitledTask(
                    title = context.getString(R.string.mods_update_task_filter),
                    runningIcon = Icons.Outlined.FilterAlt,
                    task = Task.runTask(
                        id = "ModUpdater.Filter",
                        task = { task ->
                            val totalSize = mods.size
                            val list = mods.mapIndexedNotNull { index, mod ->
                                val localFile = mod.localMod.file
                                //更新进度条
                                task.updateProgress(
                                    percentage = (index + 1).toFloat() / totalSize,
                                    message = R.string.empty_holder,
                                    localFile.nameWithoutExtension
                                )
                                val modFile = mod.remoteFile ?: return@mapIndexedNotNull null
                                val modProject = mod.projectInfo ?: return@mapIndexedNotNull null
                                ModData(
                                    file = localFile,
                                    modFile = modFile,
                                    project = modProject,
                                    mcMod = mod.mcMod
                                )
                            }
                            dataList.addAll(list)
                        }
                    )
                )
            )

            //检查更新
            tasks.add(
                TitledTask(
                    title = context.getString(R.string.mods_update_task_check_update),
                    runningIcon = Icons.Default.Checklist,
                    task = Task.runTask(
                        id = "ModUpdater.CheckUpdate",
                        task = { task ->
                            dataList.forEachIndexed { index, data ->
                                task.updateProgress(
                                    percentage = (index + 1).toFloat() / dataList.size,
                                    message = R.string.empty_holder,
                                    data.project.title
                                )
                                // 检查更新
                                data.checkUpdate(minecraft, modLoader)?.let { version ->
                                    allModsUpdate[data] = version
                                }
                            }

                            if (allModsUpdate.isEmpty()) {
                                //所有模组都是最新版本，无需更新
                                throw NoModUpdatesAvailableException()
                            }
                        }
                    )
                )
            )

            //等待用户确认模组更新
            tasks.add(
                TitledTask(
                    title = context.getString(R.string.mods_update_task_wait_for_user),
                    runningIcon = Icons.Outlined.Schedule,
                    task = Task.runTask(
                        id = "ModUpdater.WaitForUser",
                        task = {
                            if (!waitForUserConfirm(allModsUpdate.toMap())) {
                                //用户取消了更新，这里抛出取消异常，结束全部任务
                                throw ModUpdateCancelledException()
                            }
                        }
                    )
                )
            )

            //下载新版本模组
            tasks.add(
                TitledTask(
                    title = context.getString(R.string.mods_update_task_download),
                    task = Task.runTask(
                        id = "ModUpdater.UpdateMod",
                        task = { task ->
                            val mods = allModsUpdate.values.toList()
                            val updater = ModVersionUpdater(mods, tempModUpdaterDir)
                            updater.startDownload(task)
                        }
                    )
                )
            )

            //替换模组文件
            tasks.add(
                TitledTask(
                    title = context.getString(R.string.mods_update_task_replace),
                    runningIcon = Icons.Outlined.Build,
                    task = Task.runTask(
                        id = " ModUpdater.ReplaceMod",
                        task = { task ->
                            val totalCount = allModsUpdate.entries.size
                            allModsUpdate.entries.forEachIndexed { index, entry ->
                                val oldMod = entry.key
                                val newVersion = entry.value

                                val oldFile = oldMod.file
                                val newFileName = newVersion.platformFileName()
                                val cacheFile = File(tempModUpdaterDir, newFileName)

                                task.updateProgress(
                                    percentage = (index + 1).toFloat() / totalCount,
                                    message = R.string.empty_holder,
                                    oldFile.name
                                )

                                //确保所有文件都有效
                                if (modsDir.exists() && oldFile.exists() && cacheFile.exists()) {
                                    FileUtils.deleteQuietly(oldFile)
                                    val newFile = File(modsDir, newFileName)
                                    cacheFile.copyTo(target = newFile, overwrite = true)
                                }
                            }
                        }
                    )
                )
            )

            //清理缓存
            tasks.add(
                TitledTask(
                    title = context.getString(R.string.download_install_clear_temp),
                    runningIcon = Icons.Outlined.CleaningServices,
                    task = Task.runTask(
                        id = "ModUpdater.ClearTempEnds",
                        task = {
                            clearTempModUpdaterDir()
                        }
                    )
                )
            )

            _tasksFlow.update { tasks }

            for (task in tasks) {
                try {
                    ensureActive()
                    task.task.taskState = TaskState.RUNNING
                    withContext(task.task.dispatcher) {
                        task.task.task(this, task.task)
                    }
                    task.task.taskState = TaskState.COMPLETED
                } catch (th: Throwable) {
                    if (th is ModUpdateCancelledException) {
                        //用户已取消本次更新
                        onCancelled()
                        return@launch
                    }
                    if (th is NoModUpdatesAvailableException) {
                        //所有模组都是最新版本，不需要更新
                        onNoModUpdates()
                        return@launch
                    }
                    if (th is CancellationException) return@launch
                    task.task.onError(th)
                    onError(th)
                    //有任务出现异常，终止所有任务
                    return@launch
                } finally {
                    task.task.onFinally()
                }
            }

            onUpdated()
        }
    }

    fun cancel() {
        job?.cancel()
        _tasksFlow.update { emptyList() }
    }

    /**
     * 清理临时模组更新缓存目录
     */
    private suspend fun clearTempModUpdaterDir() = withContext(Dispatchers.IO) {
        PathManager.DIR_CACHE_MOD_UPDATER.takeIf { it.exists() }?.let { folder ->
            FileUtils.deleteQuietly(folder)
            lInfo("Temporary mod updater directory cleared.")
        }
    }

    private fun File.createDirAndLog(): File {
        this.mkdirs()
        lDebug("Created directory: $this")
        return this
    }
}