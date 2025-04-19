package com.movtery.zalithlauncher.setting

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.movtery.zalithlauncher.setting.enums.toGestureButtonType

/**
 * 缩放因子
 */
var scaleFactor by mutableFloatStateOf(AllSettings.resolutionRatio.getValue() / 100f)

/**
 * 手势控制
 */
var gestureControl by mutableStateOf(AllSettings.gestureControl.getValue())

/**
 * 手势控制点击时触发的按钮
 */
var gestureTriggerButton by mutableStateOf(AllSettings.gestureTriggerButton.getValue().toGestureButtonType())