package com.movtery.zalithlauncher.ui.screens

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.version.installed.Version
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * 普通的屏幕
 */
sealed interface NormalNavKey : NavKey {
    /** 启动器主页屏幕 */
    @Serializable data object LauncherMain : NormalNavKey
    /** 账号管理屏幕 */
    @Serializable data object AccountManager : NormalNavKey
    /** Web屏幕 */
    @Serializable data class WebScreen(val url: String) : NormalNavKey
    /** 版本管理屏幕 */
    @Serializable data object VersionsManager : NormalNavKey
    /** 文件选择屏幕 */
    @Serializable data class FileSelector(
        val startPath: String,
        val selectFile: Boolean,
        val saveKey: NavKey
    ) : NormalNavKey

    /** 设置嵌套子屏幕 */
    sealed interface Settings : NormalNavKey {
        /** 渲染器设置屏幕 */
        @Serializable data object Renderer : Settings
        /** 游戏设置屏幕 */
        @Serializable data object Game : Settings
        /** 控制设置屏幕 */
        @Serializable data object Control : Settings
        /** 启动器设置屏幕 */
        @Serializable data object Launcher : Settings
        /** Java管理屏幕 */
        @Serializable data object JavaManager : Settings
        /** 控制管理屏幕 */
        @Serializable data object ControlManager : Settings
        /** 关于屏幕 */
        @Serializable data object AboutInfo : Settings
    }

    /** 版本详细设置嵌套子屏幕 */
    sealed interface Versions : NormalNavKey {
        /** 版本概览屏幕 */
        @Serializable data object OverView : Versions
        /** 版本配置屏幕 */
        @Serializable data object Config : Versions
        /** 存档管理屏幕 */
        @Serializable data object SavesManager : Versions
        /** 资源包管理屏幕 */
        @Serializable data object ResourcePackManager : Versions
    }

    /** 下载游戏嵌套子屏幕 */
    sealed interface DownloadGame : NormalNavKey {
        /** 选择游戏版本屏幕 */
        @Serializable data object SelectGameVersion : Versions
        /** 选择附加内容屏幕 */
        @Serializable data class Addons(val gameVersion: String) : Versions
    }

    /** 搜索整合包屏幕 */
    @Serializable data object SearchModPack : NormalNavKey
    /** 搜索模组屏幕 */
    @Serializable data object SearchMod : NormalNavKey
    /** 搜索资源包屏幕 */
    @Serializable data object SearchResourcePack : NormalNavKey
    /** 搜索存档屏幕 */
    @Serializable data object SearchSaves : NormalNavKey
    /** 搜索光影包屏幕 */
    @Serializable data object SearchShaders : NormalNavKey

    /** 下载资源屏幕 */
    @Serializable data class DownloadAssets(
        val platform: Platform,
        val projectId: String,
        val classes: PlatformClasses,
        val iconUrl: String? = null
    ) : NormalNavKey
}

/**
 * 嵌套NavDisplay的屏幕
 */
sealed interface NestedNavKey : NavKey {
    /** 当前屏幕正在使用的堆栈 */
    val backStack: NavBackStack

    /** 设置屏幕 */
    @Serializable data class Settings(@Contextual override val backStack: NavBackStack) : NestedNavKey
    /** 版本详细设置屏幕 */
    @Serializable data class Versions(
        @Contextual override val backStack: NavBackStack,
        @Contextual val version: Version
    ) : NestedNavKey
    /** 下载屏幕 */
    @Serializable data class Download(@Contextual override val backStack: NavBackStack) : NestedNavKey

    //下载嵌套子屏幕
    /** 下载游戏屏幕 */
    @Serializable data class DownloadGame(@Contextual override val backStack: NavBackStack) : NestedNavKey
    /** 下载整合包屏幕 */
    @Serializable data class DownloadModPack(@Contextual override val backStack: NavBackStack) : NestedNavKey
    /** 下载模组屏幕 */
    @Serializable data class DownloadMod(@Contextual override val backStack: NavBackStack) : NestedNavKey
    /** 下载资源包屏幕 */
    @Serializable data class DownloadResourcePack(@Contextual override val backStack: NavBackStack) : NestedNavKey
    /** 下载存档屏幕 */
    @Serializable data class DownloadSaves(@Contextual override val backStack: NavBackStack) : NestedNavKey
    /** 下载光影包屏幕 */
    @Serializable data class DownloadShaders(@Contextual override val backStack: NavBackStack) : NestedNavKey
}

/**
 * 兼容嵌套NavDisplay的返回事件处理
 */
fun onBack(currentBackStack: NavBackStack) {
    val key = currentBackStack.lastOrNull()
    when (key) {
        //普通的屏幕，直接退出当前堆栈的上层
        is NormalNavKey -> currentBackStack.removeLastOrNull()
        is NestedNavKey -> {
            if (key.backStack.size <= 1) {
                //嵌套屏幕的堆栈处于最后一个屏幕的状态
                //可以退出当前堆栈的上层了
                currentBackStack.removeLastOrNull()
            } else {
                //退出子堆栈的上层屏幕
                key.backStack.removeLastOrNull()
            }
        }
    }
}

fun NavBackStack.navigateOnce(key: NavKey) {
    if (key == lastOrNull()) return //防止反复加载
    clearWith(key)
}

fun NavBackStack.navigateTo(key: NavKey) {
    if (key == lastOrNull()) return //防止反复加载
    add(key)
}

fun NavBackStack.navigateTo(screenKey: NavKey, useClassEquality: Boolean = false) {
    if (useClassEquality) {
        val current = lastOrNull()
        if (current != null && screenKey::class == current::class) return //防止反复加载
        add(screenKey)
    } else {
        navigateTo(screenKey)
    }
}

/**
 * 清除所有栈，并加入指定的key
 */
fun NavBackStack.clearWith(navKey: NavKey) {
    //批量替换内容，避免 Nav3 看到空帧
    this.apply {
        clear()
        add(navKey)
    }
}