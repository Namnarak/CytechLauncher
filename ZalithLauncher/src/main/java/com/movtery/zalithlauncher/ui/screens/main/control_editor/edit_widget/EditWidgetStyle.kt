package com.movtery.zalithlauncher.ui.screens.main.control_editor.edit_widget

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.layout.RendererStyleBox
import com.movtery.layer_controller.observable.ObservableBaseData
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutItem
import com.movtery.zalithlauncher.ui.screens.main.control_editor.InfoLayoutTextItem
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.isNotEmptyOrBlank

/**
 * 为控件选择外观
 */
@Composable
fun EditWidgetStyle(
    data: ObservableBaseData,
    styles: List<ObservableButtonStyle>,
    openStyleList: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (styles.isNotEmpty()) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(minSize = 120.dp)
            ) {
                items(styles) { style ->
                    ChoseStyleItem(
                        modifier = Modifier.padding(all = 8.dp),
                        style = style,
                        selected = data.buttonStyle == style.uuid,
                        onSelectedChange = { selected ->
                            data.buttonStyle = if (selected) style.uuid else null
                        }
                    )
                }
            }
        } else {
            InfoLayoutTextItem(
                modifier = Modifier.padding(all = 24.dp),
                title = stringResource(R.string.control_editor_edit_style_config_empty),
                onClick = openStyleList
            )
        }
    }
}

@Composable
private fun ChoseStyleItem(
    style: ObservableButtonStyle,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    InfoLayoutItem(
        modifier = modifier,
        onClick = {
            onSelectedChange(!selected)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RendererStyleBox(
                modifier = Modifier.size(50.dp),
                style = style,
                text = "abc",
                isDark = isSystemInDarkTheme(),
                isPressed = false
            )
            Spacer(modifier = Modifier.height(4.dp))
            MarqueeText(
                modifier = Modifier.fillMaxWidth(),
                text = style.name.takeIf { it.isNotEmptyOrBlank() } ?: stringResource(R.string.generic_unspecified),
                textAlign = TextAlign.Center
            )
            Checkbox(
                checked = selected,
                onCheckedChange = onSelectedChange
            )
        }
    }
}