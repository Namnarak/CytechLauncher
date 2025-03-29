package com.movtery.zalithlauncher.task

import java.util.UUID
import kotlin.coroutines.CoroutineContext

class SimpleTask<V>(
    id: String? = null,
    message: String = "",
    private val task: suspend () -> V
) : Task<V>(
    id = id ?: UUID.randomUUID().toString(),
    message = message
) {
    override suspend fun performMainTask(coroutineContext: CoroutineContext) {
        val result = task()
        setResult(result)
    }
}