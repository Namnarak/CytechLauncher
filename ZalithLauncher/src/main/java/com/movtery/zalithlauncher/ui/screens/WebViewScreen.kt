package com.movtery.zalithlauncher.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.movtery.zalithlauncher.state.LocalMainScreenTag
import com.movtery.zalithlauncher.state.LocalWebUrlState
import com.movtery.zalithlauncher.ui.base.BaseScreen
import org.apache.commons.io.FileUtils

const val WEB_VIEW_SCREEN_TAG = "WebViewScreen"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen() {
    BaseScreen(
        screenTag = WEB_VIEW_SCREEN_TAG,
        tagProvider = LocalMainScreenTag
    ) { isVisible ->
        val context = LocalContext.current
        val webView = remember {
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
            }
        }

        val url = LocalWebUrlState.current.currentString
        url ?: throw IllegalStateException("Url is null")

        var isWebLoading by rememberSaveable { mutableStateOf(true) }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { webView }
            ) {
                it.loadUrl(url)
                it.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        isWebLoading = false
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        isWebLoading = true
                    }
                }
                it.settings.apply {
                    javaScriptEnabled = true
                    cacheMode = WebSettings.LOAD_NO_CACHE
                }

                if (!isVisible) {
                    val webCache = context.getDir("webview", 0)
                    FileUtils.deleteQuietly(webCache)
                    CookieManager.getInstance().removeAllCookies(null)
                }
            }

            if (isWebLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp)
                        .align(Alignment.TopCenter)
                )
            }
        }
    }
}