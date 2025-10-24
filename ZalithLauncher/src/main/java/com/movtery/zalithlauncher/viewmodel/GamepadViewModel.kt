package com.movtery.zalithlauncher.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.ViewModel
import com.movtery.zalithlauncher.ui.control.gamepad.GamepadMap
import com.movtery.zalithlauncher.ui.control.gamepad.GamepadMapping
import com.movtery.zalithlauncher.ui.control.gamepad.GamepadRemap
import com.movtery.zalithlauncher.ui.control.gamepad.Joystick
import com.movtery.zalithlauncher.ui.control.gamepad.JoystickType
import com.movtery.zalithlauncher.ui.control.gamepad.keyMappingMMKV
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

private const val DPAD_PRESS_THRESHOLD = 0.85f

class GamepadViewModel() : ViewModel() {
    private val _events = MutableSharedFlow<Event>(replay = 0, extraBufferCapacity = 16)
    val events = _events.asSharedFlow()

    /**
     * 手柄与键盘按键映射绑定
     */
    private val allKeyMappings = mutableMapOf<Int, TargetKeys>()
    private val allDpadMappings = mutableMapOf<DpadDirection, TargetKeys>()

    /** 左摇杆状态 */
    private val leftJoystick = Joystick(JoystickType.Left)
    /** 右摇杆状态 */
    private val rightJoystick = Joystick(JoystickType.Right)

    /**
     * 手柄方向键按下状态
     */
    private var dpadState = mutableMapOf(
        DpadDirection.Left to false,
        DpadDirection.Right to false,
        DpadDirection.Up to false,
        DpadDirection.Down to false
    )

    /**
     * 发送一个事件
     */
    private fun sendEvent(event: Event) {
        _events.tryEmit(event)
    }

    /**
     * 手柄是否已经操作过，控制是否进行持续事件输出
     */
    var gamepadEngaged by mutableStateOf(false)
        private set

    /**
     * 上一次活动时间
     */
    private var lastActivityTime: Long = System.nanoTime()

    /**
     * 当前轮询频率等级
     */
    private var pollLevel: PollLevel = PollLevel.Close

    /**
     * 检查并更新手柄是否活动中
     * @return 当前轮询频率等级
     */
    fun checkGamepadActive(): PollLevel {
        val currentTime = System.nanoTime()
        val elapsedTime = currentTime - lastActivityTime
        pollLevel = when {
            //10秒内有活动，保持或升到High等级
            elapsedTime < 10 * 1_000_000_000L -> PollLevel.High
            //10-20秒无活动，降到Low等级
            elapsedTime < 20 * 1_000_000_000L -> PollLevel.Low
            //超过20秒无活动，降到Close等级
            else -> PollLevel.Close
        }
        gamepadEngaged = pollLevel != PollLevel.Close

        return pollLevel
    }

    /**
     * 记录手柄活动时间
     */
    private fun recordActivity() {
        lastActivityTime = System.nanoTime()
        // 如果之前是挂机状态，现在恢复活动
        if (!gamepadEngaged) {
            gamepadEngaged = true
            // 活动时立即提升到High等级
            pollLevel = PollLevel.High
        }
    }

    fun reloadAllMappings() {
        allKeyMappings.clear()
        allDpadMappings.clear()

        val mmkv = keyMappingMMKV()
        GamepadMap.entries.fastForEach { entry ->
            val mapping = mmkv.decodeParcelable(entry.identifier, GamepadMapping::class.java) ?: run {
                GamepadMapping(
                    key = entry.gamepad,
                    dpadDirection = entry.dpadDirection,
                    targetsInGame = entry.defaultKeysInGame,
                    targetsInMenu = entry.defaultKeysInMenu
                )
            }
            addInMappingsMap(mapping)
        }
    }

    private fun addInMappingsMap(mapping: GamepadMapping) {
        val dpad = mapping.dpadDirection
        val keys = TargetKeys(
            inGame = mapping.targetsInGame,
            inMenu = mapping.targetsInMenu
        )
        if (dpad != null) {
            allDpadMappings[dpad] = keys
        } else {
            allKeyMappings[mapping.key] = keys
        }
    }

    /**
     * 重置手柄与键盘按键映射绑定
     */
    fun resetMapping(gamepadMap: GamepadMap, inGame: Boolean) =
        applyMapping(gamepadMap, inGame, useDefault = true)

    /**
     * 为指定手柄映射设置目标键盘映射
     */
    fun saveMapping(gamepadMap: GamepadMap, targets: Set<String>, inGame: Boolean) =
        applyMapping(gamepadMap, inGame, customTargets = targets)

    /**
     * 保存或重置手柄与键盘按键映射绑定
     * @param gamepadMap 手柄映射对象
     * @param inGame 是否为游戏内映射（true 为游戏内，false 为菜单内）
     * @param customTargets 自定义目标键
     * @param useDefault 是否使用默认按键
     */
    private fun applyMapping(
        gamepadMap: GamepadMap,
        inGame: Boolean,
        customTargets: Set<String>? = null,
        useDefault: Boolean = false
    ) {
        val dpad = gamepadMap.dpadDirection
        val keys = if (dpad != null) allDpadMappings[dpad] else allKeyMappings[gamepadMap.gamepad]

        val (targetsInGame, targetsInMenu) = if (inGame) {
            val inGameTargets = customTargets ?: if (useDefault) gamepadMap.defaultKeysInGame else emptySet()
            inGameTargets to (keys?.getKeys(false) ?: emptySet())
        } else {
            (keys?.getKeys(true) ?: emptySet()) to (customTargets ?: if (useDefault) gamepadMap.defaultKeysInMenu else emptySet())
        }

        GamepadMapping(
            key = gamepadMap.gamepad,
            dpadDirection = dpad,
            targetsInGame = targetsInGame,
            targetsInMenu = targetsInMenu
        ).save(gamepadMap.identifier)
    }

