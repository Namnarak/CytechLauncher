package com.movtery.zalithlauncher.ui.screens

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KClass

/**
 * 兼容嵌套NavDisplay的返回事件处理
 */
fun <E: NavKey> onBack(currentBackStack: NavBackStack<E>) {
    val key = currentBackStack.lastOrNull()
    when (key) {
        //普通的屏幕，直接退出当前堆栈的上层
        is NormalNavKey -> currentBackStack.removeLastOrNull()
        is BackStackNavKey<*> -> {
            if (key.backStack.size <= 1) {
                //嵌套屏幕的堆栈处于最后一个屏幕的状态
                //可以退出当前堆栈的上层了
                currentBackStack.removeLastOrNull()
            } else {
                //退出子堆栈的上层屏幕
                key.backStack.removeLastOrNull()
            }
        }
    }
}

fun <E: NavKey> NavBackStack<E>.navigateOnce(key: E) {
    if (key == lastOrNull()) return //防止反复加载
    clearWith(key)
}

fun <E: NavKey> NavBackStack<E>.navigateTo(screenKey: E, useClassEquality: Boolean = false) {
    val current = lastOrNull()
    if (useClassEquality) {
        if (current != null && screenKey::class == current::class) return //防止反复加载
    } else {
        if (screenKey == current) return //防止反复加载
    }
    add(screenKey)
}

fun <E: NavKey> NavBackStack<E>.removeAndNavigateTo(remove: KClass<*>, screenKey: E, useClassEquality: Boolean = false) {
    removeIf { key ->
        key::class == remove
    }
    navigateTo(screenKey, useClassEquality)
}

fun <E: NavKey> NavBackStack<E>.removeAndNavigateTo(removes: List<KClass<*>>, screenKey: E, useClassEquality: Boolean = false) {
    removeIf { key ->
        key::class in removes
    }
    navigateTo(screenKey, useClassEquality)
}

/**
 * 清除所有栈，并加入指定的key
 */
fun <E: NavKey> NavBackStack<E>.clearWith(navKey: E) {
    //批量替换内容，避免 Nav3 看到空帧
    this.apply {
        clear()
        add(navKey)
    }
}

fun <E: NavKey> NavBackStack<E>.addIfEmpty(navKey: E) {
    if (isEmpty()) {
        add(navKey)
    }
}