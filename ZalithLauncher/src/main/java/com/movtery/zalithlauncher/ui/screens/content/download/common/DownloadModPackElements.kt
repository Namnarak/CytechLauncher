package com.movtery.zalithlauncher.ui.screens.content.download.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadVersionInfo
import com.movtery.zalithlauncher.ui.screens.content.download.assets.search.SearchModPackScreenKey

/**
 * 下载整合包屏幕堆栈
 */
val downloadModPackBackStack = mutableStateListOf<NavKey>(SearchModPackScreenKey)

/**
 * 状态：下载整合包子屏幕标签
 */
var downloadModPackScreenKey by mutableStateOf<NavKey?>(null)

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