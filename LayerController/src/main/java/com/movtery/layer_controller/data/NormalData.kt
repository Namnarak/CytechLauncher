package com.movtery.layer_controller.data

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
    position: ButtonPosition = ButtonPosition.Zero,
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
) {
    companion object {
        /**
         * 克隆一个新的NormalData对象（UUID、位置不同）
         */
        fun NormalData.cloneNew(): NormalData = NormalData(
            text = this.text,
            uuid = getAButtonUUID(),
            position = ButtonPosition.Center,
            buttonSize = buttonSize,
            buttonStyle = buttonStyle,
            visibilityType = visibilityType,
            clickEvents = clickEvents,
            isSwipple = isSwipple,
            isPenetrable = isPenetrable,
            isToggleable = isToggleable
        )
    }
}