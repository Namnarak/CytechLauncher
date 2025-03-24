package com.movtery.zalithlauncher.setting

/**
 * 获取动画的持续时长
 */
fun getAnimateSpeed(): Int = 200 * AllSettings.launcherAnimateSpeed.getValue()
    .coerceAtLeast(0)
    .coerceAtMost(10)