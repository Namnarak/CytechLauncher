package com.movtery.zalithlauncher.game.download.assets.platform

import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsPage

fun previousPage(
    pageNumber: Int,
    pages: List<AssetsPage?>,
    index: Int,
    limit: Int,
    onSuccess: (AssetsPage) -> Unit = {},
    onSearch: (index: Int) -> Unit = {}
) {
    val targetIndex = pageNumber - 2 //上一页在缓存中的索引
    val previousPage = pages.getOrNull(targetIndex)
    if (previousPage != null) {
        onSuccess(previousPage)
    } else {
        //重新搜索
        onSearch((index - limit).coerceAtLeast(0))
    }
}

fun nextPage(
    pageNumber: Int,
    isLastPage: Boolean,
    pages: List<AssetsPage?>,
    index: Int,
    limit: Int,
    onSuccess: (AssetsPage) -> Unit = {},
    onSearch: (index: Int) -> Unit = {}
) {
    if (!isLastPage) {
        val nextIndex = pageNumber
        //判断是否已缓存下一页
        val nextPage = pages.getOrNull(nextIndex)
        if (nextPage != null) {
            onSuccess(nextPage)
        } else {
            //搜索下一页
            onSearch(index + limit)
        }
    }
}