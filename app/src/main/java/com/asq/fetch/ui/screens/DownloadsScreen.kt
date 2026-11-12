import androidx.compose.runtime.getValue
package com.asq.fetch.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asq.fetch.R
import com.asq.fetch.data.local.DownloadEntity
import com.asq.fetch.ui.MainViewModel
import java.io.File

@Composable
fun DownloadsScreen(vm: MainViewModel) {
    val downloads by vm.downloads.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (downloads.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_downloads))
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(downloads, key = { it.id }) { d ->
            DownloadCard(d, vm, onOpen = {
                if (d.localPath != null) {
                    val f = File(d.localPath)
                    if (f.exists()) {
                        val uri = FileProvider.getUriForFile(
                            context, "${context.packageName}.fileprovider", f
                        )
                        val i = Intent(Intent.ACTION_VIEW)
                            .setDataAndType(uri, "*/*")
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        context.startActivity(Intent.createChooser(i, "Open"))
                    }
                }
            }, onShare = {
                if (d.localPath != null) {
                    val f = File(d.localPath)
                    if (f.exists()) {
                        val uri = FileProvider.getUriForFile(
                            context, "${context.packageName}.fileprovider", f
                        )
                        val i = Intent(Intent.ACTION_SEND)
                            .setType("*/*")
                            .putExtra(Intent.EXTRA_STREAM, uri)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        context.startActivity(Intent.createChooser(i, "Share"))
                    }
                }
            })
        }
    }
}

@Composable
private fun DownloadCard(
    d: DownloadEntity,
    vm: MainViewModel,
    onOpen: () -> Unit,
    onShare: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row {
                Column(Modifier.weight(1f)) {
                    Text(d.fileName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "${d.platform} • ${d.status}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                if (d.status == "RUNNING" || d.status == "QUEUED") {
                    IconButton(onClick = { vm.cancel(d) }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            }

            if (d.totalBytes > 0) {
                val progress = (d.downloadedBytes.toFloat() / d.totalBytes).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Text(
                    "${d.downloadedBytes / 1024} KB / ${d.totalBytes / 1024} KB",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            if (d.status == "COMPLETED" && d.localPath != null) {
                Row(Modifier.padding(top = 8.dp)) {
                    TextButton(onClick = onOpen) { Text(stringResource(R.string.open)) }
                    TextButton(onClick = onShare) { Text(stringResource(R.string.share)) }
                    TextButton(onClick = { vm.delete(d) }) { Text(stringResource(R.string.delete)) }
                }
            }
        }
    }
}
