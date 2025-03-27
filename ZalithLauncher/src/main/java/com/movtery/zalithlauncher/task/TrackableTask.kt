package com.movtery.zalithlauncher.task

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.cancellation.CancellationException

class TrackableTask<V>(
    val id: String,
    private val rawTask: Task<V>
) : TaskExecutionPhaseListener {
    
    //状态跟踪
    private var currentProgress: Float = 0f
    private var currentMessage: String = ""
    private val _statusFlow = MutableStateFlow(TaskStatus(id, "", 0f, TaskStatus.Status.PENDING))
    val statusFlow: StateFlow<TaskStatus> = _statusFlow

    private val stateListeners = mutableListOf<(TaskStatus) -> Unit>()

    fun interface ProgressReporter {
        /**
         * 更新进度
         * @param percentage 进度数值，范围：0f ~ 1f，若为负数，则代表不确定
         * @param message 任务的描述信息
         */
        fun updateProgress(percentage: Float, message: String)
    }

    data class TaskStatus(
        val taskId: String,
        val message: String,
        val progress: Float,
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
     * @param percentage 进度数值，范围：0f ~ 1f，若为负数，则代表不确定
     * @param message 任务的描述信息
     */
    fun updateProgress(percentage: Float, message: String) {
        notifyState(TaskStatus(
            taskId = id,
            message = message,
            progress = percentage,
            status = TaskStatus.Status.RUNNING
        ))
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
            message = currentMessage,
            progress = currentProgress,
            status = TaskStatus.Status.FAILED,
            error = CancellationException("Task was cancelled")
        ))
    }

    private fun notifyState(status: TaskStatus) {
        _statusFlow.value = status
        currentProgress = status.progress
        currentMessage = status.message
        stateListeners.forEach { it(status) }
    }

    private fun handleBeforeStart() {
        notifyState(TaskStatus(
            taskId = id,
            message = currentMessage,
            progress = 0f,
            status = TaskStatus.Status.RUNNING
        ))
    }

    private fun handleTaskEnded(result: V?) {
        notifyState(TaskStatus(
            taskId = id,
            message = currentMessage,
            progress = 1f,
            status = TaskStatus.Status.COMPLETED,
            result = result
        ))
    }

    private fun handleThrowable(e: Throwable) {
        notifyState(TaskStatus(
            taskId = id,
            message = currentMessage,
            progress = currentProgress,
            status = TaskStatus.Status.FAILED,
            error = e
        ))
    }

    private fun handleFinally() {
        if (!_statusFlow.value.status.isTerminal) {
            notifyState(TaskStatus(
                taskId = id,
                message = currentMessage,
                progress = currentProgress,
                status = TaskStatus.Status.COMPLETED
            ))
        }
    }
}