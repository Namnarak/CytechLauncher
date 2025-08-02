package com.movtery.zalithlauncher.ui.screens.content.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.outlined.Copyright
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.plugin.ApkPlugin
import com.movtery.zalithlauncher.game.plugin.PluginLoader
import com.movtery.zalithlauncher.game.plugin.appCacheIcon
import com.movtery.zalithlauncher.info.InfoDistributor
import com.movtery.zalithlauncher.path.UrlManager
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.itemLayoutColor
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsBackground
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.network.NetWorkUtils

private const val COPYRIGHT_AOSP = "Copyright © The Android Open Source Project"
private const val COPYRIGHT_KTOR = "Copyright © 2000-2023 JetBrains s.r.o."
private const val LICENSE_APACHE_2 = "Apache License 2.0"
private const val LICENSE_MIT = "MIT License"
private const val LICENSE_LGPL_3 = "LGPL-3.0 License"
private const val URL_KTOR = "https://ktor.io"

private val libraryData = listOf(
    LibraryInfo("androidx-constraintlayout-compose", COPYRIGHT_AOSP, LICENSE_APACHE_2, "https://developer.android.com/develop/ui/compose/layouts/constraintlayout"),
    LibraryInfo("androidx-material-icons-core", COPYRIGHT_AOSP, LICENSE_APACHE_2, "https://developer.android.com/jetpack/androidx/releases/compose-material"),
    LibraryInfo("androidx-material-icons-extended", COPYRIGHT_AOSP, LICENSE_APACHE_2, "https://developer.android.com/jetpack/androidx/releases/compose-material"),
    LibraryInfo("Apache Commons Codec", null, LICENSE_APACHE_2, "https://commons.apache.org/proper/commons-codec"),
    LibraryInfo("Apache Commons Compress", null, LICENSE_APACHE_2, "https://commons.apache.org/proper/commons-compress"),
    LibraryInfo("Apache Commons IO", null, LICENSE_APACHE_2, "https://commons.apache.org/proper/commons-io"),
    LibraryInfo("ByteHook", "Copyright © 2020-2024 ByteDance, Inc.", LICENSE_MIT, "https://github.com/bytedance/bhook"),
    LibraryInfo("Coil Compose", "Copyright © 2025 Coil Contributors", LICENSE_APACHE_2, "https://github.com/coil-kt/coil"),
    LibraryInfo("Coil Gifs", "Copyright © 2025 Coil Contributors", LICENSE_APACHE_2, "https://github.com/coil-kt/coil"),
    LibraryInfo("colorpicker-compose", "Copyright © 2022 skydoves (Jaewoong Eum)", LICENSE_APACHE_2, "https://github.com/skydoves/colorpicker-compose"),
    LibraryInfo("Gson", "Copyright © 2008 Google Inc.", LICENSE_APACHE_2, "https://github.com/google/gson"),
    LibraryInfo("kotlinx.coroutines", "Copyright © 2000-2020 JetBrains s.r.o.", LICENSE_APACHE_2, "https://github.com/Kotlin/kotlinx.coroutines"),
    LibraryInfo("ktor-client-cio", COPYRIGHT_KTOR, LICENSE_APACHE_2, URL_KTOR),
    LibraryInfo("ktor-client-content-negotiation", COPYRIGHT_KTOR, LICENSE_APACHE_2, URL_KTOR),
    LibraryInfo("ktor-client-core", COPYRIGHT_KTOR, LICENSE_APACHE_2, URL_KTOR),
    LibraryInfo("ktor-http", COPYRIGHT_KTOR, LICENSE_APACHE_2, URL_KTOR),
    LibraryInfo("ktor-serialization-kotlinx-json", COPYRIGHT_KTOR, LICENSE_APACHE_2, URL_KTOR),
    LibraryInfo("material-color-utilities", "Copyright 2021 Google LLC", LICENSE_APACHE_2, "https://github.com/material-foundation/material-color-utilities"),
    LibraryInfo("Maven Artifact", "Copyright © The Apache Software Foundation", LICENSE_APACHE_2, "https://github.com/apache/maven/tree/maven-3.9.9/maven-artifact"),
    LibraryInfo("Navigation 3", COPYRIGHT_AOSP, LICENSE_APACHE_2, "https://developer.android.com/jetpack/androidx/releases/navigation3"),
    LibraryInfo("NBT", "Copyright © 2016 - 2020 Querz", LICENSE_MIT, "https://github.com/Querz/NBT"),
    LibraryInfo("OkHttp", "Copyright © 2019 Square, Inc.", LICENSE_APACHE_2, "https://github.com/square/okhttp"),
    LibraryInfo("proxy-client-android", null, LICENSE_LGPL_3, "https://github.com/TouchController/TouchController"),
    LibraryInfo("StringFog", "Copyright © 2016-2023, Megatron King", LICENSE_APACHE_2, "https://github.com/MegatronKing/StringFog"),
    LibraryInfo("XZ for Java", "Copyright © The XZ for Java authors and contributors", "0BSD License", "https://tukaani.org/xz/java.html")
)

