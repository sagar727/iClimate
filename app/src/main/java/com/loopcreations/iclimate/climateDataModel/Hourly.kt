package com.loopcreations.iclimate.climateDataModel

import com.google.gson.annotations.SerializedName

data class Hourly(
    @SerializedName("time")
    val time: List<String>,
    @SerializedName("temperature_2m")
    val hourlyTemp: List<Double>,
    @SerializedName("weather_code")
    val hourlyWCode: List<Long>
)
