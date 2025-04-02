package com.movtery.zalithlauncher.ui.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

@Composable
fun BaseScreen(
    screenTag: String,
    currentTag: String?,
    tagStartWith: Boolean = false,
    content: @Composable (isVisible: Boolean) -> Unit,
) {
    var isVisible by remember { mutableStateOf(false) }

    var isLaunched by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        isLaunched = true
    }
    //且只有在布局完成了之后，才会尝试变更TAG
    //否则isVisible变更后，组件始终会获取到同一个值，导致首次加载时动画效果不生效
    if (isLaunched) isVisible =
        if (tagStartWith) currentTag?.startsWith(screenTag) == true
        else currentTag == screenTag

    Box {
        content(isVisible)

        if (!isVisible) { //禁止触摸
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0f)
                    .clickable { }
            )
        }
    }
}