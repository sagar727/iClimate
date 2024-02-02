package com.loopcreations.iclimate.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CityDao {

    @Insert
    suspend fun addCity(cityEntity: CityEntity)

    @Query("DELETE FROM city WHERE locationName = :cityName ")
    suspend fun deleteCity(cityName: String)

    @Query("SELECT * FROM city")
    suspend fun getCities():List<CityEntity>

    @Query("SELECT Count(*) FROM city WHERE locationName = :cityName")
    suspend fun findCityCountByLocationName(cityName: String):Int

    @Query("SELECT * FROM city LIMIT 1")
    suspend fun getFirstCity():CityEntity

    @Query("SELECT COUNT(*) FROM city")
    suspend fun getCityCount(): Int

    @Query("SELECT * FROM city WHERE isDefault = :isDefault")
    suspend fun getDefaultCity(isDefault: Boolean): CityEntity?

    @Query("UPDATE city SET isDefault = :isDefault WHERE locationName = :cityName")
    suspend fun updateCity(cityName: String, isDefault: Boolean)
}