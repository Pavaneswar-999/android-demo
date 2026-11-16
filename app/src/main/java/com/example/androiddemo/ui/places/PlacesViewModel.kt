package com.example.androiddemo.ui.places

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.androiddemo.data.api.NetworkClient
import com.example.androiddemo.data.local.AppDatabase
import com.example.androiddemo.data.local.Place
import com.example.androiddemo.data.repository.FloodRepository
import kotlinx.coroutines.launch

class PlacesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FloodRepository by lazy {
        val db = AppDatabase.getInstance(application)
        FloodRepository(NetworkClient.floodApi, db.placeDao())
    }

    val places: LiveData<List<Place>> = repository.getAllSavedPlaces().asLiveData()

    fun deletePlace(place: Place) {
        viewModelScope.launch {
            repository.deletePlace(place)
        }
    }
}
