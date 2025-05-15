package com.movtery.zalithlauncher.game.launch

import android.os.Parcel
import android.os.Parcelable

data class JvmLaunchInfo(
    val jvmArgs: String,
    val jreName: String? = null,
    val userHome: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        jvmArgs = parcel.readString()!!,
        jreName = parcel.readString(),
        userHome = parcel.readString()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(jvmArgs)
        dest.writeString(jreName)
        dest.writeString(userHome)
    }

    companion object CREATOR : Parcelable.Creator<JvmLaunchInfo> {
        override fun createFromParcel(parcel: Parcel): JvmLaunchInfo {
            return JvmLaunchInfo(parcel)
        }

        override fun newArray(size: Int): Array<JvmLaunchInfo?> {
            return arrayOfNulls(size)
        }
    }
}