package com.movtery.zalithlauncher.ui.control.mouse

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.ui.components.SimpleTextSlider

/** 测试虚拟鼠标层功能 */
@Composable
fun TestMouseLayout() {
    var text1 by remember { mutableStateOf("") }
    var pointerX by remember { mutableFloatStateOf(0.0f) }
    var pointerY by remember { mutableFloatStateOf(0.0f) }

    val text2 = "指针位置：x = $pointerX, y = $pointerY, 时间：${System.currentTimeMillis()}"

    var controlMode by remember { mutableStateOf(ControlMode.SLIDE) }
    var mouseSize by remember { mutableFloatStateOf(24f) }
    var mouseSpeed by remember { mutableFloatStateOf(100f) }

    val text3 = if (controlMode == ControlMode.SLIDE) "滑动模式" else "点击模式"

    Box(modifier = Modifier.fillMaxSize()) {
        VirtualPointerLayout(
            controlMode = controlMode,
            mouseSize = mouseSize.dp,
            mouseSpeed = mouseSpeed.toInt(),
            onTap = { offset ->
                pointerX = offset.x
                pointerY = offset.y
                text1 = "点击事件触发, 时间：${System.currentTimeMillis()}"
                println(text1)
            },
            onLongPress = {
                text1 = "长按事件开始, 时间：${System.currentTimeMillis()}"
                println(text1)
            },
            onLongPressEnd = {
                text1 = "长按事件结束, 时间：${System.currentTimeMillis()}"
                println(text1)
            },
            onPointerMove = { offset ->
                pointerX = offset.x
                pointerY = offset.y
            }
        )

        Column(modifier = Modifier.align(Alignment.Center)) {
            Text(text = text1)
            Text(text = text2)
        }

        Column(modifier = Modifier.padding(all = 12.dp).align(Alignment.BottomStart)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = text3)
                Checkbox(
                    checked = controlMode == ControlMode.SLIDE,
                    onCheckedChange = { checked ->
                        controlMode = if (checked) ControlMode.SLIDE else ControlMode.CLICK
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "大小")
                SimpleTextSlider(
                    value = mouseSize,
                    onValueChange = { mouseSize = it },
                    valueRange = 10f..50f,
                    toInt = true,
                    suffix = "Dp",
                    fineTuningControl = true,
                    fineTuningStep = 1f
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "速度")
                SimpleTextSlider(
                    value = mouseSpeed,
                    onValueChange = { mouseSpeed = it },
                    valueRange = 25f..300f,
                    toInt = true,
                    suffix = "%",
                    fineTuningControl = true,
                    fineTuningStep = 1f
                )
            }
        }
    }
}