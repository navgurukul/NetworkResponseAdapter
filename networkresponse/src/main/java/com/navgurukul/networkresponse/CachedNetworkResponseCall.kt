package com.navgurukul.networkresponse

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

/**
 * A Call implementation that supports caching for NetworkResponse
 */
internal class CachedNetworkResponseCall<S : Any, E : Any>(
    private val backingCall: Call<S>,
    private val errorConverter: Converter<ResponseBody, E>,
    private val successBodyType: Type,
    private val cacheManager: CacheManager,
    private val cacheConfig: CacheConfig,
    private val cacheKey: String
) : Call<NetworkResponse<S, E>> {

    override fun enqueue(callback: Callback<NetworkResponse<S, E>>) {
        synchronized(this) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val result = executeWithCache(
                        cacheManager = cacheManager,
                        cacheKey = cacheKey,
                        type = successBodyType,
                        config = cacheConfig
                    ) {
                        // Execute the actual network call
                        val response = backingCall.execute()
                        ResponseHandler.handle(response, successBodyType, errorConverter)
                    }

                    callback.onResponse(this@CachedNetworkResponseCall, Response.success(result))
                } catch (throwable: Throwable) {
                    val networkResponse = throwable.extractNetworkResponse<S, E>(errorConverter)
                    callback.onResponse(this@CachedNetworkResponseCall, Response.success(networkResponse))
                }
            }
        }
    }

    override fun isExecuted(): Boolean = synchronized(this) {
        backingCall.isExecuted
    }

    override fun clone(): Call<NetworkResponse<S, E>> = CachedNetworkResponseCall(
        backingCall.clone(),
        errorConverter,
        successBodyType,
        cacheManager,
        cacheConfig,
        cacheKey
    )

    override fun isCanceled(): Boolean = synchronized(this) {
        backingCall.isCanceled
    }

    override fun cancel() = synchronized(this) {
        backingCall.cancel()
    }

    override fun execute(): Response<NetworkResponse<S, E>> {
        throw UnsupportedOperationException("Cached Network Response call does not support synchronous execution")
    }

    override fun request(): Request = backingCall.request()

    override fun timeout(): okio.Timeout = backingCall.timeout()
}