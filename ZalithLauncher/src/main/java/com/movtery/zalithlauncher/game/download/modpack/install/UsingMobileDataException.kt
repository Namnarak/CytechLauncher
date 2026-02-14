package com.movtery.zalithlauncher.game.download.modpack.install

/**
 * 用户拒绝使用移动网络安装整合包
 */
class UsingMobileDataException: RuntimeException(
    "Users do not want to install the modpack using mobile data."
)