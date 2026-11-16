package com.example.androiddemo.ui.forecast

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.androiddemo.data.api.NetworkClient
import com.example.androiddemo.data.local.AppDatabase
import com.example.androiddemo.data.local.Place
import com.example.androiddemo.data.model.FloodForecastSummary
import com.example.androiddemo.data.repository.FloodRepository
import com.example.androiddemo.location.LocationHelper
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

sealed class ForecastUiState {
    object Loading : ForecastUiState()
    data class Success(val summary: FloodForecastSummary) : ForecastUiState()
    data class Error(val message: String) : ForecastUiState()
}

class ForecastViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FloodRepository by lazy {
        val db = AppDatabase.getInstance(application)
        FloodRepository(NetworkClient.floodApi, db.placeDao())
    }

    private val locationHelper by lazy { LocationHelper(application) }

    private val _uiState = MutableLiveData<ForecastUiState>(ForecastUiState.Loading)
    val uiState: LiveData<ForecastUiState> = _uiState

    private val _currentPlace = MutableLiveData<Place>()
    val currentPlace: LiveData<Place> = _currentPlace

    init {
        loadDefaultOrFirstPlace()
    }

    fun loadDefaultOrFirstPlace() {
        viewModelScope.launch {
            val places = repository.getAllSavedPlaces().firstOrNull() ?: emptyList()
            if (places.isNotEmpty()) {
                val activePlace = places.first()
                _currentPlace.value = activePlace
                fetchForecastForPlace(activePlace)
            } else {
                val hasLocationPermission = ContextCompat.checkSelfPermission(
                    getApplication(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (hasLocationPermission) {
                    locationHelper.getCurrentLocation(
                        onSuccess = { loc ->
                            if (loc != null) {
                                viewModelScope.launch {
                                    val savedId = repository.savePlace(
                                        name = "My Location",
                                        lat = loc.latitude,
                                        lon = loc.longitude,
                                        isCurrent = true
                                    )
                                    val newPlace = Place(
                                        id = savedId.toInt(),
                                        name = "My Location",
                                        latitude = loc.latitude,
                                        longitude = loc.longitude,
                                        isCurrentLocation = true
                                    )
                                    _currentPlace.value = newPlace
                                    fetchForecastForPlace(newPlace)
                                }
                            } else {
                                fallbackToDefaultCologne()
                            }
                        },
                        onFailure = {
                            fallbackToDefaultCologne()
                        }
                    )
                } else {
                    fallbackToDefaultCologne()
                }
            }
        }
    }

    private fun fallbackToDefaultCologne() {
        val defaultPlace = Place(
            name = "Rhine at Cologne",
            latitude = 50.9333,
            longitude = 6.9500,
            isCurrentLocation = false
        )
        _currentPlace.value = defaultPlace
        fetchForecastForPlace(defaultPlace)
    }

    fun selectPlace(place: Place) {
        _currentPlace.value = place
        fetchForecastForPlace(place)
    }

    fun refreshCurrentForecast() {
        _currentPlace.value?.let { fetchForecastForPlace(it) } ?: loadDefaultOrFirstPlace()
    }

    private fun fetchForecastForPlace(place: Place) {
        viewModelScope.launch {
            _uiState.value = ForecastUiState.Loading
            val result = repository.getFloodForecast(
                place.name,
                place.latitude,
                place.longitude
            )
            result.fold(
                onSuccess = { summary ->
                    _uiState.value = ForecastUiState.Success(summary)
                },
                onFailure = { exception ->
                    val msg = exception.localizedMessage ?: "Failed to connect to Flood Forecast API."
                    _uiState.value = ForecastUiState.Error(msg)
                }
            )
        }
    }
}
