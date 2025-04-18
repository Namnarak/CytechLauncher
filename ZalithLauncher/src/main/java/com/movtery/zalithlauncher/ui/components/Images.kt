package com.movtery.zalithlauncher.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.gif.GifDecoder
import java.io.File

@Composable
fun rememberAsyncGIFImagePainter(
    imageFile: File
): AsyncImagePainter {
    return rememberAsyncImagePainter(
        model = imageFile,
        imageLoader = ImageLoader.Builder(LocalContext.current)
            .components {
                add(GifDecoder.Factory())
            }
            .build()
    )
}