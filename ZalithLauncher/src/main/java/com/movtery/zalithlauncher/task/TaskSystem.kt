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

    fun <V> packageTask(task: Task<V>): TrackableTask<V> {
        val trackingId = UUID.randomUUID().toString()

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

                    _tasksFlow.value = tasks.values.toList()
                }
            }
        }

        if (task is ProgressAwareTask) {
            task.bindProgressReporter { percentage, message ->
                trackableTask.updateProgress(percentage, message)
            }
        }

        return trackableTask
    }

    fun submitTask(task: Task<*>) {
        submitTask(
            //打包并提交运行任务
            packageTask(task)
        )
    }

    fun <V> submitTask(trackableTask: TrackableTask<V>) {
        tasks[trackableTask.id] = trackableTask

        _tasksFlow.value = tasks.values.toList()
        trackableTask.execute()
    }

    fun cancelTask(id: String) {
        scope.launch {
            tasks[id]?.cancel()
        }
    }
}