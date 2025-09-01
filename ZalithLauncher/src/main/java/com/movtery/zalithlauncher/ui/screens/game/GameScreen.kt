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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerId
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
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SCROLL_DOWN
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SCROLL_DOWN_SINGLE
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SCROLL_UP
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SCROLL_UP_SINGLE
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SWITCH_IME
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SWITCH_MENU
import com.movtery.zalithlauncher.ui.control.control.lwjglEvent
import com.movtery.zalithlauncher.ui.control.input.TextInputMode
import com.movtery.zalithlauncher.ui.control.input.textInputHandler
import com.movtery.zalithlauncher.ui.control.mouse.SwitchableMouseLayout
import com.movtery.zalithlauncher.ui.screens.game.elements.DraggableGameBall
import com.movtery.zalithlauncher.ui.screens.game.elements.ForceCloseOperation
import com.movtery.zalithlauncher.ui.screens.game.elements.GameMenuSubscreen
import com.movtery.zalithlauncher.ui.screens.game.elements.LogBox
import com.movtery.zalithlauncher.ui.screens.game.elements.LogState
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
    /** 输入法状态 */
    var textInputMode by mutableStateOf(TextInputMode.DISABLE)
    /** 鼠标触摸指针处理层占用指针列表 */
    var touchpadOccupiedPointers = mutableSetOf<PointerId>()

    /** 可观察的 */
    var observableLayout by mutableStateOf<ObservableControlLayout?>(null)
        private set

    /** 虚拟鼠标滚动事件处理 */
    val mouseScrollEvent = MouseScrollEvent(viewModelScope)

    fun loadControlLayout(layoutFile: File? = version.getControlPath()) {
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
    val viewModel = rememberGameViewModel(version)

    ForceCloseOperation(
        operation = viewModel.forceCloseState,
        onChange = { viewModel.forceCloseState = it },
        text = stringResource(R.string.game_menu_option_force_close_text)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        GameInfoBox(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(all = 16.dp),
            version = version,
            isGameRendering = isGameRendering
        )

        ControlBoxLayout(
            modifier = Modifier.fillMaxSize(),
            observedLayout = viewModel.observableLayout,
            checkOccupiedPointers = { viewModel.touchpadOccupiedPointers.contains(it) },
            onClickEvent = { event, pressed ->
                //处理按键事件
                lwjglEvent(event, pressed)
                if (event.type == ClickEvent.Type.LauncherEvent) {
                    //处理启动器事件
                    if (pressed) {
                        when (event.key) {
                            LAUNCHER_EVENT_SWITCH_IME -> { viewModel.switchIME() }
                            LAUNCHER_EVENT_SWITCH_MENU -> { viewModel.switchMenu() }
                            LAUNCHER_EVENT_SCROLL_UP_SINGLE -> { viewModel.mouseScrollEvent.scrollSingle(isUp = true) }
                            LAUNCHER_EVENT_SCROLL_DOWN_SINGLE -> { viewModel.mouseScrollEvent.scrollSingle(isUp = false) }
                        }
                    }
                    when (event.key) {
                        LAUNCHER_EVENT_SCROLL_UP -> { viewModel.mouseScrollEvent.scrollLongPress(cancel = !pressed, isUp = true) }
                        LAUNCHER_EVENT_SCROLL_DOWN -> { viewModel.mouseScrollEvent.scrollLongPress(cancel = !pressed, isUp = false) }
                    }
                }
            },
            isCursorGrabbing = ZLBridgeStates.cursorMode == CURSOR_DISABLED
        ) {
            MouseControlLayout(
                isTouchProxyEnabled = isTouchProxyEnabled,
                modifier = Modifier.fillMaxSize(),
                onInputAreaRectUpdated = onInputAreaRectUpdated,
                textInputMode = viewModel.textInputMode,
                onCloseInputMethod = { viewModel.textInputMode = TextInputMode.DISABLE },
                onOccupiedPointer = { viewModel.touchpadOccupiedPointers.add(it) },
                onReleasePointer = { viewModel.touchpadOccupiedPointers.remove(it) }
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
            onInputMethod = { viewModel.switchIME() }
        )

        DraggableGameBall(
            showGameFps = AllSettings.showFPS.state,
            onClick = {
                viewModel.switchMenu()
            }
        )
    }

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

@Composable
private fun MouseControlLayout(
    isTouchProxyEnabled: Boolean,
    modifier: Modifier = Modifier,
    onInputAreaRectUpdated: (IntRect?) -> Unit = {},
    textInputMode: TextInputMode,
    onCloseInputMethod: () -> Unit = {},
    onOccupiedPointer: (PointerId) -> Unit = {},
    onReleasePointer: (PointerId) -> Unit = {}
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
            onMouseTap = { position ->
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