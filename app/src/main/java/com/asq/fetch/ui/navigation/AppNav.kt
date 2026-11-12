package com.asq.fetch.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.asq.fetch.R
import com.asq.fetch.ui.MainViewModel
import com.asq.fetch.ui.screens.AboutScreen
import com.asq.fetch.ui.screens.DownloadsScreen
import com.asq.fetch.ui.screens.HomeScreen
import com.asq.fetch.ui.screens.SettingsScreen

@Composable
fun AppNav(vm: MainViewModel) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            if (route != "about") {
                NavigationBar {
                    NavigationBarItem(
                        selected = route == "home",
                        onClick = { nav.navigate("home") { popUpTo("home") { inclusive = true }; launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text(stringResource(R.string.home_title)) }
                    )
                    NavigationBarItem(
                        selected = route == "downloads",
                        onClick = { nav.navigate("downloads") { popUpTo("home"); launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Download, null) },
                        label = { Text(stringResource(R.string.downloads)) }
                    )
                    NavigationBarItem(
                        selected = route == "settings",
                        onClick = { nav.navigate("settings") { popUpTo("home"); launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Settings, null) },
                        label = { Text(stringResource(R.string.settings)) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { HomeScreen(vm, onAbout = { nav.navigate("about") }) }
            composable("downloads") { DownloadsScreen(vm) }
            composable("settings") { SettingsScreen(vm, onAbout = { nav.navigate("about") }) }
            composable("about") { AboutScreen(onBack = { nav.popBackStack() }) }
        }
    }
}
