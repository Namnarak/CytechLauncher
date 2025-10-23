package com.movtery.zalithlauncher.game.launch

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class JvmLaunchInfo(
    val jvmArgs: String,
    val jreName: String? = null,
    val userHome: String? = null
) : Parcelable