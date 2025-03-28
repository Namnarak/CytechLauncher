package com.movtery.zalithlauncher.utils.network

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.path.UrlManager
import okhttp3.Call
import java.io.File
import java.io.IOException

class NetWorkUtils {
    companion object {
        /**
         * 同步下载文件到本地
         * @param url 要下载的文件URL
         * @param outputFile 要保存的目标文件
         * @throws IllegalArgumentException 当URL无效时
         * @throws IOException 当网络请求失败或文件操作失败时
         */
        @Throws(IOException::class, IllegalArgumentException::class)
        fun downloadFile(url: String, outputFile: File) {
            call(url) { call ->
                call.execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("HTTP ${response.code} - ${response.message}")
                    }

                    val body = response.body ?: throw IOException("Response body is empty")

                    outputFile.parentFile?.takeUnless { it.exists() }?.mkdirs()

                    body.byteStream().use { inputStream ->
                        outputFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            }
        }

        /**
         * 同步获取 URL 返回的字符串内容
         * @param url 要请求的URL地址
         * @return 服务器返回的字符串内容
         * @throws IllegalArgumentException 当URL无效时
         * @throws IOException 当网络请求失败或响应解析失败时
         */
        @Throws(IOException::class, IllegalArgumentException::class)
        fun fetchStringFromUrl(url: String): String {
            return call(url) { call ->
                call.execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("HTTP ${response.code} - ${response.message}")
                    }

                    return@call response.body
                        ?.use { it.string() }
                        ?: throw IOException("Empty response body")
                }
            }
        }

        private fun <T> call(url: String, call: (Call) -> T): T {
            val client = UrlManager.createOkHttpClient()
            val request = UrlManager.createRequestBuilder(url).build()

            return call(client.newCall(request))
        }

        /**
         * 展示一个提示弹窗，告知用户接下来将要在浏览器内访问的链接，用户可以选择不进行访问
         * @param link 要访问的链接
         */
        fun openLink(context: Context, link: String) {
            openLink(context, link, null)
        }

        /**
         * 展示一个提示弹窗，告知用户接下来将要在浏览器内访问的链接，用户可以选择不进行访问
         * @param link 要访问的链接
         * @param dataType 设置 intent 的数据以及显式 MIME 数据类型
         */
        fun openLink(context: Context, link: String, dataType: String?) {
            MaterialAlertDialogBuilder(context)
                .setTitle(R.string.generic_open_link)
                .setMessage(link)
                .setPositiveButton(R.string.generic_confirm) { _, _ ->
                    val uri = link.toUri()
                    val browserIntent: Intent
                    if (dataType != null) {
                        browserIntent = Intent(Intent.ACTION_VIEW)
                        browserIntent.setDataAndType(uri, dataType)
                    } else {
                        browserIntent = Intent(Intent.ACTION_VIEW, uri)
                    }
                    context.startActivity(browserIntent)
                }
                .setNegativeButton(R.string.generic_cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}