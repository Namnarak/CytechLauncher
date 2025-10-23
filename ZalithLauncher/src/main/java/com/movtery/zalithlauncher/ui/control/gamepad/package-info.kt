@file:Suppress("unused")

/**
 * 部分实现思路参考自 [G-Mapper for Android](https://github.com/Mathias-Boulay/android_gamepad_remapper/)，
 * 该项目采用 GNU Lesser General Public License v3.0（LGPL-3.0）授权。
 *
 * 本项目基于 GNU General Public License v3.0（GPL-3.0）发布。
 *
 * 说明：
 * - 启动器使用 Jetpack Compose 构建 UI，无法直接集成原库，
 *   因此参考其核心逻辑并以 Kotlin 与 Compose 风格重新实现。
 * - 原项目的部分算法与结构在保留设计意图的前提下进行了简化与重构。
 * - 本项目未直接包含或链接原项目的源代码。
 */

package com.movtery.zalithlauncher.ui.control.gamepad
