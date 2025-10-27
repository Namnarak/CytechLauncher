/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

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
    val textAlignment: TextAlignment = TextAlignment.Left,
    val textBold: Boolean = false,
    val textItalic: Boolean = false,
    val textUnderline: Boolean = false,
    val visibilityType: VisibilityType,
    val clickEvents: List<ClickEvent> = emptyList(),
    val isSwipple: Boolean,
    val isPenetrable: Boolean,
    val isToggleable: Boolean
): Widget

/**
 * 克隆一个新的NormalData对象（UUID、位置不同）
 */
public fun NormalData.cloneNew(): NormalData = NormalData(
    text = this.text,
    uuid = getAButtonUUID(),
    position = CenterPosition,
    buttonSize = buttonSize,
    buttonStyle = buttonStyle,
    textAlignment = textAlignment,
    textBold = textBold,
    textItalic = textItalic,
    textUnderline = textUnderline,
    visibilityType = visibilityType,
    clickEvents = clickEvents,
    isSwipple = isSwipple,
    isPenetrable = isPenetrable,
    isToggleable = isToggleable
)