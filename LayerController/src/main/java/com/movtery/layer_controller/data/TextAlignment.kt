package com.movtery.layer_controller.data

import androidx.compose.ui.text.style.TextAlign

/**
 * 文本对准方向
 */
enum class TextAlignment(val textAlign: TextAlign) {
    Left(TextAlign.Left),
    Center(TextAlign.Center),
    Right(TextAlign.Right)
}