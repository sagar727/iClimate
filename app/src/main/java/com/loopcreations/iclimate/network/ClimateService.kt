package com.loopcreations.iclimate.network

import com.loopcreations.iclimate.climateDataModel.ClimateData
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ClimateService {
    @GET("forecast?")

    suspend fun getClimate(
        @Query("latitude")
        lat: Double,
        @Query("longitude")
        lng: Double,
        @Query("current")
        currentParams: Array<String>,
        @Query("hourly")
        hourlyParams: Array<String>,
        @Query("daily")
        dailyParams: Array<String>,
        @Query("temperature_unit")
        tempUnit: String,
        @Query("wind_speed_unit")
        windUnit: String,
        @Query("precipitation_unit")
        prepUnit: String,
        @Query("timezone")
        timeZone: String
    ): Response<ClimateData>
}