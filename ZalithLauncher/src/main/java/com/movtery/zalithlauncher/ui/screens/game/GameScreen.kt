package com.movtery.zalithlauncher.ui.screens.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movtery.layer_controller.ControlBoxLayout
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.layout.ControlLayout
import com.movtery.layer_controller.observable.ObservableControlLayout
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.CURSOR_DISABLED
import com.movtery.zalithlauncher.bridge.ZLBridgeStates
import com.movtery.zalithlauncher.game.input.LWJGLCharSender
import com.movtery.zalithlauncher.game.keycodes.LwjglGlfwKeycode
import com.movtery.zalithlauncher.game.support.touch_controller.touchControllerInputModifier
import com.movtery.zalithlauncher.game.support.touch_controller.touchControllerTouchModifier
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.toAction
import com.movtery.zalithlauncher.ui.components.MenuState
import com.movtery.zalithlauncher.ui.control.control.HotbarRule.Companion.hotbarPercentage
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SCROLL_DOWN
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SCROLL_DOWN_SINGLE
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SCROLL_UP
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SCROLL_UP_SINGLE
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SWITCH_IME
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SWITCH_MENU
import com.movtery.zalithlauncher.ui.control.control.MinecraftHotbar
import com.movtery.zalithlauncher.ui.control.control.lwjglEvent
import com.movtery.zalithlauncher.ui.control.gyroscope.GyroscopeReader
import com.movtery.zalithlauncher.ui.control.gyroscope.isGyroscopeAvailable
import com.movtery.zalithlauncher.ui.control.input.TextInputMode
import com.movtery.zalithlauncher.ui.control.input.textInputHandler
import com.movtery.zalithlauncher.ui.control.mouse.SwitchableMouseLayout
import com.movtery.zalithlauncher.ui.screens.game.elements.DraggableGameBall
import com.movtery.zalithlauncher.ui.screens.game.elements.ForceCloseOperation
import com.movtery.zalithlauncher.ui.screens.game.elements.GameMenuSubscreen
import com.movtery.zalithlauncher.ui.screens.game.elements.HandleEventKey
import com.movtery.zalithlauncher.ui.screens.game.elements.LogBox
import com.movtery.zalithlauncher.ui.screens.game.elements.LogState
import com.movtery.zalithlauncher.ui.screens.game.elements.ReplacementControlOperation
import com.movtery.zalithlauncher.ui.screens.game.elements.ReplacementControlState
import com.movtery.zalithlauncher.ui.screens.game.elements.SendKeycodeOperation
import com.movtery.zalithlauncher.ui.screens.game.elements.SendKeycodeState
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import org.lwjgl.glfw.CallbackBridge
import java.io.File

private class GameViewModel(private val version: Version) : ViewModel() {
    /** 游戏菜单操作状态 */
    var gameMenuState by mutableStateOf(MenuState.NONE)
    /** 强制关闭弹窗操作状态 */
    var forceCloseState by mutableStateOf<ForceCloseOperation>(ForceCloseOperation.None)
    /** 发送键值操作状态 */
    var sendKeycodeState by mutableStateOf<SendKeycodeState>(SendKeycodeState.None)
    /** 更换控制布局操作状态 */
    var replacementControlState by mutableStateOf<ReplacementControlState>(ReplacementControlState.None)
    /** 输入法状态 */
    var textInputMode by mutableStateOf(TextInputMode.DISABLE)
    /** 被控制布局层标记为仅滑动的指针列表 */
    var moveOnlyPointers = mutableSetOf<PointerId>()
    /** 鼠标触摸指针处理层占用指针列表 */
    var occupiedPointers = mutableSetOf<PointerId>()

    /** 可观察的控制布局 */
    var observableLayout by mutableStateOf<ObservableControlLayout?>(null)
        private set
    /** 当前控制布局文件 */
    var currentControlFile by mutableStateOf<File?>(null)
        private set
    /** 启用控制布局 */
    var controlEnabled by mutableStateOf(true)
        private set

    fun switchControl(enabled: Boolean) {
        if (controlEnabled != enabled) controlEnabled = enabled
    }

    /** 所有已按下的按键，与同一键值的同时按下个数 */
    val pressedKeyEvents = mutableStateMapOf<String, Int>()
    /** 所有已按下的启动器事件，与同一键值的同时按下个数 */
    val pressedLauncherEvents = mutableStateMapOf<String, Int>()

    /** 虚拟鼠标滚动事件处理 */
    val mouseScrollEvent = MouseScrollEvent(viewModelScope)

    fun loadControlLayout(layoutFile: File? = version.getControlPath()) {
        observableLayout = null
        currentControlFile = layoutFile
        val layout = layoutFile?.let { file ->
            try {
                ControlLayout.loadFromFile(file)
            } catch (e: Exception) {
                lWarning("Failed to load control layout: $file", e)
                null
            }
        } ?: ControlLayout.Empty
        //将控制布局加载为可供Compose加载的形式
        observableLayout = ObservableControlLayout(layout)
    }

