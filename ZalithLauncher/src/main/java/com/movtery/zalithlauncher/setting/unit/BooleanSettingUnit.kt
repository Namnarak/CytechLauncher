package com.movtery.zalithlauncher.setting.unit

import com.movtery.zalithlauncher.setting.Settings.Manager

class BooleanSettingUnit(key: String, defaultValue: Boolean) : AbstractSettingUnit<Boolean>(key, defaultValue) {
    override fun getValue(): Boolean {
        cacheValue?.let { return it }
        return Manager.getValue(key, defaultValue) {
            it.toBoolean()
        }.also {
            cacheValue = it
        }
    }

    init {
        initState()
    }
}