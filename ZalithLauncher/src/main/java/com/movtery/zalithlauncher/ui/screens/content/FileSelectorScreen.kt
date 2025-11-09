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

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.state.FilePathSelectorData
import com.movtery.zalithlauncher.state.MutableStates
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.itemLayoutColor
import com.movtery.zalithlauncher.ui.components.itemLayoutShadowElevation
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.BaseFileItem
import com.movtery.zalithlauncher.ui.screens.content.elements.CreateNewDirDialog
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.file.sortWithFileName
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import java.io.File

/**
 * 导航至FileSelectorScreen
 */
fun NavBackStack<NavKey>.navigateToFileSelector(
    startPath: String,
    selectFile: Boolean,
    saveKey: NavKey
) = this.navigateTo(
    screenKey = NormalNavKey.FileSelector(startPath, selectFile, saveKey),
    useClassEquality = true
)

@Composable
fun FileSelectorScreen(
    key: NormalNavKey.FileSelector,
    backScreenViewModel: ScreenBackStackViewModel,
    back: () -> Unit
) {
    val startPath1 = File(key.startPath)
    var currentPath by rememberSaveable { mutableStateOf(startPath1) }

    BaseScreen(
        screenKey = key,
        currentKey = backScreenViewModel.mainScreen.currentKey,
        useClassEquality = true
    ) { isVisible ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopPathLayout(
                isVisible = isVisible,
                currentPath = currentPath.absolutePath,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            )

            var createDir by remember { mutableStateOf(false) }
            if (createDir) {
                CreateNewDirDialog(
                    onDismissRequest = { createDir = false },
                    createDir = {
                        val newDir = File(currentPath, it)
                        if (newDir.mkdir()) currentPath = newDir
                        createDir = false
                    }
                )
            }

            Row(
                modifier = Modifier
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LeftActionMenu(
                    isVisible = isVisible,
                    backEnabled = currentPath.absolutePath != startPath1.absolutePath,
                    backToParent = {
                        currentPath = currentPath.parentFile!!
                    },
                    createDir = { createDir = true },
                    selectDir = {
                        MutableStates.filePathSelector = FilePathSelectorData(key.saveKey, currentPath.absolutePath)
                        back()
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(2.5f)
                )

                FilesLayout(
                    isVisible = isVisible,
                    currentPath = currentPath,
                    updatePath = { currentPath = it },
                    selectFile = key.selectFile,
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(7.5f)
                        .padding(start = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun TopPathLayout(
    isVisible: Boolean,
    currentPath: String,
    modifier: Modifier = Modifier,
    color: Color
) {
    val surfaceYOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )

    Surface(
        modifier = modifier
            .offset {
                IntOffset(
                    x = 0,
                    y = surfaceYOffset.roundToPx()
                )
            },
        shape = MaterialTheme.shapes.extraLarge,
        color = color
    ) {
        Row(
            modifier = Modifier.padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp))
        ) {
            Text(
                text = stringResource(R.string.files_current_path, currentPath),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun LeftActionMenu(
    isVisible: Boolean,
    backEnabled: Boolean,
    backToParent: () -> Unit,
    selectDir: () -> Unit,
    createDir: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surfaceXOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    BackgroundCard(
        modifier = modifier.offset { IntOffset(x = surfaceXOffset.roundToPx(), y = 0) },
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            ScalingActionButton(
                enabled = backEnabled,
                modifier = Modifier.fillMaxWidth(),
                onClick = backToParent
            ) {
                MarqueeText(text = stringResource(R.string.files_back_to_parent))
            }
            ScalingActionButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = createDir
            ) {
                MarqueeText(text = stringResource(R.string.files_create_dir))
            }
            ScalingActionButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = selectDir
            ) {
                MarqueeText(text = stringResource(R.string.files_select_dir))
            }
        }
    }
}

@Composable
private fun FilesLayout(
    isVisible: Boolean,
    currentPath: File,
    updatePath: (File) -> Unit,
    selectFile: Boolean,
    modifier: Modifier = Modifier
) {
    val surfaceXOffset by swapAnimateDpAsState(
        targetValue = 40.dp,
        swapIn = isVisible,
        isHorizontal = true
    )

    BackgroundCard(
        modifier = modifier.offset { IntOffset(x = surfaceXOffset.roundToPx(), y = 0) },
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            currentPath.listFiles()?.toList()?.filter {
                //如果为非选择文件模式，则仅展示文件夹目录
                if (!selectFile) it.isDirectory else true
            }?.sortedWith { o1, o2 ->
                sortWithFileName(o1, o2)
            }?.takeIf {
                it.isNotEmpty()
            }?.let { files ->
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    items(files) { file ->
                        FileItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            file = file,
                            onClick = {
                                if (!selectFile && file.isDirectory) {
                                    updatePath(file)
                                }
                            }
                        )
                    }
                }
            } ?: run {
                ScalingLabel(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.files_no_selectable_content)
                )
            }
        }
    }
}

@Composable
private fun FileItem(
    modifier: Modifier = Modifier,
    file: File,
    onClick: () -> Unit = {},
    color: Color = itemLayoutColor(),
    shadowElevation: Dp = itemLayoutShadowElevation()
) {
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }
    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        color = color,
        shape = MaterialTheme.shapes.large,
        shadowElevation = shadowElevation,
        onClick = onClick
    ) {
        BaseFileItem(
            file = file,
            modifier = Modifier.padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp))
        )
    }
}