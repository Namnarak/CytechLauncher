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

package com.movtery.zalithlauncher.game.version.mod.meta

data class LiteModMetadata(
    val name: String,
    val version: String,
    val mcversion: String,
    val revision: String? = null,
    val author: String? = null,
    val classTransformerClasses: Array<String>? = null,
    val description: String? = null,
    val modpackName: String? = null,
    val modpackVersion: String? = null,
    val checkUpdateUrl: String? = null,
    val updateURI: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as LiteModMetadata
        return classTransformerClasses.contentEquals(other.classTransformerClasses)
    }

    override fun hashCode(): Int {
        return classTransformerClasses?.contentHashCode() ?: 0
    }
}