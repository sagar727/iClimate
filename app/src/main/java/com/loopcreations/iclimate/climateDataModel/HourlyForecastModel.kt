package com.loopcreations.iclimate.climateDataModel

data class HourlyForecastModel(
    val date: String,
    val code: Long,
    val temp: Double
)
