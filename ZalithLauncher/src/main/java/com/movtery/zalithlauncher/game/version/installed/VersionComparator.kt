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

package com.movtery.zalithlauncher.game.version.installed

import com.movtery.zalithlauncher.game.versioninfo.parseNewVersionFormat
import com.movtery.zalithlauncher.utils.string.compareChar
import com.movtery.zalithlauncher.utils.string.compareVersion

object VersionComparator: Comparator<Version> {
    override fun compare(o1: Version, o2: Version): Int {
        val pinned1 = o1.pinnedState
        val pinned2 = o2.pinnedState

        if (pinned1 != pinned2) {
            return if (pinned1) -1 else 1
        }

        val ver1 = o1.getVersionInfo()?.minecraftVersion
        val ver2 = o2.getVersionInfo()?.minecraftVersion

        var sort = if (ver1 != null && ver2 != null) {
            val newVer1 = parseNewVersionFormat(ver1)
            val newVer2 = parseNewVersionFormat(ver2)
            when {
                newVer1 != null && newVer2 != null -> {
                    //两个版本都是新版本命名规则
                    -newVer1.compareTo(newVer2)
                }
                newVer1 != null && newVer2 == null -> {
                    //新命名规则优先
                    -1
                }
                newVer1 == null && newVer2 != null -> {
                    //newVer2是新命名规则，应该排在前面
                    1
                }
                else -> null
            }
        } else {
            null
        }

        if (sort == null) {
            val thisVer = ver1 ?: o1.getVersionName()
            sort = -thisVer.compareVersion(ver2 ?: o2.getVersionName())
        }

        if (sort == 0) {
            sort = compareChar(o1.getVersionName(), o2.getVersionName())
        }

        return sort
    }
}