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

package com.movtery.zalithlauncher.ui.screens.content.versions.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.game.version.export.data.FileSelectionData
import kotlinx.coroutines.flow.StateFlow

sealed interface VisibleNode {
    data class FileNode(
        val data: FileSelectionData,
        private val indentation0: Int
    ) : VisibleNode {
        override val indentation: Int get() = indentation0
        override val key: String get() = data.file.absolutePath
    }

    /** 仅展示空目录提示文本 */
    data class EmptyNode(
        private val key0: String,
        private val indentation0: Int,
    ) : VisibleNode {
        override val indentation: Int get() = indentation0
        override val key: String get() = key0
    }

    val indentation: Int
    val key: String
}

@Composable
fun rememberVisibleNodes(
    list: List<FileSelectionData>
): List<VisibleNode> {
    val expandStates = list
        .flatMap { it.collectExpandStates() }
        .map { it.collectAsStateWithLifecycle() }

    return remember(list, expandStates.map { it.value }) {
        buildList {
            /**
             * 递归添加文件节点
             * @param indentation 缩进次数
             */
            fun addNodes(
                nodes: List<FileSelectionData>,
                indentation: Int
            ) {
                nodes.forEach { node ->
                    add(
                        VisibleNode.FileNode(node, indentation)
                    )

                    val child = node.child
                    if (child != null && node.expand.value) {
                        val indentation0 = indentation + 1
                        if (child.isNotEmpty()) {
                            addNodes(child, indentation0)
                        } else {
                            val key = "parent:" + node.file.absolutePath + ",indentation=" + indentation.toString()
                            add(
                                VisibleNode.EmptyNode(key,indentation0)
                            )
                        }
                    }
                }
            }

            addNodes(list, 0)
        }
    }
}

private fun FileSelectionData.collectExpandStates(): List<StateFlow<Boolean>> {
    val result = mutableListOf(expand)
    child?.forEach {
        result += it.collectExpandStates()
    }
    return result
}