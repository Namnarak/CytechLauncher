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

package com.movtery.zalithlauncher.game.download.modpack.platform.curseforge

import com.google.gson.annotations.SerializedName

class CurseForgeManifest(
    val manifestType: String,
    val manifestVersion: Int,
    val name: String,
    val version: String,
    val author: String,
    val overrides: String? = null,
    val minecraft: Minecraft,
    val files: List<ManifestFile>
) {
    data class Minecraft(
        @SerializedName("version")
        val gameVersion: String,
        val modLoaders: List<ModLoader>
    ) {
        data class ModLoader(
            val id: String,
            val primary: Boolean
        )
    }

    data class ManifestFile(
        val projectID: Int,
        val fileID: Int,
        val fileName: String? = null,
        val url: String? = null,
        val required: Boolean
    ) {
        fun getFileUrl(): String? {
            return url ?: fileName?.let {
                "https://edge.forgecdn.net/files/${fileID / 1000}/${fileID % 1000}/$it"
            }
        }
    }
}