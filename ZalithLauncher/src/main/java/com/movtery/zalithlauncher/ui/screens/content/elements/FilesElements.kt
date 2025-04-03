package com.movtery.zalithlauncher.ui.screens.content.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.utils.formatDate
import com.movtery.zalithlauncher.utils.formatFileSize
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.Date

@Composable
fun BaseFileItem(
    file: File,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    if (!file.exists()) throw IllegalArgumentException("File is not exists!")

    Row(
        modifier = modifier
    ) {
        Icon(
            modifier = Modifier.size(24.dp).align(Alignment.CenterVertically),
            painter = if (file.isDirectory) painterResource(R.drawable.ic_folder) else painterResource(R.drawable.ic_file),
            contentDescription = null,
            tint = contentColor
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
            Row {
                val date = Date(file.lastModified())
                Text(
                    text = formatDate(date),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor
                )
                if (file.isFile) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatFileSize(FileUtils.sizeOf(file)),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor
                    )
                }
            }
        }
    }
}