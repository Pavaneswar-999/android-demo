package com.example.androiddemo

import com.example.androiddemo.data.api.OpenMeteoFloodApi
import com.example.androiddemo.data.local.Place
import com.example.androiddemo.data.local.PlaceDao
import com.example.androiddemo.data.model.FloodForecastResponse
import com.example.androiddemo.data.repository.FloodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlaceValidationTest {

    private lateinit var repository: FloodRepository
    private val savedPlaces = mutableListOf<Place>()

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
        override fun getAllPlaces(): Flow<List<Place>> = flowOf(savedPlaces)
        override suspend fun getPlaceById(id: Int): Place? = savedPlaces.find { it.id == id }
        override suspend fun getCurrentLocationPlace(): Place? = savedPlaces.find { it.isCurrentLocation }
        override suspend fun insertPlace(place: Place): Long {
            savedPlaces.add(place)
            return savedPlaces.size.toLong()
        }
        override suspend fun updatePlace(place: Place) {}
        override suspend fun deletePlace(place: Place) { savedPlaces.remove(place) }
        override suspend fun deleteCurrentLocationPlace() { savedPlaces.removeAll { it.isCurrentLocation } }
    }

    @Before
    fun setUp() {
        savedPlaces.clear()
        repository = FloodRepository(fakeApi, fakeDao)
    }

    @Test
    fun `savePlace succeeds with valid name and coordinates`() = runBlocking {
        val id = repository.savePlace("Cologne", 50.9333, 6.9500)
        assertTrue(id > 0)
        assertEquals(1, savedPlaces.size)
        assertEquals("Cologne", savedPlaces.first().name)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `savePlace throws exception when name is empty`(): Unit = runBlocking {
        repository.savePlace("   ", 50.9333, 6.9500)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `savePlace throws exception when latitude is out of bounds`(): Unit = runBlocking {
        repository.savePlace("Invalid Lat", 95.0, 6.9500)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `savePlace throws exception when longitude is out of bounds`(): Unit = runBlocking {
        repository.savePlace("Invalid Lon", 50.9333, -190.0)
    }
}
