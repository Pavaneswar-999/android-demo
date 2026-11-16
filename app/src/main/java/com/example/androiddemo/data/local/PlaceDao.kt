package com.example.androiddemo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Query("SELECT * FROM places ORDER BY isCurrentLocation DESC, id ASC")
    fun getAllPlaces(): Flow<List<Place>>

    @Query("SELECT * FROM places WHERE id = :id LIMIT 1")
    suspend fun getPlaceById(id: Int): Place?

    @Query("SELECT * FROM places WHERE isCurrentLocation = 1 LIMIT 1")
    suspend fun getCurrentLocationPlace(): Place?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: Place): Long

    @Update
    suspend fun updatePlace(place: Place)

    @Delete
    suspend fun deletePlace(place: Place)

    @Query("DELETE FROM places WHERE isCurrentLocation = 1")
    suspend fun deleteCurrentLocationPlace()
}