    /**
     * 切换输入法
     */
    fun switchIME() {
        this.textInputMode = this.textInputMode.switch()
    }

    /**
     * 切换游戏菜单
     */
    fun switchMenu() {
        this.gameMenuState = this.gameMenuState.next()
    }

    init {
        loadControlLayout()
    }

    override fun onCleared() {
        this.mouseScrollEvent.cancelAll()
    }
}

private class MouseScrollEvent(private val scope: CoroutineScope) {
    /** 鼠标滚轮上 */
    private var mouseScrollUpJob: Job? = null
    /** 鼠标滚轮下 */
    private var mouseScrollDownJob: Job? = null

    private fun cancel(isUp: Boolean) {
        if (isUp) {
            mouseScrollUpJob?.cancel()
            mouseScrollUpJob = null
        } else {
            mouseScrollDownJob?.cancel()
            mouseScrollDownJob = null
        }
    }

    private fun setJob(job: Job?, isUp: Boolean) {
        if (isUp) {
            mouseScrollUpJob = job
        } else {
            mouseScrollDownJob = job
        }
    }

    /**
     * 单击响应一次滚轮滚动事件
     */
    fun scrollSingle(isUp: Boolean) {
        CallbackBridge.sendScroll(0.0, if (isUp) 1.0 else -1.0)
    }

    /**
     * 长按不间断触发滚轮滚动事件
     */
    fun scrollLongPress(cancel: Boolean, isUp: Boolean) {
        if (cancel) {
            cancel(isUp)
        } else {
            val job = scope.launch {
                while (true) {
                    try {
                        ensureActive()
                        CallbackBridge.sendScroll(0.0, if (isUp) 1.0 else -1.0)
                        delay(50)
                    } catch (_: Exception) {
                        break
                    }
                }
                setJob(null, isUp)
            }
            setJob(job, isUp)
        }
    }

    fun cancelAll() {
        mouseScrollUpJob?.cancel()
        mouseScrollDownJob?.cancel()
    }
}

@Composable
private fun rememberGameViewModel(
    version: Version
) = viewModel(
    key = version.toString()
) {
    GameViewModel(version)
}

