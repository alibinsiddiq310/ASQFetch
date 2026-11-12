package com.asq.fetch.ui.screens

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asq.fetch.R
import com.asq.fetch.ui.MainViewModel

@Composable
fun HomeScreen(vm: MainViewModel, onAbout: () -> Unit) {
    var url by rememberSaveable { mutableStateOf("") }
    val downloads by vm.downloads.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Download, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onAbout) {
                Icon(Icons.Default.Info, null)
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.url_hint)) },
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        Row {
            OutlinedButton(onClick = {
                val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val text = clip.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
                url = text
            }) { Text(stringResource(R.string.paste)) }

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = {
                    if (url.isNotBlank()) {
                        vm.startDownload(url.trim())
                        url = ""
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.download))
            }
        }

        if (url.isNotBlank()) {
            Spacer(Modifier.height(16.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        detectPlatformLabel(url),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(url, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(stringResource(R.string.recent), style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(8.dp))

        downloads.take(5).forEach { d ->
            Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(d.fileName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "${d.platform} • ${d.status}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

private fun detectPlatformLabel(url: String): String = when {
    url.contains("instagram.com") -> "Instagram"
    url.contains("tiktok.com") -> "TikTok"
    url.contains("facebook.com") || url.contains("fb.watch") -> "Facebook"
    url.contains("snapchat.com") -> "Snapchat"
    url.contains("youtube.com") || url.contains("youtu.be") -> "YouTube"
    url.contains("twitter.com") || url.contains("x.com") -> "X"
    url.contains("pinterest.com") -> "Pinterest"
    url.contains("reddit.com") -> "Reddit"
    url.contains("vimeo.com") -> "Vimeo"
    url.contains("dailymotion.com") -> "Dailymotion"
    url.contains("threads.net") -> "Threads"
    else -> "Direct URL"
}
