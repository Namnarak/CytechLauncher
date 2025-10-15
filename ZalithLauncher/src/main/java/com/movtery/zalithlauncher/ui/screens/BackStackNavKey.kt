package com.movtery.zalithlauncher.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
abstract class BackStackNavKey<E: NavKey> : NavKey {
    /** 当前屏幕正在使用的堆栈 */
    @Contextual
    val backStack: NavBackStack<E> = NavBackStack()
    /** 当前屏幕的Key */
    var currentKey by mutableStateOf<E?>(null)

    @Suppress("unused")
    fun navigateOnce(key: E) {
        backStack.navigateOnce(key)
    }

    fun navigateTo(screenKey: E, useClassEquality: Boolean = false) {
        backStack.navigateTo(screenKey, useClassEquality)
    }

    fun removeAndNavigateTo(remove: KClass<*>, screenKey: E, useClassEquality: Boolean = false) {
        backStack.removeAndNavigateTo(remove, screenKey, useClassEquality)
    }

    fun removeAndNavigateTo(removes: List<KClass<*>>, screenKey: E, useClassEquality: Boolean = false) {
        backStack.removeAndNavigateTo(removes, screenKey, useClassEquality)
    }

    fun clearWith(navKey: E) {
        backStack.clearWith(navKey)
    }
}