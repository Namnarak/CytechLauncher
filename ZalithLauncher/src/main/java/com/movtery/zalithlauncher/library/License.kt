package com.movtery.zalithlauncher.library

import com.movtery.zalithlauncher.R

/**
 * 库使用的协议信息
 * @param name 名称
 * @param raw 协议文本
 */
data class License(
    val name: String,
    val raw: Int
)

/**
 * Apache License 2.0
 * @see <a href="http://www.apache.org/licenses/LICENSE-2.0.txt">Apache License 2.0</a>
 */
val LICENSE_APACHE_2 = License("Apache License 2.0", R.raw.apache_license_2)
