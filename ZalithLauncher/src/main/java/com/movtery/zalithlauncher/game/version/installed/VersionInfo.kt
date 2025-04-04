package com.movtery.zalithlauncher.game.version.installed

import android.os.Parcel
import android.os.Parcelable

class VersionInfo(
    val minecraftVersion: String,
    val loaderInfo: LoaderInfo?
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readParcelable(LoaderInfo::class.java.classLoader)
    )

    /**
     * 拼接Minecraft的版本信息，包括ModLoader信息
     * @return 用", "分割的信息字符串
     */
    fun getInfoString(): String {
        val infoList = mutableListOf<String>().apply {
            add(minecraftVersion)
            loaderInfo?.let { info ->
                when {
                    info.name.isNotBlank() && info.version.isNotBlank() -> add("${info.name} - ${info.version}")
                    info.name.isNotBlank() -> add(info.name)
                    info.version.isNotBlank() -> add(info.version)
                }
            }
        }
        return infoList.joinToString(", ")
    }

    data class LoaderInfo(
        val name: String,
        val version: String
    ): Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: ""
        )

        /**
         * 通过加载器名称，获得对应的环境变量键名
         */
        fun getLoaderEnvKey(): String? {
            return when(name) {
                "OptiFine" -> "INST_OPTIFINE"
                "Forge" -> "INST_FORGE"
                "NeoForge" -> "INST_NEOFORGE"
                "Fabric" -> "INST_FABRIC"
                "Quilt" -> "INST_QUILT"
                "LiteLoader" -> "INST_LITELOADER"
                else -> null
            }
        }

        override fun describeContents(): Int = 0

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(name)
            dest.writeString(version)
        }

        companion object CREATOR : Parcelable.Creator<LoaderInfo> {
            override fun createFromParcel(parcel: Parcel): LoaderInfo {
                return LoaderInfo(parcel)
            }

            override fun newArray(size: Int): Array<LoaderInfo?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(minecraftVersion)
        dest.writeParcelable(loaderInfo, flags)
    }

    companion object CREATOR : Parcelable.Creator<VersionInfo> {
        override fun createFromParcel(parcel: Parcel): VersionInfo {
            return VersionInfo(parcel)
        }

        override fun newArray(size: Int): Array<VersionInfo?> {
            return arrayOfNulls(size)
        }
    }
}