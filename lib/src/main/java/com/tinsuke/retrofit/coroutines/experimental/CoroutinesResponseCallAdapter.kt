package com.tinsuke.retrofit.coroutines.experimental

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Response
import java.lang.reflect.Type
import kotlin.coroutines.experimental.CoroutineContext

internal class CoroutinesResponseCallAdapter(private val context: CoroutineContext,
                                             private val responseType: Type) : CallAdapter<Any, Deferred<Response<Any?>>> {

    override fun responseType() = responseType

    override fun adapt(call: Call<Any>) = async(context) {
        call.execute()
    }

}
