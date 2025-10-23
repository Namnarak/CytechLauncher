package com.movtery.zalithlauncher.ui.control.gamepad

import com.tencent.mmkv.MMKV

/**
 * 手柄重映射数据保存 MMKV
 */
fun remapperMMKV(): MMKV = MMKV.mmkvWithID("GamepadRemapper", MMKV.MULTI_PROCESS_MODE)

/**
 * 手柄键值绑定数据保存 MMKV
 */
fun keyMappingMMKV(): MMKV = MMKV.mmkvWithID("GamepadKeyMapping", MMKV.MULTI_PROCESS_MODE)


