package com.example.androiddemo

import com.example.androiddemo.data.api.OpenMeteoFloodApi
import com.example.androiddemo.data.local.Place
import com.example.androiddemo.data.local.PlaceDao
import com.example.androiddemo.data.model.FloodForecastResponse
import com.example.androiddemo.data.model.RiskLevel
import com.example.androiddemo.data.repository.FloodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RiskClassificationTest {

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
    fun `calculateRiskLevel returns NORMAL when discharge is less than or equal to 1 point 5 times mean`() {
        val mean = 10.0
        val discharge = 15.0 // 1.5 * 10 = 15.0
        val risk = repository.calculateRiskLevel(discharge, mean)
        assertEquals(RiskLevel.NORMAL, risk)
    }

    @Test
    fun `calculateRiskLevel returns WATCH when discharge is between 1 point 5 and 2 point 0 times mean`() {
        val mean = 10.0
        val discharge = 18.0 // 1.5 * 10 < 18.0 <= 2.0 * 10
        val risk = repository.calculateRiskLevel(discharge, mean)
        assertEquals(RiskLevel.WATCH, risk)
    }

    @Test
    fun `calculateRiskLevel returns ALERT when discharge exceeds 2 point 0 times mean`() {
        val mean = 10.0
        val discharge = 25.0 // 25.0 > 2.0 * 10
        val risk = repository.calculateRiskLevel(discharge, mean)
        assertEquals(RiskLevel.ALERT, risk)
    }

    @Test
    fun `calculateRiskLevel handles zero mean safely`() {
        val mean = 0.0
        val dischargeZero = 0.0
        val dischargePositive = 5.0

        val riskNormal = repository.calculateRiskLevel(dischargeZero, mean)
        val riskAlert = repository.calculateRiskLevel(dischargePositive, mean)

        assertEquals(RiskLevel.NORMAL, riskNormal)
        assertEquals(RiskLevel.ALERT, riskAlert)
    }

    @Test
    fun `calculateRiskLevel handles negative or invalid inputs gracefully`() {
        val mean = -5.0
        val discharge = -10.0
        val risk = repository.calculateRiskLevel(discharge, mean)
        assertEquals(RiskLevel.NORMAL, risk)
    }
}
