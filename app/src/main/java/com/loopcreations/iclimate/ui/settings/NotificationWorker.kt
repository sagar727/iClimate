package com.loopcreations.iclimate.ui.settings

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.loopcreations.iclimate.R
import com.loopcreations.iclimate.network.NetworkManager
import com.loopcreations.iclimate.repository.ClimateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotificationWorker(val context: Context, params: WorkerParameters) : Worker(context, params) {
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

                    val condition = climateData[3]
                    val currentTemp = climateData[0] + if (tempUnit) " \u2109" else " \u2103"

                    val notification =
                        NotificationCompat.Builder(context, "forecast_channel")
                            .setSmallIcon(R.drawable.baseline_device_thermostat_24)
                            .setContentTitle("Climate Forecast")
                            .setContentText("$currentTemp\n$condition")
                            .build()

                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(201, notification)
                }
            }
        }
    }
}