@Composable
fun GameScreen(
    version: Version,
    isGameRendering: Boolean,
    logState: LogState,
    onLogStateChange: (LogState) -> Unit = {},
    isTouchProxyEnabled: Boolean,
    onInputAreaRectUpdated: (IntRect?) -> Unit = {},
    eventViewModel: EventViewModel
) {
    val context = LocalContext.current
    val viewModel = rememberGameViewModel(version)
    val isGrabbing = remember(ZLBridgeStates.cursorMode) {
        ZLBridgeStates.cursorMode == CURSOR_DISABLED
    }

    SendKeycodeOperation(
        operation = viewModel.sendKeycodeState,
        onChange = { viewModel.sendKeycodeState = it },
        lifecycleScope = viewModel.viewModelScope
    )

    ForceCloseOperation(
        operation = viewModel.forceCloseState,
        onChange = { viewModel.forceCloseState = it },
        text = stringResource(R.string.game_menu_option_force_close_text)
    )

    ReplacementControlOperation(
        operation = viewModel.replacementControlState,
        onChange = { viewModel.replacementControlState = it },
        currentLayout = viewModel.currentControlFile,
        replacementControl = { viewModel.loadControlLayout(it) }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        GameInfoBox(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(all = 16.dp),
            version = version,
            isGameRendering = isGameRendering
        )

        HandleEventKey(
            keys = viewModel.pressedKeyEvents,
            handle = { key, pressed ->
                lwjglEvent(eventKey = key, isMouse = key.startsWith("GLFW_MOUSE_", false), isPressed = pressed)
            }
        )
        HandleEventKey(
            keys = viewModel.pressedLauncherEvents,
            handle = { key, pressed ->
                if (key.startsWith("GLFW_MOUSE_", false)) {
                    //处理鼠标事件
                    lwjglEvent(eventKey = key, isMouse = true, isPressed = pressed)
                } else {
                    if (pressed) {
                        when (key) {
                            LAUNCHER_EVENT_SWITCH_IME -> { viewModel.switchIME() }
                            LAUNCHER_EVENT_SWITCH_MENU -> { viewModel.switchMenu() }
                            LAUNCHER_EVENT_SCROLL_UP_SINGLE -> { viewModel.mouseScrollEvent.scrollSingle(isUp = true) }
                            LAUNCHER_EVENT_SCROLL_DOWN_SINGLE -> { viewModel.mouseScrollEvent.scrollSingle(isUp = false) }
                        }
                    }
                    when (key) {
                        LAUNCHER_EVENT_SCROLL_UP -> { viewModel.mouseScrollEvent.scrollLongPress(cancel = !pressed, isUp = true) }
                        LAUNCHER_EVENT_SCROLL_DOWN -> { viewModel.mouseScrollEvent.scrollLongPress(cancel = !pressed, isUp = false) }
                    }
                }
            }
        )

        ControlBoxLayout(
            modifier = Modifier.fillMaxSize(),
            observedLayout = viewModel.observableLayout,
            checkOccupiedPointers = { viewModel.occupiedPointers.contains(it) },
            onClickEvent = { event, pressed ->
                val events = when (event.type) {
                    ClickEvent.Type.Key -> viewModel.pressedKeyEvents
                    ClickEvent.Type.LauncherEvent -> viewModel.pressedLauncherEvents
                    else -> return@ControlBoxLayout
                }
                //获取当前已按下相同键值的按键个数
                val count = (events[event.key] ?: 0).coerceAtLeast(0)
                if (pressed) {
                    events[event.key] = count + 1
                } else if (count > 0) {
                    events[event.key] = count - 1
                }
            },
            markPointerAsMoveOnly = { viewModel.moveOnlyPointers.add(it) },
            isCursorGrabbing = ZLBridgeStates.cursorMode == CURSOR_DISABLED,
            enabled = viewModel.controlEnabled
        ) {
            MouseControlLayout(
                isTouchProxyEnabled = isTouchProxyEnabled,
                modifier = Modifier.fillMaxSize(),
                onInputAreaRectUpdated = onInputAreaRectUpdated,
                textInputMode = viewModel.textInputMode,
                onCloseInputMethod = { viewModel.textInputMode = TextInputMode.DISABLE },
                isMoveOnlyPointer = { viewModel.moveOnlyPointers.contains(it) },
                onOccupiedPointer = { viewModel.occupiedPointers.add(it) },
                onReleasePointer = {
                    viewModel.occupiedPointers.remove(it)
                    viewModel.moveOnlyPointers.remove(it)
                },
                onEnableControl = { viewModel.switchControl(true) },
                onDisableControl = { viewModel.switchControl(false) }
            )

            MinecraftHotbar(
                rule = AllSettings.hotbarRule.state,
                widthPercentage = AllSettings.hotbarWidth.state.hotbarPercentage(),
                heightPercentage = AllSettings.hotbarHeight.state.hotbarPercentage(),
                onClickSlot = { keycode ->
                    CallbackBridge.sendKeyPress(keycode)
                },
                isGrabbing = isGrabbing,
                resolutionRatio = AllSettings.resolutionRatio.state,
                onOccupiedPointer = { viewModel.occupiedPointers.add(it) },
                onReleasePointer = { viewModel.occupiedPointers.remove(it) }
            )
        }

        //陀螺仪控制
        val isGyroscopeAvailable = remember(context) {
            isGyroscopeAvailable(context = context)
        }
        if (isGrabbing && isGyroscopeAvailable && AllSettings.gyroscopeControl.state) {
            GyroscopeReader(
                xEvent = { delta ->
                    CallbackBridge.sendCursorDelta(if (AllSettings.gyroscopeInvertX.state) -delta else delta, 0f)
                },
                yEvent = { delta ->
                    CallbackBridge.sendCursorDelta(0f, if (AllSettings.gyroscopeInvertY.state) delta else -delta)
                },
                sampleRate = AllSettings.gyroscopeSampleRate.state,
                smoothing = AllSettings.gyroscopeSmoothing.state,
                smoothingWindow = AllSettings.gyroscopeSmoothingWindow.state,
                sensitivity = AllSettings.gyroscopeSensitivity.state / 100f
            )
        }

        LogBox(
            enableLog = logState.value,
            modifier = Modifier.fillMaxSize()
        )

        GameMenuSubscreen(
            state = viewModel.gameMenuState,
            closeScreen = { viewModel.gameMenuState = MenuState.HIDE },
            onForceClose = { viewModel.forceCloseState = ForceCloseOperation.Show },
            onSwitchLog = { onLogStateChange(logState.next()) },
            onRefreshWindowSize = { eventViewModel.sendEvent(EventViewModel.Event.Game.RefreshSize) },
            onInputMethod = { viewModel.switchIME() },
            onSendKeycode = { viewModel.sendKeycodeState = SendKeycodeState.ShowDialog },
            onReplacementControl = { viewModel.replacementControlState = ReplacementControlState.Show }
        )
    }

    DraggableGameBall(
        showGameFps = AllSettings.showFPS.state,
        onClick = {
            viewModel.switchMenu()
        }
    )

    LaunchedEffect(Unit) {
        eventViewModel.events
            .filterIsInstance<EventViewModel.Event.Game.ShowIme>()
            .collect {
                viewModel.textInputMode = TextInputMode.ENABLE
            }
    }
}

