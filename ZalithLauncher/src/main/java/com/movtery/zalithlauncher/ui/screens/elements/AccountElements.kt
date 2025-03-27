package com.movtery.zalithlauncher.ui.screens.elements

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.getAccountTypeName
import com.movtery.zalithlauncher.path.PathManager
import java.io.File
import java.io.IOException
import java.nio.file.Files

@Composable
fun AccountAvatar(
    modifier: Modifier = Modifier,
    avatarSize: Int = 64,
    account: Account?,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(all = 12.dp)
        ) {
            if (account != null) {
                PlayerFace(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    account = account,
                    avatarSize = avatarSize
                )
            } else {
                Icon(
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.CenterHorizontally),
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = null,
                    tint = contentColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = account?.username ?: stringResource(R.string.account_add_new_account),
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall,
                color = contentColor
            )
            if (account != null) {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = getAccountTypeName(context, account),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor
                )
            }
        }
    }
}

@Composable
fun PlayerFace(
    modifier: Modifier = Modifier,
    account: Account,
    avatarSize: Int = 64
) {
    val context = LocalContext.current
    val avatarBitmap = remember(account) {
        getAvatarFromAccount(context, account, avatarSize).asImageBitmap()
    }

    val newAvatarSize = avatarBitmap.width.dp

    Image(
        modifier = modifier.size(newAvatarSize),
        bitmap = avatarBitmap,
        contentDescription = null
    )
}

@Throws(Exception::class)
private fun getAvatarFromAccount(context: Context, account: Account, size: Int): Bitmap {
    val skin = File(PathManager.DIR_ACCOUNT_SKIN, "${account.uniqueUUID}.png")
    if (skin.exists()) {
        runCatching {
            Files.newInputStream(skin.toPath()).use { `is` ->
                val bitmap = BitmapFactory.decodeStream(`is`)
                    ?: throw IOException("Failed to read the skin picture and try to parse it to a bitmap")
                return getAvatar(bitmap, size)
            }
        }.onFailure { e ->
            Log.e("SkinLoader", "Failed to load avatar from locally!", e)
        }
    }
    return getDefaultAvatar(context, size)
}

@Throws(Exception::class)
private fun getDefaultAvatar(context: Context, size: Int): Bitmap {
    val `is` = context.assets.open("steve.png")
    return getAvatar(BitmapFactory.decodeStream(`is`), size)
}

private fun getAvatar(skin: Bitmap, size: Int): Bitmap {
    val faceOffset = Math.round(size / 18.0).toFloat()
    val scaleFactor = skin.width / 64.0f
    val faceSize = Math.round(8 * scaleFactor)
    val faceBitmap = Bitmap.createBitmap(skin, faceSize, faceSize, faceSize, faceSize, null as Matrix?, false)
    val hatBitmap = Bitmap.createBitmap(skin, Math.round(40 * scaleFactor), faceSize, faceSize, faceSize, null as Matrix?, false)
    val avatar = createBitmap(size, size)
    val canvas = android.graphics.Canvas(avatar)
    val faceScale = ((size - 2 * faceOffset) / faceSize)
    val hatScale = (size.toFloat() / faceSize)
    var matrix = Matrix()
    matrix.postScale(faceScale, faceScale)
    val newFaceBitmap = Bitmap.createBitmap(faceBitmap, 0, 0, faceSize, faceSize, matrix, false)
    matrix = Matrix()
    matrix.postScale(hatScale, hatScale)
    val newHatBitmap = Bitmap.createBitmap(hatBitmap, 0, 0, faceSize, faceSize, matrix, false)
    canvas.drawBitmap(newFaceBitmap, faceOffset, faceOffset, Paint(Paint.ANTI_ALIAS_FLAG))
    canvas.drawBitmap(newHatBitmap, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG))
    return avatar
}