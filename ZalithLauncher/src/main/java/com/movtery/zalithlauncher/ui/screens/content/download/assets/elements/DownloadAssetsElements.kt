package com.movtery.zalithlauncher.ui.screens.content.download.assets.elements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformReleaseType
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.ShimmerBox
import com.movtery.zalithlauncher.ui.components.itemLayoutColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.formatNumberByLocale
import com.movtery.zalithlauncher.utils.getTimeAgo
import com.movtery.zalithlauncher.utils.string.compareVersion

sealed interface DownloadAssetsState<T> {
    class Getting<T> : DownloadAssetsState<T>
    data class Success<T>(val result: T) : DownloadAssetsState<T>
    data class Error<T>(val message: Int, val args: Array<Any>? = null) : DownloadAssetsState<T> {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Error<*>

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
 * 下载资源项目通用详细信息
 */
class DownloadProjectInfo(
    val platform: Platform,
    val iconUrl: String? = null,
    val title: String,
    val summary: String? = null,
    val urls: Urls,
    val screenshots: List<Screenshot> = emptyList()
) {
    /**
     * 资源项目各类外链
     * @param projectUrl 平台项目链接
     * @param sourceUrl 源代码仓库链接
     * @param issuesUrl 议题链接
     * @param wikiUrl wiki链接
     */
    class Urls(
        val projectUrl: String? = null,
        val sourceUrl: String? = null,
        val issuesUrl: String? = null,
        val wikiUrl: String? = null
    ) {
        companion object {
            fun Urls.isAllNull(): Boolean {
                return projectUrl == null &&
                        sourceUrl == null &&
                        issuesUrl == null &&
                        wikiUrl   == null
            }
        }
    }

    /**
     * 屏幕截图
     * @param imageUrl 图片链接
     * @param title 截图标题
     * @param description 截图描述
     */
    class Screenshot(
        val imageUrl: String,
        val title: String? = null,
        val description: String? = null
    )
}

/**
 * 下载资源版本通用详细信息
 */
class DownloadVersionInfo(
    val platform: Platform,
    val displayName: String,
    val fileName: String,
    val gameVersion: Array<String>,
    val loaders: List<PlatformDisplayLabel>,
    val releaseType: PlatformReleaseType,
    val downloadCount: Long,
    val downloadUrl: String,
    val date: String,
    val sha1: String? = null,
    val fileSize: Long
)

/**
 * 版本、模组加载器 版本信息分组
 */
class VersionInfoMap(
    val gameVersion: String,
    val loader: PlatformDisplayLabel?,
    val infos: List<DownloadVersionInfo>
)

fun List<DownloadVersionInfo>.mapWithVersions(): List<VersionInfoMap> {
    val grouped = mutableMapOf<Pair<String, PlatformDisplayLabel?>, MutableList<DownloadVersionInfo>>()

    forEach { versionInfo ->
        val labels = versionInfo.loaders.ifEmpty {
            listOf(null) //无加载器
        }

        //为每个游戏版本和模组加载器组合分组
        versionInfo.gameVersion.forEach { gameVer ->
            labels.forEach { loaderLabel ->
                grouped.getOrPut(gameVer to loaderLabel) { mutableListOf() } += versionInfo
            }
        }
    }

    return grouped.map { (key, infos) ->
        VersionInfoMap(
            gameVersion = key.first,
            loader = key.second,
            infos = infos
        )
    }.sortedByVersionAndLoader()
}

private fun List<VersionInfoMap>.sortedByVersionAndLoader(): List<VersionInfoMap> {
    return sortedWith { a, b ->
        // 比较版本号
        val versionCompare = -a.gameVersion.compareVersion(b.gameVersion)
        if (versionCompare != 0) {
            versionCompare
        } else {
            when {
                a.loader == null && b.loader == null -> 0
                a.loader == null -> 1
                b.loader == null -> -1
                else -> a.loader.getDisplayName().compareTo(b.loader.getDisplayName())
            }
        }
    }
}

/**
 * 资源版本分组可折叠列表
 */
@Composable
fun AssetsVersionItemLayout(
    modifier: Modifier = Modifier,
    infoMap: VersionInfoMap,
    maxListHeight: Dp = 200.dp,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shadowElevation: Dp = 1.dp,
    onItemClicked: (DownloadVersionInfo) -> Unit = {}
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
            AssetsVersionHeadLayout(
                modifier = Modifier.fillMaxWidth(),
                infoMap = infoMap,
                expanded = expanded,
                onClick = { expanded = !expanded }
            )

            if (infoMap.infos.isNotEmpty()) {
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
                            items(infoMap.infos) { info ->
                                AssetsVersionListItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 4.dp),
                                    info = info,
                                    onClick = {
                                        onItemClicked(info)
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
private fun AssetsVersionHeadLayout(
    modifier: Modifier = Modifier,
    infoMap: VersionInfoMap,
    expanded: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = infoMap.gameVersion,
                style = MaterialTheme.typography.titleSmall
            )
            infoMap.loader?.let { loader ->
                LittleTextLabel(
                    text = loader.getDisplayName(),
                    shape = MaterialTheme.shapes.small
                )
            }
        }
        if (!infoMap.infos.isEmpty()) {
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
            }
        }
    }
}

@Composable
private fun AssetsVersionListItem(
    modifier: Modifier = Modifier,
    info: DownloadVersionInfo,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //直观的版本状态
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .size(34.dp)
                .clip(shape = CircleShape)
                .background(info.releaseType.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = info.releaseType.name.take(1),
                style = MaterialTheme.typography.labelLarge,
                color = info.releaseType.color
            )
        }

