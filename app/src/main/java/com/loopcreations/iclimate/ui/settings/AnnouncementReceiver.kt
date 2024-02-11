package com.loopcreations.iclimate.ui.settings

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import com.loopcreations.iclimate.network.MyTtsService
import com.loopcreations.iclimate.network.NetworkManager
import com.loopcreations.iclimate.repository.ClimateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AnnouncementReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        scheduleNext(context)
        performApiCall(context)
    }

    private fun performApiCall(context: Context) {
        val networkManager = NetworkManager(context)
        if (networkManager.checkNetwork()) {
            val repository = ClimateRepository(context)
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val tempUnit = sharedPreferences.getBoolean("temperature_unit", false)
            val temp = if (tempUnit) "fahrenheit" else "celsius"
            val windUnit = sharedPreferences.getBoolean("wind_unit", false)
            val wind = if (windUnit) "mph" else "kmh"
            val precipitationUnit = sharedPreferences.getBoolean("precipitation_unit", false)
            val precipitation = if (precipitationUnit) "inch" else "mm"
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

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleNext(context: Context) {
        val startIntent = Intent(context, AnnouncementReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            100,
            startIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val intervalMillis = 24 * 60 * 60 * 1000 //24 hours
        val nextAlarmTime = System.currentTimeMillis() + intervalMillis
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextAlarmTime,
            pendingIntent
        )
    }
}