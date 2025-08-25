package com.movtery.zalithlauncher.ui.screens

import com.movtery.zalithlauncher.game.version.installed.Version
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * 嵌套NavDisplay的屏幕
 */
sealed interface NestedNavKey {
    /** 主屏幕 */
    @Serializable class Main() : BackStackNavKey()
    /** 设置屏幕 */
    @Serializable class Settings() : BackStackNavKey()
    /** 版本详细设置屏幕 */
    @Serializable
    class VersionSettings(@Contextual val version: Version) : BackStackNavKey() {
        init {
            backStack.addIfEmpty(NormalNavKey.Versions.OverView)
        }
    }
    /** 下载屏幕 */
    @Serializable class Download() : BackStackNavKey()

    //下载嵌套子屏幕
    /** 下载游戏屏幕 */
    @Serializable class DownloadGame() : BackStackNavKey()
    /** 下载整合包屏幕 */
    @Serializable class DownloadModPack() : BackStackNavKey()
    /** 下载模组屏幕 */
    @Serializable class DownloadMod() : BackStackNavKey()
    /** 下载资源包屏幕 */
    @Serializable class DownloadResourcePack() : BackStackNavKey()
    /** 下载存档屏幕 */
    @Serializable class DownloadSaves() : BackStackNavKey()
    /** 下载光影包屏幕 */
    @Serializable class DownloadShaders() : BackStackNavKey()
}