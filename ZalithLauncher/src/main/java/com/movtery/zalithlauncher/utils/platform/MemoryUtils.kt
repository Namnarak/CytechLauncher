package com.movtery.zalithlauncher.utils.platform

import android.app.ActivityManager
import android.content.Context
import androidx.annotation.WorkerThread
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.floor
import kotlin.math.round

object MemoryUtils {
    private inline val Context.activityManager: ActivityManager
        get() = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    private inline fun getMemoryInfo(context: Context): ActivityManager.MemoryInfo {
        return ActivityManager.MemoryInfo().apply {
            context.activityManager.getMemoryInfo(this)
        }
    }

    /**
     * 获取系统总内存（单位：字节）
     */
    @WorkerThread
    @JvmStatic
    fun getTotalMemory(context: Context) = getMemoryInfo(context).totalMem

    /**
     * 获取已使用内存（单位：字节）
     */
    @WorkerThread
    @JvmStatic
    fun getUsedMemory(context: Context): Long {
        val info = getMemoryInfo(context)
        return info.totalMem - info.availMem
    }

    /**
     * 获取当前可用内存（单位：字节）
     */
    @WorkerThread
    @JvmStatic
    fun getFreeMemory(context: Context) = getMemoryInfo(context).availMem
}

private const val BYTES_PER_MB = 1024L * 1024

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