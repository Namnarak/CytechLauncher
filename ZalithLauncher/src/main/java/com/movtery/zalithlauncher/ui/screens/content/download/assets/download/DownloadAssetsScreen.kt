package com.movtery.zalithlauncher.ui.screens.content.download.assets.download

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.outlined.ImportContacts
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearch
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeFile
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeFile.Companion.fixedFileUrl
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeModLoader
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeProject
import com.movtery.zalithlauncher.game.download.assets.platform.getProject
import com.movtery.zalithlauncher.game.download.assets.platform.getVersions
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthModLoaderCategory
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthSingleProject
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthVersion
import com.movtery.zalithlauncher.game.download.assets.type.RELEASE_REGEX
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.ContentCheckBox
import com.movtery.zalithlauncher.ui.components.IconTextButton
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.ShimmerBox
import com.movtery.zalithlauncher.ui.components.SimpleTextInputField
import com.movtery.zalithlauncher.ui.components.itemLayoutColor
import com.movtery.zalithlauncher.ui.screens.content.DownloadScreenKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsIcon
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsVersionItemLayout
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadAssetsState
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadProjectInfo
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadProjectInfo.Urls.Companion.isAllNull
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadVersionInfo
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.ScreenshotItemLayout
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.VersionInfoMap
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.mapWithVersions
import com.movtery.zalithlauncher.ui.screens.main.elements.mainScreenKey
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import com.movtery.zalithlauncher.utils.network.NetWorkUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import java.io.FileNotFoundException
import java.io.IOException

@Serializable
data class DownloadAssetsScreenKey(
    val platform: Platform,
    val projectId: String,
    val classes: PlatformClasses
): NavKey

