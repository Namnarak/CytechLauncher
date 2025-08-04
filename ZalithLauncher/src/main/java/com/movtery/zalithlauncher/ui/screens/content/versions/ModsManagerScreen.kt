package com.movtery.zalithlauncher.ui.screens.content.versions

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.mod.AllModReader
import com.movtery.zalithlauncher.game.version.mod.LocalMod
import com.movtery.zalithlauncher.game.version.mod.LocalMod.Companion.isDisabled
import com.movtery.zalithlauncher.game.version.mod.LocalMod.Companion.isEnabled
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.ScalingLabel
import com.movtery.zalithlauncher.ui.components.SimpleTextInputField
import com.movtery.zalithlauncher.ui.components.TooltipIconButton
import com.movtery.zalithlauncher.ui.components.itemLayoutColor
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ByteArrayIcon
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.LoadingState
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.ModsOperation
import com.movtery.zalithlauncher.ui.screens.content.versions.elements.filterMods
import com.movtery.zalithlauncher.ui.screens.content.versions.layouts.VersionSettingsBackground
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.isNotEmptyOrBlank
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import java.io.File

@Composable
fun ModsManagerScreen(
    mainScreenKey: NavKey?,
    versionsScreenKey: NavKey?,
    version: Version
) {
    BaseScreen(
        levels1 = listOf(
            Pair(NestedNavKey.Versions::class.java, mainScreenKey)
        ),
        Triple(NormalNavKey.Versions.ModsManager, versionsScreenKey, false),
    ) { isVisible ->
        val modsDir = File(version.getGameDir(), VersionFolders.MOD.folderName)
        val modReader = remember(modsDir) { AllModReader(modsDir) }

        //触发刷新
        var refreshTrigger by remember { mutableStateOf(false) }
        var nameFilter by remember { mutableStateOf("") }

        var allMods by remember { mutableStateOf<List<LocalMod>>(emptyList()) }
        val filteredMods by remember(allMods) {
            derivedStateOf {
                allMods.takeIf { it.isNotEmpty() }?.filterMods(nameFilter)
            }
        }

        var modsState by remember { mutableStateOf<LoadingState>(LoadingState.None) }

        val yOffset by swapAnimateDpAsState(
            targetValue = (-40).dp,
            swapIn = isVisible
        )

        VersionSettingsBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 12.dp)
                .offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
            paddingValues = PaddingValues()
        ) {
            val operationScope = rememberCoroutineScope()

            when (modsState) {
                LoadingState.None -> {
                    val itemColor = itemLayoutColor()
                    val itemContentColor = MaterialTheme.colorScheme.onSurface

                    var modsOperation by remember { mutableStateOf<ModsOperation>(ModsOperation.None) }
                    fun runProgress(task: () -> Unit) {
                        operationScope.launch(Dispatchers.IO) {
                            modsOperation = ModsOperation.Progress
                            task()
                            modsOperation = ModsOperation.None
                            refreshTrigger = !refreshTrigger
                        }
                    }
                    ModsOperation(
                        modsOperation = modsOperation,
                        updateOperation = { modsOperation = it },
                        onDelete = { mod ->
                            runProgress {
                                FileUtils.deleteQuietly(mod.file)
                            }
                        }
                    )

                    Column {
                        ModsActionsHeader(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .padding(top = 4.dp)
                                .fillMaxWidth(),
                            inputFieldColor = itemColor,
                            inputFieldContentColor = itemContentColor,
                            nameFilter = nameFilter,
                            onNameFilterChange = { nameFilter = it },
                            refresh = { refreshTrigger = !refreshTrigger }
                        )

                        ModsList(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            modsList = filteredMods,
                            onEnable = { mod ->
                                operationScope.launch(Dispatchers.IO) {
                                    mod.enable()
                                }
                            },
                            onDisable = { mod ->
                                operationScope.launch(Dispatchers.IO) {
                                    mod.disable()
                                }
                            },
                            onDelete = { mod ->
                                modsOperation = ModsOperation.Delete(mod)
                            },
                            itemColor = itemColor,
                            itemContentColor = itemContentColor
                        )
                    }
                }
                LoadingState.Loading -> {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
            }
        }

        LaunchedEffect(refreshTrigger) {
            modsState = LoadingState.Loading
            try {
                allMods = modReader.readAllMods()
            } catch (_: CancellationException) {
                //已取消
            }
            modsState = LoadingState.None
        }
    }
}

