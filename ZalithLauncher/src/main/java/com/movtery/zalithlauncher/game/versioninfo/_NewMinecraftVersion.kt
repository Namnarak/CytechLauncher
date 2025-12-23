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

/**
 * 新的版本号命名规则
 */
val NEW_VERSION_PATTERN = """^(\d{2})\.(\d+)(?:\.(\d+))?(?:-(snapshot|pre|rc)-(\d+))?$""".toRegex()

/**
 * 新的版本号命名规则的版本变体类型
 */
enum class VariantType(val weight: Int) {
    /** 变体类型：快照 */
    Snapshot(1),
    /** 变体类型：预发布版 */
    Pre(2),
    /** 变体类型：发布候选 */
    RC(3)
}

private fun String.toVariantType(): VariantType? {
    return when (this) {
        "snapshot" -> VariantType.Snapshot
        "pre" -> VariantType.Pre
        "rc" -> VariantType.RC
        else -> null
    }
}

/**
 * 新的 Minecraft 版本号命名规则
 * @param fullVersion 完整的版本字符串
 * @param year 年份
 * @param updateNumber 更新序号
 * @param hotfix 热修复序号，可能是 null
 * @param variantType 变体类型
 * @param variantNumber 变体序号
 */
data class NewMinecraftVersion(
    val fullVersion: String,
    val year: Int,
    val updateNumber: Int,
    val hotfix: Int?,
    val variantType: VariantType?,
    val variantNumber: Int?
) : Comparable<NewMinecraftVersion> {
    override fun compareTo(other: NewMinecraftVersion): Int {
        if (this.year != other.year) {
            return this.year - other.year
        }

        if (this.updateNumber != other.updateNumber) {
            return this.updateNumber - other.updateNumber
        }

        //比较热修复序号 (null 视为 0)
        val thisHotfix = this.hotfix ?: 0
        val otherHotfix = other.hotfix ?: 0
        if (thisHotfix != otherHotfix) {
            return thisHotfix - otherHotfix
        }

        val thisVariantWeight = this.variantType?.weight ?: 4
        val otherVariantWeight = other.variantType?.weight ?: 4

        if (thisVariantWeight != otherVariantWeight) {
            return otherVariantWeight - thisVariantWeight
        }

        //如果变体类型相同，比较变体序号
        val thisVariantNumber = this.variantNumber ?: 0
        val otherVariantNumber = other.variantNumber ?: 0

        if (thisVariantNumber != otherVariantNumber) {
            return otherVariantNumber - thisVariantNumber
        }

        return 0
    }
}

fun NewMinecraftVersion.isRelease() = variantType == null
fun NewMinecraftVersion.isSnapshot() = variantType == VariantType.Snapshot
fun NewMinecraftVersion.isPreRelease() = variantType == VariantType.Pre
fun NewMinecraftVersion.isReleaseCandidate() = variantType == VariantType.RC

/**
 * 解析 2026年新格式的 Minecraft 版本号
 * @param versionString 版本号字符串，如 “26.1”, “26.1.1”, “26.1-snapshot-1”
 */
fun parseNewVersionFormat(versionString: String): NewMinecraftVersion? {
    val matchResult = NEW_VERSION_PATTERN.matchEntire(versionString) ?: return null
    val groups = matchResult.groups

    //年份和更新序号
    val year = groups[1]?.value?.toIntOrNull() ?: return null
    val updateNumber = groups[2]?.value?.toIntOrNull() ?: return null

    //热修复序号
    val hotfix = groups[3]?.value?.toIntOrNull()

    //变体类型和序号
    val variantTypeStr = groups[4]?.value
    val variantType = variantTypeStr?.toVariantType()
    val variantNumber = groups[5]?.value?.toIntOrNull()

    if (variantType != null && variantNumber == null) return null
    if (variantNumber != null && variantType == null) return null

    return NewMinecraftVersion(
        fullVersion = versionString,
        year = year,
        updateNumber = updateNumber,
        hotfix = hotfix,
        variantType = variantType,
        variantNumber = variantNumber
    )
}