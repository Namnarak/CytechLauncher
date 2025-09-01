package com.movtery.zalithlauncher.ui.screens.main.control_editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.SimpleTextSlider
import com.movtery.zalithlauncher.ui.components.SliderValueEditDialog
import com.movtery.zalithlauncher.ui.components.itemLayoutColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween


@Composable
fun InfoLayoutItem(
    modifier: Modifier = Modifier,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    decimalFormat: String = "#0.00",
    suffix: String? = null,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    var showValueEditDialog by remember { mutableStateOf(false) }

    InfoLayoutItem(
        modifier = modifier,
        onClick = {},
        color = color,
        contentColor = contentColor
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            SimpleTextSlider(
                modifier = Modifier.fillMaxWidth(),
                shorter = true,
                value = value,
                decimalFormat = decimalFormat,
                onValueChange = onValueChange,
                valueRange = valueRange,
                onValueChangeFinished = onValueChangeFinished,
                onTextClick = { showValueEditDialog = true },
                suffix = suffix,
                fineTuningControl = true
            )
        }
    }

    if (showValueEditDialog) {
        SliderValueEditDialog(
            onDismissRequest = { showValueEditDialog = false },
            title = title,
            valueRange = valueRange,
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = {
                onValueChangeFinished?.invoke()
            }
        )
    }
}

@Composable
fun <E> InfoLayoutItem(
    modifier: Modifier = Modifier,
    title: String,
    items: List<E>,
    selectedItem: E,
    onItemSelected: (E) -> Unit,
    getItemText: @Composable (E) -> String,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    maxListHeight: Dp = 200.dp
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = color,
        contentColor = contentColor,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            InfoListLayoutHeader(
                modifier = Modifier,
                items = items,
                title = title,
                selectedItemLayout = {
                    LittleTextLabel(text = getItemText(selectedItem))
                },
                expanded = expanded,
                onClick = { expanded = !expanded }
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
                                fun onClick() {
                                    if (expanded && selectedItem != item) {
                                        onItemSelected(item)
                                        expanded = false
                                    }
                                }

                                Row(
                                    modifier = modifier
                                        .clip(shape = MaterialTheme.shapes.medium)
                                        .clickable {
                                            onClick()
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedItem == item,
                                        onClick = {
                                            onClick()
                                        }
                                    )
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                        content = {
                                            MarqueeText(
                                                text = getItemText(item),
                                                style = MaterialTheme.typography.labelMedium
                                            )
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
}

@Composable
private fun <E> InfoListLayoutHeader(
    modifier: Modifier = Modifier,
    items: List<E>,
    title: String,
    selectedItemLayout: @Composable RowScope.() -> Unit,
    expanded: Boolean,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(all = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MarqueeText(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        Row(
            Modifier
                .weight(1f)
                .padding(all = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            selectedItemLayout()
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
            }
        }
    }
}

@Composable
fun InfoLayoutItem(
    modifier: Modifier = Modifier,
    title: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    InfoLayoutItem(
        modifier = modifier,
        onClick = {
            onValueChange(!value)
        },
        color = color,
        contentColor = contentColor
    ) {
        MarqueeText(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = value,
            onCheckedChange = onValueChange
        )
    }
}

@Composable
private fun InfoLayoutItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        shadowElevation = 1.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = MaterialTheme.shapes.large)
                .padding(all = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}