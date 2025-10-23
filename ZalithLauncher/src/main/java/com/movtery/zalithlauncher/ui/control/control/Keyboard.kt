package com.movtery.zalithlauncher.ui.control.control

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.keycodes.ControlEventKeycode
import com.movtery.zalithlauncher.ui.components.AutoSizeText
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.itemLayoutColorOnSurface
import com.movtery.zalithlauncher.ui.control.gamepad.SPECIAL_KEY_MOUSE_SCROLL_DOWN
import com.movtery.zalithlauncher.ui.control.gamepad.SPECIAL_KEY_MOUSE_SCROLL_UP
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutTextItem

private data class TabItem(val title: String)

/**
 * 虚拟键盘对话框，展示一个包含主要按键的键盘
 * @param onTouch [isTapMode] 为 `false` 时，触摸按键的回调函数
 * @param onTap [isTapMode] 为 `true` 时，点击按键的回调函数
 */
@Composable
fun Keyboard(
    onDismissRequest: () -> Unit,
    isTapMode: Boolean = false,
    onTouch: (key: String, pressed: Boolean) -> Unit = { _, _ -> },
    onTap: (key: String) -> Unit = {}
) {
    val tabs = remember {
        listOf(
            TabItem("①"),
            TabItem("②")
        )
    }

    KeyboardNavDialog(
        tabs = tabs,
        onDismissRequest = onDismissRequest
    ) { page ->
        when (page) {
            0 -> {
                MainKeyboardArea(
                    modifier = Modifier.padding(all = 12.dp),
                    isTapMode = isTapMode,
                    onTap = onTap,
                    onTouch = onTouch
                )
            }
            1 -> {
                EditingKeyboardArea(
                    modifier = Modifier.padding(all = 12.dp),
                    isTapMode = isTapMode,
                    onTap = onTap,
                    onTouch = onTouch
                )
            }
        }
    }
}

/**
 * 虚拟键盘对话框，展示一个包含主要按键的键盘，主要用于手柄键值绑定
 * @param selectedKeys 当前键绑定的所有键值
 * @param onKeyAdd 绑定新的键值
 * @param onKeyRemove 解绑键值
 */
@Composable
fun GamepadBindingKeyboard(
    selectedKeys: List<String>,
    onKeyAdd: (String) -> Unit,
    onKeyRemove: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val tabs = remember {
        listOf(
            TabItem("①"),
            TabItem("②"),
            TabItem("③")
        )
    }

    val currentSelectedKeys by rememberUpdatedState(selectedKeys)
    val currentOnKeyAdd by rememberUpdatedState(onKeyAdd)
    val currentOnKeyRemove by rememberUpdatedState(onKeyRemove)

    var refreshed by remember { mutableStateOf(false) }

    fun onKeyTap(key: String) {
        if (currentSelectedKeys.contains(key)) currentOnKeyRemove(key)
        else currentOnKeyAdd(key)
        refreshed = refreshed.not()
    }

    KeyboardNavDialog(
        tabs = tabs,
        onDismissRequest = onDismissRequest
    ) { page ->
        when (page) {
            0 -> {
                MainKeyboardArea(
                    modifier = Modifier.padding(all = 12.dp),
                    isTapMode = true,
                    onTap = { key ->
                        onKeyTap(key)
                    },
                    onTouch = { _, _ -> },
                    refreshed = refreshed,
                    isSelected = { key ->
                        currentSelectedKeys.contains(key)
                    }
                )
            }
            1 -> {
                EditingKeyboardArea(
                    modifier = Modifier.padding(all = 12.dp),
                    isTapMode = true,
                    onTap = { key ->
                        onKeyTap(key)
                    },
                    onTouch = { _, _ -> },
                    refreshed = refreshed,
                    isSelected = { key ->
                        currentSelectedKeys.contains(key)
                    }
                )
            }
            2 -> {
                GamepadSpecialArea(
                    onTap = { key ->
                        onKeyTap(key)
                    },
                    refreshed = refreshed,
                    isSelected = { key ->
                        currentSelectedKeys.contains(key)
                    }
                )
            }
        }
    }
}

