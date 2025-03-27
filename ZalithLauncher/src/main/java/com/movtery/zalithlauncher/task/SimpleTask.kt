package com.movtery.zalithlauncher.task

class SimpleTask<V>(private val task: suspend () -> V) : Task<V>() {
    override suspend fun performMainTask() {
        val result = task()
        setResult(result)
    }
}