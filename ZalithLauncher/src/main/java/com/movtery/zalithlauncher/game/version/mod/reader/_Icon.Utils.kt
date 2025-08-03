package com.movtery.zalithlauncher.game.version.mod.reader

import java.util.zip.ZipFile

fun ZipFile.tryGetIcon(iconPath: String?): ByteArray? {
    if (iconPath.isNullOrBlank()) return null

    return try {
        getInputStream(getEntry(iconPath))?.readBytes()
    } catch (_: Exception) {
        null //忽略图标读取错误
    }
}