private class ScreenViewModel(
    private val platform: Platform,
    private val projectId: String
): ViewModel() {
    //版本
    var currentVersionsJob by mutableStateOf<Job?>(null)
    private var _versionsList by mutableStateOf<List<VersionInfoMap>>(emptyList())
    var versionsResult by mutableStateOf<DownloadAssetsState<List<VersionInfoMap>>>(DownloadAssetsState.Getting())

    var showOnlyMCRelease by mutableStateOf(true)
    var searchMCVersion by mutableStateOf("")

    fun filterWith(
        showOnlyMCRelease: Boolean = this.showOnlyMCRelease,
        searchMCVersion: String = this.searchMCVersion
    ) {
        currentVersionsJob?.cancel()

        this.showOnlyMCRelease = showOnlyMCRelease
        this.searchMCVersion = searchMCVersion
        currentVersionsJob = viewModelScope.launch {
            val infos = _versionsList.filterInfos()
            versionsResult = DownloadAssetsState.Success(infos)
        }
    }

    private fun List<VersionInfoMap>.filterInfos(): List<VersionInfoMap> {
        return filter { info ->
            (!showOnlyMCRelease || RELEASE_REGEX.matcher(info.gameVersion).find()) &&
                    (searchMCVersion.isEmpty() || info.gameVersion.contains(searchMCVersion, true))
        }
    }

    fun getVersions() {
        currentVersionsJob?.cancel()
        currentVersionsJob = viewModelScope.launch {
            versionsResult = DownloadAssetsState.Getting()
            getVersions(
                projectID = projectId,
                platform = platform,
                onSuccess = { result ->
                    val infos: List<DownloadVersionInfo> = result.mapNotNull { version ->
                        when (version) {
                            is ModrinthVersion -> {
                                val files = version.files.takeIf { it.isNotEmpty() } ?: run {
                                    lWarning("No file list available, skipping -> ${version.name}")
                                    return@mapNotNull null
                                }
                                val file = files.find { it.primary } ?: files[0] //仅下载主文件
                                DownloadVersionInfo(
                                    platform = Platform.MODRINTH,
                                    displayName = version.name,
                                    fileName = file.fileName,
                                    gameVersion = version.gameVersions,
                                    loaders = version.loaders.mapNotNull { loaderName ->
                                        ModrinthModLoaderCategory.entries.find { it.facetValue() == loaderName }
                                    },
                                    releaseType = version.versionType,
                                    downloadCount = version.downloads,
                                    downloadUrl = file.url,
                                    date = version.datePublished,
                                    sha1 = file.hashes.sha1,
                                    fileSize = file.size
                                )
                            }
                            is CurseForgeFile -> {
                                val file = version.takeIf { it.fileName != null && it.fixedFileUrl() != null }
                                    // 文件名或者下载链接为空
                                    // 单独获取该文件信息
                                    ?: run {
                                        val fileId = version.id.toString()
                                        runCatching {
                                            PlatformSearch.getVersionFromCurseForge(
                                                projectID = projectId,
                                                fileID = fileId
                                            ).data
                                        }.onFailure { e ->
                                            when (e) {
                                                is FileNotFoundException -> lWarning("Could not query api.curseforge.com for deleted mods: $projectId, $fileId", e)
                                                is IOException, is SerializationException -> lWarning("Unable to fetch the file name projectID=$projectId, fileID=$fileId", e)
                                            }
                                        }.getOrNull() ?: return@mapNotNull null
                                    }

                                val downloadUrl = file.fixedFileUrl() ?: run {
                                    lWarning("No download link available, projectID=$projectId, fileID=${file.id}")
                                    return@mapNotNull null
                                }

                                val gameVersions = file.gameVersions.filter { gameVersion ->
                                    RELEASE_REGEX.matcher(gameVersion).find()
                                }.toTypedArray()

                                val sha1 = file.hashes.find { hash ->
                                    hash.algo == CurseForgeFile.Hash.Algo.SHA1
                                }

                                DownloadVersionInfo(
                                    platform = Platform.CURSEFORGE,
                                    displayName = file.displayName,
                                    fileName = file.fileName!!,
                                    gameVersion = gameVersions,
                                    loaders = file.gameVersions.mapNotNull { loaderName ->
                                        CurseForgeModLoader.entries.find {
                                            it.getDisplayName().equals(loaderName, true)
                                        }
                                    },
                                    releaseType = file.releaseType,
                                    downloadCount = file.downloadCount,
                                    downloadUrl = downloadUrl.also {
                                        println(it)
                                    },
                                    date = file.fileDate,
                                    sha1 = sha1?.value,
                                    fileSize = file.fileLength
                                )
                            }
                            else -> error("Unknown version type: $version")
                        }
                    }

                    _versionsList = infos.mapWithVersions()
                    versionsResult = DownloadAssetsState.Success(_versionsList.filterInfos())
                },
                onError = {
                    versionsResult = it
                }
            )
        }
    }

    //项目信息
    var currentProjectJob by mutableStateOf<Job?>(null)
    var projectResult by mutableStateOf<DownloadAssetsState<PlatformProject>>(DownloadAssetsState.Getting())

    fun getProject() {
        currentProjectJob?.cancel()

        currentProjectJob = viewModelScope.launch {
            projectResult = DownloadAssetsState.Getting()
            getProject(
                projectID = projectId,
                platform = platform,
                onSuccess = { result ->
                    projectResult = DownloadAssetsState.Success(result)
                },
                onError = {
                    projectResult = it
                }
            )
        }
    }

    init {
        //初始化后，获取项目、版本信息
        getVersions()
        getProject()
    }

    override fun onCleared() {
        currentVersionsJob?.cancel()
        currentProjectJob?.cancel()
    }
}

@Composable
private fun rememberDownloadAssetsViewModel(
    key: DownloadAssetsScreenKey
): ScreenViewModel {
    return viewModel(
        key = key.toString()
    ) {
        ScreenViewModel(
            platform = key.platform,
            projectId = key.projectId
        )
    }
}

/**
 * @param parentScreenKey 父屏幕Key
 * @param parentCurrentKey 父屏幕当前Key
 * @param currentKey 当前的Key
 */
