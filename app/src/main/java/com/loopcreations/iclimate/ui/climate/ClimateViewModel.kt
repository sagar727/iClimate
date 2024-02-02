package com.loopcreations.iclimate.ui.climate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.loopcreations.iclimate.climateDataModel.ClimateData
import com.loopcreations.iclimate.repository.IClimate
import com.loopcreations.iclimate.room.CityEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit

class ClimateViewModel : ViewModel() {

    val climateLiveData = MutableLiveData<ClimateData>()
    var cities = MutableLiveData<List<CityEntity>>()

    fun getWeather(repository: IClimate, lat:Double, lng:Double, temp: String, wind: String, prep: String){
        viewModelScope.launch(Dispatchers.Main) {
            repository.getClimate(climateLiveData,lat,lng,temp,wind,prep)
        }
    }

    fun addCity(repository: IClimate, cityEntity: CityEntity){
        viewModelScope.launch {
            repository.addCity(cityEntity)
        }
    }

    suspend fun getCityCount(repository: IClimate): Int {
        return repository.getCityCount()
    }

    suspend fun findCityCountByName(repository: IClimate, name: String): Int {
        return repository.findCityCountByLocationName(name)
    }

    suspend fun getDefaultCity(repository: IClimate): CityEntity? {
        return repository.getDefaultCity()
    }

    suspend fun getFirstCity(repository: IClimate): CityEntity{
        return repository.getFirstCity()
    }

    suspend fun updateCity(repository: IClimate, cityName: String, isDefault: Boolean){
        repository.updateCity(cityName, isDefault)
    }

    suspend fun deleteCity(repository: IClimate, cityName: String){
        repository.deleteCity(cityName)
    }

    fun getCities(repository: IClimate): MutableLiveData<List<CityEntity>>{
        viewModelScope.launch(Dispatchers.Main) {
            cities.value = repository.getCities()
        }
        return cities
    }
}