package com.movtery.zalithlauncher.utils

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

@Suppress("DEPRECATION")
fun <T : Parcelable> Bundle.getParcelableSafely(key: String?, clazz: Class<T>): T? {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getParcelable(key, clazz)
        else -> getParcelable(key)
    }
}

@Suppress("DEPRECATION")
fun <T: Serializable> Bundle.getSerializableSafely(key: String?, clazz: Class<T>): T? {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializable(key, clazz)
        else -> getSerializable(key) as? T
    }
}