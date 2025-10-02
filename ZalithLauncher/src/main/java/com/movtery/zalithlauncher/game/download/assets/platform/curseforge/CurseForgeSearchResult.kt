package com.movtery.zalithlauncher.game.download.assets.platform.curseforge

import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchResult
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeData
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgePagination
import com.movtery.zalithlauncher.game.download.assets.platform.searchRankWithChineseBias
import com.movtery.zalithlauncher.game.download.assets.utils.getTranslations
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsPage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class CurseForgeSearchResult(
    /**
     * 响应数据
     */
    @SerialName("data")
    val data: Array<CurseForgeData>,

    /**
     * 响应分页信息
     */
    @SerialName("pagination")
    val pagination: CurseForgePagination
): PlatformSearchResult {
    override fun getAssetsPage(classes: PlatformClasses): AssetsPage {
        val mcmodData = data.map {
            it to classes.getTranslations().getModBySlugId(it.slug)
        }
        val pageSize = pagination.pageSize
        val isLastPage = pagination.resultCount < pageSize ||
                (pagination.index + pagination.resultCount) >= pagination.totalCount

        return AssetsPage(
            pageNumber = pagination.index / pageSize + 1,
            pageIndex = pagination.index,
            totalPage = ((pagination.totalCount + pageSize - 1) / pageSize).toInt(),
            isLastPage = isLastPage,
            data = mcmodData
        )
    }

    override fun processChineseSearchResults(
        searchFilter: String,
        classes: PlatformClasses
    ): PlatformSearchResult {
        val newData = data.toList()
            .searchRankWithChineseBias(searchFilter, classes) { it.slug }
            .toTypedArray()
        return CurseForgeSearchResult(newData, pagination)
    }
}