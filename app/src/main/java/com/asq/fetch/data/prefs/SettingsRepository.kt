package com.asq.fetch.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "asq_settings")

data class AppSettings(
    val language: String = "ar",
    val theme: String = "system",
    val notifications: Boolean = true
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_LANG = stringPreferencesKey("language")
    private val KEY_THEME = stringPreferencesKey("theme")
    private val KEY_NOTIF = booleanPreferencesKey("notifications")

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            language = prefs[KEY_LANG] ?: "ar",
            theme = prefs[KEY_THEME] ?: "system",
            notifications = prefs[KEY_NOTIF] ?: true
        )
    }

    suspend fun setLanguage(value: String) {
        context.dataStore.edit { it[KEY_LANG] = value }
    }

    suspend fun setTheme(value: String) {
        context.dataStore.edit { it[KEY_THEME] = value }
    }

    suspend fun setNotifications(value: Boolean) {
        context.dataStore.edit { it[KEY_NOTIF] = value }
    }
}
