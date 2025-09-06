package com.movtery.layer_controller.observable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

/**
 * 可观察的BaseData包装类
 */
abstract class ObservableWidget {
    /**
     * 编辑模式中，是否正在编辑位置
     */
    var isEditingPos by mutableStateOf(false)

    /**
     * 编辑模式中，记录实时偏移量
     */
    var movingOffset by mutableStateOf(Offset.Zero)
}