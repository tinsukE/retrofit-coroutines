package com.tinsuke.retrofit.coroutines.experimental

import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import retrofit2.CallAdapter
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.coroutines.experimental.CoroutineContext

class CoroutinesCallAdapterFactory private constructor(private val context: CoroutineContext) : CallAdapter.Factory() {

    override fun get(returnType: Type?, annotations: Array<out Annotation>?, retrofit: Retrofit?): CallAdapter<*, *>? {
        val rawType = getRawType(returnType)
        if (rawType != Deferred::class.java) {
            return null
        }

        if (returnType !is ParameterizedType) {
            throw createInvalidReturnTypeException()
        }

        val deferredType = getParameterUpperBound(0, returnType)
        val rawDeferredType = getRawType(deferredType)

        if (rawDeferredType == Response::class.java) {
            if (deferredType !is ParameterizedType) {
                throw throw createInvalidReturnTypeException()
            }
            val responseType = getParameterUpperBound(0, deferredType)

            return CoroutinesResponseCallAdapter(context, responseType)
        } else {
            return CoroutinesCallAdapter(context, deferredType)
        }
    }

    private fun createInvalidReturnTypeException(): RuntimeException {
        return IllegalStateException("Return type must be parameterized as Deferred<Foo>, Deferred<out Foo>, " +
                "Deferred<Response<Foo>> or Deferred<Response<out Foo>>")
    }

    companion object {
        fun create(context: CoroutineContext = newFixedThreadPoolContext(5, "Network-Coroutines")): CoroutinesCallAdapterFactory {
            return CoroutinesCallAdapterFactory(context)
        }
    }

}