    private fun GamepadMapping.save(identifier: String) {
        addInMappingsMap(this)
        keyMappingMMKV().encode(identifier, this)
    }

    /**
     * 根据手柄按键键值获取对应的键盘映射代码
     * @return 若未找到，则返回null
     */
    fun findByCode(key: Int, inGame: Boolean) : Set<String>? {
        return allKeyMappings[key]?.getKeys(inGame)
    }

    /**
     * 根据手柄方向键获取对应的键盘映射代码
     * @return 若未找到，则返回null
     */
    fun findByDpad(dpadDirection: DpadDirection, inGame: Boolean): Set<String>? {
        return allDpadMappings[dpadDirection]?.getKeys(inGame)
    }

    /**
     * 根据手柄映射获取对应的键盘映射代码
     * @return 若未找到，则返回null
     */
    fun findByMap(map: GamepadMap, inGame: Boolean): Set<String>? {
        val dpad = map.dpadDirection
        val keys = if (dpad != null) allDpadMappings[dpad] else allKeyMappings[map.gamepad]
        return keys?.getKeys(inGame)
    }

    init {
        reloadAllMappings()
    }



    fun updateButton(code: Int, state: Boolean) {
        recordActivity() //记录按钮操作活动
        sendEvent(Event.Button(code, state))
    }

    fun updateMotion(axisCode: Int, value: Float) {
        recordActivity() //记录摇杆操作活动
        //更新摇杆状态
        when (axisCode) {
            GamepadRemap.MotionX.code -> leftJoystick.updateState(horizontal = value)
            GamepadRemap.MotionY.code -> leftJoystick.updateState(vertical = value)
            GamepadRemap.MotionZ.code -> rightJoystick.updateState(horizontal = value)
            GamepadRemap.MotionRZ.code -> rightJoystick.updateState(vertical = value)
        }
        sendIfDpad(axisCode, value)
    }

    private fun sendIfDpad(axisCode: Int, value: Float) {
        when (axisCode) {
            GamepadRemap.MotionHatX.code -> {
                updateDpad(DpadDirection.Left, value < -DPAD_PRESS_THRESHOLD)
                updateDpad(DpadDirection.Right, value > DPAD_PRESS_THRESHOLD)
            }
            GamepadRemap.MotionHatY.code -> {
                updateDpad(DpadDirection.Up, value < -DPAD_PRESS_THRESHOLD)
                updateDpad(DpadDirection.Down, value > DPAD_PRESS_THRESHOLD)
            }
        }
    }

    private fun updateDpad(direction: DpadDirection, pressed: Boolean) {
        val last = dpadState[direction] ?: false
        if (last != pressed) {
            dpadState[direction] = pressed
            recordActivity() //记录方向键操作活动
            sendEvent(Event.Dpad(direction, pressed))
        }
    }

    /**
     * 轮询调用，持续发送当前拥有的摇杆状态
     */
    fun pollJoystick(inGame: Boolean) {
        leftJoystick.onTick(inGame) { sendEvent(it) }
        rightJoystick.onTick(inGame) { sendEvent(it) }
    }

    /**
     * 便于记录目标键盘映射的数据类
     */
    private data class TargetKeys(
        val inGame: Set<String>,
        val inMenu: Set<String>
    ) {
        fun getKeys(isInGame: Boolean) = if (isInGame) inGame else inMenu
    }

    /**
     * 方向键方向
     */
    enum class DpadDirection {
        Up, Down, Left, Right
    }

    sealed interface Event {
        /**
         * 手柄按钮按下/松开事件
         * @param code 经过映射转化后的标准按钮键值
         */
        data class Button(val code: Int, val pressed: Boolean) : Event

        /**
         * 手柄摇杆偏移量事件
         * @param joystickType 摇杆类型（左、右）
         */
        data class StickOffset(val joystickType: JoystickType, val offset: Offset) : Event

        /**
         * 手柄摇杆方向变更事件
         * @param joystickType 摇杆类型（左、右）
         */
        data class StickDirection(val joystickType: JoystickType, val direction: Joystick.Direction) : Event

        /**
         * 手柄方向键按下/松开事件
         * @param direction 方向
         */
        data class Dpad(val direction: DpadDirection, val pressed: Boolean) : Event
    }

    enum class PollLevel(val delayMs: Long) {
        /**
         * 高轮询等级：16ms延迟 ≈ 60fps
         * 在10秒内有操作时保持此等级
         */
        High(16L),

        /**
         * 低轮询等级：200ms延迟
         * 在10-20秒无操作时降到此等级
         */
        Low(200L),

        /**
         * 不进行轮询
         * 超过20秒无操作时降到此等级
         */
        Close(10000L);
    }
}