package com.movtery.zalithlauncher.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

object TaskSystem {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val _tasksFlow: MutableStateFlow<List<Task>> = MutableStateFlow(emptyList())
    val tasksFlow: StateFlow<List<Task>> = _tasksFlow

    private val allJobs = ConcurrentHashMap<String, Job>()

    /**
     * 提交并立即运行任务
     */
    fun submitTask(task: Task) {
        addTask(task)

        allJobs[task.id] = scope.launch(task.dispatcher) {
            try {
                task.task(this@launch, task)
            } catch (th: Throwable) {
                task.onError(th)
            } finally {
                task.onFinally()
            }
        }.also { job ->
            job.invokeOnCompletion {
                removeTask(task)
                allJobs.remove(task.id)
            }
        }
    }

    /**
     * 取消任务
     */
    fun cancelTask(task: Task) {
        allJobs[task.id]?.cancel()
        removeTask(task)
    }

    /**
     * 取消任务
     */
    fun cancelTask(id: String) {
        allJobs[id]?.cancel()
        _tasksFlow.value.find { it.id == id }?.let { removeTask(it) }
    }

    /**
     * @return 是否包括任务
     */
    fun containsTask(task: Task) = _tasksFlow.value.contains(task)

    /**
     * @return 是否包括任务
     */
    fun containsTask(id: String) = _tasksFlow.value.any { it.id == id }

    private fun addTask(task: Task) {
        _tasksFlow.update { it + task }
    }

    private fun removeTask(task: Task) {
        _tasksFlow.update { it - task }
    }
}