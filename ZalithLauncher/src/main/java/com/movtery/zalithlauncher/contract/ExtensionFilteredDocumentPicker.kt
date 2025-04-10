package com.movtery.zalithlauncher.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContract
import java.util.Collections.emptyList

class ExtensionFilteredDocumentPicker(
    extension: String,
    private val allowMultiple: Boolean = false
) : ActivityResultContract<Any, List<Uri>>() {

    private val mimeType: String

    init {
        val extensionMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        mimeType = extensionMimeType ?: "*/*"
    }

    override fun createIntent(context: Context, input: Any): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            if (allowMultiple) {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
        }
    }

    override fun getSynchronousResult(
        context: Context,
        input: Any
    ): SynchronousResult<List<Uri>>? {
        return null
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        if (intent == null || resultCode != Activity.RESULT_OK) return emptyList()

        val uris = mutableListOf<Uri>()
        intent.clipData?.let { clipData ->
            for (i in 0 until clipData.itemCount) {
                clipData.getItemAt(i)?.uri?.let { uris.add(it) }
            }
        } ?: intent.data?.let { uris.add(it) }

        return uris.takeIf { it.isNotEmpty() } ?: emptyList()
    }
}