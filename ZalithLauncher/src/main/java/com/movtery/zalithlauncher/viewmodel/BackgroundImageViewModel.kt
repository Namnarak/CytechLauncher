package com.movtery.zalithlauncher.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.movtery.zalithlauncher.context.copyLocalFile
import com.movtery.zalithlauncher.path.PathManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File

/**
 * 启动器背景图片管理
 */
class BackgroundImageViewModel(): ViewModel() {
    val image: File = PathManager.FILE_BACKGROUND_IMAGE

    var isImageExists by mutableStateOf(false)
        private set

    var refreshImage by mutableStateOf(false)
        private set

    fun updateState() {
        isImageExists = image.exists()
        refreshImage = refreshImage.not()
    }

    init {
        updateState()
    }

    suspend fun deleteImage() {
        withContext(Dispatchers.IO) {
            FileUtils.deleteQuietly(image)
            updateState()
        }
    }

    suspend fun importImage(context: Context, result: Uri) {
        withContext(Dispatchers.IO) {
            context.copyLocalFile(result, image)
            updateState()
        }
    }
}