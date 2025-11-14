/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.provider

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract.Document
import android.provider.DocumentsContract.Root
import android.provider.DocumentsProvider
import android.system.ErrnoException
import android.system.Os
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

private const val COLUMN_MT_EXTRAS = "mt_extras"
private const val COLUMN_MT_PATH = "mt_path"
private const val METHOD_SET_LAST_MODIFIED = "mt:setLastModified"
private const val METHOD_SET_PERMISSIONS = "mt:setPermissions"
private const val METHOD_CREATE_SYMLINK = "mt:createSymlink"

private val DEFAULT_ROOT_PROJECTION = arrayOf(
    Root.COLUMN_ROOT_ID,
    Root.COLUMN_MIME_TYPES,
    Root.COLUMN_FLAGS,
    Root.COLUMN_ICON,
    Root.COLUMN_TITLE,
    Root.COLUMN_SUMMARY,
    Root.COLUMN_DOCUMENT_ID
)

private val DEFAULT_DOCUMENT_PROJECTION = arrayOf(
    Document.COLUMN_DOCUMENT_ID,
    Document.COLUMN_MIME_TYPE,
    Document.COLUMN_DISPLAY_NAME,
    Document.COLUMN_LAST_MODIFIED,
    Document.COLUMN_FLAGS,
    Document.COLUMN_SIZE,
    COLUMN_MT_EXTRAS
)

/**
 * A document provider for the Storage Access Framework which exposes the files in the
 * $HOME/ directory to other apps.
 *
 * Note that this replaces providing an activity matching the ACTION_GET_CONTENT intent:
 *
 * "A document provider and ACTION_GET_CONTENT should be considered mutually exclusive. If you
 * support both of them simultaneously, your app will appear twice in the system picker UI,
 * offering two different ways of accessing your stored data. This would be confusing for users."
 * - https://developer.android.com/guide/topics/providers/document-provider.html#43
 *
 * [Modified from FCL](https://github.com/FCL-Team/FoldCraftLauncher/blob/main/FCL/src/main/java/com/tungsten/fcl/scoped/FolderProvider.java)
 */
class FolderProvider : DocumentsProvider() {
    private lateinit var packageName: String
    private lateinit var dataDir: File
    private var androidDataDir: File? = null

    override fun attachInfo(context: Context, info: ProviderInfo) {
        super.attachInfo(context, info)
        packageName = context.packageName
        dataDir = context.filesDir.parentFile!!
        val externalFilesDir = context.getExternalFilesDir(null)
        if (externalFilesDir != null) {
            androidDataDir = externalFilesDir.parentFile
        }
    }

    private fun getFileForDocId(docId: String, checkExists: Boolean = true): File? {
        var filename = if (docId.startsWith(packageName)) {
            docId.removePrefix(packageName)
        } else {
            throw FileNotFoundException("$docId not found")
        }

        if (filename.startsWith("/")) filename = filename.drop(1)
        if (filename.isEmpty()) return null

        val i = filename.indexOf('/')
        val type = if (i == -1) filename else filename.substring(0, i)
        val subPath = if (i == -1) "" else filename.substring(i + 1)

        val f = when (type.lowercase()) {
            "data" -> File(dataDir, subPath)
            "android_data" -> androidDataDir?.let { File(it, subPath) }
            else -> null
        } ?: throw FileNotFoundException("$docId not found")

        if (checkExists) {
            try {
                Os.lstat(f.path)
            } catch (_: Exception) {
                throw FileNotFoundException("$docId not found")
            }
        }
        return f
    }

    override fun queryRoots(projection: Array<out String>?): Cursor {
        val appInfo: ApplicationInfo = context!!.applicationInfo
        val appName = appInfo.loadLabel(context!!.packageManager).toString()
        val result = MatrixCursor(projection ?: DEFAULT_ROOT_PROJECTION)
        val row = result.newRow()
        row.add(Root.COLUMN_ROOT_ID, packageName)
        row.add(Root.COLUMN_DOCUMENT_ID, packageName)
        row.add(Root.COLUMN_SUMMARY, packageName)
        row.add(Root.COLUMN_FLAGS, Root.FLAG_SUPPORTS_CREATE or Root.FLAG_SUPPORTS_IS_CHILD)
        row.add(Root.COLUMN_TITLE, appName)
        row.add(Root.COLUMN_MIME_TYPES, "*/*")
        row.add(Root.COLUMN_ICON, appInfo.icon)
        return result
    }

