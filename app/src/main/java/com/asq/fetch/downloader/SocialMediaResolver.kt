package com.asq.fetch.downloader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialMediaResolver @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    // رابط السيرفر الخاص بك
    private val backendBaseUrl = "https://asq-fetch-backend.onrender.com"

    suspend fun resolveUrl(url: String): String? = withContext(Dispatchers.IO) {
        val cleanUrl = url.trim()

        // إذا كان الرابط مباشراً، لا داعي لإرساله للسيرفر
        if (cleanUrl.matches(Regex(".*\\.(mp4|jpg|jpeg|png|webp|gif|mp3)$", RegexOption.IGNORE_CASE))) {
            return@withContext cleanUrl
        }

        try {
            val encodedUrl = URLEncoder.encode(cleanUrl, "UTF-8")
            val apiUrl = "$backendBaseUrl/download?url=$encodedUrl&format=best"

            val request = Request.Builder()
                .url(apiUrl)
                .build()

            // نتحقق فقط إذا كان السيرفر يستجيب
            val response = client.newCall(request).execute()
            
            // نرجع الرابط ليقوم محرك التنزيل بتحميله
            return@withContext if (response.isSuccessful) apiUrl else null
        } catch (e: Exception) {
            return@withContext null
        }
    }
}
