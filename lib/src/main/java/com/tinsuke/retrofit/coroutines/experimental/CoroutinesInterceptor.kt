package com.tinsuke.retrofit.coroutines.experimental

import retrofit2.Call
import retrofit2.Response

interface CoroutinesInterceptor {
    fun intercept(call: Call<Any>): Response<Any>
}