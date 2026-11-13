
package com.asq.fetch.downloader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialMediaResolver @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    suspend fun resolveUrl(url: String): String? = withContext(Dispatchers.IO) {
        // إذا كان الرابط مباشراً (صورة أو فيديو عادي) نرجعه كما هو
        if (url.matches(Regex(".*\\.(mp4|jpg|jpeg|png|webp|gif|mp3)$", RegexOption.IGNORE_CASE))) {
            return@withContext url
        }

        try {
            // نستخدم متصفح وهمي لخداع المنصة وإرجاع الصفحة
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .build()

            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return@withContext null

            // البحث عن رابط الفيديو (og:video) أو الصورة (og:image)
            val videoRegex = Regex("<meta[^>]+property=[\"']og:video[\"'][^>]+content=[\"'](.*?)[\"']")
            val imageRegex = Regex("<meta[^>]+property=[\"']og:image[\"'][^>]+content=[\"'](.*?)[\"']")

            val videoUrl = videoRegex.find(html)?.groupValues?.get(1)
            val imageUrl = imageRegex.find(html)?.groupValues?.get(1)

            // نرجع الفيديو إذا وجدناه، وإلا نرجع الصورة
            return@withContext videoUrl ?: imageUrl
        } catch (e: Exception) {
            return@withContext null
        }
    }
}
