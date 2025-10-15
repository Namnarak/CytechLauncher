package com.movtery.zalithlauncher.utils.animation

import androidx.compose.animation.core.Easing
import kotlin.math.cos
import kotlin.math.exp

val JellyBounce = Easing { t ->
    // 计算基于阻尼余弦模型的动画插值值
    // (1 - ...) ：让动画最终收敛到 1（目标位置）
    // 0.6f       ：振幅 A = 0.6，决定初始弹性强度，0.6 即 60% 弹性
    // exp(-8 * t)：指数衰减项，控制震荡随时间快速减弱
    // cos(6 * π * t)：余弦波形，制造回弹感，共约 3 个完整震荡周期
    // 最终返回值在 [0,1] 附近轻微过冲
    (1 - 0.6f * exp(-8 * t) * cos(6 * Math.PI * t)).toFloat()
}