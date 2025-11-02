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

package com.movtery.zalithlauncher.ui.screens.content.download.common

import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.modpack.install.ModPackInfo

/** 整合包安装状态操作 */
sealed interface ModPackInstallOperation {
    data object None : ModPackInstallOperation
    /** 警告整合包的兼容性，同意后将进行安装 */
    data class Warning(val version: PlatformVersion, val iconUrl: String?) : ModPackInstallOperation
    /** 开始安装 */
    data class Install(val version: PlatformVersion, val iconUrl: String?) : ModPackInstallOperation
    /** 警告通知权限，可以无视，并直接开始安装 */
    data class WarningForNotification(val version: PlatformVersion, val iconUrl: String?) : ModPackInstallOperation
    /** 整合包安装出现异常 */
    data class Error(val th: Throwable) : ModPackInstallOperation
    /** 整合包已成功安装 */
    data object Success : ModPackInstallOperation
}

/** 整合包版本名称自定义状态操作 */
sealed interface VersionNameOperation {
    data object None : VersionNameOperation
    /** 等待用户输入版本名称 */
    data class Waiting(val info: ModPackInfo) : VersionNameOperation
}