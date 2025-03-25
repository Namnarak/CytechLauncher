package com.movtery.zalithlauncher.ui.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.movtery.zalithlauncher.state.AbstractScreenTagState

@Composable
fun BaseScreen(
    screenTag: String,
    tagProvider: ProvidableCompositionLocal<AbstractScreenTagState>,
    content: @Composable (isVisible: Boolean) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    val currentTag = tagProvider.current.currentTag

    var isLaunched by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        isLaunched = true
    }
    //且只有在布局完成了之后，才会尝试变更TAG
    //否则isVisible变更后，组件始终会获取到同一个值，导致首次加载时动画效果不生效
    if (isLaunched) isVisible = currentTag == screenTag

    content(isVisible)
}