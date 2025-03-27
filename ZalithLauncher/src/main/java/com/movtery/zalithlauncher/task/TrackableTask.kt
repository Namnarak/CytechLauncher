package com.movtery.zalithlauncher.task

import kotlin.coroutines.cancellation.CancellationException

class TrackableTask<V>(
    val id: String,
    private val rawTask: Task<V>
) : TaskExecutionPhaseListener {
    
    //状态跟踪
    private var currentProgress = 0
    private var currentStatus = TaskStatus.Status.PENDING
    private val stateListeners = mutableListOf<(TaskStatus) -> Unit>()

    fun interface ProgressReporter {
        fun updateProgress(percentage: Int)
    }

    data class TaskStatus(
        val taskId: String,
        val progress: Int,
        val status: Status,
        val result: Any? = null,
        val error: Throwable? = null
    ) {
        enum class Status(val isTerminal: Boolean) {
            PENDING(false),
            RUNNING(false),
            COMPLETED(true),
            FAILED(true)
        }
    }

    /**
     * 添加自定义监听
     */
    fun addStateListener(listener: (TaskStatus) -> Unit) {
        stateListeners.add(listener)
    }

    /**
     * 更新当前任务进度
     */
    fun updateProgress(percentage: Int) {
        if (currentProgress != percentage) {
            notifyState(TaskStatus(
                taskId = id,
                progress = percentage,
                status = TaskStatus.Status.RUNNING
            ))
        }
    }

    /**
     * 将任务的阶段性监听器传递给原始任务，并执行原始任务
     */
    override fun execute() {
        val beforeStartListener = rawTask.getBeforeStartListener()
        val endedListener = rawTask.getEndedListener()
        val finallyListener = rawTask.getFinallyTaskListener()
        val throwableListener = rawTask.getThrowableListener()

        rawTask
            .beforeStart {
                beforeStartListener?.let { rawTask.runBeforeStartListener(it) }
                handleBeforeStart()
            }
            .ended {
                endedListener?.let { rawTask.runEndedListener(it) }
                handleTaskEnded(it)
            }
            .onThrowable { throwable ->
                throwableListener?.let { rawTask.runThrowableListener(it, throwable) }
                handleThrowable(throwable)
            }
            .finallyTask {
                finallyListener?.let { rawTask.runFinallyListener(it) }
                handleFinally()
            }
            .execute()
    }

    override fun cancel() {
        rawTask.cancel()
        notifyState(TaskStatus(
            taskId = id,
            progress = currentProgress,
            status = TaskStatus.Status.FAILED,
            error = CancellationException("Task was cancelled")
        ))
    }

    private fun notifyState(status: TaskStatus) {
        currentProgress = status.progress
        currentStatus = status.status
        stateListeners.forEach { it(status) }
    }

    private fun handleBeforeStart() {
        notifyState(TaskStatus(
            taskId = id,
            progress = 0,
            status = TaskStatus.Status.RUNNING
        ))
    }

    private fun handleTaskEnded(result: V?) {
        notifyState(TaskStatus(
            taskId = id,
            progress = 100,
            status = TaskStatus.Status.COMPLETED,
            result = result
        ))
    }

    private fun handleThrowable(e: Throwable) {
        notifyState(TaskStatus(
            taskId = id,
            progress = currentProgress,
            status = TaskStatus.Status.FAILED,
            error = e
        ))
    }

    private fun handleFinally() {
        if (!currentStatus.isTerminal) {
            notifyState(TaskStatus(
                taskId = id,
                progress = currentProgress,
                status = TaskStatus.Status.COMPLETED
            ))
        }
    }
}