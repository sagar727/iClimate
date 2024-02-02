package com.loopcreations.iclimate.network
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {
    private const val BaseUrl = "https://api.open-meteo.com/v1/"

    val retrofit by lazy { getRetrofitProvider() }

    private fun getRetrofitProvider(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BaseUrl)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
    }
}

object ApiClient {
    val apiService: ClimateService by lazy {
        RetrofitProvider.retrofit.create(ClimateService::class.java)
    }
}