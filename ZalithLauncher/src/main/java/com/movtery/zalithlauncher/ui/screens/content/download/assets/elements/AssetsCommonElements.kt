package com.movtery.zalithlauncher.ui.screens.content.download.assets.elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowLeft
import androidx.compose.material.icons.automirrored.rounded.ArrowRight
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformFilterCode
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchData
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchResult
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSortField
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.CurseForgeSearchResult
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeData
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.ModrinthSearchResult
import com.movtery.zalithlauncher.game.download.assets.type.allGameVersions
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.itemLayoutColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.formatNumberByLocale
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning

sealed interface SearchAssetsState {
    data object Searching: SearchAssetsState
    data class Success(val page: AssetsPage): SearchAssetsState
    data class Error(val message: Int, val args: Array<Any>? = null): SearchAssetsState {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Error

            if (message != other.message) return false
            if (!args.contentEquals(other.args)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = message
            result = 31 * result + (args?.contentHashCode() ?: 0)
            return result
        }
    }
}

/**
 * 资源搜索结果页面信息
 * @param pageNumber 第几页
 * @param pageIndex 页面索引
 * @param totalPage 总页数
 * @param isLastPage 是否为最后一页
 * @param result 搜索结果缓存
 */
data class AssetsPage(
    val pageNumber: Int,
    val pageIndex: Int,
    val totalPage: Int,
    val isLastPage: Boolean,
    val result: PlatformSearchResult
)

/**
 * 资源搜索结果展示列表
 * @param mapCategories 通过平台获取类别本地化信息
 * @param mapModLoaders Modrinth将模组加载器信息包含在Categories内，单独获取
 */
@Composable
fun ResultListLayout(
    modifier: Modifier = Modifier,
    searchState: SearchAssetsState,
    onReload: () -> Unit = {},
    onPreviousPage: (pageNumber: Int) -> Unit,
    onNextPage: (pageNumber: Int, isLastPage: Boolean) -> Unit,
    mapCategories: (Platform, String) -> PlatformFilterCode?,
    mapModLoaders: (String) -> PlatformDisplayLabel? = { null }
) {
    when (val state = searchState) {
        is SearchAssetsState.Searching -> {
            Box(modifier.padding(all = 12.dp)) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
        is SearchAssetsState.Success -> {
            val page = state.page
            val result = page.result
            val data = when (result) {
                is CurseForgeSearchResult -> result.data
                is ModrinthSearchResult -> result.hits
                else -> error("Unknown result type $result")
            }

            Box(modifier = modifier) {
                ResultList(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 60.dp, bottom = 6.dp),
                    data = data,
                    mapCategories = mapCategories,
                    mapModLoaders = mapModLoaders
                )

                PageController(
                    modifier = Modifier
                        .height(54.dp)
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 12.dp),
                    page = page,
                    onPreviousPage = {
                        onPreviousPage(page.pageNumber)
                    },
                    onNextPage = {
                        onNextPage(page.pageNumber, page.isLastPage)
                    }
                )
            }
        }
        is SearchAssetsState.Error -> {
            Box(modifier.padding(all = 12.dp)) {
                val message = if (state.args != null) {
                    stringResource(state.message, *state.args)
                } else {
                    stringResource(state.message)
                }

                ScalingLabel(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.download_assets_failed_to_get_result, message),
                    onClick = onReload
                )
            }
        }
    }
}

@Composable
private fun PageController(
    modifier: Modifier = Modifier,
    page: AssetsPage,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shadowElevation: Dp = 1.dp,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        shadowElevation = shadowElevation
    ) {
        Row(
            modifier = Modifier.padding(all = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = "${page.pageNumber} / ${page.totalPage}",
                style = MaterialTheme.typography.labelLarge
            )

            IconButton(
                enabled = page.pageNumber > 1, //不是第一页
                onClick = onPreviousPage
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowLeft,
                    contentDescription = stringResource(R.string.download_assets_result_previous_page)
                )
            }

            IconButton(
                enabled = !page.isLastPage, //不是最后一页
                onClick = onNextPage
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowRight,
                    contentDescription = stringResource(R.string.download_assets_result_next_page)
                )
            }
        }
    }
}