@Composable
fun DownloadAssetsScreen(
    parentScreenKey: NavKey,
    parentCurrentKey: NavKey?,
    currentKey: NavKey?,
    key: DownloadAssetsScreenKey,
    onItemClicked: (DownloadVersionInfo) -> Unit = {}
) {
    val viewModel: ScreenViewModel = rememberDownloadAssetsViewModel(key)

    BaseScreen(
        levels1 = listOf(
            Pair(DownloadScreenKey::class.java, mainScreenKey),
            Pair(DownloadAssetsScreenKey::class.java, currentKey)
        ),
        Triple(parentScreenKey, parentCurrentKey, false)
    ) { isVisible ->
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            val yOffset by swapAnimateDpAsState(targetValue = (-40).dp, swapIn = isVisible)
            Versions(
                modifier = Modifier
                    .weight(6.5f)
                    .fillMaxHeight()
                    .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                viewModel = viewModel,
                onReload = { viewModel.getVersions() },
                onItemClicked = onItemClicked
            )

            val xOffset by swapAnimateDpAsState(
                targetValue = 40.dp,
                swapIn = isVisible,
                isHorizontal = true
            )
            ProjectInfo(
                modifier = Modifier
                    .weight(3.5f)
                    .fillMaxHeight()
                    .padding(vertical = 12.dp)
                    .padding(end = 12.dp)
                    .offset { IntOffset(x = xOffset.roundToPx(), y = 0) },
                projectResult = viewModel.projectResult,
                platformClasses = key.classes,
                onReload = { viewModel.getProject() }
            )
        }
    }
}

/**
 * 所有版本列表
 */
