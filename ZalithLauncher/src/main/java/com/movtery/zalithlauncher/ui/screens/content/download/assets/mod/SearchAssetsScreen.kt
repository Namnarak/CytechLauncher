package com.movtery.zalithlauncher.ui.screens.content.download.assets.mod

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformFilterCode
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchFilter
import com.movtery.zalithlauncher.game.download.assets.platform.getPageInfo
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthModLoaderCategory
import com.movtery.zalithlauncher.game.download.assets.platform.nextPage
import com.movtery.zalithlauncher.game.download.assets.platform.previousPage
import com.movtery.zalithlauncher.game.download.assets.platform.searchAssets
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.content.DownloadScreenKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsPage
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.ResultListLayout
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.SearchAssetsState
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.SearchFilter
import com.movtery.zalithlauncher.ui.screens.main.elements.mainScreenKey
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.logging.Logger.lInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * @param parentScreenKey 父屏幕Key
 * @param parentCurrentKey 父屏幕当前Key
 * @param screenKey 屏幕的Key
 * @param currentKey 当前的Key
 * @param platformClasses 搜索资源的分类
 * @param searchPlatform 初始搜索平台
 * @param enablePlatform 是否允许更改平台
 * @param onPlatformChange 平台变更回调
 * @param categories 可用的资源类别过滤器
 * @param enableModLoader 是否允许更改模组加载器
 * @param modloaders 可用的模组加载器过滤器
 * @param mapCategories 通过平台获取类别本地化信息
 */
@Composable
fun SearchAssetsScreen(
    parentScreenKey: NavKey,
    parentCurrentKey: NavKey?,
    screenKey: NavKey,
    currentKey: NavKey?,
    platformClasses: PlatformClasses,
    searchPlatform: Platform,
    enablePlatform: Boolean = true,
    onPlatformChange: (Platform) -> Unit,
    categories: List<PlatformFilterCode>,
    enableModLoader: Boolean = false,
    modloaders: List<PlatformDisplayLabel> = emptyList(),
    mapCategories: (Platform, String) -> PlatformFilterCode?,
) {
    val coroutineScope = rememberCoroutineScope()
    var searchResult by remember { mutableStateOf<SearchAssetsState>(SearchAssetsState.Searching) }
    val pages = remember { mutableStateListOf<AssetsPage?>() }

    var currentSearchJob by remember { mutableStateOf<Job?>(null) }

    var searchFilter by remember { mutableStateOf<PlatformSearchFilter>(PlatformSearchFilter()) }
    var reloadTrigger by remember { mutableStateOf(false) }

    //自动搜索
    DisposableEffect(searchPlatform, searchFilter, reloadTrigger) {
        currentSearchJob?.cancel() //取消上一个搜索

        val searchJob = coroutineScope.launch {
            searchResult = SearchAssetsState.Searching
            searchAssets(
                searchPlatform = searchPlatform,
                searchFilter = searchFilter,
                platformClasses = platformClasses,
                onSuccess = { result ->
                    result.getPageInfo { pageNumber, pageIndex, totalPage, isLastPage ->
                        lInfo("Searched page info: {pageNumber: $pageNumber, pageIndex: $pageIndex, totalPage: $totalPage, isLastPage: $isLastPage}")

                        val page = AssetsPage(
                            pageNumber = pageNumber,
                            pageIndex = pageIndex,
                            totalPage = totalPage,
                            isLastPage = isLastPage,
                            result = result
                        )

                        val targetIndex = pageNumber - 1

                        if (pages.size > targetIndex) {
                            pages[targetIndex] = page //替换已有页
                        } else {
                            while (pages.size < targetIndex) {
                                pages += null
                            }
                            pages += page
                        }

                        searchResult = SearchAssetsState.Success(page)
                    }
                },
                onError = {
                    searchResult = it
                }
            )
        }

        currentSearchJob = searchJob

        onDispose {
            //退出时取消
            searchJob.cancel()
        }
    }

    BaseScreen(
        levels1 = listOf(
            Pair(DownloadScreenKey::class.java, mainScreenKey)
        ),
        Triple(parentScreenKey, parentCurrentKey, false),
        Triple(screenKey, currentKey, false)
    ) { isVisible ->
        Row {
            val yOffset by swapAnimateDpAsState(targetValue = (-40).dp, swapIn = isVisible)
            ResultListLayout(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(7f)
                    .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                searchState = searchResult,
                onReload = {
                    reloadTrigger = !reloadTrigger
                },
                mapCategories = mapCategories,
                mapModLoaders = { string ->
                    ModrinthModLoaderCategory.entries.find { it.facetValue() == string }
                },
                onPreviousPage = { pageNumber ->
                    previousPage(
                        pageNumber = pageNumber,
                        pages = pages,
                        index = searchFilter.index,
                        limit = searchFilter.limit,
                        onSuccess = { previousPage ->
                            searchResult = SearchAssetsState.Success(previousPage)
                        },
                        onSearch = { newIndex ->
                            searchFilter = searchFilter.copy(index = newIndex)
                        }
                    )
                },
                onNextPage = { pageNumber, isLastPage ->
                    nextPage(
                        pageNumber = pageNumber,
                        isLastPage = isLastPage,
                        pages = pages,
                        index = searchFilter.index,
                        limit = searchFilter.limit,
                        onSuccess = { nextPage ->
                            searchResult = SearchAssetsState.Success(nextPage)
                        },
                        onSearch = { newIndex ->
                            searchFilter = searchFilter.copy(index = newIndex)
                        }
                    )
                }
            )

            val xOffset by swapAnimateDpAsState(
                targetValue = 40.dp,
                swapIn = isVisible,
                isHorizontal = true
            )
            SearchFilter(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(3f)
                    .offset { IntOffset(x = xOffset.roundToPx(), y = 0) },
                contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp, end = 12.dp),
                enablePlatform = enablePlatform,
                searchPlatform = searchPlatform,
                onPlatformChange = {
                    onPlatformChange(it)
                    pages.clear()
                    searchFilter = searchFilter.copy(category = null, modloader = null, index = 0)
                },
                searchName = searchFilter.searchName,
                onSearchNameChange = {
                    pages.clear()
                    searchFilter = searchFilter.copy(searchName = it, index = 0)
                },
                gameVersion = searchFilter.gameVersion,
                onGameVersionChange = {
                    pages.clear()
                    searchFilter = searchFilter.copy(gameVersion = it, index = 0)
                },
                sortField = searchFilter.sortField,
                onSortFieldChange = {
                    pages.clear()
                    searchFilter = searchFilter.copy(sortField = it, index = 0)
                },
                categories = categories,
                category = searchFilter.category,
                onCategoryChange = {
                    pages.clear()
                    searchFilter = searchFilter.copy(category = it, index = 0)
                },
                enableModLoader = enableModLoader,
                modloaders = modloaders,
                modloader = searchFilter.modloader,
                onModLoaderChange = {
                    pages.clear()
                    searchFilter = searchFilter.copy(modloader = it, index = 0)
                }
            )
        }
    }
}