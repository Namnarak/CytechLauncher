package com.movtery.zalithlauncher.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * 在 secondaryContainer 背景上使用的 NavigationDrawerItem 颜色
 */
@Composable
fun secondaryContainerDrawerItemColors(): NavigationDrawerItemColors {
    val colorScheme = MaterialTheme.colorScheme
    return NavigationDrawerItemDefaults.colors(
        selectedContainerColor = colorScheme.secondary.copy(alpha = 0.24f),
        unselectedContainerColor = Color.Transparent,
        selectedIconColor = colorScheme.onSecondaryContainer,
        unselectedIconColor = colorScheme.onSecondaryContainer,
        selectedTextColor = colorScheme.onSecondaryContainer,
        unselectedTextColor = colorScheme.onSecondaryContainer,
    )
}