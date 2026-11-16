package com.example.androiddemo

import com.example.androiddemo.data.api.OpenMeteoFloodApi
import com.example.androiddemo.data.local.Place
import com.example.androiddemo.data.local.PlaceDao
import com.example.androiddemo.data.model.DailyFloodData
import com.example.androiddemo.data.model.FloodForecastResponse
import com.example.androiddemo.data.repository.FloodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class PeakDetectionTest {

    private lateinit var repository: FloodRepository

    private val fakeApi = object : OpenMeteoFloodApi {
        override suspend fun getFloodForecast(
            latitude: Double,
            longitude: Double,
            daily: String,
            forecastDays: Int
        ): FloodForecastResponse {
            return FloodForecastResponse()
        }
    }

    private val fakeDao = object : PlaceDao {
        override fun getAllPlaces(): Flow<List<Place>> = flowOf(emptyList())
        override suspend fun getPlaceById(id: Int): Place? = null
        override suspend fun getCurrentLocationPlace(): Place? = null
        override suspend fun insertPlace(place: Place): Long = 1L
        override suspend fun updatePlace(place: Place) {}
        override suspend fun deletePlace(place: Place) {}
        override suspend fun deleteCurrentLocationPlace() {}
    }

    @Before
    fun setUp() {
        repository = FloodRepository(fakeApi, fakeDao)
    }

    @Test
    fun `processForecastResponse correctly identifies peak discharge and date`() {
        val times = listOf("2026-11-16", "2026-11-17", "2026-11-18", "2026-11-19")
        val discharges = listOf(10.5, 14.2, 58.9, 32.1)
        val means = listOf(8.0, 8.5, 9.0, 9.2)

        val response = FloodForecastResponse(
            latitude = 50.9333,
            longitude = 6.9500,
            timezone = "UTC",
            daily = DailyFloodData(
                time = times,
                riverDischarge = discharges,
                riverDischargeMean = means
            )
        )

        val summary = repository.processForecastResponse("Rhine River", 50.9333, 6.9500, response)

        assertNotNull(summary)
        assertEquals("Rhine River", summary.placeName)
        assertEquals("2026-11-18", summary.peakDate)
        assertEquals(58.9, summary.peakDischarge!!, 0.001)
        assertEquals(4, summary.dailyItems.size)
    }
}
