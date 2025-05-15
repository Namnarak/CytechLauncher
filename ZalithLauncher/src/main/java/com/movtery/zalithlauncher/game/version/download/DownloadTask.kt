package com.movtery.zalithlauncher.game.version.download

import android.util.Log
import com.movtery.zalithlauncher.utils.file.compareSHA1
import com.movtery.zalithlauncher.utils.network.NetWorkUtils
import org.apache.commons.io.FileUtils
import java.io.File

class DownloadTask(
    val url: String,
    private val verifyIntegrity: Boolean,
    private val bufferSize: ByteArray,
    val targetFile: File,
    val sha1: String?,
    private val onDownloadFailed: (DownloadTask) -> Unit = {},
    private val onFileDownloadedSize: (Long) -> Unit = {},
    private val onFileDownloaded: () -> Unit = {}
) : Runnable {
    override fun run() {
        //若目标文件存在，验证通过或关闭完整性验证时，跳过此次下载
        if (verifySha1()) {
            downloadedSize(FileUtils.sizeOf(targetFile))
            downloadedFile()
            return
        }

        runCatching {
            NetWorkUtils.downloadFile(
                url,
                targetFile,
                bufferSize = bufferSize
            ) { size ->
                downloadedSize(size.toLong())
            }
            downloadedFile()
        }.onFailure { e ->
            Log.e(DOWNLOADER_TAG, "Download failed: ${targetFile.absolutePath}, url: $url", e)
            onDownloadFailed(this)
        }
    }

    private fun downloadedSize(size: Long) {
        onFileDownloadedSize(size)
    }

    private fun downloadedFile() {
        onFileDownloaded()
    }

    /**
     * 若目标文件存在，验证完整性
     * @return 是否跳过此次下载
     */
    private fun verifySha1(): Boolean {
        if (targetFile.exists()) {
            sha1 ?: return !verifyIntegrity
            if (!verifyIntegrity || compareSHA1(targetFile, sha1)) {
                return true
            } else {
                FileUtils.deleteQuietly(targetFile)
            }
        }
        return false
    }
}