@Composable
private fun ModsActionsHeader(
    modifier: Modifier,
    inputFieldColor: Color,
    inputFieldContentColor: Color,
    nameFilter: String,
    onNameFilterChange: (String) -> Unit = {},
    refresh: () -> Unit = {}
) {
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SimpleTextInputField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                    value = nameFilter,
                    onValueChange = { onNameFilterChange(it) },
                    hint = {
                        Text(
                            text = stringResource(R.string.generic_search),
                            style = TextStyle(color = LocalContentColor.current).copy(fontSize = 12.sp)
                        )
                    },
                    color = inputFieldColor,
                    contentColor = inputFieldContentColor,
                    singleLine = true
                )

                IconButton(
                    onClick = refresh
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.generic_refresh)
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ModsList(
    modifier: Modifier = Modifier,
    modsList: List<LocalMod>?,
    onEnable: (LocalMod) -> Unit,
    onDisable: (LocalMod) -> Unit,
    onDelete: (LocalMod) -> Unit,
    itemColor: Color,
    itemContentColor: Color,
) {
    modsList?.let { list ->
        //如果列表是空的，则是由搜索导致的
        if (list.isNotEmpty()) {
            LazyColumn(
                modifier = modifier,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                items(list) { mod ->
                    ModItemLayout(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        mod = mod,
                        onEnable = {
                            onEnable(mod)
                        },
                        onDisable = {
                            onDisable(mod)
                        },
                        onDelete = {
                            onDelete(mod)
                        },
                        itemColor = itemColor,
                        itemContentColor = itemContentColor
                    )
                }
            }
        }
    } ?: run {
        //如果为null，则代表本身就没有模组可以展示
        Box(modifier = Modifier.fillMaxSize()) {
            ScalingLabel(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(R.string.mods_manage_no_mods)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModItemLayout(
    modifier: Modifier = Modifier,
    mod: LocalMod,
    onClick: () -> Unit = {},
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onDelete: () -> Unit,
    itemColor: Color,
    itemContentColor: Color,
    shadowElevation: Dp = 1.dp
) {
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }

    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = itemColor,
        contentColor = itemContentColor,
        shadowElevation = shadowElevation
    ) {
        Row(
            modifier = Modifier.padding(all = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            //模组的封面图标
            ModIcon(
                modifier = Modifier
                    .size(48.dp)
                    .clip(shape = RoundedCornerShape(10.dp)),
                mod = mod
            )

            //模组简要信息
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!mod.notMod) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                            text = mod.name,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1
                        )
                        LittleTextLabel(
                            text = mod.loader.displayName,
                            shape = MaterialTheme.shapes.small
                        )
                    }

                    Text(
                        modifier = Modifier
                            .alpha(0.7f)
                            .basicMarquee(iterations = Int.MAX_VALUE),
                        text = stringResource(R.string.generic_file_name, mod.file.name),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                } else {
                    //非模组，只展示文件名称
                    Text(
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        text = mod.file.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1
                    )
                    LittleTextLabel(
                        text = stringResource(R.string.generic_unknown),
                        shape = MaterialTheme.shapes.small
                    )
                }
            }

            Row(
                modifier = Modifier.align(Alignment.CenterVertically),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!mod.notMod) {
                    //启用/禁用
                    Checkbox(
                        checked = mod.file.isEnabled(),
                        onCheckedChange = { checked ->
                            if (checked) onEnable()
                            else onDisable()
                        }
                    )

                    //详细信息展示
                    TooltipIconButton(
                        modifier = Modifier.size(38.dp),
                        tooltip = {
                            RichTooltip(
                                modifier = Modifier.padding(all = 3.dp),
                                title = { Text(text = stringResource(R.string.mods_manage_info)) },
                                shadowElevation = 3.dp
                            ) {
                                ModInfoTooltip(mod)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = stringResource(R.string.saves_manage_info)
                        )
                    }
                }

                IconButton(
                    modifier = Modifier.size(38.dp),
                    onClick = onDelete
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.generic_delete)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModIcon(
    modifier: Modifier = Modifier,
    mod: LocalMod
) {
    val colorMatrix = remember(mod, mod.file) { ColorMatrix() }
    colorMatrix.setToSaturation(
        if (mod.file.isDisabled()) 0f
        else 1f
    )

    ByteArrayIcon(
        modifier = modifier,
        triggerRefresh = mod,
        icon = mod.icon,
        colorFilter = ColorFilter.colorMatrix(colorMatrix)
    )
}

@Composable
private fun ModInfoTooltip(
    mod: LocalMod
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        //文件大小
        Text(text = stringResource(R.string.generic_file_size, formatFileSize(mod.fileSize)))
        //模组版本
        mod.version?.let { version ->
            Text(text = stringResource(R.string.mods_manage_version, version))
        }
        //作者
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = stringResource(R.string.mods_manage_authors))
            FlowRow(
                modifier = Modifier.weight(1f, fill = false),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                mod.authors.forEach { author ->
                    Text(text = author)
                }
            }
        }
        //模组描述
        mod.description?.takeIf { it.isNotEmptyOrBlank() }?.let { description ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = stringResource(R.string.mods_manage_description))
                Text(
                    modifier = Modifier.weight(1f, fill = false),
                    text = description
                )
            }
        }
    }
}

