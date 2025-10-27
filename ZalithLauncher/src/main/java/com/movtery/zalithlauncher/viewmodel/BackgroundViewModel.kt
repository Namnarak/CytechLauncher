package com.movtery.zalithlauncher.viewmodel

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.context.copyLocalFile
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.image.isImageFile
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File

/**
 * 启动器背景管理
 */
class BackgroundViewModel(): ViewModel() {
    val backgroundFile: File = PathManager.FILE_LAUNCHER_BACKGROUND

    /**
     * 背景文件是否有效（存在，且是有效的视频或图片）
     */
    var isValid by mutableStateOf(false)
        private set

    /**
     * 背景文件是一个视频
     */
    var isVideo by mutableStateOf(false)
        private set

    /**
     * 背景文件是一个图片
     */
    var isImage by mutableStateOf(false)
        private set

    var refreshTrigger by mutableStateOf(false)
        private set

    private suspend fun updateState() {
        withContext(Dispatchers.IO) {
            val isVideo0 = isVideoFile()
            val isImage0 = backgroundFile.isImageFile()
            //更新状态
            isVideo = isVideo0
            isImage = isImage0
            isValid = backgroundFile.exists() && (isVideo0 || isImage0)
            refreshTrigger = refreshTrigger.not()
        }
    }

    private fun isVideoFile(): Boolean {
        if (!backgroundFile.exists()) return false
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(backgroundFile.absolutePath)
            val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) != null &&
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) != null
            hasVideo
        } catch (e: Exception) {
            lWarning("An exception occurred while trying to determine if ${backgroundFile.absolutePath} is a video.", e)
            false
        } finally {
            retriever.release()
        }
    }

    init {
        viewModelScope.launch {
            updateState()
        }
    }

    suspend fun delete() {
        withContext(Dispatchers.IO) {
            FileUtils.deleteQuietly(backgroundFile)
            updateState()
        }
    }

    suspend fun import(context: Context, result: Uri) {
        withContext(Dispatchers.IO) {
            context.copyLocalFile(result, backgroundFile)
            updateState()
        }
    }
}