        //版本简要信息
        Column(
            modifier = Modifier.padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = info.displayName,
                style = MaterialTheme.typography.labelLarge
            )

            val context = LocalContext.current

            Row(
                modifier = Modifier.alpha(0.7f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                //下载量
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Outlined.Download,
                        contentDescription = null
                    )
                    Text(
                        text = formatNumberByLocale(context, info.downloadCount),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                //更新时间
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Outlined.Autorenew,
                        contentDescription = null
                    )
                    Text(
                        text = getTimeAgo(
                            context = LocalContext.current,
                            dateString = info.date
                        ),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                //版本状态
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(R.drawable.ic_package_2),
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(info.releaseType.textRes),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

/**
 * 屏幕截图与描述UI
 */
@Composable
fun ScreenshotItemLayout(
    modifier: Modifier = Modifier,
    screenshot: DownloadProjectInfo.Screenshot
) {
    val context = LocalContext.current

    //重载key，加载失败后，允许通过这个key重新加载截图
    var reloadTrigger by remember { mutableStateOf(false) }

    val imageRequest = remember(screenshot, reloadTrigger) {
        screenshot.imageUrl.takeIf { it.isNotBlank() }?.let {
            ImageRequest.Builder(context)
                .data(it)
                .crossfade(true)
                .build()
        }
    }

    val painter = rememberAsyncImagePainter(
        model = imageRequest,
        placeholder = null,
        error = painterResource(R.drawable.ic_unknown_icon)
    )

    val state by painter.state.collectAsState()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (state) {
            AsyncImagePainter.State.Empty -> {
                //NONE
            }
            is AsyncImagePainter.State.Error -> {
                IconTextButton(
                    onClick = { reloadTrigger = !reloadTrigger },
                    imageVector = Icons.Default.Refresh,
                    text = stringResource(R.string.download_assets_screenshot_reload)
                )
            }
            is AsyncImagePainter.State.Loading -> {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )
            }
            is AsyncImagePainter.State.Success -> {
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    alignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(painter.intrinsicSize.width / painter.intrinsicSize.height)
                )
            }
        }

        //标题与简介部分
        if (screenshot.title != null && screenshot.title == screenshot.description) {
            //标题与简介内容相同，则不需要两个都显示
            //会有作者喜欢把标题与简介设置成一样的内容
            Text(
                text = screenshot.title,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        } else {
            screenshot.title?.let { title ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center
                )
            }
            screenshot.description?.let { description ->
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}