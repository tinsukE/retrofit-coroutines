package com.tinsuke.retrofit.coroutines.experimental

import retrofit2.Call
import retrofit2.Response

interface CoroutinesExecutor {
    fun execute(call: Call<Any>): Response<Any>
}