package com.movtery.zalithlauncher.ui.control.control

import androidx.compose.foundation.basicMarquee
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
    onClick: (key: String) -> Unit
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
                                        onClick = onClick
                                    )
                                    KeyboardMain02(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = onClick
                                    )
                                    KeyboardMain03(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = onClick
                                    )
                                    KeyboardMain04(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = onClick
                                    )
                                    KeyboardMain05(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = onClick
                                    )
                                    KeyboardMain06(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = onClick
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
                                        onClick = onClick
                                    )
                                    Keyboard2Chunk02(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = onClick
                                    )
                                    Keyboard2Chunk03(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = onClick
                                    )
                                    Keyboard2Chunk04(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = onClick
                                    )
                                    Keyboard2Chunk05(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = onClick
                                    )
                                    Keyboard2Chunk06(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = onClick
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
    onClick: (key: String) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Esc",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_ESCAPE) }
        )
        Spacer(modifier = Modifier.weight(0.6f))
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F1",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_F1) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F2",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_F2) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F3",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_F3) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F4",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_F4) }
        )
        Spacer(modifier = Modifier.weight(0.1f))
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F5",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_F5) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F6",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_F6) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F7",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_F7) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F8",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_F8) }
        )
        Spacer(modifier = Modifier.weight(0.1f))
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F9",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_F9) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F10",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_F10) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F11",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_F11) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F12",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_F12) }
        )
    }
}

@Composable
private fun KeyboardMain02(
    modifier: Modifier = Modifier,
    onClick: (key: String) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "`",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_GRAVE_ACCENT) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "1",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_1) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "2",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_2) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "3",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_3) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "4",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_4) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "5",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_5) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "6",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_6) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "7",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_7) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "8",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_8) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "9",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_9) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "0",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_0) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "-",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_MINUS) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "+",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_EQUAL) }
        )
        KeyButton(
            modifier = Modifier.weight(1.5f),
            name = "Backspace",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_BACKSPACE) },
            aspectRatio = 1.5f
        )
    }
}

@Composable
private fun KeyboardMain03(
    modifier: Modifier = Modifier,
    onClick: (key: String) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1.3f),
            name = "Tab",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_TAB) },
            aspectRatio = 1.3f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Q",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_Q) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "W",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_W) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "E",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_E) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "R",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_R) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "T",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_T) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Y",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_Y) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "U",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_U) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "I",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_I) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "O",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_O) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "P",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_P) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "[",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_LEFT_BRACKET) }
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "]",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_RIGHT_BRACKET) }
        )
        KeyButton(
            modifier = Modifier.weight(1.2f),
            name = "\\",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_BACKSLASH) },
            aspectRatio = 1.2f
        )
    }
}

@Composable
private fun KeyboardMain04(
    modifier: Modifier = Modifier,
    onClick: (key: String) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1.4f),
            name = "Capslock",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_CAPS_LOCK) },
            aspectRatio = 1.4f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "A",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_A) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "S",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_S) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "D",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_D) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "F",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_F) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "G",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_G) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "H",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_H) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "J",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_J) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "K",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_K) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "L",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_L) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = ";",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_SEMICOLON) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "'",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_APOSTROPHE) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(2.1f),
            name = "Enter",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_ENTER) },
            aspectRatio = 2.1f
        )
    }
}

@Composable
private fun KeyboardMain05(
    modifier: Modifier = Modifier,
    onClick: (key: String) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(2f),
            name = "Shift",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_LEFT_SHIFT) },
            aspectRatio = 2f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Z",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_Z) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "X",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_X) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "C",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_C) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "V",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_V) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "B",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_B) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "N",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_N) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "M",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_M) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = ",",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_COMMA) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = ".",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_PERIOD) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "/",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_SLASH) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(2.2f),
            name = "Shift",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_RIGHT_SHIFT) },
            aspectRatio = 2.2f
        )
    }
}

@Composable
private fun KeyboardMain06(
    modifier: Modifier = Modifier,
    onClick: (key: String) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Ctrl",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_LEFT_CONTROL) },
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(1f))
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Alt",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_LEFT_ALT) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(7f),
            name = "Space",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_SPACE) },
            aspectRatio = 7f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Alt",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_RIGHT_ALT) },
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(2f), aspectRatio = 2f)
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Ctrl",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_RIGHT_CONTROL) },
            aspectRatio = 1f
        )
    }
}

@Composable
private fun Keyboard2Chunk01(
    modifier: Modifier = Modifier,
    onClick: (key: String) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Printf",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_PRINT_SCREEN) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Scroll",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_SCROLL_LOCK) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Pause",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_PAUSE) },
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(12.2f), aspectRatio = 12.2f)
    }
}

@Composable
private fun Keyboard2Chunk02(
    modifier: Modifier = Modifier,
    onClick: (key: String) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Insert",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_INSERT) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Home",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_HOME) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "PgUp",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_PAGE_UP) },
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(7.8f), aspectRatio = 7.8f)
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Num lk",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_NUM_LOCK) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "/",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_KP_DIVIDE) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "*",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_KP_MULTIPLY) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "-",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_KP_SUBTRACT) },
            aspectRatio = 1f
        )
    }
}

@Composable
private fun Keyboard2Chunk03(
    modifier: Modifier = Modifier,
    onClick: (key: String) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Delete",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_DELETE) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "End",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_END) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "PgDn",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_PAGE_DOWN) },
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(7.8f), aspectRatio = 7.8f)
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "7",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_KP_7) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "8",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_KP_8) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "9",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_KP_9) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "+",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_KP_ADD) },
            aspectRatio = 1f
        )
    }
}

@Composable
private fun Keyboard2Chunk04(
    modifier: Modifier = Modifier,
    onClick: (key: String) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        EmptyButton(modifier = Modifier.weight(11f), aspectRatio = 11f)
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "4",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_KP_4) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "5",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_KP_5) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "6",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_KP_6) },
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun Keyboard2Chunk05(
    modifier: Modifier = Modifier,
    onClick: (key: String) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        EmptyButton(modifier = Modifier.weight(1f))
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "↑",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_UP) },
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(8.8f), aspectRatio = 8.8f)
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "1",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_KP_1) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "2",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_KP_2) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "3",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_KP_3) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "Enter",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_KP_ENTER) },
            aspectRatio = 1f
        )
    }
}

@Composable
private fun Keyboard2Chunk06(
    modifier: Modifier = Modifier,
    onClick: (key: String) -> Unit
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "←",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_LEFT) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "↓",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_DOWN) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "→",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_RIGHT) },
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(8.8f), aspectRatio = 8.8f)
        KeyButton(
            modifier = Modifier.weight(1f),
            name = "0",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_KP_0) },
            aspectRatio = 1f
        )
        KeyButton(
            modifier = Modifier.weight(1f),
            name = ".",
            onClick = { onClick(ControlEventKeycode.GLFW_KEY_KP_DECIMAL) },
            aspectRatio = 1f
        )
        EmptyButton(modifier = Modifier.weight(1f))
    }
}




@Composable
private fun KeyButton(
    modifier: Modifier = Modifier,
    name: String,
    onClick: () -> Unit,
    color: Color = itemLayoutColorOnSurface(3.dp),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = MaterialTheme.shapes.medium,
    style: TextStyle = MaterialTheme.typography.labelSmall,
    maxFontSize: TextUnit = style.fontSize,
    aspectRatio: Float = 1f
) {
    Surface(
        modifier = modifier.aspectRatio(aspectRatio),
        onClick = onClick,
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