/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.control.input

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo

/**
 * A composable that displays an overlay in the area above the Input Method Editor (IME)
 * (the software keyboard) and below the status bar.
 *
 * This is useful for scenarios where you want to show content in the space that remains on the screen
 * when the keyboard is visible. The function calculates the available height by subtracting the IME height
 * and status bar height from the total screen height. It then positions a `Box` in this calculated area.
 * The primary purpose of this overlay is often to capture touch events (e.g., to dismiss the keyboard when
 * tapping the empty space) or to display UI elements that should appear only when the keyboard is active.
 *
 * The overlay is only shown when a non-floating keyboard is detected.
 *
 * @param imeInsets The [WindowInsets] for the IME. Defaults to `WindowInsets.ime`.
 * @param content The main content of the screen, which will be rendered behind the overlay.
 * @param emptyAreaContent The content to be displayed within the overlay area above the keyboard.
 *                         This is a `BoxScope` lambda, allowing you to use `BoxScope` modifiers like `align`.
 * @param onAreaChanged A callback that is invoked when the visibility of the overlay area changes.
 *                      It receives `true` if the area is shown (keyboard is up) and `false` otherwise.
 */
@Composable
fun TopOverlayAboveIme(
    imeInsets: WindowInsets = WindowInsets.ime,
    content: @Composable () -> Unit,
    emptyAreaContent: @Composable (BoxScope.() -> Unit),
    onAreaChanged: (show: Boolean) -> Unit = {}
) {
    val density = LocalDensity.current
    val screenHeightPx = LocalWindowInfo.current.containerSize.height.toFloat()

    Box(Modifier.fillMaxSize()) {
        content()

        val imeTopPx = imeInsets.getTop(density).toFloat()
        val imeBottomPx = imeInsets.getBottom(density).toFloat()

        val imeHeightPx = imeBottomPx - imeTopPx

        val showArea = when {
            //键盘未显示
            imeBottomPx <= 0f -> false
            //键盘顶部不在屏幕底部，说明是悬浮键盘
            imeTopPx > 0f && imeTopPx < screenHeightPx - 100f -> false
            //键盘高度较小且不占据整个底部区域，也认为是悬浮键盘
            imeHeightPx < screenHeightPx / 3 && imeBottomPx < screenHeightPx - 100f -> false
            else -> true
        }

        if (showArea) {
            val topOverlayHeightPx = screenHeightPx - imeBottomPx
            if (topOverlayHeightPx > 0f) {
                Box(
                    modifier = Modifier
                        .height(with(density) { topOverlayHeightPx.toDp() })
                        .fillMaxWidth(),
                    content = emptyAreaContent
                )
            }
        }

        LaunchedEffect(showArea) {
            onAreaChanged(showArea)
        }
    }
}