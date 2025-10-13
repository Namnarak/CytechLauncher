package com.movtery.zalithlauncher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * A navigation rail item, which can contain an icon and text. It features a selection
 * animation where a capsule-shaped background expands from the center when the item is selected.
 *
 * This component is designed to be a flexible building block for navigation rails.
 *
 * @param text The composable lambda that defines the text to be displayed inside the item.
 * @param onClick The callback to be invoked when this item is clicked.
 * @param selected A boolean indicating whether this item is currently selected. The selection
 *                 animation is driven by this state.
 * @param modifier The [Modifier] to be applied to the component.
 * @param icon The composable lambda for the icon to be displayed. Appears before the text.
 * @param paddingValues The padding to be applied to the content (icon and text) inside the item.
 * @param shape The shape used for clipping the item's bounds and defining its clickable area.
 * @param backgroundColor The color of the animated background that appears when the item is selected.
 * @param selectedContentColor The color for the icon and text when the item is selected.
 * @param unselectedContentColor The color for the icon and text when the item is not selected.
 */
@Composable
fun TextRailItem(
    modifier: Modifier = Modifier,
    text: @Composable RowScope.() -> Unit,
    onClick: () -> Unit,
    icon: @Composable RowScope.() -> Unit = {},
    selected: Boolean,
    paddingValues: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    shape: Shape = MaterialTheme.shapes.extraLarge,
    backgroundColor: Color = NavigationRailItemDefaults.colors().selectedIndicatorColor,
    selectedContentColor: Color = NavigationRailItemDefaults.colors().selectedIconColor,
    unselectedContentColor: Color = NavigationRailItemDefaults.colors().unselectedIconColor
) {
    val animationProgress by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "SelectionAnimation"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        //背景扩散动画
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .alpha(animationProgress)
        ) {
            val maxWidth = size.width
            val minWidth = 0f
            val currentWidth = minWidth + (maxWidth - minWidth) * animationProgress

            val left = (maxWidth - currentWidth) / 2

            //绘制胶囊形状背景
            drawRoundRect(
                color = backgroundColor,
                topLeft = Offset(left, 0f),
                size = Size(currentWidth, size.height),
                cornerRadius = CornerRadius(size.height / 2, size.height / 2)
            )
        }

        Row(
            modifier = Modifier.padding(paddingValues),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val contentColor by animateColorAsState(
                targetValue = if (selected) selectedContentColor else unselectedContentColor
            )
            CompositionLocalProvider(
                LocalContentColor provides contentColor
            ) {
                icon()
                text()
            }
        }
    }
}