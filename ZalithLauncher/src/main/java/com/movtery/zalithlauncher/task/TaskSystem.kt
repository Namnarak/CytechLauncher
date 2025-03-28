package com.movtery.zalithlauncher.task

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object TaskSystem {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val tasks = ConcurrentHashMap<String, TrackableTask<*>>()
    private val _tasksFlow = MutableStateFlow<List<TrackableTask<*>>>(emptyList())
    val tasksFlow: StateFlow<List<TrackableTask<*>>> = _tasksFlow

    private fun <V> packageTask(
        task: Task<V>,
        taskId: String? = null
    ): TrackableTask<V> {
        val trackingId = taskId ?: UUID.randomUUID().toString()

        val trackableTask = TrackableTask(
            id = trackingId,
            rawTask = task
        ).apply {
            //注入状态监听
            addStateListener { status ->
                scope.launch {
                    if (status.status.isTerminal) {
                        tasks.remove(trackingId)
                    }

                    _tasksFlow.value = tasks.values.let {
                        if (it.isEmpty()) emptyList()
                        else it.toList()
                    }
                }
            }
        }

        if (task is ProgressAwareTask) {
            task.bindProgressReporter { percentage, message ->
                trackableTask.updateProgress(percentage, message)
            }
        } else {
            trackableTask.updateProgress(-1f, task.message)
        }

        return trackableTask
    }

    fun submitTask(task: Task<*>) {
        submitTask(
            //打包并提交运行任务
            packageTask(task)
        )
    }

    fun submitTask(
        task: Task<*>,
        trackingId: String?
    ) {
        submitTask(
            //打包并提交运行任务
            packageTask(task, trackingId)
        )
    }

    fun <V> submitTask(trackableTask: TrackableTask<V>) {
        tasks[trackableTask.id] = trackableTask

        _tasksFlow.value = tasks.values.toList()
        trackableTask.execute()
    }

    /**
     * 是否包含特定id的任务
     */
    fun containsTask(trackingId: String): Boolean =
        tasks.containsKey(trackingId)

    fun cancelTask(id: String) {
        scope.launch {
            tasks[id]?.cancel()
        }
    }
}