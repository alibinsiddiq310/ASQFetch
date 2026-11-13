package com.asq.fetch.downloader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialMediaResolver @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val backendBaseUrl = "https://asq-fetch-backend.onrender.com"

    suspend fun resolveUrl(url: String): String? = withContext(Dispatchers.IO) {
        val cleanUrl = url.trim()

        // الروابط المباشرة
        if (cleanUrl.matches(Regex(".*\\.(mp4|jpg|jpeg|png|webp|gif|mp3)$", RegexOption.IGNORE_CASE))) {
            return@withContext cleanUrl
        }

        try {
            val encodedUrl = URLEncoder.encode(cleanUrl, "UTF-8")
            val apiUrl = "$backendBaseUrl/resolve?url=$encodedUrl"

            val request = Request.Builder()
                .url(apiUrl)
                .build()

            val response = client.newCall(request).execute()
            val jsonString = response.body?.string() ?: return@withContext null
            
            if (response.isSuccessful) {
                val jsonObject = JSONObject(jsonString)
                val directUrl = jsonObject.optString("direct_url")
                if (directUrl.isNotBlank()) {
                    return@withContext directUrl
                }
            }
            return@withContext null
        } catch (e: Exception) {
            return@withContext null
        }
    }
}
