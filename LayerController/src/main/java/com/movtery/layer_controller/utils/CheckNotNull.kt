package com.movtery.layer_controller.utils

interface CheckNotNull {
    /**
     * 所需的内容是否都不为null
     */
    @Throws(NullPointerException::class)
    fun checkNotNull()
}