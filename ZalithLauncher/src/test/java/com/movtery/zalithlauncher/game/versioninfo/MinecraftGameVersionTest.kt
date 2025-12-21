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

package com.movtery.zalithlauncher.game.versioninfo

import org.junit.Test
import kotlin.collections.component2

class MinecraftGameVersionTest {

    @Test
    fun testParseNewVersionFormat() {
        val testCases = mapOf(
            "26.1" to true,
            "26.1.1" to true,
            "26.1-snapshot-1" to true,
            "26.1-pre-2" to true,
            "26.1-rc-1" to true,
            "27.5.3-snapshot-5" to true,
            "26.1-snapshot" to false,
            "26.1-1" to false,
            "1.21.11" to false,
            "25w46a" to false,
            "1.21.11-pre1" to false,
            "1.21.11-rc1" to false,
            "26.0" to true,
            "26.1.0" to true,
        )

        testCases.forEach { (version, shouldSucceed) ->
            val result = parseNewVersionFormat(version)
            val success = result != null

            if (success != shouldSucceed) {
                throw AssertionError("Failed for version: $version. Expected success: $shouldSucceed, but got: $success")
            }

            println("$version: ${if (success) "PASS (${result.variantType ?: "Release"})" else "FAIL"}")
        }
    }

    @Test
    fun testFilterRelease() {
        val testCases = mapOf(
            "26.1" to true,
            "26.1.1" to true,
            "26.1-snapshot-1" to false,
            "26.1-pre-2" to false,
            "26.1-rc-1" to false,
            "27.5.3-snapshot-5" to false,
            "26.1-snapshot" to false,
            "26.1-1" to false,
            "1.21.11" to true,
            "25w46a" to false,
            "1.21.11-pre1" to false,
            "1.21.11-rc1" to false,
            "26.0" to true,
            "26.1.0" to true,
        )

        testCases.forEach { (version, shouldSucceed) ->
            val result = filterRelease(version)

            if (result && !shouldSucceed) {
                throw AssertionError("Failed for version: $version. Expected success: false, but got: true")
            }

            println("$version: ${if (result) "PASS" else "FAIL"}")
        }
    }
}