@Composable
fun GamepadSpecialArea(
    modifier: Modifier = Modifier,
    onTap: (String) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(all = 12.dp)
    ) {
        //鼠标左键
        item {
            val selected = remember(refreshed) { isSelected(ControlEventKeycode.GLFW_MOUSE_BUTTON_LEFT) }
            InfoLayoutTextItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.control_editor_edit_event_launcher_mouse_left),
                onClick = {
                    onTap(ControlEventKeycode.GLFW_MOUSE_BUTTON_LEFT)
                },
                showArrow = false,
                selected = selected
            )
        }
        //鼠标中键
        item {
            val selected = remember(refreshed) { isSelected(ControlEventKeycode.GLFW_MOUSE_BUTTON_MIDDLE) }
            InfoLayoutTextItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.control_editor_edit_event_launcher_mouse_middle),
                onClick = {
                    onTap(ControlEventKeycode.GLFW_MOUSE_BUTTON_MIDDLE)
                },
                showArrow = false,
                selected = selected
            )
        }
        //鼠标右键
        item {
            val selected = remember(refreshed) { isSelected(ControlEventKeycode.GLFW_MOUSE_BUTTON_RIGHT) }
            InfoLayoutTextItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.control_editor_edit_event_launcher_mouse_right),
                onClick = {
                    onTap(ControlEventKeycode.GLFW_MOUSE_BUTTON_RIGHT)
                },
                showArrow = false,
                selected = selected
            )
        }
        //单次鼠标滚轮上
        item {
            val selected = remember(refreshed) { isSelected(SPECIAL_KEY_MOUSE_SCROLL_UP) }
            InfoLayoutTextItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.control_editor_edit_event_launcher_mouse_scroll_up_single),
                onClick = {
                    onTap(SPECIAL_KEY_MOUSE_SCROLL_UP)
                },
                showArrow = false,
                selected = selected
            )
        }
        //单次鼠标滚轮下
        item {
            val selected = remember(refreshed) { isSelected(SPECIAL_KEY_MOUSE_SCROLL_DOWN) }
            InfoLayoutTextItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.control_editor_edit_event_launcher_mouse_scroll_down_single),
                onClick = {
                    onTap(SPECIAL_KEY_MOUSE_SCROLL_DOWN)
                },
                showArrow = false,
                selected = selected
            )
        }
    }
}

@Composable
private fun KeyboardNavDialog(
    tabs: List<TabItem>,
    onDismissRequest: () -> Unit,
    pageContent: @Composable (PagerScope.(Int) -> Unit)
) {
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
    }
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            selectedTabIndex = pagerState.currentPage
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 3.dp)
                    .fillMaxWidth(),
                shadowElevation = 3.dp,
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column {
                    //顶贴标签栏
                    SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
                        tabs.forEachIndexed { index, item ->
                            Tab(
                                selected = index == selectedTabIndex,
                                onClick = {
                                    selectedTabIndex = index
                                },
                                text = {
                                    MarqueeText(text = item.title)
                                }
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        pageContent = pageContent
                    )
                }
            }
        }
    }
}

