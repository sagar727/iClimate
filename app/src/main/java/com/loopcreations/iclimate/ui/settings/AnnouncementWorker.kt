package com.loopcreations.iclimate.ui.settings

import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.loopcreations.iclimate.network.MyTtsService
import com.loopcreations.iclimate.network.NetworkManager
import com.loopcreations.iclimate.repository.ClimateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AnnouncementWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {

        performApiCall()
        return Result.success()
    }

    private fun performApiCall() {
        val repository = ClimateRepository(context)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val tempUnit = sharedPreferences.getBoolean("temperature_unit", false)
        val temp = if (tempUnit) "fahrenheit" else "celsius"
        val windUnit = sharedPreferences.getBoolean("wind_unit", false)
        val wind = if (windUnit) "mph" else "kmh"
        val precipitationUnit = sharedPreferences.getBoolean("precipitation_unit", false)
        val precipitation = if (precipitationUnit) "inch" else "mm"

        val networkManager = NetworkManager(context)
        if (networkManager.checkNetwork()) {
            GlobalScope.launch(Dispatchers.Default) {
                val defaultCityData = repository.getDefaultCity()
                if (defaultCityData != null) {
                    val climateData = repository.getClimateForAnnouncement(
                        defaultCityData.lat,
                        defaultCityData.lng,
                        temp,
                        wind,
                        precipitation
                    )

                    val currentTemp = climateData[0] + if (tempUnit) "Fahrenheit" else "Celsius"
                    val maxTemp = climateData[1] + if (tempUnit) "Fahrenheit" else "Celsius"
                    val minTemp = climateData[2] + if (tempUnit) "Fahrenheit" else "Celsius"
                    val condition = climateData[3]
                    val currentTempText = "Hi, Good Morning!!" +
                            "Today's forecast for ${defaultCityData.locationName} is," +
                            "current temperature is $currentTemp, " +
                            "maximum high will be $maxTemp and " +
                            "minimum low will be $minTemp. " +
                            "Climate condition will be $condition."

                    val ttsServiceIntent = Intent(context, MyTtsService::class.java)
                    ttsServiceIntent.putExtra("ttsString", currentTempText)
                    context.startForegroundService(ttsServiceIntent)
                }
            }
        }
    }
}