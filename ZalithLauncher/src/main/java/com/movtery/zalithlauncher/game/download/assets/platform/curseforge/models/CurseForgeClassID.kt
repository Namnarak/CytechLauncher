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

package com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models

import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * CurseForge 资源搜索类别
 */
@Serializable(with = CurseForgeClassID.Serializer::class)
enum class CurseForgeClassID(val platform: PlatformClasses, val classID: Int, val slug: String, val folderName: String) {
    /** 模组 */
    MOD(PlatformClasses.MOD, 6, "mc-mods", VersionFolders.MOD.folderName),

    /** 整合包 */
    MOD_PACK(PlatformClasses.MOD_PACK, 4471, "modpacks", ""),

    /** 资源包 */
    RESOURCE_PACK(PlatformClasses.RESOURCE_PACK, 12, "texture-packs", VersionFolders.RESOURCE_PACK.folderName),

    /** 存档 */
    SAVES(PlatformClasses.SAVES, 17, "worlds", VersionFolders.SAVES.folderName),

    /** 光影包 */
    SHADERS(PlatformClasses.SHADERS, 6552, "shaders", VersionFolders.SHADERS.folderName);

    companion object {
        private val map = entries.associateBy { it.classID }
        fun fromId(id: Int): CurseForgeClassID = map[id] ?: error("Unknown class ID: $id")
    }

    object Serializer : KSerializer<CurseForgeClassID> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("CurseForgeClassID", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): CurseForgeClassID {
            val id = decoder.decodeInt()
            return CurseForgeClassID.fromId(id)
        }

        override fun serialize(encoder: Encoder, value: CurseForgeClassID) {
            encoder.encodeInt(value.classID)
        }
    }
}