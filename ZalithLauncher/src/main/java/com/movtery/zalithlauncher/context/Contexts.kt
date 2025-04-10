package com.movtery.zalithlauncher.context

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.provider.OpenableColumns
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.Settings
import com.movtery.zalithlauncher.utils.file.readString

fun refreshContext(context: Context) {
    PathManager.refreshPaths(context)
    Settings.refreshSettings()
}

fun getContextWrapper(context: Context): ContextWrapper {
    refreshContext(context)
    return ContextWrapper(context)
}

fun Context.readAssetFile(filePath: String): String {
    return assets.open(filePath).use { it.readString() }
}

fun Context.getFileName(uri: Uri): String? {
    return runCatching {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null

            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME).takeIf { it != -1 }?.let { columnIndex ->
                cursor.getString(columnIndex)
            }
        } ?: uri.lastPathSegment
    }.getOrNull() ?: uri.lastPathSegment
}