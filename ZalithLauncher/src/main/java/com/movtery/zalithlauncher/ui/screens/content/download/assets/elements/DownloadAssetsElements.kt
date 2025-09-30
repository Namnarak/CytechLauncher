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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDependencyType
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.ShimmerBox
import com.movtery.zalithlauncher.ui.components.itemLayoutColor
import com.movtery.zalithlauncher.ui.components.rememberMaxHeight
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
 * 版本、模组加载器 版本信息分组
 */
class VersionInfoMap(
    val gameVersion: String,
    val loader: PlatformDisplayLabel?,
    val dependencies: List<PlatformVersion.PlatformDependency>,
    val optionals: List<PlatformVersion.PlatformDependency>,
    val versions: List<PlatformVersion>,
    val isAdapt: Boolean
)

/**
 * 初始化全部版本数据，并筛选出成功初始化的所有版本
 */
suspend fun List<PlatformVersion>.initAll(
    currentProjectId: String,
    also: suspend (PlatformVersion) -> Unit = {}
): List<PlatformVersion> {
    return mapNotNull { version ->
        if (!version.initFile(currentProjectId)) return@mapNotNull null
        version.also {
            also(it)
        }
    }
}

fun List<PlatformVersion>.mapWithVersions(classes: PlatformClasses): List<VersionInfoMap> {
    val grouped = mutableMapOf<Pair<String, PlatformDisplayLabel?>, MutableList<PlatformVersion>>()

    forEach { version ->
        val labels = version.platformLoaders().ifEmpty { listOf(null) }
        version.platformGameVersion().forEach { gameVer ->
            labels.forEach { loaderLabel ->
                grouped.getOrPut(gameVer to loaderLabel) { mutableListOf() } += version
            }
        }
    }

    return grouped.map { (key, versions) ->
        //去重依赖集合
        val dependencies = versions
            .flatMap { it.platformDependencies() }
            .distinctBy { dep -> Pair(dep.projectId, dep.type) }

        VersionInfoMap(
            gameVersion = key.first,
            loader = key.second,
            dependencies = dependencies.filter { it.type == PlatformDependencyType.REQUIRED },
            optionals = dependencies.filter { it.type == PlatformDependencyType.OPTIONAL },
            versions = versions,
            isAdapt = when (classes) {
                PlatformClasses.MOD_PACK -> false //整合包将作为单独的版本下载，不再需要与现有版本进行匹配
                else -> isVersionAdapt(key.first, key.second)
            }
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
 * 当前资源版本是否与当前选择的游戏版本匹配
 */
private fun isVersionAdapt(gameVersion: String, loader: PlatformDisplayLabel?): Boolean {
    val currentVersion = VersionsManager.currentVersion
    return if (currentVersion == null) {
        false //没安装版本，无法判断
    } else {
        if (currentVersion.getVersionInfo()?.minecraftVersion != gameVersion) {
            false //游戏版本不匹配
        } else {
            //判断模组加载器匹配情况
            val loaderInfo = currentVersion.getVersionInfo()?.loaderInfo
            when {
                loader == null -> true //资源没有模组加载器信息，直接判定适配
                loaderInfo == null -> false //资源有模组加载器，但当前版本没有模组加载器信息，不适配
                else -> loaderInfo.loader.displayName.equals(loader.getDisplayName(), true)
            }
        }
    }
}

/**
 * 资源版本分组可折叠列表
 * @param defaultClasses    默认项目类型，跳转至依赖项目时。
 *                          若无法获取项目类型，将使用这个默认的项目类型
 * @param getDependency     根据项目Id获取依赖项目
 */
@Composable
fun AssetsVersionItemLayout(
    modifier: Modifier = Modifier,
    infoMap: VersionInfoMap,
    defaultClasses: PlatformClasses,
    getDependency: (projectId: String) -> PlatformProject?,
    maxListHeight: Dp = rememberMaxHeight(),
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shadowElevation: Dp = 1.dp,
    onItemClicked: (PlatformVersion) -> Unit = {},
    onDependencyClicked: (PlatformVersion.PlatformDependency, PlatformClasses) -> Unit
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
                isAdapt = infoMap.isAdapt,
                expanded = expanded,
                onClick = { expanded = !expanded }
            )

            if (infoMap.versions.isNotEmpty()) {
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
                            infoMap.dependencies.takeIf { it.isNotEmpty() }?.let { dependencies ->
                                val required = dependencies.mapNotNull { dependency ->
                                    getDependency(dependency.projectId)?.let { dependency to it }
                                }
                                dependencyLayout(
                                    list = required,
                                    titleRes = R.string.download_assets_dependency_projects,
                                    defaultClasses = defaultClasses,
                                    onDependencyClicked = onDependencyClicked
                                )
                            }
                            infoMap.optionals.takeIf { it.isNotEmpty() }?.let { optionals ->
                                val optional = optionals.mapNotNull { dependency ->
                                    getDependency(dependency.projectId)?.let { dependency to it }
                                }
                                dependencyLayout(
                                    list = optional,
                                    titleRes = R.string.download_assets_optional_projects,
                                    defaultClasses = defaultClasses,
                                    onDependencyClicked = onDependencyClicked
                                )
                            }
                            //分割线
                            if (infoMap.dependencies.isNotEmpty() || infoMap.optionals.isNotEmpty()) {
                                item {
                                    HorizontalDivider(
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp)
                                            .fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    )
                                }
                            }

                            items(infoMap.versions) { version ->
                                AssetsVersionListItem(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(all = 4.dp),
                                    version = version,
                                    onClick = {
                                        onItemClicked(version)
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

private fun LazyListScope.dependencyLayout(
    list: List<Pair<PlatformVersion.PlatformDependency, PlatformProject>>,
    titleRes: Int,
    defaultClasses: PlatformClasses,
    onDependencyClicked: (PlatformVersion.PlatformDependency, PlatformClasses) -> Unit
) {
    if (list.isNotEmpty()) {
        item {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = stringResource(titleRes),
                style = MaterialTheme.typography.labelLarge
            )
        }
        //前置项目列表
        items(list) { (dependency, dependencyProject) ->
            AssetsVersionDependencyItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 4.dp),
                project = dependencyProject,
                onClick = {
                    onDependencyClicked(dependency, dependencyProject.platformClasses(defaultClasses))
                }
            )
        }
    }
}

@Composable
private fun AssetsVersionHeadLayout(
    modifier: Modifier = Modifier,
    infoMap: VersionInfoMap,
    isAdapt: Boolean,
    expanded: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
        if (isAdapt) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = Icons.Filled.Star,
                contentDescription = null
            )
        }
        if (!infoMap.versions.isEmpty()) {
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
private fun AssetsVersionDependencyItem(
    modifier: Modifier = Modifier,
    project: PlatformProject,
    onClick: () -> Unit = {}
) {
    //项目基本信息
    val platform = remember { project.platform() }
    val title = remember { project.platformTitle() }
    val summary = remember { project.platformSummary() }
    val author = remember { project.platformAuthor() }
    val iconUrl = remember { project.platformIconUrl() }

    Row(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssetsIcon(
            modifier = Modifier
                .padding(all = 8.dp)
                .clip(shape = RoundedCornerShape(10.dp)),
            size = 42.dp,
            iconUrl = iconUrl
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            ProjectTitleHead(
                platform = platform,
                title = title,
                author = author
            )
            summary?.let { summary ->
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(2.dp))
    }
}

@Composable
private fun AssetsVersionListItem(
    modifier: Modifier = Modifier,
    version: PlatformVersion,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //直观的版本状态
        val releaseType = remember { version.platformReleaseType() }
        val displayName = remember { version.platformDisplayName() }
        val downloadCount = remember { version.platformDownloadCount() }
        val date = remember { version.platformDatePublished() }

        Box(
            modifier = Modifier
                .padding(start = 12.dp, end = 8.dp)
                .size(34.dp)
                .clip(shape = CircleShape)
                .background(releaseType.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = releaseType.name.take(1),
                style = MaterialTheme.typography.labelLarge,
                color = releaseType.color
            )
        }

        //版本简要信息
        Column(
            modifier = Modifier.padding(all = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = displayName,
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
                        text = formatNumberByLocale(context, downloadCount),
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
                            pastInstant = date
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
                        text = stringResource(releaseType.textRes),
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
    screenshot: PlatformProject.Screenshot
) {
    val context = LocalContext.current

    val imageRequest = remember(screenshot) {
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
            is AsyncImagePainter.State.Error, is AsyncImagePainter.State.Loading -> {
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