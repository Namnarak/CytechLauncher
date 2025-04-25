package com.movtery.zalithlauncher.ui.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

/**
 * 单层级基础屏幕，根据 `currentTag` 判断当前屏幕是否可见
 * @param screenTag 当前屏幕的标签
 * @param currentTag 当前屏幕正在展示的标签，通过导航提供
 * @param tagStartWith 是否启用标签 startsWith 进行判断
 */
@Composable
fun BaseScreen(
    screenTag: String,
    currentTag: String?,
    tagStartWith: Boolean = false,
    content: @Composable (isVisible: Boolean) -> Unit,
) {
    val targetVisible = remember(currentTag, screenTag, tagStartWith) {
        isTagVisible(screenTag, currentTag, tagStartWith)
    }

    //初始不可见，用于触发首次的 false -> true 动画
    val visibleState = remember { mutableStateOf(false) }

    //仅在 composition 完成后，才允许更新可见状态
    LaunchedEffect(targetVisible) {
        visibleState.value = targetVisible
    }

    BaseScreen(
        content = content,
        visible = visibleState.value
    )
}

/**
 * 双层级基础屏幕，根据父屏幕标签与子屏幕标签，判断当前屏幕是否可见
 * @param parentScreenTag 当前子屏幕归属的父屏幕标签
 * @param parentCurrentTag 当前子屏幕归属的父屏幕正在展示的标签，通过导航提供
 * @param childScreenTag 当前屏幕的标签
 * @param childCurrentTag 当前屏幕正在展示的标签，通过导航提供
 * @param parentTagStartWith 父屏幕标签是否启用标签 startsWith 进行判断
 * @param childTagStartWith 子屏幕标签是否启用标签 startsWith 进行判断
 */
@Composable
fun BaseScreen(
    parentScreenTag: String,
    parentCurrentTag: String?,
    childScreenTag: String,
    childCurrentTag: String?,
    parentTagStartWith: Boolean = false,
    childTagStartWith: Boolean = false,
    content: @Composable (isVisible: Boolean) -> Unit,
) {
    val targetVisible = remember(parentScreenTag, parentCurrentTag, childScreenTag, childCurrentTag) {
        val parentVisible = isTagVisible(parentScreenTag, parentCurrentTag, parentTagStartWith)
        val childVisible = isTagVisible(childScreenTag, childCurrentTag, childTagStartWith)
        parentVisible && childVisible
    }

    //初始不可见，用于触发首次的 false -> true 动画
    val visibleState = remember { mutableStateOf(false) }

    //仅在 composition 完成后，才允许更新可见状态
    LaunchedEffect(targetVisible) {
        visibleState.value = targetVisible
    }

    BaseScreen(
        content = content,
        visible = visibleState.value
    )
}

@Composable
private fun BaseScreen(
    content: @Composable (isVisible: Boolean) -> Unit,
    visible: Boolean
) {
    Box {
        content(visible)

        if (!visible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0f)
                    .clickable { }
            )
        }
    }
}

private fun isTagVisible(tag: String, current: String?, startsWith: Boolean): Boolean {
    return if (startsWith) current?.startsWith(tag) == true else current == tag
}