package com.example.androiddemo.ui.places

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.androiddemo.data.api.NetworkClient
import com.example.androiddemo.data.local.AppDatabase
import com.example.androiddemo.data.repository.FloodRepository
import kotlinx.coroutines.launch

class AddPlaceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FloodRepository by lazy {
        val db = AppDatabase.getInstance(application)
        FloodRepository(NetworkClient.floodApi, db.placeDao())
    }

    private val _saveResult = MutableLiveData<Result<Long>>()
    val saveResult: LiveData<Result<Long>> = _saveResult

    fun savePlace(name: String, latStr: String, lonStr: String, isCurrentLocation: Boolean = false) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) {
            _saveResult.value = Result.failure(IllegalArgumentException("Place name cannot be empty"))
            return
        }

        val lat = latStr.toDoubleOrNull()
        if (lat == null || lat < -90.0 || lat > 90.0) {
            _saveResult.value = Result.failure(IllegalArgumentException("Latitude must be a valid number between -90 and 90"))
            return
        }

        val lon = lonStr.toDoubleOrNull()
        if (lon == null || lon < -180.0 || lon > 180.0) {
            _saveResult.value = Result.failure(IllegalArgumentException("Longitude must be a valid number between -180 and 180"))
            return
        }

        viewModelScope.launch {
            try {
                val id = repository.savePlace(cleanName, lat, lon, isCurrentLocation)
                _saveResult.value = Result.success(id)
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            }
        }
    }
}
