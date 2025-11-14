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

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CurseForgeFingerprintsMatches(
    val data: Result
) {
    @Serializable
    data class Result(
        val isCacheBuilt: Boolean,
        val exactMatches: List<FingerprintMatch>? = null,
        val exactFingerprints: List<Long>? = null,
        val partialMatches: List<FingerprintMatch>? = null,
        val partialMatchFingerprints: JsonElement? = null,
        val additionalProperties: List<Long>? = null,
        val installedFingerprints: List<Long>? = null,
        val unmatchedFingerprints: List<Long>? = null
    ) {
        @Serializable
        data class FingerprintMatch(
            val id: Int,
            val file: CurseForgeFile,
            val latestFiles: List<CurseForgeFile>
        )
    }
}