@Composable
private fun MainKeyboardArea(
    modifier: Modifier = Modifier,
    isTapMode: Boolean,
    onTap: (String) -> Unit,
    onTouch: (String, Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean = { false }
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyboardMain01(
            modifier = Modifier.fillMaxWidth(),
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyboardMain02(
            modifier = Modifier.fillMaxWidth(),
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyboardMain03(
            modifier = Modifier.fillMaxWidth(),
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyboardMain04(
            modifier = Modifier.fillMaxWidth(),
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyboardMain05(
            modifier = Modifier.fillMaxWidth(),
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyboardMain06(
            modifier = Modifier.fillMaxWidth(),
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
    }
}

@Composable
private fun EditingKeyboardArea(
    modifier: Modifier = Modifier,
    isTapMode: Boolean,
    onTap: (String) -> Unit,
    onTouch: (String, Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean = { false }
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Keyboard2Chunk01(
            modifier = Modifier.fillMaxWidth(),
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        Keyboard2Chunk02(
            modifier = Modifier.fillMaxWidth(),
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        Keyboard2Chunk03(
            modifier = Modifier.fillMaxWidth(),
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        Keyboard2Chunk04(
            modifier = Modifier.fillMaxWidth(),
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        Keyboard2Chunk05(
            modifier = Modifier.fillMaxWidth(),
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        Keyboard2Chunk06(
            modifier = Modifier.fillMaxWidth(),
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
    }
}

@Composable
private fun KeyboardMain01(
    modifier: Modifier = Modifier,
    isTapMode: Boolean,
    onTap: (key: String) -> Unit,
    onTouch: (key: String, pressed: Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Esc",
            identifier = ControlEventKeycode.GLFW_KEY_ESCAPE,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        Spacer(modifier = Modifier.weight(0.6f))
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F1",
            identifier = ControlEventKeycode.GLFW_KEY_F1,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F2",
            identifier = ControlEventKeycode.GLFW_KEY_F2,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F3",
            identifier = ControlEventKeycode.GLFW_KEY_F3,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F4",
            identifier = ControlEventKeycode.GLFW_KEY_F4,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        Spacer(modifier = Modifier.weight(0.1f))
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F5",
            identifier = ControlEventKeycode.GLFW_KEY_F5,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F6",
            identifier = ControlEventKeycode.GLFW_KEY_F6,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F7",
            identifier = ControlEventKeycode.GLFW_KEY_F7,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F8",
            identifier = ControlEventKeycode.GLFW_KEY_F8,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        Spacer(modifier = Modifier.weight(0.1f))
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F9",
            identifier = ControlEventKeycode.GLFW_KEY_F9,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F10",
            identifier = ControlEventKeycode.GLFW_KEY_F10,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F11",
            identifier = ControlEventKeycode.GLFW_KEY_F11,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F12",
            identifier = ControlEventKeycode.GLFW_KEY_F12,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
    }
}

@Composable
private fun KeyboardMain02(
    modifier: Modifier = Modifier,
    isTapMode: Boolean,
    onTap: (key: String) -> Unit,
    onTouch: (key: String, pressed: Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "`",
            identifier = ControlEventKeycode.GLFW_KEY_GRAVE_ACCENT,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "1",
            identifier = ControlEventKeycode.GLFW_KEY_1,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "2",
            identifier = ControlEventKeycode.GLFW_KEY_2,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "3",
            identifier = ControlEventKeycode.GLFW_KEY_3,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "4",
            identifier = ControlEventKeycode.GLFW_KEY_4,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "5",
            identifier = ControlEventKeycode.GLFW_KEY_5,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "6",
            identifier = ControlEventKeycode.GLFW_KEY_6,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "7",
            identifier = ControlEventKeycode.GLFW_KEY_7,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "8",
            identifier = ControlEventKeycode.GLFW_KEY_8,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "9",
            identifier = ControlEventKeycode.GLFW_KEY_9,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "0",
            identifier = ControlEventKeycode.GLFW_KEY_0,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "-",
            identifier = ControlEventKeycode.GLFW_KEY_MINUS,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "+",
            identifier = ControlEventKeycode.GLFW_KEY_EQUAL,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1.5f),
            name = "Backspace",
            identifier = ControlEventKeycode.GLFW_KEY_BACKSPACE,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1.5f
        )
    }
}

@Composable
private fun KeyboardMain03(
    modifier: Modifier = Modifier,
    isTapMode: Boolean,
    onTap: (key: String) -> Unit,
    onTouch: (key: String, pressed: Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1.3f),
            name = "Tab",
            identifier = ControlEventKeycode.GLFW_KEY_TAB,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1.3f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Q",
            identifier = ControlEventKeycode.GLFW_KEY_Q,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "W",
            identifier = ControlEventKeycode.GLFW_KEY_W,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "E",
            identifier = ControlEventKeycode.GLFW_KEY_E,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "R",
            identifier = ControlEventKeycode.GLFW_KEY_R,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "T",
            identifier = ControlEventKeycode.GLFW_KEY_T,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Y",
            identifier = ControlEventKeycode.GLFW_KEY_Y,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "U",
            identifier = ControlEventKeycode.GLFW_KEY_U,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "I",
            identifier = ControlEventKeycode.GLFW_KEY_I,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "O",
            identifier = ControlEventKeycode.GLFW_KEY_O,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "P",
            identifier = ControlEventKeycode.GLFW_KEY_P,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "[",
            identifier = ControlEventKeycode.GLFW_KEY_LEFT_BRACKET,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "]",
            identifier = ControlEventKeycode.GLFW_KEY_RIGHT_BRACKET,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected
        )
        KeyButton(
            modifier = Modifier.weight(1.2f),
            name = "\\",
            identifier = ControlEventKeycode.GLFW_KEY_BACKSLASH,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1.2f
        )
    }
}

@Composable
private fun KeyboardMain04(
    modifier: Modifier = Modifier,
    isTapMode: Boolean,
    onTap: (key: String) -> Unit,
    onTouch: (key: String, pressed: Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1.4f),
            name = "Capslock",
            identifier = ControlEventKeycode.GLFW_KEY_CAPS_LOCK,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1.4f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "A",
            identifier = ControlEventKeycode.GLFW_KEY_A,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "S",
            identifier = ControlEventKeycode.GLFW_KEY_S,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "D",
            identifier = ControlEventKeycode.GLFW_KEY_D,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F",
            identifier = ControlEventKeycode.GLFW_KEY_F,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "G",
            identifier = ControlEventKeycode.GLFW_KEY_G,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "H",
            identifier = ControlEventKeycode.GLFW_KEY_H,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "J",
            identifier = ControlEventKeycode.GLFW_KEY_J,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "K",
            identifier = ControlEventKeycode.GLFW_KEY_K,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "L",
            identifier = ControlEventKeycode.GLFW_KEY_L,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = ";",
            identifier = ControlEventKeycode.GLFW_KEY_SEMICOLON,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "'",
            identifier = ControlEventKeycode.GLFW_KEY_APOSTROPHE,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(2.1f),
            name = "Enter",
            identifier = ControlEventKeycode.GLFW_KEY_ENTER,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 2.1f
        )
    }
}

@Composable
private fun KeyboardMain05(
    modifier: Modifier = Modifier,
    isTapMode: Boolean,
    onTap: (key: String) -> Unit,
    onTouch: (key: String, pressed: Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(2f),
            name = "Shift",
            identifier = ControlEventKeycode.GLFW_KEY_LEFT_SHIFT,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 2f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Z",
            identifier = ControlEventKeycode.GLFW_KEY_Z,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "X",
            identifier = ControlEventKeycode.GLFW_KEY_X,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "C",
            identifier = ControlEventKeycode.GLFW_KEY_C,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "V",
            identifier = ControlEventKeycode.GLFW_KEY_V,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "B",
            identifier = ControlEventKeycode.GLFW_KEY_B,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "N",
            identifier = ControlEventKeycode.GLFW_KEY_N,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "M",
            identifier = ControlEventKeycode.GLFW_KEY_M,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = ",",
            identifier = ControlEventKeycode.GLFW_KEY_COMMA,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = ".",
            identifier = ControlEventKeycode.GLFW_KEY_PERIOD,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "/",
            identifier = ControlEventKeycode.GLFW_KEY_SLASH,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(2.2f),
            name = "Shift",
            identifier = ControlEventKeycode.GLFW_KEY_RIGHT_SHIFT,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 2.2f
        )
    }
}

@Composable
private fun KeyboardMain06(
    modifier: Modifier = Modifier,
    isTapMode: Boolean,
    onTap: (key: String) -> Unit,
    onTouch: (key: String, pressed: Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Ctrl",
            identifier = ControlEventKeycode.GLFW_KEY_LEFT_CONTROL,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(1f))
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Alt",
            identifier = ControlEventKeycode.GLFW_KEY_LEFT_ALT,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(7f),
            name = "Space",
            identifier = ControlEventKeycode.GLFW_KEY_SPACE,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 7f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Alt",
            identifier = ControlEventKeycode.GLFW_KEY_RIGHT_ALT,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(2f), aspectRatio = 2f)
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Ctrl",
            identifier = ControlEventKeycode.GLFW_KEY_RIGHT_CONTROL,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
    }
}

@Composable
private fun Keyboard2Chunk01(
    modifier: Modifier = Modifier,
    isTapMode: Boolean,
    onTap: (key: String) -> Unit,
    onTouch: (key: String, pressed: Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Printf",
            identifier = ControlEventKeycode.GLFW_KEY_PRINT_SCREEN,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Scroll",
            identifier = ControlEventKeycode.GLFW_KEY_SCROLL_LOCK,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Pause",
            identifier = ControlEventKeycode.GLFW_KEY_PAUSE,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(12.2f), aspectRatio = 12.2f)
    }
}

@Composable
private fun Keyboard2Chunk02(
    modifier: Modifier = Modifier,
    isTapMode: Boolean,
    onTap: (key: String) -> Unit,
    onTouch: (key: String, pressed: Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Insert",
            identifier = ControlEventKeycode.GLFW_KEY_INSERT,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Home",
            identifier = ControlEventKeycode.GLFW_KEY_HOME,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "PgUp",
            identifier = ControlEventKeycode.GLFW_KEY_PAGE_UP,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(7.8f), aspectRatio = 7.8f)
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Num lk",
            identifier = ControlEventKeycode.GLFW_KEY_NUM_LOCK,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "/",
            identifier = ControlEventKeycode.GLFW_KEY_KP_DIVIDE,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "*",
            identifier = ControlEventKeycode.GLFW_KEY_KP_MULTIPLY,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "-",
            identifier = ControlEventKeycode.GLFW_KEY_KP_SUBTRACT,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
    }
}

@Composable
private fun Keyboard2Chunk03(
    modifier: Modifier = Modifier,
    isTapMode: Boolean,
    onTap: (key: String) -> Unit,
    onTouch: (key: String, pressed: Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Delete",
            identifier = ControlEventKeycode.GLFW_KEY_DELETE,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "End",
            identifier = ControlEventKeycode.GLFW_KEY_END,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "PgDn",
            identifier = ControlEventKeycode.GLFW_KEY_PAGE_DOWN,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(7.8f), aspectRatio = 7.8f)
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "7",
            identifier = ControlEventKeycode.GLFW_KEY_KP_7,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "8",
            identifier = ControlEventKeycode.GLFW_KEY_KP_8,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "9",
            identifier = ControlEventKeycode.GLFW_KEY_KP_9,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "+",
            identifier = ControlEventKeycode.GLFW_KEY_KP_ADD,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
    }
}

