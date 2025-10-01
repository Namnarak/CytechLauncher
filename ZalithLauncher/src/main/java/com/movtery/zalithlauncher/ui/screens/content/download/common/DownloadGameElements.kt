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
