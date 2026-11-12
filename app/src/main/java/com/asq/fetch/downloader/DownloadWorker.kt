package com.asq.fetch.downloader

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.asq.fetch.R
import com.asq.fetch.data.local.DownloadDao
import com.asq.fetch.data.local.DownloadEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val dao: DownloadDao
) : CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_ID = "id"
        const val CHANNEL_ID = "asq_downloads"
    }

    override suspend fun doWork(): Result {
        val id = inputData.getLong(KEY_ID, -1L)
        if (id < 0) return Result.failure()
        val entity = dao.findById(id) ?: return Result.failure()

        return try {
            performDownload(entity)
            Result.success()
        } catch (e: CancellationException) {
            dao.update(entity.copy(status = "CANCELLED", updatedAt = System.currentTimeMillis()))
            throw e
        } catch (e: Exception) {
            dao.update(
                entity.copy(
                    status = "FAILED",
                    error = e.message,
                    updatedAt = System.currentTimeMillis()
                )
            )
            Result.retry()
        }
    }

    private suspend fun performDownload(entity: DownloadEntity) {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        val dir = applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: applicationContext.filesDir
        dir.mkdirs()
        val file = File(dir, entity.fileName)

        val existing = if (file.exists()) file.length() else 0L
        val requestBuilder = Request.Builder()
            .url(entity.url)
            .header("User-Agent", "Mozilla/5.0 (Android) ASQFetch")
        if (existing > 0) requestBuilder.header("Range", "bytes=$existing-")

        val response = client.newCall(requestBuilder.build()).execute()
        if (!response.isSuccessful) throw IOException("HTTP ${response.code}")

        val body = response.body ?: throw IOException("Empty response body")
        val startAt = if (response.code == 206) existing else 0L
        if (startAt == 0L && file.exists()) file.delete()
        val total = if (body.contentLength() > 0) body.contentLength() + startAt else 0L

        dao.update(
            entity.copy(
                status = "RUNNING",
                totalBytes = total,
                downloadedBytes = startAt,
                updatedAt = System.currentTimeMillis()
            )
        )

        body.byteStream().use { input ->
            FileOutputStream(file, startAt > 0).use { output ->
                val buffer = ByteArray(8192)
                var written = startAt
                var lastEmit = 0L
                while (!isStopped) {
                    val read = input.read(buffer)
                    if (read == -1) break
                    output.write(buffer, 0, read)
                    written += read
                    val now = System.currentTimeMillis()
                    if (now - lastEmit > 500) {
                        lastEmit = now
                        dao.update(
                            entity.copy(
                                status = "RUNNING",
                                downloadedBytes = written,
                                totalBytes = total,
                                updatedAt = now
                            )
                        )
                    }
                }
                if (isStopped) {
                    dao.update(
                        entity.copy(
                            status = "CANCELLED",
                            downloadedBytes = file.length(),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                    return
                }
            }
        }

        dao.update(
            entity.copy(
                status = "COMPLETED",
                downloadedBytes = file.length(),
                totalBytes = file.length(),
                localPath = file.absolutePath,
                updatedAt = System.currentTimeMillis()
            )
        )
        notifyCompleted(entity, file)
    }

    private fun notifyCompleted(entity: DownloadEntity, file: File) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Downloads", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val uri = FileProvider.getUriForFile(
            applicationContext,
            "${applicationContext.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val pending = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(entity.fileName)
            .setContentText("Download complete")
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        nm.notify(entity.id.toInt(), notif)
    }
}