@Composable
private fun Keyboard2Chunk04(
    modifier: Modifier = Modifier,
    isTapMode: Boolean,
    onTap: (key: String) -> Unit,
    onTouch: (key: String, pressed: Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        EmptyButton(modifier = Modifier.weight(11f), aspectRatio = 11f)
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "4",
            identifier = ControlEventKeycode.GLFW_KEY_KP_4,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "5",
            identifier = ControlEventKeycode.GLFW_KEY_KP_5,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "6",
            identifier = ControlEventKeycode.GLFW_KEY_KP_6,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun Keyboard2Chunk05(
    modifier: Modifier = Modifier,
    isTapMode: Boolean,
    onTap: (key: String) -> Unit,
    onTouch: (key: String, pressed: Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        EmptyButton(modifier = Modifier.weight(1f))
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "↑",
            identifier = ControlEventKeycode.GLFW_KEY_UP,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(8.8f), aspectRatio = 8.8f)
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "1",
            identifier = ControlEventKeycode.GLFW_KEY_KP_1,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "2",
            identifier = ControlEventKeycode.GLFW_KEY_KP_2,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "3",
            identifier = ControlEventKeycode.GLFW_KEY_KP_3,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Enter",
            identifier = ControlEventKeycode.GLFW_KEY_KP_ENTER,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
    }
}

@Composable
private fun Keyboard2Chunk06(
    modifier: Modifier = Modifier,
    isTapMode: Boolean,
    onTap: (key: String) -> Unit,
    onTouch: (key: String, pressed: Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "←",
            identifier = ControlEventKeycode.GLFW_KEY_LEFT,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "↓",
            identifier = ControlEventKeycode.GLFW_KEY_DOWN,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "→",
            identifier = ControlEventKeycode.GLFW_KEY_RIGHT,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(8.8f), aspectRatio = 8.8f)
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "0",
            identifier = ControlEventKeycode.GLFW_KEY_KP_0,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = ".",
            identifier = ControlEventKeycode.GLFW_KEY_KP_DECIMAL,
            isTapMode = isTapMode,
            onTap = onTap,
            onTouch = onTouch,
            refreshed = refreshed,
            isSelected = isSelected,
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(1f))
    }
}




