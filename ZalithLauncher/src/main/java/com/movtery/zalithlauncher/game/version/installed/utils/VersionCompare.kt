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

package com.movtery.zalithlauncher.game.version.installed.utils

import com.movtery.zalithlauncher.utils.string.isBiggerOrEqualTo
import com.movtery.zalithlauncher.utils.string.isBiggerTo
import com.movtery.zalithlauncher.utils.string.isLowerOrEqualTo
import com.movtery.zalithlauncher.utils.string.isLowerTo
import java.util.regex.Pattern

private val mSnapshotRegex = Pattern.compile("^\\d+[a-zA-Z]\\d+[a-zA-Z]$")

/**
 * 判断版本是否大于某个版本
 * @param releaseVer 若判断该版本为正式版，则与它比较
 * @param snapshotVer 若判断该版本为快照版，则与它比较
 */
fun String.isBiggerVer(releaseVer: String, snapshotVer: String): Boolean {
    return if (isSnapshotVer()) isBiggerTo(snapshotVer)
    else isBiggerTo(releaseVer)
}

/**
 * 判断版本是否大于等于某个版本
 * @param releaseVer 若判断该版本为正式版，则与它比较
 * @param snapshotVer 若判断该版本为快照版，则与它比较
 */
fun String.isBiggerOrEqualVer(releaseVer: String, snapshotVer: String): Boolean {
    return if (isSnapshotVer()) isBiggerOrEqualTo(snapshotVer)
    else isBiggerOrEqualTo(releaseVer)
}

/**
 * 判断版本是否小于某个版本
 * @param releaseVer 若判断该版本为正式版，则与它比较
 * @param snapshotVer 若判断该版本为快照版，则与它比较
 */
fun String.isLowerVer(releaseVer: String, snapshotVer: String): Boolean {
    return if (isSnapshotVer()) isLowerTo(snapshotVer)
    else isLowerTo(releaseVer)
}

/**
 * 判断版本是否小于等于某个版本
 * @param releaseVer 若判断该版本为正式版，则与它比较
 * @param snapshotVer 若判断该版本为快照版，则与它比较
 */
fun String.isLowerOrEqualVer(releaseVer: String, snapshotVer: String): Boolean {
    return if (isSnapshotVer()) isLowerOrEqualTo(snapshotVer)
    else isLowerOrEqualTo(releaseVer)
}

/**
 * 判断当前版本是否为快照版本
 */
fun String.isSnapshotVer(): Boolean = mSnapshotRegex.matcher(this).matches()