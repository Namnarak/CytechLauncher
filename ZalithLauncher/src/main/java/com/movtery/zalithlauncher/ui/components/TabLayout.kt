package com.movtery.zalithlauncher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.utils.animation.getAnimateTween

@Composable
fun TabLayout(
    modifier: Modifier = Modifier,
    content: @Composable TabLayoutScope.() -> Unit
) {
    val scope = remember { TabLayoutScope() }

    Column(
        modifier = modifier.verticalScroll(state = rememberScrollState())
    ) {
        with(scope) {
            content()
        }
    }
}

@DslMarker
annotation class TabLayoutDsl

@TabLayoutDsl
class TabLayoutScope {
    @Composable
    fun TabItem(
        modifier: Modifier = Modifier,
        painter: Painter,
        contentDescription: String?,
        text: String,
        selected: Boolean,
        selectedColor: Color = MaterialTheme.colorScheme.primaryContainer,
        contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        unSelectedContentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
        shape: Shape = MaterialTheme.shapes.extraLarge,
        color: Color = Color.Transparent,
        onClick: () -> Unit = {}
    ) {
        val backgroundColor: Color by animateColorAsState(
            targetValue = if (selected) selectedColor else color,
            animationSpec = getAnimateTween()
        )
        val contentColor1: Color by animateColorAsState(
            targetValue = if (selected) contentColor else unSelectedContentColor,
            animationSpec = getAnimateTween()
        )

        Box(
            modifier = modifier
                .clip(shape)
                .clickable(onClick = onClick)
                .fillMaxWidth()
                .background(color = backgroundColor)
        ) {
            Row(
                modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
            ) {
                Icon(
                    painter = painter,
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterVertically),
                    tint = contentColor1
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = text,
                    modifier = Modifier.align(Alignment.CenterVertically),
                    color = contentColor1,
                    softWrap = true,
                    fontSize = 12.sp
                )
            }
        }
    }
}