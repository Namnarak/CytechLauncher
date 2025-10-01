package com.movtery.zalithlauncher.game.version.mod

import com.tencent.mmkv.MMKV

/**
 * 模组项目缓存 MMKV，文件 HASH 值对应项目
 */
fun modProjectCache(): MMKV = MMKV.mmkvWithID("ModProjectHashMapper", MMKV.MULTI_PROCESS_MODE)
/**
 * 模组版本文件缓存 MMKV，文件 HASH 值对应平台文件
 */
fun modFileCache(): MMKV = MMKV.mmkvWithID("ModFileHashMapper", MMKV.MULTI_PROCESS_MODE)
