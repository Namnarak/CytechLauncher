package com.movtery.zalithlauncher.ui.screens.content.settings.layouts

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.unit.BooleanSettingUnit
import com.movtery.zalithlauncher.setting.unit.IntSettingUnit
import com.movtery.zalithlauncher.setting.unit.StringSettingUnit
import com.movtery.zalithlauncher.ui.components.SimpleTextSlider
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import kotlin.enums.EnumEntries

@DslMarker
annotation class SettingsLayoutDsl

@SettingsLayoutDsl
class SettingsLayoutScope {
    @Composable
    fun SwitchSettingsLayout(
        modifier: Modifier = Modifier,
        unit: BooleanSettingUnit,
        title: String,
        summary: String? = null,
        onCheckedChange: (Boolean) -> Unit = {}
    ) {
        var checked by rememberSaveable { mutableStateOf(unit.getValue()) }

        fun change(value: Boolean) {
            checked = value
            unit.put(checked).save()
            onCheckedChange(checked)
        }

        SwitchSettingsLayout(
            checked = checked,
            onCheckedChange = { value ->
                change(value)
            },
            modifier = modifier,
            title = title,
            summary = summary
        )
    }

    @Composable
    fun SwitchSettingsLayout(
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier,
        title: String,
        summary: String? = null
    ) {
        fun change(value: Boolean) {
            onCheckedChange(value)
        }

        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape = MaterialTheme.shapes.extraLarge)
                .clickable { change(!checked) }
                .padding(all = 8.dp)
                .padding(bottom = 4.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                TitleAndSummary(title, summary)
            }
            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                modifier = Modifier.align(Alignment.CenterVertically),
                checked = checked,
                onCheckedChange = { value -> change(value) }
            )
        }
    }


    @Composable
    fun SliderSettingsLayout(
        modifier: Modifier = Modifier,
        unit: IntSettingUnit,
        title: String,
        summary: String? = null,
        valueRange: ClosedFloatingPointRange<Float>,
        steps: Int = 0,
        suffix: String? = null,
        onValueChange: (Int) -> Unit = {},
        fineTuningControl: Boolean = false
    ) {
        var value by rememberSaveable { mutableIntStateOf(unit.getValue()) }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(all = 8.dp)
                .padding(bottom = 4.dp)
        ) {
            TitleAndSummary(title, summary)
            SimpleTextSlider(
                modifier = Modifier.fillMaxWidth(),
                value = value.toFloat(),
                onValueChange = { newValue ->
                    value = newValue.toInt()
                    onValueChange(value)
                },
                onValueChangeFinished = {
                    unit.put(value).save()
                },
                toInt = true,
                valueRange = valueRange,
                steps = steps,
                suffix = suffix,
                fineTuningControl = fineTuningControl,
                fineTuningStep = 1f
            )
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun <E: Enum<E>> EnumSettingsLayout(
        modifier: Modifier = Modifier,
        unit: StringSettingUnit,
        entries: EnumEntries<E>,
        title: String,
        summary: String? = null,
        getRadioText: @Composable (E) -> String,
        getRadioEnable: (E) -> Boolean,
        onValueChange: (E) -> Unit = {}
    ) {
        var value by rememberSaveable { mutableStateOf(unit.getValue()) }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(all = 8.dp)
                .padding(bottom = 4.dp)
        ) {
            TitleAndSummary(title, summary)
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = getAnimateTween()),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                entries.forEach { enum ->
                    Row {
                        val radioText = getRadioText(enum)
                        RadioButton(
                            enabled = getRadioEnable(enum),
                            selected = value == enum.name,
                            onClick = {
                                if (value == enum.name) return@RadioButton
                                value = enum.name
                                unit.put(value).save()
                                onValueChange(enum)
                            }
                        )
                        Text(
                            text = radioText,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun <E> ListSettingsLayout(
        modifier: Modifier = Modifier,
        unit: StringSettingUnit,
        items: List<E>,
        title: String,
        summary: String? = null,
        getItemText: @Composable (E) -> String,
        getItemId: (E) -> String,
        onValueChange: (E) -> Unit = {}
    ) {
        require(items.isNotEmpty()) { "Items list cannot be empty" }

        val currentId = unit.getValue()
        val defaultValue = unit.defaultValue
        val initialItem = remember(items, currentId, defaultValue) {
            items.firstOrNull { getItemId(it) == currentId }
                ?: items.firstOrNull { getItemId(it) == defaultValue }
                ?: items.first()
        }
        var selectedItem by remember { mutableStateOf(initialItem) }
        var expanded by remember { mutableStateOf(false) }

        Row(modifier = modifier.fillMaxWidth().padding(bottom = 4.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape = MaterialTheme.shapes.extraLarge)
                        .clickable { expanded = !expanded }
                        .padding(all = 8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        TitleAndSummary(title, summary)
                        Spacer(modifier = Modifier.height(height = 4.dp))
                        Text(
                            text = stringResource(R.string.settings_element_selected, getItemText(selectedItem)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    val rotation by animateFloatAsState(
                        targetValue = if (expanded) -180f else 0f,
                        animationSpec = getAnimateTween()
                    )
                    IconButton(
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(34.dp)
                            .rotate(rotation),
                        onClick = { expanded = !expanded }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowDropDown,
                            contentDescription = stringResource(if (expanded) R.string.generic_expand else R.string.generic_collapse),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Column(modifier = Modifier.animateContentSize(animationSpec = getAnimateTween())) {
                    if (expanded) {
                        repeat(items.size) { index ->
                            val item = items[index]
                            ListItem(
                                modifier = Modifier.fillMaxWidth(),
                                selected = getItemId(selectedItem) == getItemId(item),
                                itemName = getItemText(item),
                                onClick = {
                                    if (getItemId(selectedItem) != getItemId(item)) {
                                        selectedItem = item
                                        unit.put(getItemId(item)).save()
                                        onValueChange(item)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ListItem(
        selected: Boolean,
        itemName: String,
        modifier: Modifier = Modifier,
        onClick: () -> Unit = {}
    ) {
        Row(modifier = modifier.clickable(onClick = onClick)) {
            RadioButton(
                selected = selected,
                onClick = onClick
            )
            Text(
                text = itemName,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }

    @Composable
    fun TextInputSettingsLayout(
        modifier: Modifier = Modifier,
        unit: StringSettingUnit,
        title: String,
        summary: String? = null,
        onValueChange: (String) -> Unit = {},
        singleLine: Boolean = true,
        textColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        var value by remember { mutableStateOf(unit.getValue()) }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(all = 8.dp)
                .padding(bottom = 4.dp)
        ) {
            TitleAndSummary(title = title, summary = summary)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                textStyle = MaterialTheme.typography.labelMedium,
                onValueChange = {
                    value = it
                    unit.put(value).save()
                    onValueChange(value)
                },
                singleLine = singleLine,
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors().copy(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                )
            )
        }
    }

    @Composable
    private fun TitleAndSummary(
        title: String,
        summary: String? = null,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        summary?.let { text ->
            Spacer(
                modifier = Modifier.height(height = 4.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}