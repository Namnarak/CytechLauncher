package com.movtery.layer_controller.layout

import com.google.gson.annotations.SerializedName
import com.movtery.layer_controller.data.NormalData
import com.movtery.layer_controller.data.TextData
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.utils.randomUUID

/**
 * 控制布局单个层级，像图层一样存储控制组件
 * @param name 层级的名称
 * @param hide 是否隐藏层级
 * @param visibilityType 层级的可见场景
 * @param normalButtons 普通的按钮列表
 * @param textBoxes 文本显示框列表
 */
data class ControlLayer(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("uuid")
    val uuid: String = randomUUID(),
    @SerializedName("hide")
    val hide: Boolean = false,
    @SerializedName("visibilityType")
    val visibilityType: VisibilityType = VisibilityType.ALWAYS,
    @SerializedName("normalButtons")
    val normalButtons: List<NormalData> = emptyList(),
    @SerializedName("textBoxes")
    val textBoxes: List<TextData> = emptyList()
)