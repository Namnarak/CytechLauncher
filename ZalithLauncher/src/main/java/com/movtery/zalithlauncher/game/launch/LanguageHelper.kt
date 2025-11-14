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

package com.movtery.zalithlauncher.game.launch

import com.movtery.zalithlauncher.game.version.installed.utils.isLowerOrEqualVer
import com.movtery.zalithlauncher.utils.getSystemLanguage

private fun getOlderLanguage(lang: String): String {
    val underscoreIndex = lang.indexOf('_')
    return if (underscoreIndex != -1) {
        //只将下划线后面的字符转换为大写
        val builder = StringBuilder(lang.substring(0, underscoreIndex + 1))
        builder.append(lang.substring(underscoreIndex + 1).uppercase())
        builder.toString()
    } else lang
}

private fun getLanguage(versionId: String): String {
    val lang = getSystemLanguage()
    return if (versionId.isLowerOrEqualVer("1.10.2", "16w32a")) {
        getOlderLanguage(lang) // 1.10 -
    } else {
        lang
    }
}

fun MCOptions.loadLanguage(versionId: String) {
    if (!containsKey("lang")) {
        val lang = getLanguage(versionId)
        set("lang", lang)
    }
}