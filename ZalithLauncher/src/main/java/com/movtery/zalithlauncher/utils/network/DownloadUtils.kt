package com.movtery.zalithlauncher.utils.network

import com.movtery.zalithlauncher.path.UrlManager
import okhttp3.Call
import java.io.File
import java.io.IOException

class DownloadUtils {
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
    }
}