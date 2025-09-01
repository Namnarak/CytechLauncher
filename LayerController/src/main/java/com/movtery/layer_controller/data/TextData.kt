package com.movtery.layer_controller.data

import androidx.compose.ui.geometry.Offset
import com.google.gson.annotations.SerializedName
import com.movtery.layer_controller.utils.lang.TranslatableString

/**
 * @param text 按钮显示的文本
 */
open class TextData(
    @SerializedName("text")
    var text: TranslatableString,
    uuid: String = getAButtonUUID(),
    position: Offset = Offset.Zero,
    buttonSize: ButtonSize = ButtonSize.Default,
    buttonStyle: String? = null,
    visibilityType: VisibilityType = VisibilityType.ALWAYS
) : BaseData(
    uuid = uuid,
    position = position,
    buttonSize = buttonSize,
    buttonStyle = buttonStyle,
    visibilityType = visibilityType
)