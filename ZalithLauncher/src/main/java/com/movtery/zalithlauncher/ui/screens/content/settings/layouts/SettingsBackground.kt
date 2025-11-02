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

package com.movtery.zalithlauncher.ui.screens.content.settings.layouts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.ui.components.BackgroundCard

@Composable
fun SettingsBackground(
    modifier: Modifier = Modifier,
    content: @Composable SettingsLayoutScope.() -> Unit
) {
    SettingsBackground(
        modifier = modifier,
        contentPadding = 8.dp,
        content = content
    )
}

@Composable
fun SettingsBackground(
    modifier: Modifier = Modifier,
    contentPadding: Dp,
    content: @Composable SettingsLayoutScope.() -> Unit
) {
    BackgroundCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        SettingsLayout(contentPadding, content)
    }
}

@Composable
fun SettingsBackground(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable SettingsLayoutScope.() -> Unit
) {
    SettingsBackground(
        modifier = modifier,
        contentPadding = 8.dp,
        onClick = onClick,
        content = content
    )
}

@Composable
fun SettingsBackground(
    modifier: Modifier = Modifier,
    contentPadding: Dp,
    onClick: () -> Unit,
    content: @Composable SettingsLayoutScope.() -> Unit
) {
    BackgroundCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        onClick = onClick
    ) {
        SettingsLayout(contentPadding, content)
    }
}

@Composable
private fun SettingsLayout(
    contentPadding: Dp,
    content: @Composable SettingsLayoutScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .clip(shape = MaterialTheme.shapes.large)
            .padding(contentPadding)
    ) {
        val scope = remember { SettingsLayoutScope() }

        with(scope) {
            content()
        }
    }
}