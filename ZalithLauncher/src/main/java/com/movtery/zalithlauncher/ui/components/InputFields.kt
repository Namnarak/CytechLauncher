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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SimpleTextInputField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    hint: (@Composable () -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = RoundedCornerShape(50f),
    shadowElevation: Dp = 1.dp,
    height: Dp = 32.dp,
    contextPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    textStyle: TextStyle = TextStyle(color = contentColor).copy(fontSize = 12.sp),
    cursorBrush: Brush = SolidColor(LocalTextSelectionColors.current.handleColor),
    singleLine: Boolean = false,
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = shape,
        shadowElevation = shadowElevation
    ) {
        BasicTextField(
            modifier = Modifier
                .height(height)
                .padding(contextPadding),
            value = value,
            onValueChange = onValueChange,
            textStyle = textStyle,
            cursorBrush = cursorBrush,
            singleLine = singleLine,
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        hint?.invoke()
                    }
                    innerTextField()
                }
            }
        )
    }
}