package com.movtery.zalithlauncher.game.version.mod

import android.os.Parcel
import android.os.Parcelable
import com.movtery.zalithlauncher.game.download.assets.platform.ModLoaderDisplayLabel
import com.movtery.zalithlauncher.game.download.assets.platform.Platform

/**
 * 模组在平台上对应的文件
 * @param id 文件ID
 * @param platform 所属平台
 * @param datePublished 发布日期
 */
class ModFile(
    val id: String,
    val projectId: String,
    val platform: Platform,
    val loaders: Array<ModLoaderDisplayLabel>,
    val datePublished: String
): Parcelable {
    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(projectId)
        dest.writeParcelable(platform, flags)
        dest.writeParcelableArray(loaders, flags)
        dest.writeString(datePublished)
    }

    companion object CREATOR: Parcelable.Creator<ModFile> {
        override fun createFromParcel(source: Parcel): ModFile {
            val id = source.readString()!!
            val projectId = source.readString()!!
            val platform = source.readParcelable<Platform>(Platform::class.java.classLoader)!!
            val parcelableArray = source.readParcelableArray(ModLoaderDisplayLabel::class.java.classLoader)
            val datePublished = source.readString()!!
            return ModFile(
                id = id,
                projectId = projectId,
                platform = platform,
                loaders = Array(parcelableArray!!.size) { i ->
                    parcelableArray[i] as ModLoaderDisplayLabel
                },
                datePublished = datePublished
            )
        }

        override fun newArray(size: Int): Array<out ModFile?> {
            return arrayOfNulls(size)
        }
    }
}