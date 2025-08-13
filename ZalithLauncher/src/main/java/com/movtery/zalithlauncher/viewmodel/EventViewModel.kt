package com.movtery.zalithlauncher.viewmodel

import android.view.KeyEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {
    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    /**
     * 发送一个事件
     */
    fun sendEvent(event: Event) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    sealed interface Event {
        sealed interface Key : Event {
            data object StartKeyCapture : Key
            data object StopKeyCapture : Key
            data class OnKeyDown(val key: KeyEvent) : Key
        }
        data object ShowIme : Event
    }
}