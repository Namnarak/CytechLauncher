package com.movtery.zalithlauncher.components

import android.content.Context
import android.content.res.AssetManager
import android.util.Log.i
import com.movtery.zalithlauncher.context.copyAssetFile
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.file.child
import com.movtery.zalithlauncher.utils.file.readString
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class UnpackComponentsTask(val context: Context, val component: Components) : AbstractUnpackTask() {
    private lateinit var am: AssetManager
    private lateinit var rootDir: File
    private lateinit var versionFile: File
    private lateinit var input: InputStream
    private var isCheckFailed: Boolean = false

    init {
        runCatching {
            am = context.assets
            rootDir = if (component.privateDirectory) PathManager.DIR_FILES_PRIVATE else PathManager.DIR_FILES_EXTERNAL
            versionFile = File("$rootDir/components/${component.component}/version")
            input = am.open("components/${component.component}/version")
        }.getOrElse {
            isCheckFailed = true
        }
    }

    fun isCheckFailed() = isCheckFailed

    override fun isNeedUnpack(): Boolean {
        if (isCheckFailed) return false

        if (!versionFile.exists()) {
            requestEmptyParentDir(versionFile)
            i("Unpack Components", "${component.component}: Pack was installed manually, or does not exist...")
            return true
        } else {
            val fis = FileInputStream(versionFile)
            val release1 = input.readString()
            val release2 = fis.readString()
            if (release1 != release2) {
                requestEmptyParentDir(versionFile)
                return true
            } else {
                i("UnpackPrep", "${component.component}: Pack is up-to-date with the launcher, continuing...")
                return false
            }
        }
    }

    override suspend fun run() {
        val fileList = am.list("components/${component.component}")
        for (fileName in fileList!!) {
            context.copyAssetFile(
                "components/${component.component}/$fileName",
                rootDir.child("components", component.component, fileName),
                true
            )
        }
    }

    private fun requestEmptyParentDir(file: File) {
        file.parentFile!!.apply {
            if (exists() and isDirectory) {
                FileUtils.deleteDirectory(this)
            }
            mkdirs()
        }
    }
}