private data class LibraryInfo(
    val name: String,
    val copyrightInfo: String?,
    val license: String,
    val webUrl: String
)

@Composable
fun AboutInfoScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: NavKey?,
    mainScreenKey: NavKey?,
    openLicense: (raw: Int) -> Unit
) {
    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.AboutInfo, settingsScreenKey, false)
    ) { isVisible ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(all = 12.dp)
        ) {
            item {
                val yOffset by swapAnimateDpAsState(
                    targetValue = (-40).dp,
                    swapIn = isVisible
                )
                ChunkLayout(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                    title = stringResource(R.string.about_launcher_title)
                ) {
                    val context = LocalContext.current
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ButtonIconItem(
                            icon = painterResource(R.drawable.ic_launcher),
                            title = InfoDistributor.LAUNCHER_NAME,
                            text = stringResource(R.string.about_launcher_version, BuildConfig.VERSION_NAME),
                            buttonText = stringResource(R.string.about_launcher_project_link),
                            onButtonClick = { NetWorkUtils.openLink(context, UrlManager.URL_PROJECT) }
                        )

                        ButtonIconItem(
                            icon = painterResource(R.drawable.ic_movtery),
                            title = stringResource(R.string.about_launcher_author_movtery_title),
                            text = stringResource(R.string.about_launcher_author_movtery_text, InfoDistributor.LAUNCHER_NAME),
                            buttonText = stringResource(R.string.about_launcher_sponsor),
                            onButtonClick = { NetWorkUtils.openLink(context, UrlManager.URL_SUPPORT) }
                        )
                    }
                }
            }

            item {
                val yOffset by swapAnimateDpAsState(
                    targetValue = (-40).dp,
                    swapIn = isVisible,
                    delayMillis = 50
                )
                ChunkLayout(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                    title = stringResource(R.string.about_acknowledgements_title)
                ) {
                    val context = LocalContext.current
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        LinkIconItem(
                            icon = painterResource(R.drawable.ic_bangbang93),
                            title = "bangbang93",
                            text = stringResource(R.string.about_acknowledgements_bangbang93_text, InfoDistributor.LAUNCHER_SHORT_NAME),
                            openLink = { NetWorkUtils.openLink(context, "https://afdian.com/a/bangbang93") }
                        )
                        LinkIconItem(
                            icon = painterResource(R.drawable.ic_fcl),
                            title = "Fold Craft Launcher",
                            text = stringResource(R.string.about_acknowledgements_fcl_text, InfoDistributor.LAUNCHER_SHORT_NAME),
                            openLicense = { openLicense(R.raw.fcl_license) },
                            openLink = { NetWorkUtils.openLink(context, "https://github.com/FCL-Team/FoldCraftLauncher") }
                        )
                        LinkIconItem(
                            icon = painterResource(R.drawable.ic_hmcl),
                            title = "Hello Minecraft! Launcher",
                            text = stringResource(R.string.about_acknowledgements_hmcl_text, InfoDistributor.LAUNCHER_SHORT_NAME),
                            openLicense = { openLicense(R.raw.hmcl_license) },
                            openLink = { NetWorkUtils.openLink(context, "https://github.com/HMCL-dev/HMCL") }
                        )
                        LinkIconItem(
                            icon = painterResource(R.drawable.ic_mcmod),
                            title = stringResource(R.string.about_acknowledgements_mcmod),
                            text = stringResource(R.string.about_acknowledgements_mcmod_text, InfoDistributor.LAUNCHER_SHORT_NAME),
                            openLink = { NetWorkUtils.openLink(context, "https://www.mcmod.cn/") }
                        )
                        LinkIconItem(
                            icon = painterResource(R.drawable.ic_pcl2),
                            title = "Plain Craft Launcher 2",
                            text = stringResource(R.string.about_acknowledgements_pcl_text, InfoDistributor.LAUNCHER_SHORT_NAME),
                            openLink = { NetWorkUtils.openLink(context, "https://github.com/Meloong-Git/PCL") }
                        )
                        LinkIconItem(
                            icon = painterResource(R.drawable.ic_pojav),
                            title = "PojavLauncher",
                            text = stringResource(R.string.about_acknowledgements_pojav_text, InfoDistributor.LAUNCHER_SHORT_NAME),
                            openLicense = { openLicense(R.raw.pojav_license) },
                            openLink = { NetWorkUtils.openLink(context, "https://github.com/PojavLauncherTeam/PojavLauncher") }
                        )
                    }
                }
            }

            item {
                val yOffset by swapAnimateDpAsState(
                    targetValue = (-40).dp,
                    swapIn = isVisible,
                    delayMillis = 100
                )
                //额外依赖库板块
                ChunkLayout(
                    modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                    title = stringResource(R.string.about_library_title)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        libraryData.forEach { info ->
                            LibraryInfoItem(info = info)
                        }
                    }
                }
            }

            PluginLoader.allPlugins.takeIf { it.isNotEmpty() }?.let { allPlugins ->
                item {
                    val yOffset by swapAnimateDpAsState(
                        targetValue = (-40).dp,
                        swapIn = isVisible,
                        delayMillis = 150
                    )
                    //已加载插件板块
                    ChunkLayout(
                        modifier = Modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
                        title = stringResource(R.string.about_plugin_title)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            allPlugins.forEach { apkPlugin ->
                                PluginInfoItem(apkPlugin = apkPlugin)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChunkLayout(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    SettingsBackground(
        modifier = modifier,
        contentPadding = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(width = 8.dp))
            HorizontalDivider(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 12.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun LinkIconItem(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    text: String,
    openLicense: (() -> Unit)? = null,
    openLink: (() -> Unit)? = null,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        shadowElevation = 1.dp,
        onClick = {}
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .size(34.dp)
                    .clip(shape = RoundedCornerShape(6.dp)),
                painter = icon,
                contentDescription = null,
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = text,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row {
                openLicense?.let {
                    IconButton(
                        onClick = it
                    ) {
                        Icon(
                            modifier = Modifier.size(22.dp),
                            imageVector = Icons.Outlined.Copyright,
                            contentDescription = "License"
                        )
                    }
                }
                openLink?.let {
                    IconButton(
                        onClick = it
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Link,
                            contentDescription = stringResource(R.string.generic_open_link)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ButtonIconItem(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    text: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        shadowElevation = 1.dp,
        onClick = {}
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .size(34.dp)
                    .clip(shape = RoundedCornerShape(6.dp)),
                painter = icon,
                contentDescription = null,
                contentScale = ContentScale.Fit
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    modifier = Modifier.alpha(0.7f),
                    text = text,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedButton(
                onClick = onButtonClick
            ) {
                Text(text = buttonText)
            }
        }
    }
}

@Composable
private fun PluginInfoItem(
    apkPlugin: ApkPlugin,
    modifier: Modifier = Modifier,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        shadowElevation = 1.dp,
        onClick = {}
    ) {
        val context = LocalContext.current
        Row(
            modifier = Modifier
                .padding(all = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val iconFile = appCacheIcon(apkPlugin.packageName)
            if (iconFile.exists()) {
                val model = remember(context, iconFile) {
                    ImageRequest.Builder(context)
                        .data(iconFile)
                        .build()
                }
                AsyncImage(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(shape = RoundedCornerShape(8.dp)),
                    model = model,
                    contentDescription = null,
                    contentScale = ContentScale.Fit
                )
            } else {
                Image(
                    modifier = Modifier.size(34.dp),
                    painter = painterResource(R.drawable.ic_unknown_icon),
                    contentDescription = null,
                    contentScale = ContentScale.Fit
                )
            }

            Column(
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(
                    text = apkPlugin.appName,
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    modifier = Modifier.alpha(0.7f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = apkPlugin.packageName,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (apkPlugin.appVersion.isNotEmpty()) {
                        Text(
                            text = apkPlugin.appVersion,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryInfoItem(
    info: LibraryInfo,
    modifier: Modifier = Modifier,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        shadowElevation = 1.dp,
        onClick = {}
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = info.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Column(
                    modifier = Modifier.alpha(0.7f)
                ) {
                    info.copyrightInfo?.let { copyrightInfo ->
                        Text(
                            text = copyrightInfo,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = "Licensed under the ${info.license}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            val context = LocalContext.current
            IconButton(
                modifier = Modifier.align(Alignment.CenterVertically),
                onClick = {
                    NetWorkUtils.openLink(context, info.webUrl)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Launch,
                    contentDescription = null
                )
            }
        }
    }
}