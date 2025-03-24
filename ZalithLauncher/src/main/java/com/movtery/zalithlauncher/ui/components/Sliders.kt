package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.DecimalFormat

@Composable
fun SimpleTextSlider(
    modifier: Modifier = Modifier,
    value: Float,
    enabled: Boolean = true,
    onValueChange: (Float) -> Unit,
    toInt: Boolean = false,
    suffix: String? = null,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
) {
    val formatter = DecimalFormat("#0.00")
    fun getTextString(value: Float) = "${if (toInt) {
        value.toInt()
    } else {
        formatter.format(value)
    }}${if (suffix == null) "" else " $suffix"}"

    var textString by rememberSaveable { mutableStateOf(getTextString(value)) }

    Row(
        modifier = modifier
    ) {
        Slider(
            value = value,
            enabled = enabled,
            onValueChange = {
                textString = getTextString(it)
                onValueChange(it)
            },
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.weight(1f)
        )
        Surface(
            modifier = Modifier
                .padding(start = 12.dp)
                .align(Alignment.CenterVertically),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = textString,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 4.dp)
            )
        }
    }
}