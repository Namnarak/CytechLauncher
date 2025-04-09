package com.movtery.zalithlauncher.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

abstract class AbstractUnpackTask {
    /**
     * 描述当前解压任务进度的状态
     */
    var taskMessage by mutableStateOf<String?>(null)

    abstract fun isNeedUnpack(): Boolean
    abstract suspend fun run()
}