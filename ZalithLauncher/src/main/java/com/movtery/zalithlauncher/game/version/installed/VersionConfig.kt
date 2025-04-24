package com.movtery.zalithlauncher.game.version.installed

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.VersionsManager.getZalithVersionPath
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.string.StringUtils
import com.movtery.zalithlauncher.utils.string.StringUtils.Companion.getStringNotNull
import java.io.File
import java.io.FileWriter

class VersionConfig(private var versionPath: File) : Parcelable {
    private var isolationType: IsolationType = IsolationType.FOLLOW_GLOBAL
    private var javaRuntime: String = ""
    private var jvmArgs: String = ""
    private var renderer: String = ""
    private var driver: String = ""
    private var control: String = ""
    private var customPath: String = ""
    private var customInfo: String = ""
    private var versionSummary: String = ""
    var ramAllocation: Int = -1

    constructor(
        filePath: File,
        isolationType: IsolationType = IsolationType.FOLLOW_GLOBAL,
        javaRuntime: String = "",
        jvmArgs: String = "",
        renderer: String = "",
        driver: String = "",
        control: String = "",
        customPath: String = "",
        customInfo: String = "",
        versionSummary: String = "",
        ramAllocation: Int = -1
    ) : this(filePath) {
        this.isolationType = isolationType
        this.javaRuntime = javaRuntime
        this.jvmArgs = jvmArgs
        this.renderer = renderer
        this.driver = driver
        this.control = control
        this.customPath = customPath
        this.customInfo = customInfo
        this.versionSummary = versionSummary
        this.ramAllocation = ramAllocation
    }

    fun copy(): VersionConfig = VersionConfig(
        versionPath,
        getIsolationTypeNotNull(isolationType),
        getStringNotNull(javaRuntime),
        getStringNotNull(jvmArgs),
        getStringNotNull(renderer),
        getStringNotNull(driver),
        getStringNotNull(control),
        getStringNotNull(customPath),
        getStringNotNull(customInfo),
        getStringNotNull(versionSummary),
        ramAllocation
    )

    fun save() {
        runCatching {
            saveWithThrowable()
        }.onFailure { e ->
            Log.e("Save Version Config", "$this\n${StringUtils.throwableToString(e)}")
        }
    }

    @Throws(Throwable::class)
    fun saveWithThrowable() {
        Log.i("Save Version Config", "Trying to save: $this")
        val zalithVersionPath = getZalithVersionPath(versionPath)
        val configFile = File(zalithVersionPath, "version.config")
        if (!zalithVersionPath.exists()) zalithVersionPath.mkdirs()

        FileWriter(configFile, false).use {
            val json = GSON.toJson(this)
            it.write(json)
        }
        Log.i("Save Version Config", "Saved: $this")
    }

    fun getVersionPath() = versionPath

    fun setVersionPath(versionPath: File) {
        this.versionPath = versionPath
    }

    fun isIsolation(): Boolean = when(getIsolationTypeNotNull(isolationType)) {
        IsolationType.FOLLOW_GLOBAL -> AllSettings.versionIsolation.getValue()
        IsolationType.ENABLE -> true
        IsolationType.DISABLE -> false
    }

    fun getIsolationType() = getIsolationTypeNotNull(isolationType)

    fun setIsolationType(isolationType: IsolationType) { this.isolationType = isolationType }

    fun getJavaRuntime(): String = getStringNotNull(javaRuntime)

    fun setJavaRuntime(dir: String) { this.javaRuntime = dir }

    fun getJvmArgs(): String = getStringNotNull(jvmArgs)

    fun setJvmArgs(args: String) { this.jvmArgs = args }

    fun getRenderer(): String = getStringNotNull(renderer)

    fun setRenderer(renderer: String) { this.renderer = renderer }

    fun getDriver(): String = getStringNotNull(driver)

    fun setDriver(driver: String) { this.driver = driver }

    fun getControl(): String = getStringNotNull(control)

    fun setControl(control: String) { this.control = control }

    fun getCustomPath(): String = getStringNotNull(customPath)

    fun setCustomPath(customPath: String) { this.customPath = customPath }

    fun getCustomInfo(): String = getStringNotNull(customInfo)

    fun setCustomInfo(customInfo: String) { this.customInfo = customInfo }

    fun getVersionSummary(): String = getStringNotNull(versionSummary)

    fun setVersionSummary(versionSummary: String) { this.versionSummary = versionSummary }

    private fun getIsolationTypeNotNull(type: IsolationType?) = type ?: IsolationType.FOLLOW_GLOBAL

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.run {
            writeString(versionPath.absolutePath)
            writeInt(getIsolationTypeNotNull(isolationType).ordinal)
            writeString(getStringNotNull(javaRuntime))
            writeString(getStringNotNull(jvmArgs))
            writeString(getStringNotNull(renderer))
            writeString(getStringNotNull(driver))
            writeString(getStringNotNull(control))
            writeString(getStringNotNull(customPath))
            writeString(getStringNotNull(customInfo))
            writeString(getStringNotNull(versionSummary))
            writeInt(ramAllocation)
        }
    }

    companion object CREATOR : Parcelable.Creator<VersionConfig> {
        override fun createFromParcel(parcel: Parcel): VersionConfig {
            val versionPath = File(parcel.readString().orEmpty())
            val isolationType = IsolationType.entries.getOrNull(parcel.readInt()) ?: IsolationType.FOLLOW_GLOBAL
            val javaRuntime = parcel.readString().orEmpty()
            val jvmArgs = parcel.readString().orEmpty()
            val renderer = parcel.readString().orEmpty()
            val driver = parcel.readString().orEmpty()
            val control = parcel.readString().orEmpty()
            val customPath = parcel.readString().orEmpty()
            val customInfo = parcel.readString().orEmpty()
            val versionSummary = parcel.readString().orEmpty()
            val ramAllocation = parcel.readInt()

            return VersionConfig(
                versionPath,
                isolationType,
                javaRuntime,
                jvmArgs,
                renderer,
                driver,
                control,
                customPath,
                customInfo,
                versionSummary,
                ramAllocation
            )
        }

        override fun newArray(size: Int): Array<VersionConfig?> {
            return arrayOfNulls(size)
        }

        fun parseConfig(versionPath: File): VersionConfig {
            val configFile = File(getZalithVersionPath(versionPath), "version.config")

            return runCatching getConfig@{
                when {
                    configFile.exists() -> {
                        //读取此文件的内容，并解析为VersionConfig
                        val config = GSON.fromJson(configFile.readText(), VersionConfig::class.java)
                        config.setVersionPath(versionPath)
                        config
                    }
                    else -> createNewConfig(versionPath)
                }
            }.getOrElse { e ->
                Log.e("Refresh Versions", StringUtils.throwableToString(e))
                createNewConfig(versionPath)
            }
        }

        private fun createNewConfig(versionPath: File): VersionConfig {
            val config = VersionConfig(versionPath)
            return config.apply { save() }
        }

        fun createIsolation(versionPath: File): VersionConfig {
            val config = VersionConfig(versionPath)
            config.setIsolationType(IsolationType.ENABLE)
            return config
        }
    }

    enum class IsolationType(val textRes: Int) {
        FOLLOW_GLOBAL(R.string.generic_follow_global),
        ENABLE(R.string.generic_open),
        DISABLE(R.string.generic_close)
    }
}