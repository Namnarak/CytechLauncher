package com.movtery.zalithlauncher.ui.screens.game.elements

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.control.ControlData
import com.movtery.zalithlauncher.game.control.ControlManager
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleListDialog
import java.io.File

sealed interface ReplacementControlState {
    data object None : ReplacementControlState
    /** 显示更换控制布局的对话框 */
    data object Show : ReplacementControlState
}

@Composable
fun ReplacementControlOperation(
    operation: ReplacementControlState,
    onChange: (ReplacementControlState) -> Unit,
    currentLayout: File?,
    replacementControl: (File) -> Unit
) {
    when (operation) {
        ReplacementControlState.None -> {}
        ReplacementControlState.Show -> {
            ReplacementControlDialog(
                currentLayout = currentLayout,
                onLayoutSelected = { data ->
                    replacementControl(data.file)
                },
                onDismissRequest = {
                    onChange(ReplacementControlState.None)
                }
            )
        }
    }
}

@Composable
private fun ReplacementControlDialog(
    currentLayout: File?,
    onLayoutSelected: (ControlData) -> Unit,
    onDismissRequest: () -> Unit
) {
    val dataList by ControlManager.dataList.collectAsState()
    val controls = remember(dataList) { dataList.filter { it.isSupport } }

    if (controls.isNotEmpty()) {
        val locale = LocalConfiguration.current.locales[0]

        SimpleListDialog(
            title = stringResource(R.string.game_menu_option_replacement_control),
            items = controls,
            itemTextProvider = { it.controlLayout.info.name.translate(locale) },
            isCurrent = { currentLayout?.name == it.file.name },
            onItemSelected = onLayoutSelected,
            onDismissRequest = onDismissRequest
        )
    } else {
        SimpleAlertDialog(
            title = stringResource(R.string.game_menu_option_replacement_control),
            text = stringResource(R.string.control_manage_list_empty),
            onConfirm = onDismissRequest,
            dismissText = stringResource(R.string.generic_refresh),
            onDismiss = {
                ControlManager.refresh()
            }
        )
    }
}