package com.loopcreations.iclimate.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "city")
data class CityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val locationName: String,
    val lat: Double,
    val lng: Double,
    var isDefault: Boolean
)
