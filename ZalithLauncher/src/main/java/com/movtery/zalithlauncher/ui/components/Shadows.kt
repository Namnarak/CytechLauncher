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

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.viewmodel.influencedByBackground

/**
 * 启动器元素阴影高度
 * @param dpValue 元素阴影高度
 * @param influencedByBackground 如果启动器设置了背景内容，则使阴影为0Dp
 */
@Composable
fun itemLayoutShadowElevation(
    dpValue: Dp = 1.dp,
    influencedByBackground: Boolean = true
): Dp {
    return influencedByBackground(
        value = dpValue,
        influenced = 0.dp,
        enabled = influencedByBackground
    )
}