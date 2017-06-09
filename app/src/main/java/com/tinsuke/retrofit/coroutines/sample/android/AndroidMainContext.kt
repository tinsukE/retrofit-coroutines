package com.tinsuke.retrofit.coroutines.sample.android

import android.os.Handler
import android.os.Looper
import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.ContinuationInterceptor

/**
 * Android main thread context, executes everything on the main thread
 * Based on:
 * https://medium.com/@macastiblancot/android-coroutines-getting-rid-of-runonuithread-and-callbacks-cleaner-thread-handling-and-more-234c0a9bd8eb
 */
object AndroidMainContext : AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor {

    private val mainLooperHandler = Handler(Looper.getMainLooper())

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return MainThreadContinuation(mainLooperHandler, continuation)
    }

    private class MainThreadContinuation<T>(private val mainLooperHandler: Handler,
                                            private val continuation: Continuation<T>) : Continuation<T> by continuation {
        override fun resume(value: T) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                continuation.resume(value)
            } else {
                mainLooperHandler.post { continuation.resume(value) }
            }
        }

        override fun resumeWithException(exception: Throwable) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                continuation.resumeWithException(exception)
            } else {
                mainLooperHandler.post { continuation.resumeWithException(exception) }
            }
        }
    }
}
