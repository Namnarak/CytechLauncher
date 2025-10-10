package com.movtery.zalithlauncher.utils.platform

import android.app.ActivityManager
import android.content.Context
import androidx.annotation.WorkerThread
import com.movtery.zalithlauncher.utils.device.Architecture
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.round

private const val BYTES_PER_MB = 1024L * 1024

private inline val Context.activityManager: ActivityManager
    get() = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

private fun getMemoryInfo(context: Context): ActivityManager.MemoryInfo {
    return ActivityManager.MemoryInfo().apply {
        context.activityManager.getMemoryInfo(this)
    }
}

/**
 * 获取系统总内存（单位：字节）
 */
@WorkerThread
fun getTotalMemory(context: Context) = getMemoryInfo(context).totalMem

/**
 * 获取已使用内存（单位：字节）
 */
@WorkerThread
fun getUsedMemory(context: Context): Long {
    val info = getMemoryInfo(context)
    return info.totalMem - info.availMem
}

/**
 * 获取当前可用内存（单位：字节）
 */
@WorkerThread
fun getFreeMemory(context: Context) = getMemoryInfo(context).availMem

/**
 * 为设置项获取最大可设置的内存值（为系统预留一些可用内存）
 */
@WorkerThread
fun getMaxMemoryForSettings(context: Context): Int {
    val deviceRam = getTotalMemory(context).bytesToMB()
    val maxRam: Int = if (Architecture.is32BitsDevice || deviceRam < 2048) {
        min(1024.0, deviceRam).toInt()
    } else {
        //To have a minimum for the device to breathe
        (deviceRam - (if (deviceRam < 3064) 800 else 1024)).toInt()
    }
    return maxRam
}

/**
 * 转换为 MB 单位
 */
fun Long.bytesToMB(decimals: Int = 2, roundDown: Boolean = false): Double {
    val megaBytes = this.toDouble() / BYTES_PER_MB
    return if (decimals == 0) {
        if (roundDown) floor(megaBytes) else round(megaBytes)
    } else {
        val roundingMode = if (roundDown) RoundingMode.DOWN else RoundingMode.HALF_UP
        BigDecimal(megaBytes).setScale(decimals, roundingMode).toDouble()
    }
}