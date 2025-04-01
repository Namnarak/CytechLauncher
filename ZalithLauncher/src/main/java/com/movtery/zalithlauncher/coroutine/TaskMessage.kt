package com.movtery.zalithlauncher.coroutine

/**
 * 任务的进度描述信息
 * @param resId 本地资源ID
 * @param args 附加参数
 */
data class TaskMessage(val resId: Int, val args: Any? = null)