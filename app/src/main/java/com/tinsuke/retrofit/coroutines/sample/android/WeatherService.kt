package com.tinsuke.retrofit.coroutines.sample.android

import com.tinsuke.retrofit.coroutines.sample.android.model.WeatherData
import kotlinx.coroutines.experimental.Deferred
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherService {
    @GET("weather?")
    fun getWeatherDataCall(@Query("APPID") apiKey:String, @Query("q") city: String): Call<WeatherData>

    @GET("weather?")
    fun getWeatherDataCoroutine(@Query("APPID") apiKey:String, @Query("q") city: String): Deferred<WeatherData>

    @GET("weather?")
    fun getWeatherDataCoroutineResponse(@Query("APPID") apiKey:String, @Query("q") city: String): Deferred<Response<WeatherData>>
}