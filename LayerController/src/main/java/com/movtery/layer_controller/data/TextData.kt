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
import com.movtery.layer_controller.utils.getAButtonUUID
import kotlinx.serialization.Serializable

/**
 * @param text 按钮显示的文本
 */
@Serializable
data class TextData(
    val text: TranslatableString,
    val uuid: String,
    val position: ButtonPosition,
    val buttonSize: ButtonSize,
    val buttonStyle: String? = null,
    val textAlignment: TextAlignment = TextAlignment.Left,
    val textBold: Boolean = false,
    val textItalic: Boolean = false,
    val textUnderline: Boolean = false,
    val visibilityType: VisibilityType
): Widget

/**
 * 克隆一个新的TextData对象（UUID、位置不同）
 */
public fun TextData.cloneNew(): TextData = TextData(
    text = this.text,
    uuid = getAButtonUUID(),
    position = CenterPosition,
    buttonSize = buttonSize,
    buttonStyle = buttonStyle,
    textAlignment = textAlignment,
    textBold = textBold,
    textItalic = textItalic,
    textUnderline = textUnderline,
    visibilityType = visibilityType
)