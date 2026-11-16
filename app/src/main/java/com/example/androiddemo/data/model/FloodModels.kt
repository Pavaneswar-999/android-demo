package com.example.androiddemo.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FloodForecastResponse(
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
    @Json(name = "timezone") val timezone: String? = null,
    @Json(name = "daily") val daily: DailyFloodData? = null,
    @Json(name = "daily_units") val dailyUnits: DailyUnits? = null
)

@JsonClass(generateAdapter = true)
data class DailyFloodData(
    @Json(name = "time") val time: List<String>? = null,
    @Json(name = "river_discharge") val riverDischarge: List<Double?>? = null,
    @Json(name = "river_discharge_mean") val riverDischargeMean: List<Double?>? = null,
    @Json(name = "river_discharge_max") val riverDischargeMax: List<Double?>? = null
)

@JsonClass(generateAdapter = true)
data class DailyUnits(
    @Json(name = "river_discharge") val riverDischargeUnit: String? = "m³/s"
)

enum class RiskLevel {
    NORMAL,
    WATCH,
    ALERT
}

data class DailyRiskItem(
    val date: String,
    val discharge: Double,
    val meanDischarge: Double,
    val maxDischarge: Double,
    val riskLevel: RiskLevel
)

data class FloodForecastSummary(
    val placeName: String,
    val latitude: Double,
    val longitude: Double,
    val todayDischarge: Double?,
    val peakDate: String?,
    val peakDischarge: Double?,
    val dailyItems: List<DailyRiskItem>
)
