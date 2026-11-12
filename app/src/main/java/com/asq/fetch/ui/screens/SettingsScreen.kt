import androidx.compose.runtime.getValue
package com.asq.fetch.ui.screens

import android.app.Activity
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
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.asq.fetch.R
import com.asq.fetch.ui.MainViewModel

@Composable
fun SettingsScreen(vm: MainViewModel, onAbout: () -> Unit) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(stringResource(R.string.language), style = MaterialTheme.typography.titleMedium)
        Row {
            FilterChip(
                selected = settings.language == "ar",
                onClick = {
                    vm.setLanguage("ar")
                    (context as? Activity)?.recreate()
                },
                label = { Text("العربية") }
            )
            Spacer(Modifier.width(8.dp))
            FilterChip(
                selected = settings.language == "en",
                onClick = {
                    vm.setLanguage("en")
                    (context as? Activity)?.recreate()
                },
                label = { Text("English") }
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium)
        Row {
            FilterChip(
                selected = settings.theme == "light",
                onClick = { vm.setTheme("light") },
                label = { Text(stringResource(R.string.light)) }
            )
            Spacer(Modifier.width(8.dp))
            FilterChip(
                selected = settings.theme == "dark",
                onClick = { vm.setTheme("dark") },
                label = { Text(stringResource(R.string.dark)) }
            )
            Spacer(Modifier.width(8.dp))
            FilterChip(
                selected = settings.theme == "system",
                onClick = { vm.setTheme("system") },
                label = { Text(stringResource(R.string.system)) }
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.notifications),
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = settings.notifications,
                onCheckedChange = { vm.toggleNotifications(it) }
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(onClick = onAbout, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.about))
        }
    }
}
