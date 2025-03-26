package com.movtery.zalithlauncher.ui.screens.settings.layouts

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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.setting.unit.BooleanSettingUnit
import com.movtery.zalithlauncher.setting.unit.IntSettingUnit
import com.movtery.zalithlauncher.setting.unit.StringSettingUnit
import com.movtery.zalithlauncher.ui.components.SimpleTextSlider
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

        Row(
            modifier = modifier
                .clickable {
                    change(!checked)
                }
                .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(alignment = Alignment.CenterVertically)
            ) {
                TitleAndSummary(title, summary)
            }
            Spacer(
                modifier = Modifier.width(width = 8.dp)
            )

            Switch(
                modifier = Modifier.align(alignment = Alignment.CenterVertically),
                checked = checked,
                onCheckedChange = { value ->
                    change(value)
                }
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
        onValueChange: (Int) -> Unit = {}
    ) {
        var value by rememberSaveable { mutableIntStateOf(unit.getValue()) }

        Column(
            modifier = modifier.padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                TitleAndSummary(title, summary)
            }

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
                suffix = suffix
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
        onValueChange: (E) -> Unit = {}
    ) {
        var value by rememberSaveable { mutableStateOf(unit.getValue()) }

        Column(
            modifier = modifier.padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                TitleAndSummary(title, summary)
            }

            FlowRow(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth(),
            ) {
                entries.forEach { enum ->
                    Row {
                        val radioText = getRadioText(enum)
                        RadioButton(
                            selected = value == enum.name,
                            onClick = {
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