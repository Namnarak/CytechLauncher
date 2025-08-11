package com.movtery.zalithlauncher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.animation.getAnimateTweenJellyBounce
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState

/**
 * 菜单状态
 */
enum class MenuState {
    /**
     * 初始化 (刚加载时)
     */
    NONE {
        override fun next() = SHOW
    },

    /**
     * 展示中
     */
    SHOW {
        override fun next() = HIDE
    },

    /**
     * 隐藏中
     */
    HIDE {
        override fun next() = SHOW
    };

    abstract fun next(): MenuState
}

@Composable
fun MenuSubscreen(
    state: MenuState,
    closeScreen: () -> Unit,
    backgroundColor: Color = Color.Black.copy(alpha = 0.25f),
    backgroundAnimDuration: Int = 150,
    content: @Composable ColumnScope.() -> Unit
) {
    val visible = state == MenuState.SHOW
    val animationProgress = remember { Animatable(0f) }
    var shouldRender by remember { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible) {
            shouldRender = true
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(backgroundAnimDuration)
            )
        } else {
            animationProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(backgroundAnimDuration)
            )
            shouldRender = false
        }
    }

    val bgAlpha by remember {
        derivedStateOf { animationProgress.value }
    }
    val menuOffset by swapAnimateDpAsState(
        targetValue = 40.dp,
        swapIn = visible,
        isHorizontal = true,
        animationSpec = getAnimateTweenJellyBounce()
    )

    if (shouldRender) {
        Box(modifier = Modifier.fillMaxSize()) {
            //背景阴影层
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(bgAlpha)
                    .background(color = backgroundColor)
                    .clickable(
                        indication = null, //禁用水波纹点击效果
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = closeScreen
                    )
            )

            //Menu
            if (animationProgress.value > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxWidth(fraction = 1f / 3f)
                        .fillMaxHeight()
                        .padding(top = 12.dp, end = 12.dp, bottom = 12.dp)
                        .offset {
                            IntOffset(x = menuOffset.roundToPx(), y = 0)
                        }
                ) {
                    Card(
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(animationProgress.value),
                        content = content
                    )
                }
            }
        }
    }
}

@Composable
fun MenuTextButton(
    modifier: Modifier = Modifier,
    text: String,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shadowElevation: Dp = 1.dp,
    onClick: () -> Unit = {}
) {
    MenuButtonLayout(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        shadowElevation = shadowElevation,
        onClick = onClick,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MarqueeText(
            modifier = Modifier.padding(all = 14.dp),
            text = text
        )
    }
}

@Composable
fun MenuSwitchButton(
    modifier: Modifier = Modifier,
    text: String,
    switch: Boolean,
    onSwitch: (Boolean) -> Unit,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shadowElevation: Dp = 1.dp
) {
    MenuButtonLayout(
        modifier = modifier,
        shape = shape,
        color = color,
        contentColor = contentColor,
        shadowElevation = shadowElevation,
        onClick = { onSwitch(!switch) }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MarqueeText(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .weight(1f),
                text = text
            )
            Switch(
                checked = switch,
                onCheckedChange = onSwitch
            )
        }
    }
}

@Composable
fun MenuButtonLayout(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = itemLayoutColor(),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shadowElevation: Dp = 1.dp,
    onClick: () -> Unit = {},
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit
) {
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }

    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        shape = shape,
        color = color,
        contentColor = contentColor,
        shadowElevation = shadowElevation,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            content = content
        )
    }
}