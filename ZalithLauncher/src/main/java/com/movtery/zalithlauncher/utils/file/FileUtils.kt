package com.movtery.zalithlauncher.utils.file

import android.annotation.SuppressLint
import android.util.Log
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.compareChar
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.FileInputStream

fun compareSHA1(file: File, sourceSHA: String?, default: Boolean = true): Boolean {
    val computedSHA = runCatching {
        FileInputStream(file).use { fis ->
            String(Hex.encodeHex(DigestUtils.sha1(fis)))
        }
    }.getOrElse { e ->
        Log.i("CompareSHA1", "An exception occurred while reading, returning the default value.", e)
        return default
    }

    return sourceSHA?.equals(computedSHA, ignoreCase = true) ?: default
}

@SuppressLint("DefaultLocale")
fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB")
    var unitIndex = 0
    var value = bytes.toDouble()
    //循环获取合适的单位
    while (value >= 1024 && unitIndex < units.size - 1) {
        value /= 1024.0
        unitIndex++
    }
    return String.format("%.2f %s", value, units[unitIndex])
}

fun sortWithFileName(o1: File, o2: File): Int {
    val isDir1 = o1.isDirectory
    val isDir2 = o2.isDirectory

    //目录排在前面，文件排在后面
    if (isDir1 && !isDir2) return -1
    if (!isDir1 && isDir2) return 1

    return compareChar(o1.name, o2.name)
}

const val INVALID_CHARACTERS_REGEX = "[\\\\/:*?\"<>|\\t\\n]"

@Throws(InvalidFilenameException::class)
fun checkFilenameValidity(str: String) {
    val illegalCharsRegex = INVALID_CHARACTERS_REGEX.toRegex()

    val illegalChars = illegalCharsRegex.findAll(str)
        .map { it.value }
        .distinct()
        .joinToString("")

    if (illegalChars.isNotEmpty()) {
        throw InvalidFilenameException("The filename contains illegal characters", illegalChars)
    }

    if (str.length > 255) {
        throw InvalidFilenameException("Invalid filename length", str.length)
    }
}