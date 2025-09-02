package com.movtery.zalithlauncher.ui.screens.main.control_editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.layer_controller.data.ButtonSize
import com.movtery.layer_controller.data.VisibilityType
import com.movtery.layer_controller.observable.ObservableBaseData
import com.movtery.zalithlauncher.R

/**
 * 编辑控件基本信息
 */
@Composable
fun EditWidgetInfo(
    data: ObservableBaseData,
    onPreviewRequested: () -> Unit,
    onDismissRequested: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(start = 4.dp, end = 8.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier)

        //可见场景
        InfoLayoutListItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_visibility),
            items = VisibilityType.entries,
            selectedItem = data.visibilityType,
            onItemSelected = { data.visibilityType = it },
            getItemText = { type ->
                val textRes = when (type) {
                    VisibilityType.ALWAYS -> R.string.control_editor_edit_visibility_always
                    VisibilityType.IN_GAME -> R.string.control_editor_edit_visibility_in_game
                    VisibilityType.IN_MENU -> R.string.control_editor_edit_visibility_in_menu
                }
                stringResource(textRes)
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        //x
        InfoLayoutSliderItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_position_x),
            value = data.position.x / 10f,
            onValueChange = {
                data.position = data.position.copy(x = (it * 10).toInt())
                onPreviewRequested()
            },
            valueRange = 0f..100f,
            onValueChangeFinished = onDismissRequested,
            decimalFormat = "#0.0",
            suffix = "%"
        )

        //y
        InfoLayoutSliderItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_position_y),
            value = data.position.y / 10f,
            onValueChange = {
                data.position = data.position.copy(y = (it * 10).toInt())
                onPreviewRequested()
            },
            valueRange = 0f..100f,
            onValueChangeFinished = onDismissRequested,
            decimalFormat = "#0.0",
            suffix = "%"
        )

        Spacer(modifier = Modifier.height(4.dp))

        //尺寸类型
        InfoLayoutListItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.control_editor_edit_size_type),
            items = ButtonSize.Type.entries,
            selectedItem = data.buttonSize.type,
            onItemSelected = { data.buttonSize = data.buttonSize.copy(type = it) },
            getItemText = { type ->
                val textRes = when (type) {
                    ButtonSize.Type.Dp -> R.string.control_editor_edit_size_type_dp
                    ButtonSize.Type.Percentage -> R.string.control_editor_edit_size_type_percentage
                    ButtonSize.Type.WrapContent -> R.string.control_editor_edit_size_type_wrap_content
                }
                stringResource(textRes)
            }
        )

        when (data.buttonSize.type) {
            ButtonSize.Type.Dp -> {
                val screenSize = LocalWindowInfo.current.containerSize
                val density = LocalDensity.current
                val screenWidth = with(density) { screenSize.width.toDp() }.value
                val screenHeight = with(density) { screenSize.height.toDp() }.value

                //绝对宽度
                InfoLayoutSliderItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.control_editor_edit_size_width),
                    value = data.buttonSize.widthDp,
                    onValueChange = {
                        data.buttonSize = data.buttonSize.copy(widthDp = it)
                        onPreviewRequested()
                    },
                    valueRange = 5f..screenWidth,
                    onValueChangeFinished = onDismissRequested,
                    suffix = "Dp"
                )

                //绝对高度
                InfoLayoutSliderItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.control_editor_edit_size_height),
                    value = data.buttonSize.heightDp,
                    onValueChange = {
                        data.buttonSize = data.buttonSize.copy(heightDp = it)
                        onPreviewRequested()
                    },
                    valueRange = 5f..screenHeight,
                    onValueChangeFinished = onDismissRequested,
                    suffix = "Dp"
                )
            }
            ButtonSize.Type.Percentage -> {
                @Composable fun ButtonSize.Reference.getReferenceText(): String {
                    val textRes = when (this) {
                        ButtonSize.Reference.ScreenWidth -> R.string.control_editor_edit_size_reference_screen_width
                        ButtonSize.Reference.ScreenHeight -> R.string.control_editor_edit_size_reference_screen_height
                    }
                    return stringResource(textRes)
                }

                //百分比宽度
                InfoLayoutSliderItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.control_editor_edit_size_width),
                    value = data.buttonSize.widthPercentage / 10f,
                    onValueChange = {
                        data.buttonSize = data.buttonSize.copy(widthPercentage = (it * 10).toInt())
                        onPreviewRequested()
                    },
                    valueRange = 5f..100f,
                    onValueChangeFinished = onDismissRequested,
                    decimalFormat = "#0.0",
                    suffix = "%"
                )

                //控件宽度参考对象
                InfoLayoutListItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.control_editor_edit_size_width_reference),
                    items = ButtonSize.Reference.entries,
                    selectedItem = data.buttonSize.widthReference,
                    onItemSelected = { data.buttonSize = data.buttonSize.copy(widthReference = it) },
                    getItemText = { it.getReferenceText() }
                )

                //百分比高度
                InfoLayoutSliderItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.control_editor_edit_size_height),
                    value = data.buttonSize.heightPercentage / 10f,
                    onValueChange = {
                        data.buttonSize = data.buttonSize.copy(heightPercentage = (it * 10).toInt())
                        onPreviewRequested()
                    },
                    valueRange = 5f..100f,
                    onValueChangeFinished = onDismissRequested,
                    decimalFormat = "#0.0",
                    suffix = "%"
                )

                //控件高度参考对象
                InfoLayoutListItem(
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.control_editor_edit_size_height_reference),
                    items = ButtonSize.Reference.entries,
                    selectedItem = data.buttonSize.heightReference,
                    onItemSelected = { data.buttonSize = data.buttonSize.copy(heightReference = it) },
                    getItemText = { it.getReferenceText() }
                )
            }
            ButtonSize.Type.WrapContent -> {}
        }

        Spacer(Modifier)
    }
}