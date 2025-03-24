package com.movtery.zalithlauncher.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun AutoWidthSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(color),
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    border: BorderStroke? = null,
    content: @Composable () -> Unit
) {
    var maxContentWidth by remember { mutableIntStateOf(0) }

    Surface(
        modifier = modifier
            .widthIn(max = maxContentWidth.dp)
            .wrapContentWidth()
            .animateContentSize(),
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxHeight()
                .onSizeChanged { size ->
                    maxContentWidth = size.width.coerceAtLeast(maxContentWidth)
                }
        ) {
            content()
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DraggableBottomSheet(
    modifier: Modifier = Modifier,
    minHeight: Dp = 20.dp,
    sheetColor: Color = Color(0xFF222222),
    handleColor: Color = Color.LightGray,
    shape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    tonalElevation: Dp = 4.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    val minHeightPx = with(density) { minHeight.toPx() }

    val coroutineScope = rememberCoroutineScope()
    val sheetOffset = remember { Animatable(0f) }

    var maxHeightPx by remember { mutableFloatStateOf(0f) }
    val initialized = remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, sheetOffset.value.roundToInt()) }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { _, dragAmount ->
                            coroutineScope.launch {
                                val newOffset = (sheetOffset.value + dragAmount)
                                    .coerceIn(0f, maxHeightPx)
                                sheetOffset.snapTo(newOffset)
                            }
                        },
                        onDragEnd = {
                            coroutineScope.launch {
                                val target = if (maxHeightPx == 0f) 0f else {
                                    val shouldExpand = sheetOffset.value < maxHeightPx / 2
                                    if (shouldExpand) 0f else maxHeightPx
                                }
                                sheetOffset.animateTo(target, spring())
                            }
                        }
                    )
                },
            tonalElevation = tonalElevation,
            shape = shape,
            color = sheetColor
        ) {
            Column(
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        val totalHeight = coordinates.size.height.toFloat()
                        val newMaxHeight = totalHeight - minHeightPx
                        if (newMaxHeight != maxHeightPx) {
                            maxHeightPx = newMaxHeight
                        }
                    }
            ) {
                DragHandle(
                    minHeight = minHeight,
                    onClick = {
                        coroutineScope.launch {
                            val target = if (sheetOffset.value > maxHeightPx / 2) 0f else maxHeightPx
                            sheetOffset.animateTo(target, spring())
                        }
                    },
                    handleColor = handleColor
                )

                LaunchedEffect(maxHeightPx) {
                    if (maxHeightPx > 0 && !initialized.value) {
                        sheetOffset.snapTo(maxHeightPx)
                        initialized.value = true
                    }
                    //当maxHeight变化时调整offset到有效范围
                    sheetOffset.snapTo(sheetOffset.value.coerceIn(0f, maxHeightPx))
                }

                content()
            }
        }
    }
}

@Composable
private fun DragHandle(
    minHeight: Dp,
    onClick: () -> Unit,
    handleColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(minHeight)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(4.dp)
                .background(
                    color = handleColor,
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}

@Composable
fun CardLayout(
    painter: Painter,
    contentDescription: String?,
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colorScheme.surface,
    iconSize: Dp = 40.dp,
    fontSize: TextUnit = TextUnit.Unspecified,
    iconTint: Color = LocalContentColor.current,
    textColor: Color = Color.Unspecified
) {
    BaseIconTextLayout(
        label = "CardScale",
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        color = color,
    ) {
        Column(
            modifier = Modifier
                .padding(start = 12.dp, top = 18.dp, end = 12.dp, bottom = 18.dp),
        ) {
            Icon(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(iconSize)
                    .align(Alignment.CenterHorizontally),
                tint = iconTint
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                fontSize = fontSize,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = textColor
            )
        }
    }
}

@Composable
fun IconTextItemLayout(
    painter: Painter,
    contentDescription: String?,
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colorScheme.surface,
    iconSize: Dp = 40.dp,
    fontSize: TextUnit = TextUnit.Unspecified,
    iconTint: Color = LocalContentColor.current,
    textColor: Color = Color.Unspecified
) {
    BaseIconTextLayout(
        label = "IconTextItemScale",
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        color = color,
    ) {
        Row (
            modifier = Modifier
                .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
        ) {
            Icon(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier
                    .size(iconSize)
                    .align(Alignment.CenterVertically),
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = fontSize,
                modifier = Modifier.align(Alignment.CenterVertically),
                color = textColor
            )
        }
    }
}

@Composable
private fun BaseIconTextLayout(
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.4f),
        label = label
    )

    Surface(
        shape = shape,
        color = color,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                clip = true
                shadowElevation = 0f
            }
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
            ) {
                onClick?.invoke()
            },
        content = content
    )
}