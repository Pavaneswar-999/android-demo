package com.example.androiddemo.data.repository

import com.example.androiddemo.data.api.OpenMeteoFloodApi
import com.example.androiddemo.data.local.PlaceDao
import com.example.androiddemo.data.model.DailyRiskItem
import com.example.androiddemo.data.model.FloodForecastResponse
import com.example.androiddemo.data.model.FloodForecastSummary
import com.example.androiddemo.data.model.RiskLevel
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FloodRepository(
    private val api: OpenMeteoFloodApi,
    private val placeDao: PlaceDao
) {

    suspend fun getFloodForecast(
        placeName: String,
        latitude: Double,
        longitude: Double
    ): Result<FloodForecastSummary> {
        return try {
            val response = api.getFloodForecast(latitude, longitude)
            val summary = processForecastResponse(placeName, latitude, longitude, response)
            Result.success(summary)
        } catch (e: HttpException) {
            if (e.code() == 404) {
                Result.failure(IllegalStateException("No modeled river basin cell within 5km for $placeName (Lat: $latitude, Lon: $longitude). Please select a river location from Places."))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun calculateRiskLevel(discharge: Double, mean: Double): RiskLevel {
        val safeDischarge = if (discharge < 0 || discharge.isNaN() || discharge.isInfinite()) 0.0 else discharge
        val safeMean = if (mean < 0 || mean.isNaN() || mean.isInfinite()) 0.0 else mean

        return when {
            safeMean <= 0.0 -> {
                if (safeDischarge > 0.0) RiskLevel.ALERT else RiskLevel.NORMAL
            }
            safeDischarge > 2.0 * safeMean -> RiskLevel.ALERT
            safeDischarge > 1.5 * safeMean -> RiskLevel.WATCH
            else -> RiskLevel.NORMAL
        }
    }

    fun processForecastResponse(
        placeName: String,
        latitude: Double,
        longitude: Double,
        response: FloodForecastResponse
    ): FloodForecastSummary {
        val daily = response.daily ?: throw IllegalArgumentException("Forecast daily data unavailable")
        val times = daily.time ?: emptyList()
        val discharges = daily.riverDischarge ?: emptyList()
        val means = daily.riverDischargeMean ?: emptyList()
        val maxes = daily.riverDischargeMax ?: emptyList()

        val dailyItems = mutableListOf<DailyRiskItem>()
        var peakDischarge: Double? = null
        var peakDate: String? = null
        var todayDischarge: Double? = null

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        response.timezone?.let { tzId ->
            try {
                sdf.timeZone = TimeZone.getTimeZone(tzId)
            } catch (_: Exception) {}
        }
        val todayStr = sdf.format(Date())

        val count = times.size
        for (i in 0 until count) {
            val date = times.getOrNull(i) ?: continue
            val rawDischarge = discharges.getOrNull(i) ?: 0.0
            val rawMean = means.getOrNull(i) ?: 0.0
            val rawMax = maxes.getOrNull(i) ?: rawDischarge

            val discharge = if (rawDischarge < 0 || rawDischarge.isNaN() || rawDischarge.isInfinite()) 0.0 else rawDischarge
            val mean = if (rawMean < 0 || rawMean.isNaN() || rawMean.isInfinite()) 0.0 else rawMean
            val max = if (rawMax < 0 || rawMax.isNaN() || rawMax.isInfinite()) discharge else rawMax

            val risk = calculateRiskLevel(discharge, mean)

            dailyItems.add(
                DailyRiskItem(
                    date = date,
                    discharge = discharge,
                    meanDischarge = mean,
                    maxDischarge = max,
                    riskLevel = risk
                )
            )

            if (date == todayStr) {
                todayDischarge = discharge
            }

            if (peakDischarge == null || discharge > peakDischarge) {
                peakDischarge = discharge
                peakDate = date
            }
        }

        if (todayDischarge == null && dailyItems.isNotEmpty()) {
            todayDischarge = dailyItems.first().discharge
        }

        return FloodForecastSummary(
            placeName = placeName,
            latitude = latitude,
            longitude = longitude,
            todayDischarge = todayDischarge,
            peakDate = peakDate,
            peakDischarge = peakDischarge,
            dailyItems = dailyItems
        )
    }

    fun getAllSavedPlaces() = placeDao.getAllPlaces()

    suspend fun savePlace(name: String, lat: Double, lon: Double, isCurrent: Boolean = false): Long {
        if (lat < -90.0 || lat > 90.0) throw IllegalArgumentException("Latitude must be between -90 and 90")
        if (lon < -180.0 || lon > 180.0) throw IllegalArgumentException("Longitude must be between -180 and 180")
        if (name.isBlank()) throw IllegalArgumentException("Place name cannot be empty")

        if (isCurrent) {
            placeDao.deleteCurrentLocationPlace()
        }

        return placeDao.insertPlace(
            com.example.androiddemo.data.local.Place(
                name = name,
                latitude = lat,
                longitude = lon,
                isCurrentLocation = isCurrent
            )
        )
    }

    suspend fun deletePlace(place: com.example.androiddemo.data.local.Place) {
        placeDao.deletePlace(place)
    }
}
