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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

@Composable
fun FloatingBall(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    color: Color = Color.Black.copy(alpha = 0.25f),
    contentColor: Color = Color.White.copy(alpha = 0.95f),
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val viewConfig = LocalViewConfiguration.current

        val parentWidth = constraints.maxWidth
        val parentHeight = constraints.maxHeight

        var ballSize by remember { mutableStateOf(IntSize.Zero) }
        var offsetX by remember { mutableStateOf(Offset.Zero.x) }
        var offsetY by remember { mutableStateOf(Offset.Zero.y) }

        Surface(
            modifier = modifier
                .onSizeChanged { size ->
                    ballSize = size
                    val x = ((parentWidth - ballSize.width) / 2f) //默认位置 TopCenter
                    offsetX = x.coerceIn(0f, (parentWidth - ballSize.width).toFloat())
                    offsetY = 0f.coerceIn(0f, (parentHeight - ballSize.height).toFloat())
                }
                .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
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
                                val newX = offsetX + delta.x
                                val newY = offsetY + delta.y
                                offsetX = newX.coerceIn(0f, (parentWidth - ballSize.width).toFloat())
                                offsetY = newY.coerceIn(0f, (parentHeight - ballSize.height).toFloat())
                            }
                            change.consume()
                        }

                        if (!isDragging) {
                            //非拖动事件，判定为一次点击
                            onClick()
                        }
                    }
                },
            color = color,
            contentColor = contentColor,
            shape = shape
        ) {
            content()
        }
    }
}