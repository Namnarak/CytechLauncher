package com.movtery.zalithlauncher.game.launch

import android.os.Parcel
import android.os.Parcelable

data class JarInfo(
    val jarPath: String,
    val jreName: String? = null,
    val jvmArgs: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(jarPath)
        dest.writeString(jreName)
        dest.writeString(jvmArgs)
    }

    companion object CREATOR : Parcelable.Creator<JarInfo> {
        override fun createFromParcel(parcel: Parcel): JarInfo {
            return JarInfo(parcel)
        }

        override fun newArray(size: Int): Array<JarInfo?> {
            return arrayOfNulls(size)
        }
    }
}