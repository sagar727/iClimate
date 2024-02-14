package com.loopcreations.iclimate.repository

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.loopcreations.iclimate.climateDataModel.ClimateData
import com.loopcreations.iclimate.network.ApiClient
import com.loopcreations.iclimate.room.CityDatabase
import com.loopcreations.iclimate.room.CityEntity

class ClimateRepository(context: Context) : IClimate {

    private val db = CityDatabase.getDatabase(context)

    override suspend fun addCity(cityEntity: CityEntity) {
        db.cityDao().addCity(cityEntity)
    }

    override suspend fun deleteCity(cityName: String) {
        db.cityDao().deleteCity(cityName)
    }

    override suspend fun updateCity(cityName: String, isDefault: Boolean) {
        db.cityDao().updateCity(cityName, isDefault)
    }

    override suspend fun findCityCountByLocationName(name: String): Int {
        return db.cityDao().findCityCountByLocationName(name)
    }

    override suspend fun getCityCount(): Int {
        return db.cityDao().getCityCount()
    }

    override suspend fun getDefaultCity(): CityEntity? {
        return db.cityDao().getDefaultCity(true)
    }

    override suspend fun getFirstCity(): CityEntity {
        return db.cityDao().getFirstCity()
    }

    override suspend fun getCities(): List<CityEntity> {
        return db.cityDao().getCities()
    }

    override suspend fun getClimate(
        climateLiveData: MutableLiveData<ClimateData>,
        lat: Double,
        lng: Double,
        temp: String,
        wind: String,
        prep: String,
    ) {
        val currentParams: Array<String> = arrayOf(
            "temperature_2m",
            "relative_humidity_2m",
            "apparent_temperature",
            "is_day",
            "precipitation",
            "rain",
            "showers",
            "snowfall",
            "weather_code",
            "pressure_msl",
            "wind_speed_10m",
            "wind_gusts_10m"
        )
        val hourlyParams: Array<String> = arrayOf("temperature_2m", "weather_code")
        val dailyParams: Array<String> =
            arrayOf("weather_code", "temperature_2m_max", "temperature_2m_min")
        val timezone = "auto"
        val climateCall = ApiClient.apiService.getClimate(
            lat,
            lng,
            currentParams,
            hourlyParams,
            dailyParams,
            temp,
            wind,
            prep,
            timezone
        )

        try {
            if (climateCall.code() == 200) {
                climateLiveData.value = climateCall.body()
            }else{
                climateLiveData.value = null
            }
        }catch (_:Exception){
            climateLiveData.value = null
        }
    }

    suspend fun getClimateForAnnouncement(
        lat: Double,
        lng: Double,
        temp: String,
        wind: String,
        prep: String,
        isForAnnouncement: Boolean
    ): ArrayList<String> {
        val currentParams: Array<String> = arrayOf(
            "temperature_2m",
            "relative_humidity_2m",
            "apparent_temperature",
            "is_day",
            "precipitation",
            "rain",
            "showers",
            "snowfall",
            "weather_code",
            "pressure_msl",
            "wind_speed_10m",
            "wind_gusts_10m"
        )
        val hourlyParams: Array<String> = arrayOf("temperature_2m", "weather_code")
        val dailyParams: Array<String> =
            arrayOf("weather_code", "temperature_2m_max", "temperature_2m_min")
        val timezone = "auto"
        val climateCall = ApiClient.apiService.getClimate(
            lat,
            lng,
            currentParams,
            hourlyParams,
            dailyParams,
            temp,
            wind,
            prep,
            timezone
        )
        val climateData: ArrayList<String> = ArrayList()

        try {
            if (climateCall.code() == 200) {
                val data = climateCall.body()
                val currTemp = data?.current?.currentTemp
                val maxTemp = data?.daily?.dailyTempMax?.get(0)
                val minTemp = data?.daily?.dailyTempMin?.get(0)
                val climateCondition: String = when(if(isForAnnouncement) data?.daily?.dailyWCode?.get(0) else data?.current?.currentWCode){
                    0 ->{
                        "Clear Sky"
                    }
                    1 ->{
                        "Mainly clear"
                    }
                    2 ->{
                        "Partly cloudy"
                    }
                    3 ->{
                        "Overcast"
                    }
                    45 ->{
                        "Fog"
                    }
                    48 ->{
                        "Depositing rime fog"
                    }
                    51 ->{
                        "Drizzle: Light intensity"
                    }
                    53 ->{
                        "Drizzle: Moderate intensity"
                    }
                    55 ->{
                        "Drizzle: Dense intensity"
                    }
                    56 ->{
                        "Freezing Drizzle: Light intensity"
                    }
                    57 ->{
                        "Freezing Drizzle: Dense intensity"
                    }
                    61 ->{
                        "Rain: Slight intensity"
                    }
                    63 ->{
                        "Rain: Moderate intensity"
                    }
                    65 ->{
                        "Rain: Heavy intensity"
                    }
                    66 ->{
                        "Freezing Rain: Light intensity"
                    }
                    67 ->{
                        "Freezing Rain: Heavy intensity"
                    }
                    71 ->{
                        "Snow fall: Slight intensity"
                    }
                    73 ->{
                        "Snow fall: Moderate intensity"
                    }
                    75 ->{
                        "Snow fall: Heavy intensity"
                    }
                    77 ->{
                        "Snow grains"
                    }
                    80 ->{
                        "Rain showers: Slight"
                    }
                    81 ->{
                        "Rain showers: Moderate"
                    }
                    82 ->{
                        "Rain showers: Violent"
                    }
                    85 ->{
                        "Snow showers slight"
                    }
                    86 ->{
                        "Snow showers heavy"
                    }
                    95 ->{
                        "Thunderstorm: Slight or moderate"
                    }
                    96 ->{
                        "Thunderstorm with slight hail"
                    }
                    99 ->{
                        "Thunderstorm with heavy hail"
                    }
                    else -> {
                        ""
                    }
                }
                climateData.add(currTemp.toString())
                climateData.add(maxTemp.toString())
                climateData.add(minTemp.toString())
                climateData.add(climateCondition)
            }else{
                climateData.add("Sorry, Something went wrong!! Please try again later.")
            }
        }catch (_:Exception){
            climateData.add("Sorry, Something went wrong!! Please try again later.")
        }
        return climateData
    }
}

interface IClimate {
    suspend fun getClimate(
        climateLiveData: MutableLiveData<ClimateData>,
        lat: Double,
        lng: Double,
        temp: String,
        wind: String,
        prep: String,
    )

    suspend fun getCities(): List<CityEntity>

    suspend fun addCity(cityEntity: CityEntity)

    suspend fun deleteCity(cityName: String)

    suspend fun updateCity(cityName: String, isDefault: Boolean)

    suspend fun findCityCountByLocationName(name: String): Int

    suspend fun getDefaultCity(): CityEntity?

    suspend fun getFirstCity(): CityEntity

    suspend fun getCityCount(): Int
}