@Composable
private fun KeyButton(
    modifier: Modifier = Modifier,
    name: String,
    identifier: String,
    isTapMode: Boolean,
    onTap: (identifier: String) -> Unit,
    onTouch: (identifier: String, pressed: Boolean) -> Unit,
    refreshed: Any? = null,
    isSelected: (String) -> Boolean,
    color: Color = itemLayoutColorOnSurface(3.dp),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = MaterialTheme.shapes.medium,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    style: TextStyle = MaterialTheme.typography.labelSmall,
    maxFontSize: TextUnit = style.fontSize,
    aspectRatio: Float = 1f
) {
    /**
     * 当前按钮是否为按下的状态
     */
    val isSelected = remember(refreshed) { isSelected(identifier) }
    var pressed by remember { mutableStateOf(false) }
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnTouch by rememberUpdatedState(onTouch)

    val borderWidth by animateDpAsState(
        if (pressed || isSelected) 2.dp
        else (-1).dp
    )

    Surface(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        //点击模式不处理触摸事件
                        if (!isTapMode) currentOnTouch(identifier, true)

                        //等待松开
                        tryAwaitRelease()

                        pressed = false
                        if (!isTapMode) currentOnTouch(identifier, false)
                    },
                    onTap = {
                        if (isTapMode) currentOnTap(identifier)
                    }
                )
            }
            .border(
                width = borderWidth,
                color = borderColor,
                shape = shape
            ),
        color = color,
        contentColor = contentColor,
        shape = shape
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AutoSizeText(
                modifier = Modifier.basicMarquee(Int.MAX_VALUE),
                text = name,
                style = style,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(maxFontSize = maxFontSize)
            )
        }
    }
}

@Composable
private fun EmptyButton(
    modifier: Modifier = Modifier,
    aspectRatio: Float = 1f
) {
    Spacer(modifier = modifier.aspectRatio(aspectRatio))
}