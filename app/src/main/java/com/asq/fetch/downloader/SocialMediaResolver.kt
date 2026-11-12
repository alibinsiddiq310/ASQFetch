package com.asq.fetch.downloader

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.sokomishalov.skraper.client.SkraperClient
import ru.sokomishalov.skraper.model.Media
import ru.sokomishalov.skraper.provider.facebook.FacebookSkraper
import ru.sokomishalov.skraper.provider.instagram.InstagramSkraper
import ru.sokomishalov.skraper.provider.pinterest.PinterestSkraper
import ru.sokomishalov.skraper.provider.tiktok.TikTokSkraper
import ru.sokomishalov.skraper.provider.twitter.TwitterSkraper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialMediaResolver @Inject constructor() {

    private val skraperClient = SkraperClient()

    private val scrapers = listOf(
        InstagramSkraper(skraperClient),
        TikTokSkraper(skraperClient),
        TwitterSkraper(skraperClient),
        PinterestSkraper(skraperClient),
        FacebookSkraper(skraperClient)
    )

    suspend fun resolveUrl(url: String): String? = withContext(Dispatchers.IO) {
        if (url.matches(Regex(".*\\.(mp4|jpg|jpeg|png|webp|gif)$", RegexOption.IGNORE_CASE))) {
            return@withContext url
        }

        for (skraper in scrapers) {
            try {
                val post = skraper.getPost(url) ?: continue
                val media: Media? = post.media.firstOrNull()
                if (media != null) {
                    return@withContext media.url
                }
            } catch (e: Exception) {
                continue
            }
        }
        return@withContext null
    }
}
