package com.example.weathercheck

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherMapApi {

    @GET("weather")
    fun getCoordinate(
        @Query("lat") latitude:String,
        @Query("lon") longitude:String,
        @Query("units") unit:String
    ):Call<OpenWeatherMapResponse>

    @GET("weather")
    fun getByCityName(
        @Query("q") cityName:String,
        @Query("units") unit:String
    ):Call<OpenWeatherMapResponse>
}