package com.movtery.layer_controller.data

import com.google.gson.annotations.SerializedName
import com.movtery.layer_controller.utils.randomUUID

/**
 * @param position 按钮的位置（x, y为百分比值）
 * @param buttonSize 按钮的大小
 * @param buttonStyle 按钮的样式的uuid
 * @param visibilityType 按钮可见的场景
 */
open class BaseData(
    @SerializedName("uuid")
    val uuid: String = getAButtonUUID(),
    @SerializedName("position")
    var position: ButtonPosition = ButtonPosition.Zero,
    @SerializedName("buttonSize")
    var buttonSize: ButtonSize = ButtonSize.Default,
    @SerializedName("buttonStyle")
    var buttonStyle: String? = null,
    @SerializedName("visibilityType")
    var visibilityType: VisibilityType = VisibilityType.ALWAYS
) {
    companion object {
        internal fun getAButtonUUID() = randomUUID(18)
    }
}