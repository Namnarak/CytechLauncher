package com.movtery.zalithlauncher.setting.unit

import com.movtery.zalithlauncher.setting.Settings.Manager

class DoubleSettingUnit(key: String, defaultValue: Double) : AbstractSettingUnit<Double>(key, defaultValue) {
    override fun getValue(): Double {
        cacheValue?.let { return it }
        return Manager.getValue(key, defaultValue) {
            it.toDoubleOrNull()
        }.also {
            cacheValue = it
        }
    }

    init {
        initState()
    }
}