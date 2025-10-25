package com.movtery.zalithlauncher.ui.screens

import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.game.version.installed.Version
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * 嵌套NavDisplay的屏幕
 */
sealed interface NestedNavKey {
    /** 启动屏幕 */
    @Serializable class Splash() : BackStackNavKey<NavKey>()
    /** 主屏幕 */
    @Serializable class Main() : BackStackNavKey<NavKey>()
    /** 设置屏幕 */
    @Serializable class Settings() : BackStackNavKey<NavKey>()
    /** 版本详细设置屏幕 */
    @Serializable
    class VersionSettings(@Contextual val version: Version) : BackStackNavKey<NavKey>() {
        init {
            backStack.addIfEmpty(NormalNavKey.Versions.OverView)
        }
    }
    /** 下载屏幕 */
    @Serializable class Download() : BackStackNavKey<NavKey>()

    //下载嵌套子屏幕
    /** 下载游戏屏幕 */
    @Serializable class DownloadGame() : BackStackNavKey<NavKey>()
    /** 下载整合包屏幕 */
    @Serializable class DownloadModPack() : BackStackNavKey<NavKey>()
    /** 下载模组屏幕 */
    @Serializable class DownloadMod() : BackStackNavKey<NavKey>()
    /** 下载资源包屏幕 */
    @Serializable class DownloadResourcePack() : BackStackNavKey<NavKey>()
    /** 下载存档屏幕 */
    @Serializable class DownloadSaves() : BackStackNavKey<NavKey>()
    /** 下载光影包屏幕 */
    @Serializable class DownloadShaders() : BackStackNavKey<NavKey>()
}