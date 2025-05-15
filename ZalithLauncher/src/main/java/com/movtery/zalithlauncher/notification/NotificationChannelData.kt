package com.movtery.zalithlauncher.notification

import android.app.NotificationManager.IMPORTANCE_HIGH
import com.movtery.zalithlauncher.R

enum class NotificationChannelData(
    val channelId: String,
    val channelName: Int,
    val channelDescription: Int?,
    val level: Int,
) {
    JVM_SERVICE_CHANNEL("jvm.service", R.string.notification_data_jvm_service_name, null, IMPORTANCE_HIGH)
}