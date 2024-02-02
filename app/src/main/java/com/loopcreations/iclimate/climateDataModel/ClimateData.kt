package com.loopcreations.iclimate.climateDataModel

import com.google.gson.annotations.SerializedName

data class ClimateData (
    @SerializedName("current")
    val current: Current,
    @SerializedName("current_units")
    val currentUnits: CurrentUnits,
    @SerializedName("daily")
    val daily: Daily,
    @SerializedName("daily_units")
    val dailyUnits: DailyUnits,
    @SerializedName("elevation")
    val elevation: Int,
    @SerializedName("generationtime_ms")
    val generationTime: Double,
    @SerializedName("hourlyUnits")
    val hourlyUnits: HourlyUnits,
    @SerializedName("hourly")
    val hourly: Hourly,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("timezone")
    val timezone: String,
    @SerializedName("timezone_abbreviation")
    val timezoneAab: String,
    @SerializedName("utc_offset_seconds")
    val utcOffsetSeconds: Int
)