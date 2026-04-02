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

import com.movtery.zalithlauncher.utils.string.compareChar
import com.movtery.zalithlauncher.utils.string.compareVersion
import org.jackhuang.hmcl.util.versioning.GameVersionNumber

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
            -GameVersionNumber.compare(ver1, ver2)
        } else {
            null
        }

        if (sort == null) {
            val thisVer = ver1 ?: o1.getVersionName()
            val otherVer = ver2 ?: o2.getVersionName()
            sort = -thisVer.compareVersion(otherVer)
        }

        if (sort == 0) {
            sort = compareChar(o1.getVersionName(), o2.getVersionName())
        }

        return sort
    }
}