package com.movtery.zalithlauncher.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

val GSON = GsonBuilder().setPrettyPrinting().create()

/**
 * 格式化时间戳
 */
fun formatDate(
    date: Date,
    pattern: String = "yyyy-MM-dd HH:mm:ss",
    locale: Locale = Locale.getDefault(),
    timeZone: TimeZone = TimeZone.getDefault()
): String {
    val formatter = SimpleDateFormat(pattern, locale)
    formatter.timeZone = timeZone
    return formatter.format(date)
}

fun copyText(label: String?, text: String?, context: Context) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text))
}