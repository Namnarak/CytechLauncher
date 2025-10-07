package com.movtery.zalithlauncher.ui.control.control

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.game.keycodes.ControlEventKeycode
import com.movtery.zalithlauncher.ui.components.AutoSizeText
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.itemLayoutColorOnSurface

private data class TabItem(val title: String)

@Composable
fun Keyboard(
    onDismissRequest: () -> Unit,
    onTouch: (key: String, pressed: Boolean) -> Unit
) {
    val tabs = remember {
        listOf(
            TabItem("①"),
            TabItem("②")
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
                    TabRow(selectedTabIndex = selectedTabIndex) {
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
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                    ) { page ->
                        when (page) {
                            0 -> {
                                Column(
                                    modifier = Modifier.padding(all = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    KeyboardMain01(
                                        modifier = Modifier.fillMaxWidth(),
                                        onTouch = onTouch
                                    )
                                    KeyboardMain02(
                                        modifier = Modifier.fillMaxWidth(),
                                        onTouch = onTouch
                                    )
                                    KeyboardMain03(
                                        modifier = Modifier.fillMaxWidth(),
                                        onTouch = onTouch
                                    )
                                    KeyboardMain04(
                                        modifier = Modifier.fillMaxWidth(),
                                        onTouch = onTouch
                                    )
                                    KeyboardMain05(
                                        modifier = Modifier.fillMaxWidth(),
                                        onTouch = onTouch
                                    )
                                    KeyboardMain06(
                                        modifier = Modifier.fillMaxWidth(),
                                        onTouch = onTouch
                                    )
                                }
                            }
                            1 -> {
                                Column(
                                    modifier = Modifier.padding(all = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Keyboard2Chunk01(
                                        modifier = Modifier.fillMaxWidth(),
                                        onTouch = onTouch
                                    )
                                    Keyboard2Chunk02(
                                        modifier = Modifier.fillMaxWidth(),
                                        onTouch = onTouch
                                    )
                                    Keyboard2Chunk03(
                                        modifier = Modifier.fillMaxWidth(),
                                        onTouch = onTouch
                                    )
                                    Keyboard2Chunk04(
                                        modifier = Modifier.fillMaxWidth(),
                                        onTouch = onTouch
                                    )
                                    Keyboard2Chunk05(
                                        modifier = Modifier.fillMaxWidth(),
                                        onTouch = onTouch
                                    )
                                    Keyboard2Chunk06(
                                        modifier = Modifier.fillMaxWidth(),
                                        onTouch = onTouch
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyboardMain01(
    modifier: Modifier = Modifier,
    onTouch: (key: String, pressed: Boolean) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Esc",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_ESCAPE, it) }
        )
        Spacer(modifier = Modifier.weight(0.6f))
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F1",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_F1, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F2",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_F2, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F3",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_F3, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F4",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_F4, it) }
        )
        Spacer(modifier = Modifier.weight(0.1f))
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F5",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_F5, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F6",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_F6, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F7",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_F7, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F8",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_F8, it) }
        )
        Spacer(modifier = Modifier.weight(0.1f))
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F9",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_F9, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F10",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_F10, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F11",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_F11, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F12",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_F12, it) }
        )
    }
}

@Composable
private fun KeyboardMain02(
    modifier: Modifier = Modifier,
    onTouch: (key: String, pressed: Boolean) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "`",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_GRAVE_ACCENT, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "1",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_1, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "2",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_2, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "3",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_3, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "4",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_4, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "5",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_5, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "6",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_6, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "7",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_7, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "8",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_8, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "9",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_9, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "0",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_0, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "-",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_MINUS, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "+",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_EQUAL, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1.5f),
            name = "Backspace",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_BACKSPACE, it) },
            aspectRatio = 1.5f
        )
    }
}

@Composable
private fun KeyboardMain03(
    modifier: Modifier = Modifier,
    onTouch: (key: String, pressed: Boolean) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1.3f),
            name = "Tab",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_TAB, it) },
            aspectRatio = 1.3f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Q",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_Q, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "W",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_W, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "E",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_E, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "R",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_R, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "T",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_T, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Y",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_Y, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "U",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_U, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "I",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_I, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "O",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_O, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "P",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_P, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "[",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_LEFT_BRACKET, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "]",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_RIGHT_BRACKET, it) }
        )
        KeyButton(
            modifier = Modifier.weight(1.2f),
            name = "\\",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_BACKSLASH, it) },
            aspectRatio = 1.2f
        )
    }
}

