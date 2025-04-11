package com.movtery.zalithlauncher.utils.animation

import android.view.animation.BounceInterpolator
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.state.MutableStates

/**
 * 获取动画的持续时长
 */
fun getAnimateSpeed(): Int = calculateAnimationTime(
    AllSettings.launcherAnimateSpeed.getValue().coerceIn(0, 10),
    1500,
    0.1f
)

/**
 * 获取切换动画的类型
 */
fun getAnimateType(): TransitionAnimationType {
    val currentValue = AllSettings.launcherSwapAnimateType.getValue()
    return TransitionAnimationType.entries.find { it.name == currentValue } ?: TransitionAnimationType.BOUNCE
}

/**
 * 页面切换动画是否关闭
 */
fun isSwapAnimateClosed() = MutableStates.launcherAnimateType == TransitionAnimationType.CLOSE

fun <E> getAnimateTween(
    delayMillis: Int = 0
): FiniteAnimationSpec<E> = tween(
    durationMillis = getAnimateSpeed(),
    delayMillis = delayMillis
)

fun <E> getAnimateTweenBounce(
    delayMillis: Int = 0
): FiniteAnimationSpec<E> = tween(
    durationMillis = getAnimateSpeed(),
    delayMillis = delayMillis,
    easing = { fraction ->
        BounceInterpolator().getInterpolation(fraction)
    }
)

fun <E> getAnimateTweenJellyBounce(
    delayMillis: Int = 0
): FiniteAnimationSpec<E> = tween(
    durationMillis = getAnimateSpeed(),
    delayMillis = delayMillis,
    easing = { fraction ->
        JellyBounceInterpolator().getInterpolation(fraction)
    }
)

fun <E> getSwapAnimateTween(
    swapIn: Boolean,
    delayMillis: Int = 0
): FiniteAnimationSpec<E> {
    val type = getAnimateType()
    return when (type) {
        TransitionAnimationType.CLOSE -> snap()
        TransitionAnimationType.BOUNCE -> if (swapIn) getAnimateTweenBounce(delayMillis) else getAnimateTween(delayMillis)
        TransitionAnimationType.JELLY_BOUNCE -> if (swapIn) getAnimateTweenJellyBounce(delayMillis) else getAnimateTween(delayMillis)
        else -> getAnimateTween(delayMillis)
    }
}

@Composable
fun swapAnimateDpAsState(
    targetValue: Dp,
    swapIn: Boolean,
    delayMillis: Int = 0
): State<Dp> {
    return if (!isSwapAnimateClosed()) {
        animateDpAsState(
            targetValue = if (swapIn) 0.dp else targetValue,
            animationSpec = getSwapAnimateTween(swapIn, delayMillis = delayMillis)
        )
    } else {
        rememberUpdatedState(newValue = 0.dp)
    }
}

/**
 * 计算根据倍速调整后的时间（毫秒）
 * @param minFactor 最快时相对于 baseTime 的缩放比例（0.25 = 最快时是 1/4 时间）
 * @return 根据倍速调整后的时间
 */
fun calculateAnimationTime(speed: Int, baseTime: Int, minFactor: Float = 0.25f): Int {
    val factor = 1f - (speed / 10f) * (1f - minFactor)
    return (baseTime * factor).toInt()
}