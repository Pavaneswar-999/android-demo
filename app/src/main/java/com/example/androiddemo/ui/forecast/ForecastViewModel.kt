package com.example.androiddemo.ui.forecast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.androiddemo.data.api.NetworkClient
import com.example.androiddemo.data.local.AppDatabase
import com.example.androiddemo.data.local.Place
import com.example.androiddemo.data.model.FloodForecastSummary
import com.example.androiddemo.data.repository.FloodRepository
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
            val activePlace = places.firstOrNull() ?: Place(
                name = "Rhine at Cologne",
                latitude = 50.9333,
                longitude = 6.9500,
                isCurrentLocation = false
            )
            _currentPlace.value = activePlace
            fetchForecastForPlace(activePlace)
        }
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
