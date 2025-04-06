package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

@Composable
fun DownShadow(
    modifier: Modifier = Modifier,
    height: Dp
) {
    Box(
        modifier = modifier
            .height(height)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.15f),
                        Color.Transparent
                    )
                )
            )
    )
}