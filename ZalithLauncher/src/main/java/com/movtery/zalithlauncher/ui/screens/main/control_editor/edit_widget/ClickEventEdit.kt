package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.observable.ObservableNormalData
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.keycodes.ControlEventKeyName
import com.movtery.zalithlauncher.game.keycodes.ControlEventKeycode
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.itemLayoutColorOnSurface
import com.movtery.zalithlauncher.ui.control.control.Keyboard
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SCROLL_DOWN
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SCROLL_DOWN_SINGLE
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SCROLL_UP
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SCROLL_UP_SINGLE
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SWITCH_IME
import com.movtery.zalithlauncher.ui.control.control.LAUNCHER_EVENT_SWITCH_MENU
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutSwitchItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutTextItem

private data class TabItem(val title: Int)

@Composable
fun EditWidgetClickEvent(
    data: ObservableNormalData,
    switchControlLayers: (ObservableNormalData) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(start = 4.dp, end = 8.dp)
            .fillMaxSize()
    ) {
        val tabs = remember {
            listOf(
                TabItem(R.string.control_editor_edit_event_basic),
                TabItem(R.string.control_editor_edit_event_launcher),
                TabItem(R.string.control_editor_edit_event_key)
            )
        }

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

        //顶贴标签栏
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, item ->
                Tab(
                    selected = index == selectedTabIndex,
                    onClick = {
                        selectedTabIndex = index
                    },
                    text = {
                        MarqueeText(text = stringResource(item.title))
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
        ) { page ->
            when (page) {
                0 -> {
                    EditBasicEvent(
                        modifier = Modifier.fillMaxSize(),
                        data = data,
                        switchControlLayers = switchControlLayers
                    )
                }
                1 -> {
                    EditLauncherEvent(
                        modifier = Modifier.fillMaxSize(),
                        data = data
                    )
                }
                2 -> {
                    EditKeyEvent(
                        modifier = Modifier.fillMaxSize(),
                        data = data
                    )
                }
            }
        }
    }
}

@Composable
private fun EditBasicEvent(
    modifier: Modifier = Modifier,
    data: ObservableNormalData,
    switchControlLayers: (ObservableNormalData) -> Unit
) {
    Column(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier)

        //滑动触发
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_swipple),
            value = data.isSwipple,
            onValueChange = { value ->
                data.isSwipple = value
            }
        )

        //带动鼠标
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_penetrable),
            value = data.isPenetrable,
            onValueChange = { value ->
                data.isPenetrable = value
            }
        )

        //可开关
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_toggleable),
            value = data.isToggleable,
            onValueChange = { value ->
                data.isToggleable = value
            }
        )

        //切换控制层可见性
        InfoLayoutTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_switch_layers),
            onClick = {
                switchControlLayers(data)
            }
        )

        Spacer(Modifier)
    }
}

/**
 * 启动器事件缓存，优化加载性能
 */
private data class LauncherEventData(
    val switchIme: Boolean = false,
    val switchMenu: Boolean = false,
    val mouseLeft: Boolean = false,
    val mouseMiddle: Boolean = false,
    val mouseRight: Boolean = false,
    val mouseScrollUp: Boolean = false,
    val mouseScrollUpSingle: Boolean = false,
    val mouseScrollDown: Boolean = false,
    val mouseScrollDownSingle: Boolean = false
)

