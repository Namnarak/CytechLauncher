package com.movtery.zalithlauncher.task

class SimpleTask<V>(
    message: String = "",
    private val task: suspend () -> V
) : Task<V>(
    message = message
) {
    override suspend fun performMainTask() {
        val result = task()
        setResult(result)
    }
}