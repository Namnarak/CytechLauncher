package com.movtery.zalithlauncher.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
abstract class BackStackNavKey : NavKey {
    /** 当前屏幕正在使用的堆栈 */
    @Contextual
    val backStack: NavBackStack = mutableStateListOf()
    /** 当前屏幕的Key */
    var currentKey by mutableStateOf<NavKey?>(null)

    @Suppress("unused")
    fun navigateOnce(key: NavKey) {
        backStack.navigateOnce(key)
    }

    fun navigateTo(screenKey: NavKey, useClassEquality: Boolean = false) {
        backStack.navigateTo(screenKey, useClassEquality)
    }

    fun removeAndNavigateTo(remove: KClass<*>, screenKey: NavKey, useClassEquality: Boolean = false) {
        backStack.removeAndNavigateTo(remove, screenKey, useClassEquality)
    }

    fun clearWith(navKey: NavKey) {
        backStack.clearWith(navKey)
    }
}