/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.coroutine

import com.movtery.zalithlauncher.coroutine.TaskFlowExecutor.TaskPhase
import com.movtery.zalithlauncher.utils.logging.Logger.lWarning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

/**
 * 动态任务流执行器，按照阶段顺序执行任务流
 */
class TaskFlowExecutor(
    private val scope: CoroutineScope
) {
    /**
     * 任务流阶段，包含一个阶段的所有任务
     */
    data class TaskPhase(
        val tasks: List<TitledTask>,
        val onComplete: (suspend () -> Unit)? = null
    )

    /**
     * 当前所有的任务流阶段
     */
    private val phases: MutableList<TaskPhase> = mutableListOf()

    private val _tasksFlow: MutableStateFlow<List<TitledTask>> = MutableStateFlow(emptyList())
    val tasksFlow: StateFlow<List<TitledTask>> = _tasksFlow

    private var job: Job? = null
    /** 当前任务流阶段索引 */
    private var currentPhaseIndex: Int = -1

    /**
     * 获取下一个阶段
     */
    private fun getNextPhase(): TaskPhase? {
        currentPhaseIndex++
        return phases.getOrNull(currentPhaseIndex)
    }

    /**
     * 添加阶段到末尾
     */
    fun addPhase(phase: TaskPhase) {
        phases.add(phase)
    }

    /**
     * 添加阶段列表到末尾
     */
    fun addPhases(phases: List<TaskPhase>) {
        this.phases.addAll(phases)
    }

    /**
     * 同步执行多阶段任务流
     */
    suspend fun executePhases(
        onComplete: () -> Unit = {},
        onError: (Throwable) -> Unit = {},
        onCancel: () -> Unit = {}
    ) = withContext(Dispatchers.IO) {
        currentPhaseIndex = -1

        while (true) {
            try {
                ensureActive()
                val phase = getNextPhase() ?: break
                //更新当前任务列表
                _tasksFlow.update { phase.tasks }

                //执行阶段内的所有任务
                for (task in phase.tasks) {
                    ensureActive()
                    task.task.taskState = TaskState.RUNNING

                    //为每个任务创建独立的Job，以便可以单独取消
                    withContext(task.task.dispatcher) {
                        task.task.task(this, task.task)
                    }

                    task.task.taskState = TaskState.COMPLETED
                }

                //执行阶段完成回调
                phase.onComplete?.invoke()
            } catch (th: Throwable) {
                lWarning("An exception occurred while executing the task flow.", th)
                if (th is CancellationException) {
                    onCancel()
                } else {
                    onError(th)
                    return@withContext
                }
            }
        }

        onComplete()
    }

    /**
     * 异步执行多阶段任务流
     */
    fun executePhasesAsync(
        onStart: suspend () -> Unit = {},
        onComplete: () -> Unit = {},
        onError: (Throwable) -> Unit = {},
        onCancel: () -> Unit = {}
    ) {
        job = scope.launch(Dispatchers.IO) {
            onStart()
            executePhases(onComplete, onError, onCancel)
        }
    }

    fun isRunning(): Boolean = job != null

    fun cancel() {
        job?.cancel()
        job = null

        _tasksFlow.update { emptyList() }
        currentPhaseIndex = -1
    }
}

/**
 * 构建任务流阶段
 */
fun buildPhase(
    onComplete: (suspend () -> Unit)? = null,
    builderAction: MutableList<TitledTask>.() -> Unit
): TaskPhase {
    val tasks: List<TitledTask> = buildList(builderAction = builderAction)
    return TaskPhase(
        tasks = tasks,
        onComplete = onComplete
    )
}