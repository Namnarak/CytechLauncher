package com.movtery.zalithlauncher.game.control

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.layout.ControlLayout
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.layer_controller.utils.newRandomFileName
import com.movtery.layer_controller.utils.saveToFile
import com.movtery.zalithlauncher.context.copyAssetFile
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.file.readString
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.InputStream

/**
 * 控制布局管理者
 */
object ControlManager {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _dataList = MutableStateFlow<List<ControlData>>(emptyList())
    val dataList: StateFlow<List<ControlData>> = _dataList

    /**
     * 当前选择的控制布局
     */
    var selectedLayout by mutableStateOf<ControlData?>(null)

    private var currentJob: Job? = null

    /**
     * 是否正在刷新控制布局
     */
    var isRefreshing by mutableStateOf(false)
        private set

    /**
     * 获取一个新的布局文件文件，名称随机
     */
    private fun getNewRandomFile() = File(PathManager.DIR_CONTROL_LAYOUTS, "${newRandomFileName()}.json")

    /**
     * 检查当前是否不存在控制布局，不存在则解压一份默认控制布局
     * @param context 访问assets的上下文
     */
    fun checkDefaultAndRefresh(context: Context) {
        scope.launch(Dispatchers.IO) {
            val files = (PathManager.DIR_CONTROL_LAYOUTS.listFiles() ?: emptyArray())
                .filter { file ->
                    file.isFile && file.exists() && file.extension.equals("json", true)
                }
            if (files.isEmpty()) {
                unpackDefaultControl(context)
            }
            refresh()
        }
    }

    fun refresh() {
        currentJob?.cancel()
        currentJob = scope.launch(Dispatchers.IO) {
            isRefreshing = true

            _dataList.update { emptyList() }
            PathManager.DIR_CONTROL_LAYOUTS.listFiles()?.mapNotNull { file ->
                if (!(file.isFile && file.exists() && file.extension.equals("json", true))) return@mapNotNull null

                var isSupport = true
                val layout: ControlLayout = try {
                    ControlLayout.loadFromFile(file)
                } catch (_: IllegalArgumentException) {
                    isSupport = false
                    runCatching {
                        ControlLayout.loadFromFileUncheck(file)
                    }.onFailure { e ->
                        lWarning("Failed to load control layout! file = $file", e)
                    }.getOrNull() ?: return@mapNotNull null
                } catch (e: Exception) {
                    lWarning("Failed to load control layout! file = $file", e)
                    return@mapNotNull null
                }

                ControlData(
                    file = file,
                    controlLayout = ObservableControlLayout(layout),
                    isSupport = isSupport
                )
            }?.let { list ->
                _dataList.update {
                    list.sortedBy {
                        if (it.isSupport) it.controlLayout.info.name.default
                        else it.file.name
                    }
                }
            }
            checkSettings()

            isRefreshing = false
        }
    }

    /**
     * 检查并更新设置
     */
    private fun checkSettings() {
        val setting = AllSettings.controlLayout.getValue()

        selectedLayout = _dataList.value.find { it.file.name == setting && it.isSupport }
            ?: dataList.value.firstOrNull { it.isSupport }
                ?.also { AllSettings.controlLayout.save(it.file.name) }

        if (selectedLayout == null) {
            AllSettings.controlLayout.reset()
        }
    }

    /**
     * 解压默认控制布局
     */
    private suspend fun unpackDefaultControl(
        context: Context
    ) = withContext(Dispatchers.IO) {
        try {
            val file = getNewRandomFile()
            context.copyAssetFile(fileName = "default_layout.json", output = file, overwrite = false)
        } catch (e: Exception) {
            lWarning("Failed to unpack default control layout", e)
        }
    }

    /**
     * 选择控制布局
     */
    fun selectControl(data: ControlData) {
        if (!data.file.exists() || !data.isSupport) return
        AllSettings.controlLayout.save(data.file.name)
        selectedLayout = data
    }

    /**
     * 在协程内删除控制布局
     */
    fun deleteControl(data: ControlData) {
        scope.launch(Dispatchers.IO) {
            if (!data.file.exists()) return@launch
            FileUtils.deleteQuietly(data.file)
            refresh()
        }
    }

    /**
     * 在协程内保存控制布局的数据
     */
    fun saveControl(
        data: ControlData,
        summitError: (Exception) -> Unit
    ) {
        scope.launch(Dispatchers.IO) {
            if (!data.file.exists()) {
                refresh()
                return@launch
            }
            val layout = data.controlLayout.pack()
            try {
                layout.saveToFile(data.file)
            } catch (e: Exception) {
                summitError(e)
                FileUtils.deleteQuietly(data.file)
            }
            refresh()
        }
    }

    /**
     * 尝试导入控制布局
     */
    suspend fun importControl(
        inputStream: InputStream,
        onSerializationError: (Exception) -> Unit,
        catchedError: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        val file = getNewRandomFile()
        try {
            inputStream.use { stream ->
                val jsonString = stream.readString()
                val layout = ControlLayout.loadFromString(jsonString)
                layout.saveToFile(file)
            }
        } catch (e: SerializationException) {
            FileUtils.deleteQuietly(file)
            onSerializationError(e)
        } catch (e: Exception) {
            FileUtils.deleteQuietly(file)
            catchedError(e)
        }
    }
}