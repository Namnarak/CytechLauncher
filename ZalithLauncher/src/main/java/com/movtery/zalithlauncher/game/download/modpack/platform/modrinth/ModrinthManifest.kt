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

package com.movtery.zalithlauncher.game.download.modpack.platform.modrinth

class ModrinthManifest(
    val game: String,
    val formatVersion: Int,
    val versionId: String,
    val name: String,
    /** optional */
    val summary: String? = null,
    val files: Array<ManifestFile>,
    val dependencies: Map<String, String>
) {
    class ManifestFile(
        val path: String,
        val hashes: Hashes,
        /** optional */
        val env: Env? = null,
        val downloads: Array<String>,
        val fileSize: Long
    ) {
        class Hashes(
            val sha1: String,
            val sha512: String
        )

        class Env(
            val client: String,
            val server: String
        )
    }
}