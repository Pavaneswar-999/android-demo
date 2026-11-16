package com.example.androiddemo.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.androiddemo.data.api.NetworkClient
import com.example.androiddemo.data.local.AppDatabase
import com.example.androiddemo.data.model.RiskLevel
import com.example.androiddemo.data.repository.FloodRepository
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

class FloodCheckWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(appContext)
            val repository = FloodRepository(NetworkClient.floodApi, db.placeDao())
            val places = repository.getAllSavedPlaces().firstOrNull() ?: emptyList()

            for (place in places) {
                val forecastResult = repository.getFloodForecast(
                    place.name,
                    place.latitude,
                    place.longitude
                )

                forecastResult.getOrNull()?.let { summary ->
                    val upcoming7Days = summary.dailyItems.take(7)
                    val hasAlert = upcoming7Days.any { it.riskLevel == RiskLevel.ALERT }
                    if (hasAlert) {
                        NotificationHelper.showFloodAlertNotification(
                            appContext,
                            summary.placeName,
                            summary.peakDate,
                            summary.peakDischarge
                        )
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "river_flood_daily_check"

        fun scheduleDailyCheck(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<FloodCheckWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
