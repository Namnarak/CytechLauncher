package com.movtery.zalithlauncher.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.task.Task
import com.movtery.zalithlauncher.task.TaskSystemAdapter
import com.movtery.zalithlauncher.task.TrackableTask
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class TaskViewModel(
    private val taskSystem: TaskSystemAdapter
) : ViewModel() {

    private val _tasks = mutableStateListOf<TrackableTask.TaskStatus>()
    val tasks: List<TrackableTask.TaskStatus> get() = _tasks

    init {
        viewModelScope.launch {
            taskSystem.updates
                .onStart { Log.i("TaskViewModel", "The state flow starts") }
                .catch { e -> Log.e("TaskViewModel", "The state flow failed", e) }
                .collectLatest { status ->
                    synchronized(_tasks) {
                        _tasks.removeAll { it.taskId == status.taskId }
                        if (!status.status.isTerminal) {
                            _tasks.add(status)
                        }
                    }
                }
        }
    }

    fun submitTask(task: Task<*>) {
        submitTask(
            //打包并提交运行任务
            taskSystem.packageTask(task)
        )
    }

    fun submitTask(task:  TrackableTask<*>) {
        taskSystem.submitTask(task)
    }

    fun cancelTask(taskId: String) {
        taskSystem.cancelTask(taskId)
    }
}