package com.movtery.zalithlauncher.utils

import android.util.Log
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
