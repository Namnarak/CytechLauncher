package com.movtery.zalithlauncher.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

@Composable
fun rememberAutoScrollToEndState(
    delayMillis: Long = 100,
    animationSpec: AnimationSpec<Float> = tween(durationMillis = 500)
): ScrollState {
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        delay(delayMillis)
        if (scrollState.maxValue > 0) {
            scrollState.animateScrollTo(
                value = scrollState.maxValue,
                animationSpec = animationSpec
            )
        }
    }

    return scrollState
}