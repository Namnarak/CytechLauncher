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

package com.movtery.zalithlauncher.game.download.modpack.install

import com.movtery.zalithlauncher.game.addons.modloader.ModLoader

/**
 * 整合包信息
 * @param name 整合包名称
 * @param summary 整合包的简介（可用到版本描述上）
 * @param files 整合包所有需要下载的模组
 * @param loaders 整合包需要安装的模组加载器
 * @param gameVersion 整合包需要的游戏版本
 */
data class ModPackInfo(
    val name: String,
    val summary: String? = null,
    val files: List<ModFile>,
    val loaders: List<Pair<ModLoader, String>>,
    val gameVersion: String
)
