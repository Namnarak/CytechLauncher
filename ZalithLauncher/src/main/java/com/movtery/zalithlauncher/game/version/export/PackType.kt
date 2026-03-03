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

package com.movtery.zalithlauncher.game.version.export

/**
 * 打包的类型
 */
enum class PackType(val options: PackEditOptions) {
    /**
     * MCBBS 导出格式
     */
    MCBBS(
        PackEditOptions(
            requireAuthor = true,
            requireJvmArgs = true,
            requireJavaArgs = true,
            requireWebsiteUrl = true,
            requireMinMemory = true,
        )
    ),

    /**
     * Modrinth 标准导出格式
     */
    Modrinth(
        PackEditOptions(
            requirePackRemote = true,
        )
    ),

    /**
     * MultiMC 导出格式
     */
    MultiMC(
        PackEditOptions(
            requireAuthor = true,
            requireMinMemory = true,
            requireMaxMemory = true,
        )
    ),
}

data class PackEditOptions(
    val requireAuthor: Boolean = false,
    val requireJvmArgs: Boolean = false,
    val requireJavaArgs: Boolean = false,
    val requireWebsiteUrl: Boolean = false,
    val requireMinMemory: Boolean = false,
    val requireMaxMemory: Boolean = false,
    val requirePackRemote: Boolean = false
)