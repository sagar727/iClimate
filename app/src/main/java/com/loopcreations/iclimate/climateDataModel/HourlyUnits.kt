package com.loopcreations.iclimate.climateDataModel

import com.google.gson.annotations.SerializedName

data class HourlyUnits(
    @SerializedName("time")
    val time: String,
    @SerializedName("temperature_2m")
    val temp: String,
    @SerializedName("weather_code")
    val wCode: String
)
