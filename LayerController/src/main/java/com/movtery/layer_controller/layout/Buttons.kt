package com.movtery.layer_controller.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntSize
import com.movtery.layer_controller.observable.ObservableButtonStyle
import com.movtery.layer_controller.observable.ObservableTextData
import com.movtery.layer_controller.utils.buttonContentColorAsState
import com.movtery.layer_controller.utils.buttonSize
import com.movtery.layer_controller.utils.buttonStyle
import com.movtery.layer_controller.utils.editMode

@Composable
internal fun TextButton(
    isEditMode: Boolean,
    data: ObservableTextData,
    getSize: () -> IntSize,
    getStyle: () -> ObservableButtonStyle?,
    isPressed: Boolean,
    onTapInEditMode: () -> Unit = {}
) {
    val style = remember(data, data.buttonStyle) {
        getStyle() ?: ObservableButtonStyle.Default
    }

    val locale = LocalConfiguration.current.locales[0]

    Box(
        modifier = Modifier
            .buttonSize(data)
            .buttonStyle(style, isPressed)
            .editMode(
                isEditMode = isEditMode,
                data = data,
                getSize = getSize,
                onTapInEditMode = onTapInEditMode
            ),
        contentAlignment = Alignment.Center
    ) {
        val color by buttonContentColorAsState(style, isPressed)
        Text(
            text = data.text.translate(locale),
            color = color
        )
    }
}