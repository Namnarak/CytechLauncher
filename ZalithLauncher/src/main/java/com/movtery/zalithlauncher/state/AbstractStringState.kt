package com.movtery.zalithlauncher.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

abstract class AbstractStringState {
    var currentString by mutableStateOf<String?>(null)
        private set

    fun update(string: String?) {
        currentString = string
    }
}