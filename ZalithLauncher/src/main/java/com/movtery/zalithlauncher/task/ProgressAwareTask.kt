package com.movtery.zalithlauncher.task

abstract class ProgressAwareTask<V> : Task<V>() {
    private lateinit var progressReporter: TrackableTask.ProgressReporter

    fun bindProgressReporter(reporter: TrackableTask.ProgressReporter) {
        this.progressReporter = reporter
    }

    protected fun updateProgress(percentage: Int) {
        progressReporter.updateProgress(percentage)
    }
}