package com.asq.fetch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.asq.fetch.data.local.DownloadDao
import com.asq.fetch.data.local.DownloadEntity
import com.asq.fetch.data.prefs.AppSettings
import com.asq.fetch.data.prefs.SettingsRepository
import com.asq.fetch.downloader.DownloadScheduler
import com.asq.fetch.downloader.SocialMediaResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val dao: DownloadDao,
    private val scheduler: DownloadScheduler,
    private val settingsRepo: SettingsRepository,
    private val socialResolver: SocialMediaResolver
) : ViewModel() {

    val downloads: StateFlow<List<DownloadEntity>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettings> = settingsRepo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun startDownload(url: String) {
        viewModelScope.launch {
            val cleanUrl = url.trim()
            
            // تحليل الرابط
            val directUrl = socialResolver.resolveUrl(cleanUrl)
            
            if (directUrl == null) {
                // فشل التحليل (منصة غير مدعومة أو رابط خاطئ)
                return@launch
            }

            val fileName = directUrl.substringAfterLast('/').substringBefore('?')
                .ifBlank { "file_${System.currentTimeMillis()}" }
            
            val platform = detectPlatform(cleanUrl)

            val id = dao.insert(
                DownloadEntity(
                    url = directUrl,
                    fileName = fileName,
                    platform = platform,
                    status = "QUEUED"
                )
            )
            scheduler.enqueue(id)
        }
    }

    fun cancel(entity: DownloadEntity) {
        scheduler.cancel(entity.id)
        viewModelScope.launch {
            dao.update(entity.copy(status = "CANCELLED", updatedAt = System.currentTimeMillis()))
        }
    }

    fun delete(entity: DownloadEntity) {
        viewModelScope.launch { dao.delete(entity) }
    }

    fun setLanguage(value: String) {
        viewModelScope.launch { settingsRepo.setLanguage(value) }
    }

    fun setTheme(value: String) {
        viewModelScope.launch { settingsRepo.setTheme(value) }
    }

    fun toggleNotifications(value: Boolean) {
        viewModelScope.launch { settingsRepo.setNotifications(value) }
    }

    private fun detectPlatform(url: String): String = when {
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
        else -> "Direct"
    }
}
