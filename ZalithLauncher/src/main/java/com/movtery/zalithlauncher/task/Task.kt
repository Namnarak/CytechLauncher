package com.movtery.zalithlauncher.task

import androidx.annotation.CheckResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

abstract class Task<V>(
    val id: String,
    val message: Pair<Int, Any> = Pair(-1, Unit),
): TaskExecutionPhaseListener {
    private var scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var throwableFromTask: Throwable? = null
    private var beforeStart: Pair<suspend () -> Unit, CoroutineDispatcher>? = null
    private var ended: Pair<OnTaskEndedListener<V>, CoroutineDispatcher>? = null
    private var finally: Pair<suspend () -> Unit, CoroutineDispatcher>? = null
    private var onTaskThrowable: Pair<OnTaskThrowableListener, CoroutineDispatcher>? = null
    private var result: V? = null

    protected abstract suspend fun performMainTask(coroutineContext: CoroutineContext)

    @CheckResult(SUGGEST)
    open fun setScope(scope: CoroutineScope): Task<V> {
        this.scope = scope
        return this
    }

    private fun setThrowable(e: Throwable) {
        this.throwableFromTask = e
    }

    private fun checkThrowable() {
        throwableFromTask?.let {
            onThrowable(it)
        }
    }

    /**
     * 与主任务使用同一个执行者
     */
    @CheckResult(SUGGEST)
    fun beforeStart(runnable: suspend () -> Unit): Task<V> {
        return beforeStart(Dispatchers.Default, runnable)
    }

    @CheckResult(SUGGEST)
    fun beforeStart(dispatcher: CoroutineDispatcher, runnable: suspend () -> Unit): Task<V> {
        this.beforeStart = Pair(runnable, dispatcher)
        return this
    }

    fun getBeforeStartListener() = this.beforeStart

    /**
     * 与主任务使用同一个执行者
     */
    @CheckResult(SUGGEST)
    fun ended(listener: OnTaskEndedListener<V>): Task<V> {
        return ended(Dispatchers.Default, listener)
    }

    @CheckResult(SUGGEST)
    fun ended(dispatcher: CoroutineDispatcher, listener: OnTaskEndedListener<V>): Task<V> {
        this.ended = Pair(listener, dispatcher)
        return this
    }

    fun getEndedListener() = this.ended

    /**
     * 与主任务使用同一个执行者
     */
    @CheckResult(SUGGEST)
    fun finallyTask(runnable: suspend () -> Unit): Task<V> {
        return finallyTask(Dispatchers.Default, runnable)
    }

    @CheckResult(SUGGEST)
    fun finallyTask(dispatcher: CoroutineDispatcher, runnable: suspend () -> Unit): Task<V> {
        this.finally = Pair(runnable, dispatcher)
        return this
    }

    fun getFinallyTaskListener() = this.finally

    /**
     * 与主任务使用同一个执行者
     */
    @CheckResult(SUGGEST)
    fun onThrowable(listener: OnTaskThrowableListener): Task<V> {
        return onThrowable(Dispatchers.Default, listener)
    }

    @CheckResult(SUGGEST)
    fun onThrowable(dispatcher: CoroutineDispatcher, listener: OnTaskThrowableListener): Task<V> {
        this.onTaskThrowable = Pair(listener, dispatcher)
        return this
    }

    fun getThrowableListener() = this.onTaskThrowable

    fun setResult(result: V) {
        this.result = result
    }

    private suspend fun runAndThrowException(runnable: suspend () -> Unit) {
        try {
            runnable()
        } catch (t: Throwable) {
            throw t
        }
    }

    fun runBeforeStartListener(listener: Pair<suspend () -> Unit, CoroutineDispatcher>) {
        scope.launch(listener.second) {
            runCatching {
                runAndThrowException(listener.first)
            }.onFailure { t ->
                setThrowable(t)
            }
        }
    }

    fun runEndedListener(listener: Pair<OnTaskEndedListener<V>, CoroutineDispatcher>) {
        scope.launch(listener.second) {
            runCatching {
                runAndThrowException { listener.first.onEnded(result) }
            }.onFailure { t ->
                onThrowable(t)
            }
        }
    }

    fun runFinallyListener(listener: Pair<suspend () -> Unit, CoroutineDispatcher>) {
        scope.launch(listener.second) {
            runCatching {
                runAndThrowException(listener.first)
            }
        }
    }

    fun runThrowableListener(
        listener: Pair<OnTaskThrowableListener, CoroutineDispatcher>,
        throwable: Throwable
    ) {
        scope.launch(listener.second) {
            runCatching {
                runAndThrowException { listener.first.onThrowable(throwable) }
            }
        }
    }

    override fun onBeforeStart() {
        this.beforeStart?.let { runBeforeStartListener(it) }
    }

    override fun execute() {
        onBeforeStart()
        checkThrowable()
        scope.launch {
            try {
                coroutineContext.ensureActive()
                performMainTask(coroutineContext)
                onEnded()
            } catch (t: Throwable) {
                setThrowable(t)
            } finally {
                if (!isCanceled()) {
                    checkThrowable()
                    onFinally()
                }
            }
        }
    }

    override fun cancel() {
        scope.cancel(CancellationException("cancelled"))
    }

    /**
     * 任务是否已经取消
     */
    fun isCanceled() = !TaskSystem.containsTask(id)

    override fun onEnded() {
        this.ended?.let { runEndedListener(it) }
    }

    override fun onFinally() {
        this.finally?.let { runFinallyListener(it) }
    }

    override fun onThrowable(throwable: Throwable) {
        this.onTaskThrowable?.let { runThrowableListener(it, throwable) }
    }

    companion object {
        const val SUGGEST = "NOT_REQUIRED_TO_EXECUTE"

        @CheckResult(SUGGEST)
        fun <V> runTask(id: String? = null, task: suspend () -> V): Task<V> {
            return SimpleTask(id = id, task = task)
        }

        @CheckResult(SUGGEST)
        fun <V> runTask(id: String? = null, message: Int, task: suspend () -> V): Task<V> {
            return SimpleTask(id = id, message = Pair(message, Unit), task = task)
        }

        @CheckResult(SUGGEST)
        fun <V> runTask(id: String? = null, message: Pair<Int, Any>, task: suspend () -> V): Task<V> {
            return SimpleTask(id = id, message = message, task = task)
        }

        @CheckResult(SUGGEST)
        fun <V> runTask(id: String? = null, scope: CoroutineScope, task: suspend () -> V): Task<V> {
            return SimpleTask(id = id, task = task).setScope(scope)
        }

        @CheckResult(SUGGEST)
        fun <V> runTask(id: String? = null, message: Int, scope: CoroutineScope, task: suspend () -> V): Task<V> {
            return SimpleTask(id = id, message = Pair(message, Unit), task = task).setScope(scope)
        }

        @CheckResult(SUGGEST)
        fun <V> runTask(id: String? = null, message: Pair<Int, Any>, scope: CoroutineScope, task: suspend () -> V): Task<V> {
            return SimpleTask(id = id, message = message, task = task).setScope(scope)
        }
    }
}