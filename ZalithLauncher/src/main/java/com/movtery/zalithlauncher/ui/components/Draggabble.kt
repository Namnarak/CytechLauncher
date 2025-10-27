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

package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun BoxWithConstraintsScope.DraggableBox(
    alignment: Alignment = Alignment.TopCenter,
    onClick: () -> Unit = {},
    color: Color = Color.Black.copy(alpha = 0.25f),
    contentColor: Color = Color.White.copy(alpha = 0.95f),
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable () -> Unit
) {
    var offset by remember { mutableStateOf(Offset.Zero) }
    var boxSize by remember { mutableStateOf(IntSize(0, 0)) }
    var initialized by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val screenSize = remember(maxWidth, maxHeight) {
        with(density) {
            IntSize(
                width = maxWidth.roundToPx(),
                height = maxHeight.roundToPx()
            )
        }
    }

    val screenWidthPx = screenSize.width.toFloat()
    val screenHeightPx = screenSize.height.toFloat()
    val viewConfig = LocalViewConfiguration.current

    val offsetState by rememberUpdatedState(offset)
    val boxSizeState by rememberUpdatedState(boxSize)
    val screenWidthPxState by rememberUpdatedState(screenWidthPx)
    val screenHeightPxState by rememberUpdatedState(screenHeightPx)

    Surface(
        modifier = Modifier
            .onGloballyPositioned { layoutCoordinates ->
                boxSize = layoutCoordinates.size
                if (!initialized && boxSize.width > 0 && boxSize.height > 0) {
                    val placeableSize = IntSize(
                        screenWidthPx.toInt() - boxSize.width,
                        screenHeightPx.toInt() - boxSize.height
                    )
                    val alignedOffset = alignment.align(IntSize.Zero, placeableSize, LayoutDirection.Ltr)
                    offset = Offset(alignedOffset.x.toFloat(), alignedOffset.y.toFloat())
                    initialized = true
                }
            }
            .absoluteOffset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)

                    val startPosition = down.position
                    var isDragging = false

                    drag(down.id) { change ->
                        val delta = change.positionChange()
                        val distanceFromStart = (change.position - startPosition).getDistance()

                        if (!isDragging && distanceFromStart > viewConfig.touchSlop) {
                            //超出了拖动检测距离，说明是真的在进行拖动
                            //标记当前为拖动，避免松开手指后，判定为点击事件
                            isDragging = true
                        }

                        if (isDragging) { //只有在拖动的情况下，才会变更位置
                            val newX = offsetState.x + delta.x
                            val newY = offsetState.y + delta.y
                            val maxX = screenWidthPxState - boxSizeState.width
                            val maxY = screenHeightPxState - boxSizeState.height
                            offset = Offset(
                                x = max(0f, min(newX, maxX)),
                                y = max(0f, min(newY, maxY))
                            )
                        }
                        change.consume()
                    }

                    if (!isDragging) {
                        //非拖动事件，判定为一次点击
                        onClick()
                    }
                }
            }
            .wrapContentSize(),
        color = color,
        contentColor = contentColor,
        shape = shape
    ) {
        content()
    }
}