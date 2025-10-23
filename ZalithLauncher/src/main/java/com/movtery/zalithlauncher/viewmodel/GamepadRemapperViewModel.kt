package com.movtery.zalithlauncher.viewmodel

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.ui.control.gamepad.GamepadRemapOperation
import com.movtery.zalithlauncher.ui.control.gamepad.GamepadRemapper
import com.movtery.zalithlauncher.ui.control.gamepad.remapperMMKV
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GamepadRemapperViewModel(): ViewModel() {
    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    /**
     * 所有设备对应的映射
     */
    val allRemappers = mutableMapOf<String, GamepadRemapper>()

    /**
     * 是否正在保存映射
     */
    var isSavingMapping by mutableStateOf(false)

    /**
     * 可视化操作流程
     */
    var uiOperation by mutableStateOf<GamepadRemapOperation>(GamepadRemapOperation.None)

    /**
     * 发送一个事件
     */
    fun sendEvent(event: Event) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    fun startRemapperUI(deviceName: String) {
        if (uiOperation == GamepadRemapOperation.None) {
            uiOperation = GamepadRemapOperation.Tip(deviceName)
        }
    }

    fun findMapping(deviceName: String): GamepadRemapper? {
        return allRemappers[deviceName] ?: loadByDeviceName(deviceName)?.also { remapper ->
            applyMapping(deviceName, remapper)
        }
    }

    /**
     * 应用一个设备的映射
     */
    fun applyMapping(
        deviceName: String,
        motionMapping: Map<Int, Int>,
        keyMapping: Map<Int, Int>
    ) {
        applyMapping(
            deviceName = deviceName,
            remapper = GamepadRemapper(motionMapping, keyMapping)
        )
    }

    fun applyMapping(deviceName: String, remapper: GamepadRemapper) {
        allRemappers[deviceName] = remapper
    }

    private fun loadByDeviceName(deviceName: String): GamepadRemapper? {
        val mmkv = remapperMMKV()
        return mmkv.decodeParcelable(deviceName, GamepadRemapper::class.java)
    }

    private val saveMutex = Mutex()

    /**
     * 保存所有设备的重映射数据
     */
    fun save() {
        viewModelScope.launch {
            saveMutex.withLock {
                isSavingMapping = true
                val mmkv = remapperMMKV()
                allRemappers.entries.forEach { entry ->
                    mmkv.encode(entry.key, entry.value)
                }
                isSavingMapping = false
            }
        }
    }

    sealed interface Event {
        /**
         * 手柄按键事件
         */
        data class Button(val code: Int, val event: KeyEvent) : Event

        /**
         * 手柄运动事件
         */
        data class Axis(val event: MotionEvent) : Event
    }
}