package com.asq.fetch.downloader

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun enqueue(id: Long) {
        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workDataOf(DownloadWorker.KEY_ID to id))
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag("asq_download")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "asq_download_$id",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun cancel(id: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("asq_download_$id")
    }
}
