package com.movtery.zalithlauncher.setting.unit

import android.os.Parcelable
import com.movtery.zalithlauncher.setting.launcherMMKV

/**
 * Parcelable 设置单元，将 Parcelable 保存到设置配置文件中
 */
class ParcelableSettingUnit<E: Parcelable>(
    key: String,
    defaultValue: E,
    private val clazz: Class<E>
): AbstractSettingUnit<E>(key, defaultValue) {
    override fun getValue(): E {
        val mmkv = launcherMMKV()
        val value: E? = mmkv.decodeParcelable(key, clazz)
        return (value ?: defaultValue).also {
            state = it
        }
    }

    override fun saveValue(v: E) {
        val mmkv = launcherMMKV()
        mmkv.encode(key, v)
        state = v
    }
}

inline fun <reified E: Parcelable> parcelableSettingUnit(
    key: String,
    defaultValue: E
): ParcelableSettingUnit<E> {
    return ParcelableSettingUnit(
        key = key,
        defaultValue = defaultValue,
        clazz = E::class.java
    )
}