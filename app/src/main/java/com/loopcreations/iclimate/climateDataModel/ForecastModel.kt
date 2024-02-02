package com.loopcreations.iclimate.climateDataModel

data class ForecastModel(
    val date: String,
    val code: Int,
    val minTemp: Double,
    val maxTemp: Double
)
