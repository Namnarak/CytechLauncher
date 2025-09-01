package com.movtery.layer_controller.data

import androidx.compose.ui.geometry.Offset
import com.google.gson.annotations.SerializedName
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.utils.lang.TranslatableString

/**
 * @param clickEvents 点击事件组
 * @param isSwipple 滑动可与周围的按钮联动操作
 * @param isPenetrable 是否允许将触摸事件向下穿透
 * @param isToggleable 是否用开关的形式切换按下状态
 */
class NormalData(
    text: TranslatableString,
    uuid: String = getAButtonUUID(),
    position: Offset = Offset.Zero,
    buttonSize: ButtonSize = ButtonSize.Default,
    buttonStyle: String? = null,
    visibilityType: VisibilityType = VisibilityType.ALWAYS,
    @SerializedName("clickEvents")
    var clickEvents: List<ClickEvent> = emptyList(),
    @SerializedName("isSwipple")
    var isSwipple: Boolean = false,
    @SerializedName("isPenetrable")
    var isPenetrable: Boolean = false,
    @SerializedName("isToggleable")
    var isToggleable: Boolean = false
) : TextData(
    text = text,
    uuid = uuid,
    position = position,
    buttonSize = buttonSize,
    buttonStyle = buttonStyle,
    visibilityType = visibilityType
)