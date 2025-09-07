package com.movtery.layer_controller.data

import com.movtery.layer_controller.data.lang.TranslatableString
import com.movtery.layer_controller.event.ClickEvent
import com.movtery.layer_controller.utils.getAButtonUUID
import kotlinx.serialization.Serializable

/**
 * @param clickEvents 点击事件组
 * @param isSwipple 滑动可与周围的按钮联动操作
 * @param isPenetrable 是否允许将触摸事件向下穿透
 * @param isToggleable 是否用开关的形式切换按下状态
 */
@Serializable
data class NormalData(
    val text: TranslatableString,
    val uuid: String,
    val position: ButtonPosition,
    val buttonSize: ButtonSize,
    val buttonStyle: String? = null,
    val visibilityType: VisibilityType,
    val clickEvents: List<ClickEvent> = emptyList(),
    val isSwipple: Boolean,
    val isPenetrable: Boolean,
    val isToggleable: Boolean
): Widget {
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