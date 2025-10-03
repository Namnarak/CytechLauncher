package com.movtery.zalithlauncher.game.download.assets.platform

import com.movtery.zalithlauncher.game.download.assets.utils.getTranslations
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsPage
import com.movtery.zalithlauncher.utils.string.LevCalculator
import com.movtery.zalithlauncher.utils.string.containsChinese
import kotlin.math.max

private const val CONTAIN_CHINESE_WEIGHT = 10

/**
 * 平台的搜索结果实现
 */
interface PlatformSearchResult {
    /**
     * 将平台项目搜索结果与 mcmod 信息打包在一起，作为一页
     */
    fun getAssetsPage(classes: PlatformClasses): AssetsPage

    /**
     * 参考源代码：[HMCL Github](https://github.com/HMCL-dev/HMCL/blob/57018be/HMCL/src/main/java/org/jackhuang/hmcl/game/LocalizedRemoteModRepository.java#L65-L103)
     * 原项目版权归原作者所有，遵循GPL v3协议
     * @return 对于中文搜索结果的优先级排序
     */
    fun processChineseSearchResults(searchFilter: String, classes: PlatformClasses): PlatformSearchResult
}

/**
 * 对于中文搜索结果的优先级排序
 * 参考源代码：[HMCL Github](https://github.com/HMCL-dev/HMCL/blob/57018be/HMCL/src/main/java/org/jackhuang/hmcl/game/LocalizedRemoteModRepository.java#L65-L103)
 * 原项目版权归原作者所有，遵循GPL v3协议
 */
fun <T> List<T>.searchRankWithChineseBias(
    searchFilter: String,
    classes: PlatformClasses,
    getSlug: (T) -> String?
): List<T> {
    val (chineseResults, englishResults) = partition { mod ->
        classes.getTranslations()
            .getModBySlugId(getSlug(mod))
            ?.name
            ?.takeIf { it.isNotBlank() && it.containsChinese() } != null
    }

    val levCalculator = LevCalculator()
    val sortedChineseResults = chineseResults.map { mod ->
        val translation = classes.getTranslations()
            .getModBySlugId(getSlug(mod))!!
        val modName = translation.name

        val relevanceScore = when {
            searchFilter.isEmpty() || modName.isEmpty() ->
                max(searchFilter.length, modName.length)
            else -> {
                var levDistance = levCalculator.calc(searchFilter, modName)
                searchFilter.forEach { char ->
                    if (modName.contains(char)) levDistance -= CONTAIN_CHINESE_WEIGHT
                }
                levDistance
            }
        }
        mod to relevanceScore
    }.sortedBy { it.second }
        .map { it.first }

    return sortedChineseResults + englishResults
}