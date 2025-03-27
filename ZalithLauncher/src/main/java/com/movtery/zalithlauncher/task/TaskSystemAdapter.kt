package com.movtery.zalithlauncher.task

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class TaskSystemAdapter {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val tasks = ConcurrentHashMap<String, TrackableTask<*>>()
    private val _updates = MutableSharedFlow<TrackableTask.TaskStatus>()
    val updates: SharedFlow<TrackableTask.TaskStatus> = _updates.asSharedFlow()

    fun <V> packageTask(task: Task<V>): TrackableTask<V> {
        val trackingId = UUID.randomUUID().toString()

        val trackableTask = TrackableTask(
            id = trackingId,
            rawTask = task
        ).apply {
            //注入状态监听
            addStateListener { status ->
                scope.launch {
                    _updates.emit(status)
                    if (status.status.isTerminal) {
                        tasks.remove(trackingId)
                    }
                }
            }
        }

        if (task is ProgressAwareTask) {
            task.bindProgressReporter { percentage ->
                trackableTask.updateProgress(percentage)
            }
        }

        return trackableTask
    }

    fun <V> submitTask(trackableTask: TrackableTask<V>) {
        tasks[trackableTask.id] = trackableTask
        trackableTask.execute()
    }

    fun cancelTask(id: String) {
        tasks[id]?.cancel()
        tasks.remove(id)
    }
}