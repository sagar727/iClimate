package com.loopcreations.iclimate.climateDataModel

import com.google.gson.annotations.SerializedName

data class CurrentUnits(
    @SerializedName("apparent_temperature")
    val apparentTemp: String,
    @SerializedName("interval")
    val interval: String,
    @SerializedName("is_day")
    val isDay: String,
    @SerializedName("precipitation")
    val precipitation: String,
    @SerializedName("pressure_msl")
    val pressure: String,
    @SerializedName("rain")
    val rain: String,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity: String,
    @SerializedName("showers")
    val showers: String,
    @SerializedName("snowfall")
    val snowfall: String,
    @SerializedName("temperature_2m")
    val temp: String,
    @SerializedName("time")
    val time: String,
    @SerializedName("weather_code")
    val wCode: String,
    @SerializedName("wind_gusts_10m")
    val windGusts: String,
    @SerializedName("wind_speed_10m")
    val windSpeed: String
)