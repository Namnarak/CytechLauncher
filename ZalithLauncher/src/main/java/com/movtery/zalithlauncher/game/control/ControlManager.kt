package com.movtery.zalithlauncher.game.control

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.layer_controller.layout.ControlLayout
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import kotlin.collections.find
import kotlin.collections.firstOrNull

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

    fun refresh() {
        currentJob?.cancel()
        currentJob = scope.launch {
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
                    controlLayout = layout,
                    isSupport = isSupport
                )
            }?.let { list ->
                _dataList.update {
                    list.sortedBy { it.file.name }
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

        selectedLayout = _dataList.value.find { it.file.name == setting }
            ?: dataList.value.firstOrNull { it.isSupport }
                ?.also { AllSettings.controlLayout.save(it.file.name) }

        if (selectedLayout == null) {
            AllSettings.controlLayout.reset()
        }
    }

    /**
     * 选择控制布局
     */
    fun selectControl(data: ControlData) {
        if (!data.file.exists()) return //文件不存在了（？
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
}