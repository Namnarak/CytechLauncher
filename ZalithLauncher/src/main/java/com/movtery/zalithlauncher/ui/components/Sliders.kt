package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowLeft
import androidx.compose.material.icons.automirrored.rounded.ArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.utils.math.addBigDecimal
import com.movtery.zalithlauncher.utils.math.subtractBigDecimal
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
    fineTuningControl: Boolean = false,
    fineTuningStep: Float = 0.5f
) {
    val formatter = DecimalFormat("#0.00")
    fun getTextString(value: Float) = "${if (toInt) {
        value.toInt()
    } else {
        formatter.format(value)
    }}${if (suffix == null) "" else " $suffix"}"

    var textString by rememberSaveable { mutableStateOf(getTextString(value)) }

    fun changeValue(newValue: Float, finished: Boolean) {
        textString = getTextString(newValue)
        onValueChange(newValue)
        if (finished) onValueChangeFinished?.invoke()
    }

    Row(
        modifier = modifier
    ) {
        Slider(
            value = value,
            enabled = enabled,
            onValueChange = { changeValue(it, false) },
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
            Row(
                modifier = Modifier.padding(PaddingValues(horizontal = 8.dp, vertical = 4.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = textString,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                if (fineTuningControl) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        modifier = Modifier.size(26.dp),
                        onClick = { changeValue(value.subtractBigDecimal(fineTuningStep), true) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowLeft,
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        modifier = Modifier.size(26.dp),
                        onClick = { changeValue(value.addBigDecimal(fineTuningStep), true) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowRight,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}