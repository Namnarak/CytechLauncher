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

package com.movtery.layer_controller.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 按键点击事件
 * @param type 绑定的点击事件类型
 * @param key 事件唯一标识
 */
@Serializable
data class ClickEvent(
    @SerialName("type")
    val type: Type,
    @SerialName("key")
    val key: String
) {
    @Serializable
    enum class Type {
        /**
         * 点击触发按键
         */
        @SerialName("key")
        Key,

        /**
         * 点击触发启动器事件
         */
        @SerialName("launcher_event")
        LauncherEvent,

        /**
         * 点击开关按键层级
         */
        @SerialName("switch_layer")
        SwitchLayer
    }
}