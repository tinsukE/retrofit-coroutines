package com.tinsuke.retrofit.coroutines.sample.android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.tinsuke.retrofit.coroutines.experimental.CoroutinesCallAdapterFactory
import com.tinsuke.retrofit.coroutines.experimental.CoroutinesInterceptor
import com.tinsuke.retrofit.coroutines.sample.android.model.WeatherData
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val callTextView = findViewById(R.id.call_text_view) as TextView
        val coroutineTextView = findViewById(R.id.coroutine_text_view) as TextView
        val coroutineResponseTextView = findViewById(R.id.coroutine_response_text_view) as TextView

        val networkPool = newFixedThreadPoolContext(5, "Network")

        val interceptor = object : CoroutinesInterceptor {
            override fun intercept(call: Call<Any>): Response<Any> {
                val response = call.execute()
                if (!response.isSuccessful) {
                    return response
                }

                val body = response.body()
                if (body is WeatherData) {
                    return Response.success(body.copy(name = body.name.toUpperCase()))
                }

                return response
            }
        }

        val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(CoroutinesCallAdapterFactory.create(networkPool, interceptor))
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://api.openweathermap.org/data/2.5/")
                .client(OkHttpClient.Builder()
                        .addInterceptor(HttpLoggingInterceptor()
                                .setLevel(HttpLoggingInterceptor.Level.BASIC))
                        .build()
                )
                .build()

        val service = retrofit.create(WeatherService::class.java)

        // The usual way
        service.getWeatherDataCall(getString(R.string.owm_api_key), "Amsterdam").enqueue(object : Callback<WeatherData> {
            override fun onResponse(call: Call<WeatherData>, response: Response<WeatherData>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    callTextView.text = "It works.\nWeather in ${data.name}: ${data.weather.first().description}"
                } else {
                    callTextView.text = "Oh no, a non-2XX HTTP response! $response"
                }
            }

            override fun onFailure(call: Call<WeatherData>, t: Throwable) {
                when (t) {
                    is IOException -> callTextView.text = "Oh no, a networking or conversion error! ${t.localizedMessage}"
                    else -> callTextView.text = "Oh no, an unknown error! ${t.localizedMessage}"
                }
            }
        })

        // Using coroutines
        launch(UI) {
            try {
                // This will already start the request in a NetworkPool thread
                val request = service.getWeatherDataCoroutine(getString(R.string.owm_api_key), "Amsterdam")
                // But execution only blocks on await()
                val data = request.await()
                // From now on we are in a CommonPool thread
                coroutineTextView.text = "It works via coroutines too!\nWeather in ${data.name}: ${data.weather.first().description}"
            } catch (exception: HttpException) {
                coroutineTextView.text = "Oh no, a non-2XX HTTP response! ${exception.localizedMessage}"
            } catch (exception: IOException) {
                coroutineTextView.text = "Oh no, a networking or conversion error! ${exception.localizedMessage}"
            } catch (exception: Throwable) {
                coroutineTextView.text = "Oh no, an unknown error! ${exception.localizedMessage}"
            }
        }

        // Using coroutines with a Response<> return type
        launch(UI) {
            try {
                // This will already start the request in a NetworkPool thread
                val request = service.getWeatherDataCoroutineResponse(getString(R.string.owm_api_key), "Amsterdam")
                // But execution only blocks on await()
                val response = request.await()
                // From now on we are in a CommonPool thread
                if (response.isSuccessful) {
                    val data = response.body()
                    coroutineResponseTextView.text = "It works via coroutines with response too!\nWeather in ${data.name}: ${data.weather.first().description}"
                } else {
                    coroutineResponseTextView.text = "Oh no, a non-2XX HTTP response! $response"
                }
            } catch (exception: IOException) {
                coroutineResponseTextView.text = "Oh no, a networking or conversion error! ${exception.localizedMessage}"
            } catch (exception: Throwable) {
                coroutineResponseTextView.text = "Oh no, an unknown error! ${exception.localizedMessage}"
            }
        }
    }
}
