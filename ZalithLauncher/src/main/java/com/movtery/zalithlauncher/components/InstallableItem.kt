package com.movtery.zalithlauncher.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class InstallableItem(
    val name: String,
    val summary: String?,
    val task: AbstractUnpackTask
) : Comparable<InstallableItem> {
    var isRunning by mutableStateOf(false)
    var isFinished by mutableStateOf(false)

    override fun compareTo(other: InstallableItem): Int {
        return name.compareTo(other.name, ignoreCase = true)
    }
}