@Composable
private fun ResultList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    data: Array<out PlatformSearchData>,
    mapCategories: (Platform, String) -> PlatformFilterCode?,
    mapModLoaders: (String) -> PlatformDisplayLabel? = { null }
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(data) { item ->
            val itemModifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)

            when (item) {
                is ModrinthSearchResult.ModrinthProject -> {
                    val modloaders = item.displayCategories
                        ?.mapNotNull {
                            mapModLoaders(it)
                        }
                        ?.toSet()
                        ?.takeIf { it.isNotEmpty() }

                    val categories = item.displayCategories
                        ?.mapNotNull {
                            mapCategories(Platform.MODRINTH, it)
                        }
                        ?.toSet()
                        ?.takeIf { it.isNotEmpty() }
                        ?: item.categories
                            ?.take(4) //没有主要类别，则展示前4个
                            ?.mapNotNull {
                                mapCategories(Platform.MODRINTH, it)
                            }
                            ?.toSet()
                            ?.takeIf { it.isNotEmpty() }

                    ResultItemLayout(
                        modifier = itemModifier,
                        platform = Platform.MODRINTH,
                        title = item.title ?: "",
                        description = item.description ?: "",
                        iconUrl = item.iconUrl,
                        author = item.author,
                        downloads = item.downloads,
                        follows = item.follows,
                        modloaders = modloaders?.sortedWith { o1, o2 -> o1.index() - o2.index() },
                        categories = categories?.sortedWith { o1, o2 -> o1.index() - o2.index() }
                    )
                }
                is CurseForgeData -> {
                    val modloaders: Set<PlatformDisplayLabel>? = item.latestFilesIndexes.mapNotNull {
                        it.modLoader //通过最新文件获取模组加载器信息
                    }.toSet().takeIf { it.isNotEmpty() }

                    val categories = item.categories.mapNotNull {
                        mapCategories(Platform.CURSEFORGE, it.id.toString())
                    }.toSet().takeIf { it.isNotEmpty() }

                    ResultItemLayout(
                        modifier = itemModifier,
                        platform = Platform.CURSEFORGE,
                        title = item.name,
                        description = item.summary,
                        iconUrl = item.logo.url,
                        author = item.authors[0].name,
                        downloads = item.downloadCount,
                        modloaders = modloaders?.sortedWith { o1, o2 -> o1.index() - o2.index() },
                        categories = categories?.sortedWith { o1, o2 -> o1.index() - o2.index() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultItemLayout(
    modifier: Modifier = Modifier,
    platform: Platform,
    title: String,
    description: String,
    iconUrl: String? = null,
    author: String? = null,
    downloads: Long = 0L,
    follows: Long? = null,
    modloaders: List<PlatformDisplayLabel>? = null,
    categories: List<PlatformFilterCode>? = null,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shadowElevation: Dp = 1.dp,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        shadowElevation = shadowElevation,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(all = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AssetsIcon(
                modifier = Modifier
                    .size(72.dp)
                    .clip(shape = RoundedCornerShape(10.dp))
                    .align(Alignment.CenterVertically),
                iconUrl = iconUrl
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                //标题栏、作者栏、平台标签
                Row(
                    modifier = Modifier.height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    //标题栏、作者栏
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .basicMarquee(iterations = Int.MAX_VALUE),
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                        author?.let {
                            VerticalDivider(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(vertical = 4.dp)
                            )
                            Text(
                                modifier = Modifier.alpha(0.7f),
                                text = stringResource(R.string.download_assets_result_authors, it),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    }
                    //平台标签
                    PlatformIdentifier(platform = platform)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    //描述
                    Text(
                        modifier = Modifier.weight(1f),
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    //下载量、收藏量
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.alpha(0.7f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                modifier = Modifier.size(18.dp),
                                imageVector = Icons.Outlined.Download,
                                contentDescription = null
                            )
                            Text(
                                text = formatNumberByLocale(context, downloads),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        follows?.let {
                            Row(
                                modifier = Modifier.alpha(0.7f),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    modifier = Modifier.size(16.dp),
                                    imageVector = Icons.Outlined.FavoriteBorder,
                                    contentDescription = null
                                )
                                Text(
                                    text = formatNumberByLocale(context, it),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                //标签栏
                FlowRow(
                    modifier = Modifier.alpha(0.7f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    modloaders?.let {
                        it.forEach { modloader ->
                            Text(
                                text = modloader.getDisplayName(),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    categories?.let {
                        it.forEach { category ->
                            Text(
                                text = stringResource(category.getDisplayName()),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 平台标识元素，展示平台Logo + 平台名称
 */
@Composable
fun PlatformIdentifier(
    modifier: Modifier = Modifier,
    platform: Platform,
    iconSize: Dp = 12.dp,
    color: Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = MaterialTheme.colorScheme.onTertiary,
    shape: Shape = MaterialTheme.shapes.large,
    textStyle: TextStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
) {
    val drawable = when (platform) {
        Platform.CURSEFORGE -> R.drawable.ic_curseforge
        Platform.MODRINTH -> R.drawable.ic_modrinth
    }

    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = shape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Image(
                modifier = Modifier.size(iconSize),
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
                painter = painterResource(drawable),
                contentDescription = platform.displayName
            )
            Text(
                text = platform.displayName,
                style = textStyle
            )
        }
    }
}

/**
 * 资源封面网络图标
 * @param iconUrl 图标链接
 * @param triggerRefresh 强制刷新key
 */
@Composable
fun AssetsIcon(
    modifier: Modifier = Modifier,
    iconUrl: String? = null,
    triggerRefresh: Any? = null
) {
    val context = LocalContext.current

    val imageRequest = remember(iconUrl, triggerRefresh) {
        iconUrl?.takeIf { it.isNotBlank() }?.let {
            ImageRequest.Builder(context)
                .data(it)
                .listener(
                    onError = { _, result -> lWarning("Coil: error = ${result.throwable}") }
                )
                .crossfade(true)
                .build()
        }
    }

    AsyncImage(
        model = imageRequest,
        contentDescription = null,
        alignment = Alignment.Center,
        contentScale = ContentScale.Fit,
        placeholder = painterResource(R.drawable.ic_unknown_icon),
        error = painterResource(R.drawable.ic_unknown_icon),
        modifier = modifier
    )
}

/**
 * 搜索资源过滤器UI
 * @param enablePlatform 是否允许更改目标平台
 * @param searchPlatform 目标平台
 * @param searchName 搜索名称
 * @param gameVersion 游戏版本
 * @param sortField 排序方式
 * @param categories 可用资源类别列表
 * @param category 资源类别
 * @param enableModLoader 是否启用模组加载器过滤
 * @param modloaders 可用模组加载器列表
 * @param modloader 模组加载器
 */
@Composable
fun SearchFilter(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    enablePlatform: Boolean = true,
    searchPlatform: Platform,
    onPlatformChange: (Platform) -> Unit = {},
    searchName: String,
    onSearchNameChange: (String) -> Unit = {},
    gameVersion: String?,
    onGameVersionChange: (String?) -> Unit = {},
    sortField: PlatformSortField,
    onSortFieldChange: (PlatformSortField) -> Unit = {},
    categories: List<PlatformFilterCode>,
    category: PlatformFilterCode?,
    onCategoryChange: (PlatformFilterCode?) -> Unit = {},
    enableModLoader: Boolean = true,
    modloaders: List<PlatformDisplayLabel> = emptyList(),
    modloader: PlatformDisplayLabel? = null,
    onModLoaderChange: (PlatformDisplayLabel?) -> Unit = {}
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = searchName,
                onValueChange = onSearchNameChange,
                shape = MaterialTheme.shapes.large,
                label = {
                    Text(text = stringResource(R.string.download_assets_filter_search_name))
                },
                singleLine = true
            )
        }

        if (enablePlatform) {
            item {
                FilterListLayout(
                    modifier = Modifier.fillMaxWidth(),
                    items = Platform.entries,
                    selectedItem = searchPlatform,
                    onItemSelected = {
                        onPlatformChange(it!!)
                    },
                    getItemName = { it.displayName },
                    title = stringResource(R.string.download_assets_filter_search_platform),
                    cancelable = false
                )
            }
        }

        item {
            FilterListLayout(
                modifier = Modifier.fillMaxWidth(),
                items = allGameVersions,
                selectedItem = gameVersion,
                onItemSelected = {
                    onGameVersionChange(it)
                },
                getItemName = { it },
                title = stringResource(R.string.download_assets_filter_game_version)
            )
        }

        item {
            FilterListLayout(
                modifier = Modifier.fillMaxWidth(),
                items = PlatformSortField.entries,
                selectedItem = sortField,
                onItemSelected = {
                    onSortFieldChange(it!!)
                },
                getItemName = {
                    stringResource(it.getDisplayName())
                },
                title = stringResource(R.string.download_assets_filter_sort_field),
                cancelable = false
            )
        }

        item {
            FilterListLayout(
                modifier = Modifier.fillMaxWidth(),
                items = categories,
                selectedItem = category,
                onItemSelected = {
                    onCategoryChange(it)
                },
                getItemName = {
                    stringResource(it.getDisplayName())
                },
                title = stringResource(R.string.download_assets_filter_category)
            )
        }

        if (enableModLoader) {
            item {
                FilterListLayout(
                    modifier = Modifier.fillMaxWidth(),
                    items = modloaders,
                    selectedItem = modloader,
                    onItemSelected = {
                        onModLoaderChange(it)
                    },
                    getItemName = {
                        it.getDisplayName()
                    },
                    title = stringResource(R.string.download_assets_filter_modloader)
                )
            }
        }
    }
}

/**
 * 过滤器列表UI
 * @param items 可选的item
 * @param selectedItem 当前选中的item
 * @param onItemSelected 选中item时的回调
 * @param getItemName 获取item的显示名称
 * @param cancelable 是否允许取消选择（清除已选择的item）
 */
@Composable
private fun <E> FilterListLayout(
    modifier: Modifier = Modifier,
    items: List<E>,
    selectedItem: E?,
    onItemSelected: (E?) -> Unit,
    getItemName: @Composable (E) -> String,
    title: String,
    cancelable: Boolean = true,
    maxListHeight: Dp = 200.dp,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shadowElevation: Dp = 1.dp
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        shadowElevation = shadowElevation
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            FilterListHeader(
                modifier = Modifier.fillMaxWidth(),
                items = items,
                title = title,
                selectedName = selectedItem?.let { getItemName(it) },
                expanded = expanded,
                cancelable = cancelable,
                onClick = { expanded = !expanded },
                onClear = { onItemSelected(null) }
            )

            if (items.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically(animationSpec = getAnimateTween()),
                        exit = shrinkVertically(animationSpec = getAnimateTween()) + fadeOut(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = maxListHeight)
                                .padding(vertical = 4.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(items) { item ->
                                FilterListItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 4.dp),
                                    selected = selectedItem == item,
                                    itemName = getItemName(item),
                                    onClick = {
                                        if (expanded && selectedItem != item) {
                                            onItemSelected(item)
                                            expanded = false
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun <E> FilterListHeader(
    modifier: Modifier = Modifier,
    items: List<E>,
    title: String,
    selectedName: String? = null,
    expanded: Boolean,
    cancelable: Boolean = true,
    onClick: () -> Unit = {},
    onClear: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            LittleTextLabel(
                text = selectedName ?: stringResource(R.string.download_assets_filter_none),
                shape = MaterialTheme.shapes.small
            )
        }

        if (!items.isEmpty()) {
            Row(
                modifier = Modifier.padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val rotation by animateFloatAsState(
                    targetValue = if (expanded) -180f else 0f,
                    animationSpec = getAnimateTween()
                )
                Icon(
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotation),
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = stringResource(if (expanded) R.string.generic_expand else R.string.generic_collapse)
                )
                if (selectedName != null && cancelable) {
                    IconButton(
                        modifier = Modifier
                            .size(28.dp),
                        onClick = onClear
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = Icons.Outlined.Clear,
                            contentDescription = stringResource(R.string.generic_clear)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterListItem(
    modifier: Modifier = Modifier,
    selected: Boolean,
    itemName: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = itemName,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}