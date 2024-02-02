package com.loopcreations.iclimate.climateDataModel

import com.google.gson.annotations.SerializedName

data class DailyUnits(
    @SerializedName("temperature_2m_max")
    val tempMax: String,
    @SerializedName("temperature_2m_min")
    val tempMin: String,
    @SerializedName("time")
    val time: String,
    @SerializedName("weather_code")
    val wCode: String
)