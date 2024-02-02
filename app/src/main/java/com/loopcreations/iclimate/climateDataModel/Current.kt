package com.loopcreations.iclimate.climateDataModel

import com.google.gson.annotations.SerializedName

data class Current(
    @SerializedName("apparent_temperature")
    val feelsTemp: Double,
    @SerializedName("interval")
    val interval: Int,
    @SerializedName("is_day")
    val isDay: Int,
    @SerializedName("precipitation")
    val precipitation: Double,
    @SerializedName("pressure_msl")
    val pressure: Double,
    @SerializedName("rain")
    val rain: Double,
    @SerializedName("relative_humidity_2m")
    val humidity: Int,
    @SerializedName("showers")
    val showers: Double,
    @SerializedName("snowfall")
    val snowfall: Double,
    @SerializedName("temperature_2m")
    val currentTemp: Double,
    @SerializedName("time")
    val time: String,
    @SerializedName("weather_code")
    val currentWCode: Int,
    @SerializedName("wind_gusts_10m")
    val gusts: Double,
    @SerializedName("wind_speed_10m")
    val wind: Double
)