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

package com.movtery.zalithlauncher.ui.screens.content.versions.elements

import com.movtery.zalithlauncher.game.version.mod.meta.PackMcMeta
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import com.movtery.zalithlauncher.utils.string.stripColorCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.zip.ZipFile

/** 资源包操作状态 */
sealed interface ResourcePackOperation {
    data object None : ResourcePackOperation
    /** 执行任务中 */
    data object Progress : ResourcePackOperation
    /** 重命名资源包输入对话框 */
    data class RenamePack(val packInfo: ResourcePackInfo) : ResourcePackOperation
    /** 删除资源包输入对话框 */
    data class DeletePack(val packInfo: ResourcePackInfo) : ResourcePackOperation
}

/**
 * 简易的资源包过滤器
 */
data class ResourcePackFilter(
    val onlyShowValid: Boolean,
    val filterName: String
)

/**
 * 简易过滤器，过滤指定名称的资源包
 */
fun List<ResourcePackInfo>.filterPacks(filter: ResourcePackFilter) = this.filter {
    val valid = !filter.onlyShowValid || it.isValid
    val nameMatched = filter.filterName.isEmpty() ||
            //用清除了格式化代码的名称进行判断
            it.rawName.contains(filter.filterName, true)
    valid && nameMatched
}

/**
 * 资源包信息类
 */
data class ResourcePackInfo(
    /** 资源包文件 */
    val file: File,
    /** 提前计算好的文件大小（文件夹形式的资源包不计算文件大小） */
    val fileSize: Long? = null,
    /** 清除颜色替换符后的文件名 */
    val rawName: String = file.name.stripColorCodes(),
    /** 显示名称（如果是压缩包类型的资源包，将被去掉扩展名） */
    val displayName: String = if (file.isDirectory) file.name else file.nameWithoutExtension,
    /** 资源包是否有效 */
    val isValid: Boolean,
    /** 资源包的描述信息 */
    val description: String?,
    /** 资源包的格式版本 */
    val packFormat: Int?,
    /** 资源包的图标 */
    val icon: ByteArray?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResourcePackInfo

        if (isValid != other.isValid) return false
        if (packFormat != other.packFormat) return false
        if (file != other.file) return false
        if (description != other.description) return false
        if (icon != null) {
            if (other.icon == null) return false
            if (!icon.contentEquals(other.icon)) return false
        } else if (other.icon != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isValid.hashCode()
        result = 31 * result + (packFormat ?: 0)
        result = 31 * result + file.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (icon?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * 解析资源包文件，游戏内仅支持加载文件夹、文件后缀为zip的资源包
 * @param file 资源包文件
 */
suspend fun parseResourcePack(file: File): ResourcePackInfo? = withContext(Dispatchers.IO) {
    runCatching {
        var isValid = false
        var metaContent: String? = null
        var iconBytes: ByteArray? = null
        var fileSize: Long? = null

        if (file.isDirectory) { //文件夹形式的资源包
            //资源包元数据
            File(file, "pack.mcmeta").takeIf { it.exists() }?.let { metaFile ->
                metaContent = metaFile.readText()
            }
            //尝试读取资源包的图标
            File(file, "pack.png").takeIf { it.exists() }?.let { iconFile ->
                iconBytes = iconFile.readBytes()
            }
        } else if (file.extension == "zip") { //压缩包形式的资源包
            //性能、速度考虑，仅压缩包形式的资源包可以计算文件大小
            fileSize = FileUtils.sizeOf(file)

            ZipFile(file).use { zip ->
                //资源包元数据
                zip.getEntry("pack.mcmeta")?.let { metaEntry ->
                    metaContent = zip.getInputStream(metaEntry).bufferedReader().readText()
                }
                //尝试读取资源包的图标
                zip.getEntry("pack.png")?.let { iconEntry ->
                    iconBytes = zip.getInputStream(iconEntry).readBytes()
                }
            }
        }

        val meta = metaContent?.let { content ->
            runCatching {
                GSON.fromJson(content, PackMcMeta::class.java)
            }.onFailure {
                lWarning("Failed to parse the resource package metadata: ${file.absolutePath}", it)
            }.getOrNull()
        }?.also {
            //解析成功，则代表其是一个有效的格式
            isValid = true
        }

        ResourcePackInfo(
            file = file,
            fileSize = fileSize,
            isValid = isValid,
            description = meta?.pack?.description?.toPlainText(),
            packFormat = meta?.pack?.packFormat,
            icon = iconBytes
        )
    }.onFailure {
        lWarning("Failed to parse the resource package: ${file.absolutePath}", it)
    }.getOrNull()
}