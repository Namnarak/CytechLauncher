package com.movtery.zalithlauncher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.movtery.zalithlauncher.task.TaskSystemAdapter

class TaskViewModelFactory(
    private val taskSystem: TaskSystemAdapter
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(taskSystem) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}