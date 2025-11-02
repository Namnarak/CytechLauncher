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

import com.movtery.zalithlauncher.game.download.game.GameDownloadInfo

/** 游戏安装状态操作 */
sealed interface GameInstallOperation {
    data object None : GameInstallOperation
    /** 开始安装 */
    data class Install(val info: GameDownloadInfo) : GameInstallOperation
    /** 警告通知权限，可以无视，并直接开始安装 */
    data class WarningForNotification(val info: GameDownloadInfo) : GameInstallOperation
    /** 游戏安装出现异常 */
    data class Error(val th: Throwable) : GameInstallOperation
    /** 游戏已成功安装 */
    data object Success : GameInstallOperation
}
