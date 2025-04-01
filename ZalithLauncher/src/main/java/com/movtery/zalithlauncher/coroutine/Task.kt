package com.movtery.zalithlauncher.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class Task private constructor(
    val id: String,
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    val task: suspend CoroutineScope.(Task) -> Unit,
    val onError: (Throwable) -> Unit = {},
    val onFinally: () -> Unit = {}
) {
    private var currentProgress: Float = -1f
    private var currentMessage: TaskMessage? = null

    private var _taskStatus: MutableStateFlow<TaskStatus> = MutableStateFlow(TaskStatus(currentProgress, currentMessage))
    val taskStatus: StateFlow<TaskStatus> = _taskStatus

    /**
     * 更新进度
     * @param percentage 进度百分比，-1f代表进度不确定
     */
    fun updateProgress(percentage: Float, notify: Boolean = true) {
        this.currentProgress = percentage.coerceIn(-1f, 1f)
        if (notify) notifyStatus()
    }

    /**
     * 更新进度、任务描述消息
     * @param percentage 进度百分比，-1f代表进度不确定
     * @param message 任务描述消息
     */
    fun updateProgress(percentage: Float, message: Int) {
        this.updateProgress(percentage = percentage, notify = false)
        this.updateMessage(message = message, notify = false)
        notifyStatus()
    }

    /**
     * 更新进度、任务描述消息
     * @param percentage 进度百分比，-1f代表进度不确定
     * @param message 任务描述消息
     */
    fun updateProgress(percentage: Float, message: TaskMessage?) {
        this.updateProgress(percentage = percentage, notify = false)
        this.updateMessage(message = message, notify = false)
        notifyStatus()
    }

    /**
     * 更新任务描述消息
     * @param message 任务描述消息
     */
    fun updateMessage(message: Int, notify: Boolean = true) {
        this.currentMessage = TaskMessage(message, Unit)
        if (notify) notifyStatus()
    }

    /**
     * 更新任务描述消息
     * @param message 任务描述消息
     */
    fun updateMessage(message: TaskMessage?, notify: Boolean = true) {
        this.currentMessage = message
        if (notify) notifyStatus()
    }

    private fun notifyStatus() {
        _taskStatus.value = TaskStatus(currentProgress, currentMessage)
    }

    override fun equals(other: Any?): Boolean = other is Task && other.id == this.id

    override fun hashCode(): Int = id.hashCode()

    companion object {
        fun runTask(
            id: String? = null,
            task: suspend CoroutineScope.(Task) -> Unit
        ): Task =
            Task(id = id ?: getRandomID(), task = task)

        fun runTask(
            id: String? = null,
            dispatcher: CoroutineDispatcher = Dispatchers.Default,
            task: suspend CoroutineScope.(Task) -> Unit
        ): Task =
            Task(id = id ?: getRandomID(), dispatcher = dispatcher, task = task)

        fun runTask(
            id: String? = null,
            dispatcher: CoroutineDispatcher = Dispatchers.Default,
            task: suspend CoroutineScope.(Task) -> Unit,
            onError: (Throwable) -> Unit
        ): Task =
            Task(id = id ?: getRandomID(), dispatcher = dispatcher, task = task, onError = onError)

        fun runTask(
            id: String? = null,
            dispatcher: CoroutineDispatcher = Dispatchers.Default,
            task: suspend CoroutineScope.(Task) -> Unit,
            onFinally: () -> Unit
        ): Task =
            Task(id = id ?: getRandomID(), dispatcher = dispatcher, task = task, onFinally = onFinally)

        fun runTask(
            id: String? = null,
            dispatcher: CoroutineDispatcher = Dispatchers.Default,
            task: suspend CoroutineScope.(Task) -> Unit,
            onError: (Throwable) -> Unit,
            onFinally: () -> Unit
        ): Task =
            Task(id = id ?: getRandomID(), dispatcher = dispatcher, task = task, onError = onError, onFinally = onFinally)

        private fun getRandomID(): String = UUID.randomUUID().toString()
    }
}