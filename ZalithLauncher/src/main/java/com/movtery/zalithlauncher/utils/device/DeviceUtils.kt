package com.movtery.zalithlauncher.utils.device

import android.content.pm.PackageManager
import android.view.InputDevice

fun checkVulkanSupport(packageManager: PackageManager): Boolean {
    return packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL) &&
            packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION)
}

/**
 * 检查是否接入实体鼠标
 */
fun isPhysicalMouseConnected(): Boolean {
    return InputDevice.getDeviceIds()
        .takeIf { it.isNotEmpty() }
        ?.any { id ->
            val device = InputDevice.getDevice(id) ?: return@any false
            (device.sources and InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE
        } ?: false
}