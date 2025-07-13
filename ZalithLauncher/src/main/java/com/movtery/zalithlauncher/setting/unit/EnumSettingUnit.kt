package com.movtery.zalithlauncher.setting.unit

import com.movtery.zalithlauncher.setting.Settings.Manager

/**
 * 枚举设置单元，将枚举保存到设置配置文件中
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

    init {
        initState()
    }
}

inline fun <reified E : Enum<E>> enumSettingUnit(
    key: String,
    defaultValue: E
): EnumSettingUnit<E> {
    return EnumSettingUnit(
        key = key,
        defaultValue = defaultValue,
        getEnum = { raw -> enumValues<E>().firstOrNull { it.name == raw } }
    )
}
