package com.movtery.zalithlauncher.ui.screens.elements

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.createBitmap
import com.movtery.zalithlauncher.R
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountType
import com.movtery.zalithlauncher.game.account.getAccountTypeName
import com.movtery.zalithlauncher.game.account.otherserver.models.AuthResult
import com.movtery.zalithlauncher.game.account.otherserver.models.Servers.Server
import com.movtery.zalithlauncher.path.PathManager
import java.io.File
import java.io.IOException
import java.nio.file.Files

/**
 * 微软登录的操作状态
 */
sealed interface MicrosoftLoginOperation {
    data object None : MicrosoftLoginOperation
    data object RunTask: MicrosoftLoginOperation
}

/**
 * 添加认证服务器时的状态
 */
sealed interface ServerOperation {
    data object None : ServerOperation
    data object AddNew : ServerOperation
    data class Delete(val serverName: String, val serverIndex: Int) : ServerOperation
    data class Add(val serverUrl: String) : ServerOperation
    data class OnThrowable(val throwable: Throwable) : ServerOperation
}

/**
 * 账号操作的状态
 */
sealed interface AccountOperation {
    data object None : AccountOperation
    data class Delete(val account: Account) : AccountOperation
    data class Refresh(val account: Account) : AccountOperation
    data class OnFailed(val error: String) : AccountOperation
}

/**
 * 认证服务器登陆时的状态
 */
sealed interface OtherLoginOperation {
    data object None : OtherLoginOperation
    data class OnLogin(val server: Server) : OtherLoginOperation
    data class OnFailed(val error: String) : OtherLoginOperation
    data class SelectRole(
        val profiles: List<AuthResult.AvailableProfiles>,
        val selected: (AuthResult.AvailableProfiles) -> Unit
    ) : OtherLoginOperation
}

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

@Composable
fun AccountItem(
    modifier: Modifier = Modifier,
    currentAccount: Account?,
    account: Account,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onSelected: (uniqueUUID: String) -> Unit = {},
    onRefreshClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    val selected = currentAccount?.uniqueUUID == account.uniqueUUID

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.inversePrimary,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (selected) return@clickable
                    onSelected(account.uniqueUUID)
                }
                .clip(shape = MaterialTheme.shapes.large)
                .padding(all = 8.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = {
                    if (selected) return@RadioButton
                    onSelected(account.uniqueUUID)
                }
            )
            PlayerFace(
                modifier = Modifier.align(Alignment.CenterVertically),
                account = account,
                avatarSize = 46
            )
            Spacer(modifier = Modifier.width(18.dp))
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            ) {
                val context = LocalContext.current
                Text(text = account.username)
                Text(
                    text = getAccountTypeName(context, account),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Row {
                IconButton(
                    onClick = onRefreshClick,
                    enabled = account.accountType != AccountType.LOCAL.tag
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_refresh),
                        contentDescription = stringResource(R.string.generic_refresh)
                    )
                }
                IconButton(
                    onClick = onDeleteClick
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = stringResource(R.string.generic_delete)
                    )
                }
            }
        }
    }
}

@Composable
fun LoginItem(
    modifier: Modifier = Modifier,
    serverName: String,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(start = 4.dp, top = 12.dp, bottom = 12.dp, end = 4.dp)
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(R.drawable.ic_add),
            contentDescription = serverName,
            tint = contentColor
        )
        Spacer(
            modifier = Modifier.width(8.dp)
        )
        Text(
            modifier = Modifier.align(Alignment.CenterVertically),
            text = serverName,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun ServerItem(
    modifier: Modifier = Modifier,
    server: Server,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    onClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(start = 4.dp)
    ) {
        Text(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            text = server.serverName,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(
            modifier = Modifier.width(8.dp)
        )
        IconButton(
            onClick = onDeleteClick
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = stringResource(R.string.generic_delete),
                tint = contentColor
            )
        }
    }
}

@Composable
fun OtherServerLoginDialog(
    server: Server,
    onRegisterClick: (url: String) -> Unit = {},
    onDismissRequest: () -> Unit = {},
    onConfirm: (email: String, password: String) -> Unit = { _, _ -> }
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(shape = MaterialTheme.shapes.extraLarge) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = server.serverName,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.size(16.dp))
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    isError = email.isEmpty(),
                    label = { Text(text = stringResource(R.string.account_label_email)) },
                    supportingText = {
                        if (email.isEmpty()) {
                            Text(text = stringResource(R.string.account_supporting_email_invalid_empty))
                        }
                    }
                )
                Spacer(modifier = Modifier.size(8.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    isError = password.isEmpty(),
                    label = { Text(text = stringResource(R.string.account_label_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Transparent,
                    ),
                    supportingText = {
                        if (password.isEmpty()) {
                            Text(text = stringResource(R.string.account_supporting_password_invalid_empty))
                        }
                    }
                )
                if (!server.register.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = stringResource(R.string.account_other_login_register),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Start).clickable { onRegisterClick(server.register) }
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                } else Spacer(modifier = Modifier.size(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onDismissRequest
                    ) {
                        Text(text = stringResource(R.string.generic_cancel))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (email.isNotEmpty() && password.isNotEmpty()) {
                                onConfirm(email, password)
                            }
                        }
                    ) {
                        Text(text = stringResource(R.string.generic_confirm))
                    }
                }
            }
        }
    }
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