@Composable
private fun EditLauncherEvent(
    modifier: Modifier = Modifier,
    data: ObservableNormalData
) {
    //启动器事件开启状态缓存
    var eventData by remember { mutableStateOf(LauncherEventData()) }
    //在协程中重新计算开启状态
    LaunchedEffect(data.clickEvents) {
        var switchIme = false
        var switchMenu = false
        var mouseLeft = false
        var mouseMiddle = false
        var mouseRight = false
        var mouseScrollUp = false
        var mouseScrollUpSingle = false
        var mouseScrollDown = false
        var mouseScrollDownSingle = false
        data.clickEvents.forEach { event ->
            if (event.type == ClickEvent.Type.LauncherEvent) {
                if (!switchIme) switchIme = event.key == LAUNCHER_EVENT_SWITCH_IME
                if (!switchMenu) switchMenu = event.key == LAUNCHER_EVENT_SWITCH_MENU
                if (!mouseLeft) mouseLeft = event.key == ControlEventKeycode.GLFW_MOUSE_BUTTON_LEFT
                if (!mouseMiddle) mouseMiddle = event.key == ControlEventKeycode.GLFW_MOUSE_BUTTON_MIDDLE
                if (!mouseRight) mouseRight = event.key == ControlEventKeycode.GLFW_MOUSE_BUTTON_RIGHT
                if (!mouseScrollUp) mouseScrollUp = event.key == LAUNCHER_EVENT_SCROLL_UP
                if (!mouseScrollUpSingle) mouseScrollUpSingle = event.key == LAUNCHER_EVENT_SCROLL_UP_SINGLE
                if (!mouseScrollDown) mouseScrollDown = event.key == LAUNCHER_EVENT_SCROLL_DOWN
                if (!mouseScrollDownSingle) mouseScrollDownSingle = event.key == LAUNCHER_EVENT_SCROLL_DOWN_SINGLE
            }
        }
        eventData = LauncherEventData(
            switchIme = switchIme,
            switchMenu = switchMenu,
            mouseLeft = mouseLeft,
            mouseMiddle = mouseMiddle,
            mouseRight = mouseRight,
            mouseScrollUp = mouseScrollUp,
            mouseScrollUpSingle = mouseScrollUpSingle,
            mouseScrollDown = mouseScrollDown,
            mouseScrollDownSingle = mouseScrollDownSingle
        )
    }

    Column(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier)

        //切换输入法
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.game_menu_option_input_method),
            value = eventData.switchIme,
            onValueChange = { value ->
                val event = ClickEvent(ClickEvent.Type.LauncherEvent, LAUNCHER_EVENT_SWITCH_IME)
                if (value) {
                    data.addEvent(event)
                } else {
                    data.removeEvent(event)
                }
            }
        )

        //切换菜单
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_launcher_switch_menu),
            value = eventData.switchMenu,
            onValueChange = { value ->
                val event = ClickEvent(ClickEvent.Type.LauncherEvent, LAUNCHER_EVENT_SWITCH_MENU)
                if (value) {
                    data.addEvent(event)
                } else {
                    data.removeEvent(event)
                }
            }
        )

        //鼠标左键
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_launcher_mouse_left),
            value = eventData.mouseLeft,
            onValueChange = { value ->
                val event = ClickEvent(
                    ClickEvent.Type.LauncherEvent,
                    ControlEventKeycode.GLFW_MOUSE_BUTTON_LEFT
                )
                if (value) {
                    data.addEvent(event)
                } else {
                    data.removeEvent(event)
                }
            }
        )

        //鼠标中键
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_launcher_mouse_middle),
            value = eventData.mouseMiddle,
            onValueChange = { value ->
                val event = ClickEvent(
                    ClickEvent.Type.LauncherEvent,
                    ControlEventKeycode.GLFW_MOUSE_BUTTON_MIDDLE
                )
                if (value) {
                    data.addEvent(event)
                } else {
                    data.removeEvent(event)
                }
            }
        )

        //鼠标右键
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_launcher_mouse_right),
            value = eventData.mouseRight,
            onValueChange = { value ->
                val event = ClickEvent(
                    ClickEvent.Type.LauncherEvent,
                    ControlEventKeycode.GLFW_MOUSE_BUTTON_RIGHT
                )
                if (value) {
                    data.addEvent(event)
                } else {
                    data.removeEvent(event)
                }
            }
        )

        //鼠标滚轮上
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_launcher_mouse_scroll_up),
            value = eventData.mouseScrollUp,
            onValueChange = { value ->
                val event = ClickEvent(ClickEvent.Type.LauncherEvent, LAUNCHER_EVENT_SCROLL_UP)
                if (value) {
                    data.addEvent(event)
                } else {
                    data.removeEvent(event)
                }
            }
        )

        //单次鼠标滚轮上
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_launcher_mouse_scroll_up_single),
            value = eventData.mouseScrollUpSingle,
            onValueChange = { value ->
                val event =
                    ClickEvent(ClickEvent.Type.LauncherEvent, LAUNCHER_EVENT_SCROLL_UP_SINGLE)
                if (value) {
                    data.addEvent(event)
                } else {
                    data.removeEvent(event)
                }
            }
        )

        //鼠标滚轮下
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_launcher_mouse_scroll_down),
            value = eventData.mouseScrollDown,
            onValueChange = { value ->
                val event = ClickEvent(ClickEvent.Type.LauncherEvent, LAUNCHER_EVENT_SCROLL_DOWN)
                if (value) {
                    data.addEvent(event)
                } else {
                    data.removeEvent(event)
                }
            }
        )

        //单次鼠标滚轮下
        InfoLayoutSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_event_launcher_mouse_scroll_down_single),
            value = eventData.mouseScrollDownSingle,
            onValueChange = { value ->
                val event =
                    ClickEvent(ClickEvent.Type.LauncherEvent, LAUNCHER_EVENT_SCROLL_DOWN_SINGLE)
                if (value) {
                    data.addEvent(event)
                } else {
                    data.removeEvent(event)
                }
            }
        )

        Spacer(Modifier)
    }
}

@Composable
private fun EditKeyEvent(
    modifier: Modifier = Modifier,
    data: ObservableNormalData
) {
    var showKeyboard by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.padding(horizontal = 2.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            InfoLayoutTextItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.control_editor_edit_event_key_new),
                onClick = {
                    showKeyboard = true
                },
                showArrow = false
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(data.clickEvents.filter { it.type == ClickEvent.Type.Key }) { event ->
            EditKeyItem(
                modifier = Modifier.fillMaxWidth(),
                keyEvent = event,
                onDelete = {
                    data.removeEvent(event)
                }
            )
        }
    }

    if (showKeyboard) {
        Keyboard(
            onDismissRequest = {
                showKeyboard = false
            },
            onClick = { selectedKey ->
                val event = ClickEvent(type = ClickEvent.Type.Key, key = selectedKey)
                data.addEvent(event)
                showKeyboard = false
            }
        )
    }
}

@Composable
private fun EditKeyItem(
    modifier: Modifier = Modifier,
    keyEvent: ClickEvent,
    onDelete: () -> Unit,
    color: Color = itemLayoutColorOnSurface(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    val name = remember(keyEvent.key) { ControlEventKeyName.getNameByKey(keyEvent.key) }

    InfoLayoutItem(
        modifier = modifier,
        onClick = {},
        color = color,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MarqueeText(
                text = stringResource(R.string.control_editor_edit_event_key_value, name ?: keyEvent.key),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        IconButton(
            onClick = onDelete
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.generic_delete)
            )
        }
    }
}