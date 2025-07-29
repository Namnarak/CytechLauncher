package com.movtery.zalithlauncher.ui.screens.content.download.common

import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadVersionInfo

/** 整合包安装状态操作 */
sealed interface ModPackInstallOperation {
    data object None : ModPackInstallOperation
    /** 警告整合包的兼容性，同意后将进行安装 */
    data class Warning(val info: DownloadVersionInfo) : ModPackInstallOperation
    /** 开始安装 */
    data class Install(val info: DownloadVersionInfo) : ModPackInstallOperation
    /** 警告通知权限，可以无视，并直接开始安装 */
    data class WarningForNotification(val info: DownloadVersionInfo) : ModPackInstallOperation
    /** 整合包安装出现异常 */
    data class Error(val th: Throwable) : ModPackInstallOperation
    /** 整合包已成功安装 */
    data object Success : ModPackInstallOperation
}