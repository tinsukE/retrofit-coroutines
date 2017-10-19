package com.tinsuke.retrofit.coroutines.experimental

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.HttpException
import java.lang.reflect.Type
import kotlin.coroutines.experimental.CoroutineContext

internal class CoroutinesCallAdapter(private val context: CoroutineContext,
                                     private val responseType: Type,
                                     private val executor: CoroutinesExecutor) : CallAdapter<Any, Deferred<Any>> {

    override fun responseType() = responseType

    override fun adapt(call: Call<Any>): Deferred<Any> {
        return async(context) {
            val response = executor.execute(call)
            if (response.isSuccessful) {
                response.body()
            } else {
                throw HttpException(response)
            }
        }.apply {
            invokeOnCompletion {
                if (isCancelled) {
                    call.cancel()
                }
            }
        }
    }

}