@Composable
private fun KeyboardMain04(
    modifier: Modifier = Modifier,
    onTouch: (key: String, pressed: Boolean) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1.4f),
            name = "Capslock",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_CAPS_LOCK, it) },
            aspectRatio = 1.4f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "A",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_A, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "S",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_S, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "D",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_D, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_F, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "G",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_G, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "H",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_H, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "J",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_J, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "K",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_K, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "L",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_L, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = ";",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_SEMICOLON, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "'",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_APOSTROPHE, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(2.1f),
            name = "Enter",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_ENTER, it) },
            aspectRatio = 2.1f
        )
    }
}

@Composable
private fun KeyboardMain05(
    modifier: Modifier = Modifier,
    onTouch: (key: String, pressed: Boolean) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(2f),
            name = "Shift",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_LEFT_SHIFT, it) },
            aspectRatio = 2f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Z",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_Z, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "X",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_X, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "C",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_C, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "V",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_V, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "B",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_B, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "N",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_N, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "M",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_M, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = ",",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_COMMA, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = ".",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_PERIOD, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "/",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_SLASH, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(2.2f),
            name = "Shift",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_RIGHT_SHIFT, it) },
            aspectRatio = 2.2f
        )
    }
}

@Composable
private fun KeyboardMain06(
    modifier: Modifier = Modifier,
    onTouch: (key: String, pressed: Boolean) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Ctrl",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_LEFT_CONTROL, it) },
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(1f))
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Alt",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_LEFT_ALT, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(7f),
            name = "Space",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_SPACE, it) },
            aspectRatio = 7f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Alt",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_RIGHT_ALT, it) },
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(2f), aspectRatio = 2f)
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Ctrl",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_RIGHT_CONTROL, it) },
            aspectRatio = 1f
        )
    }
}

@Composable
private fun Keyboard2Chunk01(
    modifier: Modifier = Modifier,
    onTouch: (key: String, pressed: Boolean) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Printf",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_PRINT_SCREEN, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Scroll",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_SCROLL_LOCK, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Pause",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_PAUSE, it) },
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(12.2f), aspectRatio = 12.2f)
    }
}

@Composable
private fun Keyboard2Chunk02(
    modifier: Modifier = Modifier,
    onTouch: (key: String, pressed: Boolean) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Insert",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_INSERT, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Home",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_HOME, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "PgUp",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_PAGE_UP, it) },
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(7.8f), aspectRatio = 7.8f)
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Num lk",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_NUM_LOCK, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "/",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_KP_DIVIDE, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "*",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_KP_MULTIPLY, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "-",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_KP_SUBTRACT, it) },
            aspectRatio = 1f
        )
    }
}

@Composable
private fun Keyboard2Chunk03(
    modifier: Modifier = Modifier,
    onTouch: (key: String, pressed: Boolean) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Delete",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_DELETE, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "End",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_END, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "PgDn",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_PAGE_DOWN, it) },
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(7.8f), aspectRatio = 7.8f)
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "7",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_KP_7, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "8",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_KP_8, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "9",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_KP_9, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "+",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_KP_ADD, it) },
            aspectRatio = 1f
        )
    }
}

@Composable
private fun Keyboard2Chunk04(
    modifier: Modifier = Modifier,
    onTouch: (key: String, pressed: Boolean) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        EmptyButton(modifier = Modifier.weight(11f), aspectRatio = 11f)
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "4",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_KP_4, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "5",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_KP_5, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "6",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_KP_6, it) },
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun Keyboard2Chunk05(
    modifier: Modifier = Modifier,
    onTouch: (key: String, pressed: Boolean) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        EmptyButton(modifier = Modifier.weight(1f))
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "↑",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_UP, it) },
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(8.8f), aspectRatio = 8.8f)
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "1",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_KP_1, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "2",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_KP_2, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "3",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_KP_3, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Enter",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_KP_ENTER, it) },
            aspectRatio = 1f
        )
    }
}

@Composable
private fun Keyboard2Chunk06(
    modifier: Modifier = Modifier,
    onTouch: (key: String, pressed: Boolean) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "←",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_LEFT, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "↓",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_DOWN, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "→",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_RIGHT, it) },
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(8.8f), aspectRatio = 8.8f)
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "0",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_KP_0, it) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = ".",
            onTouch = { onTouch(ControlEventKeycode.GLFW_KEY_KP_DECIMAL, it) },
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(1f))
    }
}




@Composable
private fun KeyButton(
    modifier: Modifier = Modifier,
    name: String,
    onTouch: (pressed: Boolean) -> Unit,
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
    var pressed by remember { mutableStateOf(false) }
    val currentOnTouch by rememberUpdatedState(onTouch)

    val borderWidth by animateDpAsState(
        if (pressed) 2.dp
        else (-1).dp
    )

    Surface(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        currentOnTouch(true)
                        //等待松开
                        tryAwaitRelease()
                        pressed = false
                        currentOnTouch(false)
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