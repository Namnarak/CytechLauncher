package com.movtery.zalithlauncher.coroutine

/**
 * 任务的执行状态，方便追查任务的运行进度
 * @param progress 任务的运行进度，范围在 -1f ~ 1f，小于 0 代表任务进度不确定
 * @param message 任务的进度描述信息
 */
data class TaskStatus(
    val progress: Float,
    val message: TaskMessage?
)