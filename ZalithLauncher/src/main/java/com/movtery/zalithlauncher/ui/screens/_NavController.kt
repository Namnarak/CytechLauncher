package com.movtery.zalithlauncher.ui.screens

import androidx.navigation.NavController
import com.movtery.zalithlauncher.ui.screens.content.DOWNLOAD_SCREEN_TAG
import com.movtery.zalithlauncher.ui.screens.content.FILE_SELECTOR_SCREEN_TAG
import com.movtery.zalithlauncher.ui.screens.content.WEB_VIEW_SCREEN_TAG

fun NavController.navigateOnce(screenTag: String) {
    if (screenTag == currentDestination?.route) return //防止反复加载
    navigate(screenTag) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavController.navigateTo(screenTag: String, startWith: Boolean = false) {
    if (
        screenTag == currentDestination?.route ||
        startWith && currentDestination?.route?.startsWith(screenTag) == true
    ) return //防止反复加载
    navigate(screenTag)
}

/**
 * 导航至WebViewScreen并访问特定网址
 */
fun NavController.navigateToWeb(webUrl: String) = this.navigateTo("$WEB_VIEW_SCREEN_TAG$webUrl", true)

/**
 * 导航至FileSelectorScreen
 */
fun NavController.navigateToFileSelector(
    startPath: String,
    selectFile: Boolean,
    saveTag: String
) = this.navigateTo("${FILE_SELECTOR_SCREEN_TAG}startPath=$startPath&saveTag=$saveTag&selectFile=$selectFile", true)

/**
 * 导航至DownloadScreen
 */
fun NavController.navigateToDownload(targetScreen: String? = null) {
    val route = if (targetScreen != null) {
        "${DOWNLOAD_SCREEN_TAG}?targetScreen=$targetScreen"
    } else {
        DOWNLOAD_SCREEN_TAG
    }
    this.navigateTo(route, true)
}