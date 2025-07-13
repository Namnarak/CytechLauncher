package com.movtery.zalithlauncher.setting.unit

import com.movtery.zalithlauncher.setting.Settings.Manager

/**
 * 枚举设置单元，将枚举保存到设置配置文件中
 * @param getEnum 根据枚举的 `name` 字符串，获取枚举对象
 */
class EnumSettingUnit<E : Enum<E>>(
    key: String,
    defaultValue: E,
    private val getEnum: (String) -> E?
) : AbstractSettingUnit<E>(key, defaultValue) {
    override fun getValue(): E {
        cacheValue?.let { return it }
        return Manager.getValue(key, defaultValue, getEnum).also {
            cacheValue = it
        }
    }
}