@Composable
private fun Versions(
    modifier: Modifier = Modifier,
    viewModel: ScreenViewModel,
    onReload: () -> Unit = {},
    onItemClicked: (DownloadVersionInfo) -> Unit = {}
) {
    when (val versions = viewModel.versionsResult) {
        is DownloadAssetsState.Getting -> {
            Box(modifier.padding(all = 12.dp)) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
        is DownloadAssetsState.Success -> {
            Column(
                modifier = modifier
            ) {
                //简单过滤条件
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ContentCheckBox(
                        checked = viewModel.showOnlyMCRelease,
                        onCheckedChange = { viewModel.filterWith(showOnlyMCRelease = it) }
                    ) {
                        Text(
                            text = stringResource(R.string.download_assets_show_only_mc_release),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    SimpleTextInputField(
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        value = viewModel.searchMCVersion,
                        onValueChange = { viewModel.filterWith(searchMCVersion = it) },
                        color = itemLayoutColor(),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        singleLine = true,
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface).copy(fontSize = 12.sp),
                        hint = {
                            Text(
                                text = stringResource(R.string.download_assets_search_mc_versions),
                                style = TextStyle(color = MaterialTheme.colorScheme.onSurface).copy(fontSize = 12.sp)
                            )
                        }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    items(versions.result) { info ->
                        AssetsVersionItemLayout(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            infoMap = info,
                            onItemClicked = onItemClicked
                        )
                    }
                }
            }
        }
        is DownloadAssetsState.Error -> {
            Box(modifier.padding(all = 12.dp)) {
                val message = if (versions.args != null) {
                    stringResource(versions.message, *versions.args)
                } else {
                    stringResource(versions.message)
                }

                ScalingLabel(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(R.string.download_assets_failed_to_get_versions, message),
                    onClick = onReload
                )
            }
        }
    }
}

/**
 * 项目信息板块
 */
@Composable
private fun ProjectInfo(
    modifier: Modifier = Modifier,
    projectResult: DownloadAssetsState<PlatformProject>,
    platformClasses: PlatformClasses,
    onReload: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        when (val result = projectResult) {
            is DownloadAssetsState.Getting -> {
                LazyColumn(
                    contentPadding = PaddingValues(all = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    //图标、标题、简介的骨架
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ShimmerBox(
                                modifier = Modifier.size(72.dp)
                            )
                            Column(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                //标题
                                ShimmerBox(
                                    modifier = Modifier
                                        .fillMaxWidth(0.6f)
                                        .height(20.dp),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                //简介
                                ShimmerBox(
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .height(16.dp),
                                    shape = RoundedCornerShape(4.dp)
                                )
                            }
                        }
                    }
                }
            }
            is DownloadAssetsState.Success -> {
                val project = result.result
                val info = when (project) {
                    is ModrinthSingleProject -> {
                        DownloadProjectInfo(
                            platform = Platform.MODRINTH,
                            iconUrl = project.iconUrl,
                            title = project.title,
                            summary = project.description,
                            urls = DownloadProjectInfo.Urls(
                                projectUrl = "https://modrinth.com/${platformClasses.modrinth!!.facetValue()}/${project.slug}",
                                sourceUrl = project.sourceUrl,
                                issuesUrl = project.issuesUrl,
                                wikiUrl = project.wikiUrl
                            ),
                            screenshots = project.gallery.map { gallery ->
                                DownloadProjectInfo.Screenshot(
                                    imageUrl = gallery.url,
                                    title = gallery.title,
                                    description = gallery.description
                                )
                            }
                        )
                    }
                    is CurseForgeProject -> {
                        val data = project.data
                        DownloadProjectInfo(
                            platform = Platform.CURSEFORGE,
                            iconUrl = data.logo.url,
                            title = data.name,
                            summary = data.summary,
                            urls = DownloadProjectInfo.Urls(
                                projectUrl = "https://www.curseforge.com/minecraft/${platformClasses.curseforge.slug}/${data.slug}",
                                sourceUrl = data.links.sourceUrl,
                                issuesUrl = data.links.issuesUrl,
                                wikiUrl = data.links.wikiUrl
                            ),
                            screenshots = data.screenshots.map { screenshot ->
                                DownloadProjectInfo.Screenshot(
                                    imageUrl = screenshot.url,
                                    title = screenshot.title,
                                    description = screenshot.description
                                )
                            }
                        )
                    }
                    else -> error("Unknown project type: $project")
                }

                LazyColumn(
                    contentPadding = PaddingValues(all = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    //图标、标题、简介
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AssetsIcon(
                                modifier = Modifier.size(72.dp),
                                iconUrl = info.iconUrl
                            )
                            //标题、简介
                            Column(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = info.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                                info.summary?.let { summary ->
                                    Text(
                                        text = summary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    //相关链接
                    if (!info.urls.isAllNull()) {
                        item {
                            val context = LocalContext.current
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.download_assets_links),
                                    style = MaterialTheme.typography.titleMedium
                                )

                                info.urls.projectUrl?.let { url ->
                                    IconTextButton(
                                        onClick = { NetWorkUtils.openLink(context, url) },
                                        iconSize = 18.dp,
                                        painter = when (info.platform) {
                                            Platform.CURSEFORGE -> painterResource(R.drawable.ic_curseforge)
                                            Platform.MODRINTH -> painterResource(R.drawable.ic_modrinth)
                                        },
                                        text = stringResource(R.string.download_assets_project_link)
                                    )
                                }
                                info.urls.sourceUrl?.let { url ->
                                    IconTextButton(
                                        onClick = { NetWorkUtils.openLink(context, url) },
                                        iconSize = 18.dp,
                                        imageVector = Icons.Default.Code,
                                        text = stringResource(R.string.download_assets_source_link)
                                    )
                                }
                                info.urls.issuesUrl?.let { url ->
                                    IconTextButton(
                                        onClick = { NetWorkUtils.openLink(context, url) },
                                        iconSize = 18.dp,
                                        painter = painterResource(R.drawable.ic_chat_info),
                                        text = stringResource(R.string.download_assets_issues_link)
                                    )
                                }
                                info.urls.wikiUrl?.let { url ->
                                    IconTextButton(
                                        onClick = { NetWorkUtils.openLink(context, url) },
                                        iconSize = 18.dp,
                                        imageVector = Icons.Outlined.ImportContacts,
                                        text = stringResource(R.string.download_assets_wiki_link)
                                    )
                                }
                            }
                        }
                    }

                    //屏幕截图
                    items(info.screenshots) { screenshot ->
                        ScreenshotItemLayout(
                            modifier = Modifier.fillMaxWidth(),
                            screenshot = screenshot
                        )
                    }
                }
            }
            is DownloadAssetsState.Error -> {
                Box(Modifier
                    .fillMaxSize()
                    .padding(all = 12.dp)) {
                    val message = if (result.args != null) {
                        stringResource(result.message, *result.args)
                    } else {
                        stringResource(result.message)
                    }

                    ScalingLabel(
                        modifier = Modifier.align(Alignment.Center),
                        text = stringResource(R.string.download_assets_failed_to_get_project, message),
                        onClick = onReload
                    )
                }
            }
        }
    }
}