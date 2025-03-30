package com.movtery.zalithlauncher.task

import java.util.UUID

abstract class ProgressAwareTask<V>(
    id: String? = null
) : Task<V>(id = id ?: UUID.randomUUID().toString()) {
    private lateinit var progressReporter: TrackableTask.ProgressReporter

    fun bindProgressReporter(reporter: TrackableTask.ProgressReporter) {
        this.progressReporter = reporter
    }

    /**
     * 更新当前任务进度
     * @param percentage 进度数值，范围：0f ~ 1f，若为负数，则代表不确定
     */
    protected fun updateProgress(percentage: Float) {
        updateProgress(percentage, -1)
    }

    /**
     * 更新当前任务进度
     * @param percentage 进度数值，范围：0f ~ 1f，若为负数，则代表不确定
     * @param message 任务的描述信息
     */
    protected fun updateProgress(percentage: Float, message: Int) {
        progressReporter.updateProgress(percentage, message)
    }

    /**
     * 更新当前任务进度
     * @param percentage 进度数值，范围：0f ~ 1f，若为负数，则代表不确定
     * @param message 任务的描述信息
     */
    protected fun updateProgress(percentage: Float, message: Pair<Int, Any>) {
        progressReporter.updateProgress(percentage, message)
    }
}