    override fun queryDocument(documentId: String, projection: Array<out String>?): Cursor {
        val result = MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION)
        includeFile(result, documentId, null)
        return result
    }

    override fun queryChildDocuments(
        parentDocumentId: String,
        projection: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val parentId = parentDocumentId.trimEnd('/')
        val result = MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION)
        val parent = getFileForDocId(parentId)
        if (parent == null) {
            includeFile(result, "$parentId/data", dataDir)
            androidDataDir?.takeIf { it.exists() }?.let {
                includeFile(result, "$parentId/android_data", it)
            }
        } else {
            parent.listFiles()?.forEach { file ->
                includeFile(result, "$parentId/${file.name}", file)
            }
        }
        return result
    }

    override fun openDocument(
        documentId: String,
        mode: String,
        signal: CancellationSignal?
    ): ParcelFileDescriptor {
        val file = getFileForDocId(documentId, false)
            ?: throw FileNotFoundException("$documentId not found")
        val fileMode = when (mode) {
            "r" -> ParcelFileDescriptor.MODE_READ_ONLY
            "w", "wt" -> ParcelFileDescriptor.MODE_WRITE_ONLY or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE
            "wa" -> ParcelFileDescriptor.MODE_WRITE_ONLY or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_APPEND
            "rw" -> ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE
            "rwt" -> ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE
            else -> throw IllegalArgumentException("Invalid mode: $mode")
        }
        return ParcelFileDescriptor.open(file, fileMode)
    }

    override fun onCreate(): Boolean = true

    override fun createDocument(parentDocumentId: String, mimeType: String, displayName: String): String {
        val parent = getFileForDocId(parentDocumentId)
        if (parent != null) {
            var newFile = File(parent, displayName)
            var noConflictId = 2
            while (newFile.exists()) {
                newFile = File(parent, "$displayName ($noConflictId)")
                noConflictId++
            }
            try {
                val succeeded = if (Document.MIME_TYPE_DIR == mimeType) newFile.mkdir() else newFile.createNewFile()
                if (succeeded) {
                    return if (parentDocumentId.endsWith("/")) "$parentDocumentId${newFile.name}"
                    else "$parentDocumentId/${newFile.name}"
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        throw FileNotFoundException("Failed to create document in $parentDocumentId with name $displayName")
    }

    override fun deleteDocument(documentId: String) {
        val file = getFileForDocId(documentId)
        if (file == null || !deleteFile(file)) {
            throw FileNotFoundException("Failed to delete document $documentId")
        }
    }

    private fun deleteFile(file: File): Boolean {
        if (file.isDirectory && !isSymbolicLink(file)) {
            file.listFiles()?.forEach { if (!deleteFile(it)) return false }
        }
        return file.delete()
    }

    private fun isSymbolicLink(file: File): Boolean = try {
        val stat = Os.lstat(file.path)
        (stat.st_mode and 0xF000) == 0xA000
    } catch (e: ErrnoException) {
        e.printStackTrace()
        false
    }

    override fun removeDocument(documentId: String, parentDocumentId: String) {
        deleteDocument(documentId)
    }

    override fun renameDocument(documentId: String, displayName: String): String {
        val file = getFileForDocId(documentId)
        if (file != null) {
            val target = File(file.parentFile, displayName)
            if (file.renameTo(target)) {
                val i = documentId.lastIndexOf('/', documentId.length - 2)
                return "${documentId.substring(0, i)}/$displayName"
            }
        }
        throw FileNotFoundException("Failed to rename document $documentId to $displayName")
    }

    override fun moveDocument(sourceDocumentId: String, sourceParentDocumentId: String, targetParentDocumentId: String): String {
        val sourceFile = getFileForDocId(sourceDocumentId)
        val targetDir = getFileForDocId(targetParentDocumentId)
        if (sourceFile != null && targetDir != null) {
            val targetFile = File(targetDir, sourceFile.name)
            if (!targetFile.exists() && sourceFile.renameTo(targetFile)) {
                return if (targetParentDocumentId.endsWith("/"))
                    "$targetParentDocumentId${targetFile.name}"
                else
                    "$targetParentDocumentId/${targetFile.name}"
            }
        }
        throw FileNotFoundException("Failed to move document $sourceDocumentId to $targetParentDocumentId")
    }

    override fun getDocumentType(documentId: String): String {
        val file = getFileForDocId(documentId)
        return if (file == null) Document.MIME_TYPE_DIR else getMimeType(file)
    }

    override fun isChildDocument(parentDocumentId: String, documentId: String): Boolean =
        documentId.startsWith(parentDocumentId)

    private fun getMimeType(file: File): String {
        return if (file.isDirectory) {
            Document.MIME_TYPE_DIR
        } else {
            val name = file.name
            val lastDot = name.lastIndexOf('.')
            if (lastDot >= 0) {
                val extension = name.substring(lastDot + 1).lowercase()
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            } else null
        } ?: "application/octet-stream"
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle {
        super.call(method, arg, extras)?.let { return it }
        if (!method.startsWith("mt:")) return Bundle()

        val out = Bundle()
        try {
            val uri: Uri? = extras?.getParcelable("uri")
            val pathSegments = uri?.pathSegments ?: return out
            val documentId = if (pathSegments.size >= 4) pathSegments[3] else pathSegments[1]
            when (method) {
                METHOD_SET_LAST_MODIFIED -> {
                    val file = getFileForDocId(documentId)
                    out.putBoolean("result", file?.setLastModified(extras.getLong("time")) == true)
                }
                METHOD_SET_PERMISSIONS -> {
                    val file = getFileForDocId(documentId)
                    if (file != null) {
                        try {
                            Os.chmod(file.path, extras.getInt("permissions"))
                            out.putBoolean("result", true)
                        } catch (e: ErrnoException) {
                            out.putBoolean("result", false)
                            out.putString("message", e.message)
                        }
                    } else out.putBoolean("result", false)
                }
                METHOD_CREATE_SYMLINK -> {
                    val file = getFileForDocId(documentId, false)
                    val path = extras.getString("path")
                    if (file != null && path != null) {
                        try {
                            Os.symlink(path, file.path)
                            out.putBoolean("result", true)
                        } catch (e: ErrnoException) {
                            out.putBoolean("result", false)
                            out.putString("message", e.message)
                        }
                    } else out.putBoolean("result", false)
                }
                else -> {
                    out.putBoolean("result", false)
                    out.putString("message", "Unsupported method: $method")
                }
            }
        } catch (e: Exception) {
            out.putBoolean("result", false)
            out.putString("message", e.toString())
        }
        return out
    }

    @Throws(FileNotFoundException::class)
    private fun includeFile(result: MatrixCursor, docId: String, givenFile: File?) {
        val file = givenFile ?: getFileForDocId(docId)
        if (file == null) {
            val row = result.newRow()
            row.add(Document.COLUMN_DOCUMENT_ID, packageName)
            row.add(Document.COLUMN_DISPLAY_NAME, packageName)
            row.add(Document.COLUMN_SIZE, 0L)
            row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR)
            row.add(Document.COLUMN_LAST_MODIFIED, 0)
            row.add(Document.COLUMN_FLAGS, 0)
            return
        }

        var flags = 0
        if (file.isDirectory && file.canWrite()) flags = flags or Document.FLAG_DIR_SUPPORTS_CREATE
        else if (file.canWrite()) flags = flags or Document.FLAG_SUPPORTS_WRITE

        if (file.parentFile?.canWrite() == true) {
            flags = flags or Document.FLAG_SUPPORTS_DELETE or Document.FLAG_SUPPORTS_RENAME
        }

        val path = file.path
        val displayName: String
        var addExtras = false

        when (path) {
            dataDir.path -> displayName = "data"
            androidDataDir?.path -> displayName = "android_data"
            else -> {
                displayName = file.name
                addExtras = true
            }
        }

        val row = result.newRow()
        row.add(Document.COLUMN_DOCUMENT_ID, docId)
        row.add(Document.COLUMN_DISPLAY_NAME, displayName)
        row.add(Document.COLUMN_SIZE, file.length())
        row.add(Document.COLUMN_MIME_TYPE, getMimeType(file))
        row.add(Document.COLUMN_LAST_MODIFIED, file.lastModified())
        row.add(Document.COLUMN_FLAGS, flags)
        row.add(COLUMN_MT_PATH, file.absolutePath)

        if (addExtras) {
            try {
                val stat = Os.lstat(path)
                val sb = StringBuilder()
                sb.append(stat.st_mode)
                    .append("|").append(stat.st_uid)
                    .append("|").append(stat.st_gid)
                if ((stat.st_mode and 0xF000) == 0xA000) {
                    sb.append("|").append(Os.readlink(path))
                }
                row.add(COLUMN_MT_EXTRAS, sb.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}