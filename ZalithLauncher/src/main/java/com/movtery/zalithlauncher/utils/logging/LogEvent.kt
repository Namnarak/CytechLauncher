package com.movtery.zalithlauncher.utils.logging

import java.io.OutputStream
import java.util.concurrent.CountDownLatch

/**
 * [Modified from HMCL](https://github.com/HMCL-dev/HMCL/blob/57018be/HMCLCore/src/main/java/org/jackhuang/hmcl/util/logging/LogEvent.java)
 */
sealed class LogEvent {
    data class LogMessage(
        val time: Long,
        val caller: String?,
        val level: Level,
        val message: String,
        val throwable: Throwable?
    ) : LogEvent()

    data class ExportLog(
        val output: OutputStream,
        val latch: CountDownLatch = CountDownLatch(1)
    ) : LogEvent()

    data object Shutdown : LogEvent()
}