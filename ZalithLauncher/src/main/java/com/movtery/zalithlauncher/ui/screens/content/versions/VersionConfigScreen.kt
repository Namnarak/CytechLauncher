package com.movtery.zalithlauncher.ui.screens.content.versions

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.game.plugin.driver.DriverPluginManager
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.game.version.installed.VersionConfig
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.state.MutableStates
import com.movtery.zalithlauncher.state.ObjectStates
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.IDItem
import com.movtery.zalithlauncher.ui.components.SimpleIDListLayout
import com.movtery.zalithlauncher.ui.components.SimpleListLayout
import com.movtery.zalithlauncher.ui.components.TextInputLayout
import com.movtery.zalithlauncher.ui.screens.content.versions.layouts.VersionSettingsBackground
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.getMessageOrToString

const val VERSION_CONFIG_SCREEN_TAG = "VersionConfigScreen"

@Composable
fun VersionConfigScreen() {
    BaseScreen(
        screenTag = VERSION_CONFIG_SCREEN_TAG,
        currentTag = MutableStates.versionSettingsScreenTag
    ) { isVisible ->

        val config = VersionsManager.versionBeingSet?.takeIf { it.isValid() }?.getVersionConfig() ?: run {
            ObjectStates.backToLauncherScreen()
            return@BaseScreen
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(all = 12.dp)
        ) {
            val yOffset1 by swapAnimateDpAsState(
                targetValue = (-40).dp,
                swapIn = isVisible
            )

            VersionConfigs(
                config = config,
                modifier = Modifier.offset { IntOffset(x = 0, y = yOffset1.roundToPx()) }
            )

            Spacer(modifier = Modifier.height(12.dp))
            val yOffset2 by swapAnimateDpAsState(
                targetValue = (-40).dp,
                swapIn = isVisible,
                delayMillis = 50
            )

            GameConfigs(
                config = config,
                modifier = Modifier.offset { IntOffset(x = 0, y = yOffset2.roundToPx()) }
            )
        }
    }
}

@Composable
private fun VersionConfigs(
    config: VersionConfig,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    VersionSettingsBackground(modifier = modifier) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp).padding(top = 4.dp, bottom = 8.dp),
            text = stringResource(R.string.versions_config_version_settings),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge
        )

        SimpleListLayout(
            items = VersionConfig.IsolationType.entries,
            currentId = config.getIsolationType().name,
            defaultId = VersionConfig.IsolationType.FOLLOW_GLOBAL.name,
            title = stringResource(R.string.versions_config_isolation),
            getItemText = { stringResource(it.textRes) },
            getItemId = { it.takeIf { it != VersionConfig.IsolationType.FOLLOW_GLOBAL }?.name ?: "" },
            onValueChange = { type ->
                if (config.getIsolationType() != type) {
                    config.setIsolationType(type)
                    config.saveOrShowError(context)
                }
            }
        )

        SimpleIDListLayout(
            items = getIDList(Renderers.getCompatibleRenderers(context).second) { IDItem(it.getUniqueIdentifier(), it.getRendererName()) },
            currentId = config.getRenderer(),
            defaultId = "",
            title = stringResource(R.string.versions_config_renderer),
            onValueChange = { item ->
                if (config.getRenderer() != item.id) {
                    config.setRenderer(item.id)
                    config.saveOrShowError(context)
                }
            }
        )

        SimpleIDListLayout(
            items = getIDList(DriverPluginManager.getDriverList()) { IDItem(it.id, it.name) },
            currentId = config.getDriver(),
            defaultId = "",
            title = stringResource(R.string.versions_config_vulkan_driver),
            onValueChange = { item ->
                if (config.getDriver() != item.id) {
                    config.setDriver(item.id)
                    config.saveOrShowError(context)
                }
            }
        )
    }
}

@Composable
private fun GameConfigs(
    config: VersionConfig,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    VersionSettingsBackground(modifier = modifier) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp).padding(top = 4.dp, bottom = 8.dp),
            text = stringResource(R.string.versions_config_game_settings),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge
        )

        SimpleIDListLayout(
            items = getIDList(RuntimesManager.getRuntimes().filter { it.isCompatible }) { IDItem(it.name, it.name) },
            currentId = config.getJavaRuntime(),
            defaultId = "",
            title = stringResource(R.string.settings_game_java_runtime_title),
            summary = stringResource(R.string.versions_config_java_runtime_summary),
            onValueChange = { item ->
                if (config.getJavaRuntime() != item.id) {
                    config.setJavaRuntime(item.id)
                    config.saveOrShowError(context)
                }
            }
        )

        TextInputLayout(
            currentValue = config.getCustomInfo(),
            title = stringResource(R.string.settings_game_version_custom_info_title),
            summary = stringResource(R.string.settings_game_version_custom_info_summary),
            onValueChange = { value ->
                if (config.getCustomInfo() != value) {
                    config.setCustomInfo(value)
                    config.saveOrShowError(context)
                }
            },
            label = {
                Text(text = stringResource(R.string.versions_config_follow_global_if_blank))
            }
        )

        TextInputLayout(
            currentValue = config.getJvmArgs(),
            title = stringResource(R.string.settings_game_jvm_args_title),
            summary = stringResource(R.string.settings_game_jvm_args_summary),
            onValueChange = { value ->
                if (config.getJvmArgs() != value) {
                    config.setJvmArgs(value)
                    config.saveOrShowError(context)
                }
            },
            label = {
                Text(text = stringResource(R.string.versions_config_follow_global_if_blank))
            }
        )
    }
}

@Composable
private fun <E> getIDList(list: List<E>, toIDItem: (E) -> IDItem): List<IDItem> {
    return list.map {
        toIDItem(it)
    }.toMutableList().apply {
        add(0, IDItem("", stringResource(R.string.generic_follow_global)))
    }
}

private fun VersionConfig.saveOrShowError(context: Context) {
    runCatching {
        saveWithThrowable()
    }.onFailure { e ->
        Log.e(VERSION_CONFIG_SCREEN_TAG, "Failed to save version config!", e)
        ObjectStates.updateThrowable(
            ObjectStates.ThrowableMessage(
                title = context.getString(R.string.versions_config_failed_to_save),
                message = e.getMessageOrToString()
            )
        )
    }
}