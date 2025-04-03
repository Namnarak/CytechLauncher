package com.movtery.zalithlauncher.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object MutableStates {
    /**
     * 状态：当前主屏幕的标签
     */
    var mainScreenTag by mutableStateOf<String?>(null)

    /**
     * 状态：当前主屏幕的标签
     */
    var settingsScreenTag by mutableStateOf<String?>(null)

    /**
     * 状态：文件、目录路径选择器
     */
    var filePathSelector by mutableStateOf<FilePathSelectorData?>(null)
}

/**
 * 文件、目录选择器数据类
 */
data class FilePathSelectorData(
    /**
     * 用于标识当前路径的需求方标签
     */
    val saveTag: String,
    /**
     * 选择的路径
     */
    val path: String
)