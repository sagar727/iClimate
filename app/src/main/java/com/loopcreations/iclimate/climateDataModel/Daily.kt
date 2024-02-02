package com.loopcreations.iclimate.climateDataModel

import com.google.gson.annotations.SerializedName

data class Daily(
    @SerializedName("temperature_2m_max")
    val dailyTempMax: List<Double>,
    @SerializedName("temperature_2m_min")
    val dailyTempMin: List<Double>,
    @SerializedName("time")
    val time: List<String>,
    @SerializedName("weather_code")
    val dailyWCode: List<Int>
)