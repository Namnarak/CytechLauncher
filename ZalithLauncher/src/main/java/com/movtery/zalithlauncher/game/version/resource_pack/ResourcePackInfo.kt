package com.movtery.zalithlauncher.game.version.resource_pack

import com.movtery.zalithlauncher.utils.string.stripColorCodes
import java.io.File

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