package com.movtery.zalithlauncher.utils.logging

import android.content.Context
import android.util.Log
import com.movtery.zalithlauncher.path.PathManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.DeflaterOutputStream
import kotlin.coroutines.CoroutineContext

/**
 * [Modified from HMCL](https://github.com/HMCL-dev/HMCL/blob/57018bef47417108b75e2298ab61f89a7586b1b9/HMCLCore/src/main/java/org/jackhuang/hmcl/util/logging/Logger.java)
 */
object Logger : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default + Job()

    private lateinit var PACKAGE_PREFIX: String
    private val isInitialized = AtomicBoolean(false)
    private val channel = Channel<LogEvent>(Channel.UNLIMITED)

    private var logRetentionDays = 7
    private var currentLogFile: File? = null
    private var logWriter: PrintWriter? = null
    private var inMemoryLogs: ByteArrayOutputStream? = null

    /**
     * 初始化日志
     * @param retentionDays 日志保留天数
     */
    fun initialize(context: Context, retentionDays: Int = 7) {
        PACKAGE_PREFIX = "${context.packageName}."

        if (!isInitialized.compareAndSet(false, true)) return

        logRetentionDays = retentionDays.coerceAtLeast(0)

        launch(Dispatchers.IO) {
            setupLogWriter()
            processEvents()
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            runBlocking { shutdown() }
        })
    }

    private suspend fun setupLogWriter() = withContext(Dispatchers.IO) {
        try {
            currentLogFile = createLogFile()
            logWriter = PrintWriter(currentLogFile!!.writer())
        } catch (e: IOException) {
            logInternal("Logger.setupLogWriter", Level.WARNING, "Failed to create log file", e)
            inMemoryLogs = ByteArrayOutputStream(1024 * 1024) // 1MB buffer
            logWriter = PrintWriter(inMemoryLogs!!)
        }
    }

    private fun createLogFile(): File {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss", Locale.US)
        var file: File
        var counter = 0

        do {
            val suffix = if (counter == 0) "" else ".$counter"
            file = File(PathManager.DIR_LAUNCHER_LOGS, "log_${dateFormat.format(Date())}$suffix.log")
            counter++
        } while (file.exists())

        file.createNewFile()
        return file
    }

    private suspend fun processEvents() = withContext(Dispatchers.IO) {
        for (event in channel) {
            when (event) {
                is LogEvent.LogMessage -> handleLogMessage(event)
                is LogEvent.ExportLog -> handleExport(event)
                LogEvent.Shutdown -> {
                    cleanup()
                    return@withContext
                }
            }
        }
    }

    private fun handleLogMessage(event: LogEvent.LogMessage) {
        val formatted = formatMessage(event)

        //输出到 Logcat
        when (event.level) {
            Level.ERROR -> Log.e("AppLog", formatted)
            Level.WARNING -> Log.w("AppLog", formatted)
            Level.INFO -> Log.i("AppLog", formatted)
            Level.DEBUG -> Log.d("AppLog", formatted)
            Level.TRACE -> Log.v("AppLog", formatted)
        }

        logWriter?.apply {
            println(formatted)
            event.throwable?.printStackTrace(this)
            flush()
        }
    }

    private fun formatMessage(event: LogEvent.LogMessage): String {
        val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(event.time)
        val caller = event.caller?.let {
            if (it.startsWith(PACKAGE_PREFIX)) "~${it.substring(PACKAGE_PREFIX.length)}" else it
        } ?: "Unknown"

        return buildString {
            append("[$time] [")
            append(caller)
            append("/")
            append(event.level.name)
            append("] ")
            append(event.message)
        }
    }

    private fun handleExport(event: LogEvent.ExportLog) {
        try {
            logWriter?.flush()
            when {
                currentLogFile != null -> {
                    currentLogFile!!.inputStream().use { input ->
                        event.output.use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                inMemoryLogs != null -> {
                    inMemoryLogs!!.writeTo(event.output)
                }
            }
        } catch (e: IOException) {
            logInternal("Logger.exportLogs", Level.WARNING, "Export failed", e)
        } finally {
            event.latch.countDown()
        }
    }

    /**
     * 导出日志
     */
    suspend fun exportLogs(output: OutputStream) = withContext(Dispatchers.IO) {
        val event = LogEvent.ExportLog(output)
        channel.send(event)
        event.latch.await()
    }

    private suspend fun shutdown() {
        channel.send(LogEvent.Shutdown)
        (coroutineContext[Job]!!).join()
    }

    private fun cleanup() {
        logWriter?.close()
        performLogRotation()
        deleteOldLogs()
    }

    private fun performLogRotation() {
        currentLogFile?.let { file ->
            try {
                val compressedFile = File(file.parent, "${file.name}.zip")
                file.inputStream().use { input ->
                    DeflaterOutputStream(compressedFile.outputStream()).use { output ->
                        input.copyTo(output)
                    }
                }
                file.delete()
            } catch (e: IOException) {
                logInternal("Logger.compressLogs", Level.WARNING, "Log compression failed", e)
            }
        }
    }

    private fun deleteOldLogs() {
        PathManager.DIR_LAUNCHER_LOGS.listFiles()?.let { files ->
            val cutoff = System.currentTimeMillis() - logRetentionDays * 86400000L
            files.filter {
                //过滤出日期超过指定天数的日志文件
                it.lastModified() < cutoff
            }.forEach {
                it.delete()
            }
        }
    }

    fun lError(msg: String, t: Throwable? = null) =
        log(Level.ERROR, findCaller(), msg, t)

    fun lWarning(msg: String, t: Throwable? = null) =
        log(Level.WARNING, findCaller(), msg, t)

    fun lInfo(msg: String, t: Throwable? = null) =
        log(Level.INFO, findCaller(), msg, t)

    fun lDebug(msg: String, t: Throwable? = null) =
        log(Level.DEBUG, findCaller(), msg, t)

    fun lTrace(msg: String, t: Throwable? = null) =
        log(Level.TRACE, findCaller(), msg, t)

    /**
     * 输出日志
     */
    fun log(level: Level, caller: String?, message: String, throwable: Throwable? = null) {
        if (!isInitialized.get()) return

        val event = LogEvent.LogMessage(
            time = System.currentTimeMillis(),
            caller = caller,
            level = level,
            message = message,
            throwable = throwable
        )

        launch {
            channel.send(event)
        }
    }

    /**
     * 找到调用者
     */
    private fun findCaller(): String? {
        return Throwable().stackTrace.firstOrNull { element ->
            element.className != this::class.java.name &&
                    !element.className.startsWith("kotlin.coroutines") &&
                    !element.className.startsWith("kotlinx.coroutines")
        }?.let {
            val className = it.className.substringAfterLast('.')
            "$className.${it.methodName}"
        }
    }

    private fun logInternal(caller: String, level: Level, message: String, t: Throwable?) {
        val event = LogEvent.LogMessage(
            System.currentTimeMillis(),
            caller,
            level,
            message,
            t
        )
        runBlocking { channel.send(event) }
    }
}