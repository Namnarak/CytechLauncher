package com.movtery.zalithlauncher.game.download.assets.platform.curseforge

import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchResult
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeData
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
    val pagination: Pagination
) : PlatformSearchResult {
    @Serializable
    class Pagination(
        /**
         * 查询当前开始的索引
         */
        @SerialName("index")
        val index: Int,

        /**
         * 页面大小
         */
        @SerialName("pageSize")
        val pageSize: Int,

        /**
         * 查询返回的结果数
         */
        @SerialName("resultCount")
        val resultCount: Int,

        /**
         * 与查询匹配的结果总数
         */
        @SerialName("totalCount")
        val totalCount: Long
    )
}