@Composable
private fun GameInfoBox(
    modifier: Modifier = Modifier,
    version: Version,
    isGameRendering: Boolean
) {
    AnimatedVisibility(
        visible = !isGameRendering,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                //提示信息
                Column(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.game_loading),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.game_loading_version_name, version.getVersionName()),
                        style = MaterialTheme.typography.labelLarge
                    )
                    version.getVersionInfo()?.let { info ->
                        Text(
                            text = stringResource(R.string.game_loading_version_info, info.getInfoString()),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * 鼠标控制层
 * @param isTouchProxyEnabled 是否启用控制代理（TouchController模组支持）
 * @param textInputMode 输入法状态
 * @param isMoveOnlyPointer 检查指针是否被标记为仅处理滑动事件
 * @param onOccupiedPointer 标记指针已被占用
 * @param onReleasePointer 标记指针已被释放
 * @param onEnableControl 启用控制布局操控层
 * @param onDisableControl 禁用控制布局操控层
 */
@Composable
private fun MouseControlLayout(
    isTouchProxyEnabled: Boolean,
    modifier: Modifier = Modifier,
    onInputAreaRectUpdated: (IntRect?) -> Unit,
    textInputMode: TextInputMode,
    onCloseInputMethod: () -> Unit,
    isMoveOnlyPointer: (PointerId) -> Boolean,
    onOccupiedPointer: (PointerId) -> Unit,
    onReleasePointer: (PointerId) -> Unit,
    onEnableControl: () -> Unit,
    onDisableControl: () -> Unit
) {
    Box(
        modifier = modifier
            .then(
                if (isTouchProxyEnabled) {
                    Modifier
                        .touchControllerTouchModifier()
                        .touchControllerInputModifier(
                            onInputAreaRectUpdated = onInputAreaRectUpdated,
                        )
                } else Modifier
            )
            .textInputHandler(
                mode = textInputMode,
                sender = LWJGLCharSender,
                onCloseInputMethod = onCloseInputMethod
            )
    ) {

        val capturedSpeedFactor = AllSettings.mouseCaptureSensitivity.state / 100f
        val capturedTapMouseAction = AllSettings.gestureTapMouseAction.state.toAction()
        val capturedLongPressMouseAction = AllSettings.gestureLongPressMouseAction.state.toAction()

        SwitchableMouseLayout(
            modifier = Modifier.fillMaxSize(),
            cursorMode = ZLBridgeStates.cursorMode,
            onTouch = onEnableControl,
            onMouse = onDisableControl,
            onTap = { position ->
                CallbackBridge.putMouseEventWithCoords(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT.toInt(), position.x.sumPosition(), position.y.sumPosition())
            },
            onCapturedTap = { position ->
                if (AllSettings.gestureControl.state) {
                    CallbackBridge.putMouseEvent(capturedTapMouseAction)
                }
            },
            onLongPress = {
                CallbackBridge.putMouseEvent(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT.toInt(), true)
            },
            onLongPressEnd = {
                CallbackBridge.putMouseEvent(LwjglGlfwKeycode.GLFW_MOUSE_BUTTON_LEFT.toInt(), false)
            },
            onCapturedLongPress = {
                if (AllSettings.gestureControl.state) {
                    CallbackBridge.putMouseEvent(capturedLongPressMouseAction, true)
                }
            },
            onCapturedLongPressEnd = {
                if (AllSettings.gestureControl.state) {
                    CallbackBridge.putMouseEvent(capturedLongPressMouseAction, false)
                }
            },
            onPointerMove = { pos ->
                pos.sendPosition()
            },
            onCapturedMove = { delta ->
                CallbackBridge.sendCursorDelta(
                    delta.x * capturedSpeedFactor,
                    delta.y * capturedSpeedFactor
                )
            },
            onMouseScroll = { scroll ->
                CallbackBridge.sendScroll(scroll.x.toDouble(), scroll.y.toDouble())
            },
            onMouseButton = { button, pressed ->
                val code = LWJGLCharSender.getMouseButton(button) ?: return@SwitchableMouseLayout
                CallbackBridge.sendMouseButton(code.toInt(), pressed)
            },
            isMoveOnlyPointer = isMoveOnlyPointer,
            onOccupiedPointer = onOccupiedPointer,
            onReleasePointer = onReleasePointer
        )
    }
}

private fun Offset.sendPosition() {
    CallbackBridge.sendCursorPos(x.sumPosition(), y.sumPosition())
}

private fun Float.sumPosition(): Float {
    return (this * (AllSettings.resolutionRatio.state / 100f))
}