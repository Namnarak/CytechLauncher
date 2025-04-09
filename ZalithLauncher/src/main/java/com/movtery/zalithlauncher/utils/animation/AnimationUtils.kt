package com.movtery.zalithlauncher.utils.animation

import android.view.animation.BounceInterpolator
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import com.movtery.zalithlauncher.setting.AllSettings

/**
 * 获取动画的持续时长
 */
fun getAnimateSpeed(): Int = calculateAnimationTime(
    AllSettings.launcherAnimateSpeed.getValue().coerceIn(0, 10),
    1500,
    0.1f
)

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

/**
 * 计算根据倍速调整后的时间（毫秒）
 * @param minFactor 最快时相对于 baseTime 的缩放比例（0.25 = 最快时是 1/4 时间）
 * @return 根据倍速调整后的时间
 */
fun calculateAnimationTime(speed: Int, baseTime: Int, minFactor: Float = 0.25f): Int {
    val factor = 1f - (speed / 10f) * (1f - minFactor)
    return (baseTime * factor).toInt()
}