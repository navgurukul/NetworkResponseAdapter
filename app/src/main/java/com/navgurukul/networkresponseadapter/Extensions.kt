package com.navgurukul.networkresponseadapter

import kotlinx.coroutines.delay
import java.lang.reflect.Type

/**
 * Retries the given [block] for the specified number of times in the case of [NetworkResponse.NetworkError]
 *
 * @param T The success body type of [NetworkResponse]
 * @param U The error body type of [NetworkResponse]
 * @param times The number of times this request should be retried
 * @param initialDelay The initial amount of time to wait before retrying
 * @param maxDelay The max amount of time to wait before retrying
 * @param factor Multiply current delay time with this on each retry
 * @param block The suspending function to be retried
 * @return The NetworkResponse value whether it be successful or failed after retrying
 */
suspend inline fun <T : Any, U : Any> executeWithRetry(
    times: Int = 10,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000, // 1 second
    factor: Double = 2.0,
    block: suspend () -> NetworkResponse<T, U>
): NetworkResponse<T, U> {
    var currentDelay = initialDelay
    repeat(times - 1) {
        when (val response = block()) {
            is NetworkResponse.NetworkError -> {
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
            }
            else -> return response
        }
    }
    return block() // last attempt
}
fun <String> removeDuplicateString(list: ArrayList<String>): ArrayList<String> {

    val newList = ArrayList<String>()

    for (element in list) {
        if (!newList.contains(element)) {
            newList.add(element)
        }
    }
    return newList
}

/**
 * Executes a network request with caching support
 *
 * @param T The success body type of [NetworkResponse]
 * @param U The error body type of [NetworkResponse]
 * @param cacheManager The cache manager instance
 * @param cacheKey The cache key for this request
 * @param type The type of the response body for deserialization
 * @param config Cache configuration
 * @param networkCall The network call to execute
 * @return The NetworkResponse with caching applied
 */
suspend inline fun <T : Any, U : Any> executeWithCache(
    cacheManager: CacheManager,
    cacheKey: String,
    type: Type,
    config: CacheConfig = CacheConfig(),
    crossinline networkCall: suspend () -> NetworkResponse<T, U>
): NetworkResponse<T, U> {
    return when (config.strategy) {
        CacheStrategy.CACHE_ONLY -> {
            cacheManager.getCachedResponse<T>(cacheKey, type, config)
                ?: NetworkResponse.NetworkError(java.io.IOException("No cached data available"))
        }

        CacheStrategy.NETWORK_ONLY -> {
            val response = networkCall()
            if (response is NetworkResponse.Success) {
                cacheManager.cacheResponse(cacheKey, response, config.maxAgeSeconds)
            }
            response
        }

        CacheStrategy.CACHE_FIRST -> {
            val cachedResponse = cacheManager.getCachedResponse<T>(cacheKey, type, config)
            if (cachedResponse != null) {
                cachedResponse
            } else {
                val networkResponse = networkCall()
                if (networkResponse is NetworkResponse.Success) {
                    cacheManager.cacheResponse(cacheKey, networkResponse, config.maxAgeSeconds)
                }
                networkResponse
            }
        }

        CacheStrategy.NETWORK_FIRST -> {
            try {
                val networkResponse = networkCall()
                if (networkResponse is NetworkResponse.Success) {
                    cacheManager.cacheResponse(cacheKey, networkResponse, config.maxAgeSeconds)
                }
                networkResponse
            } catch (e: Exception) {
                // Fallback to cache on network error
                cacheManager.getCachedResponse<T>(cacheKey, type, config)
                    ?: NetworkResponse.NetworkError(java.io.IOException("Network failed and no cache available", e))
            }
        }

        CacheStrategy.CACHE_WITH_EXPIRY -> {
            if (config.forceRefresh) {
                val networkResponse = networkCall()
                if (networkResponse is NetworkResponse.Success) {
                    cacheManager.cacheResponse(cacheKey, networkResponse, config.maxAgeSeconds)
                }
                networkResponse
            } else {
                val cachedResponse = cacheManager.getCachedResponse<T>(cacheKey, type, config)
                if (cachedResponse != null) {
                    cachedResponse
                } else {
                    val networkResponse = networkCall()
                    if (networkResponse is NetworkResponse.Success) {
                        cacheManager.cacheResponse(cacheKey, networkResponse, config.maxAgeSeconds)
                    }
                    networkResponse
                }
            }
        }
    }
}

/**
 * Combines retry logic with caching
 */
suspend inline fun <T : Any, U : Any> executeWithRetryAndCache(
    cacheManager: CacheManager,
    cacheKey: String,
    type: Type,
    config: CacheConfig = CacheConfig(),
    times: Int = 3,
    initialDelay: Long = 100,
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    crossinline networkCall: suspend () -> NetworkResponse<T, U>
): NetworkResponse<T, U> {
    return executeWithCache(
        cacheManager = cacheManager,
        cacheKey = cacheKey,
        type = type,
        config = config
    ) {
        executeWithRetry(times, initialDelay, maxDelay, factor) {
            networkCall()
        }
    }
}

/**
 * Overloaded invoke operator to get the successful body or null in NetworkResponse class
 *
 * @param T the success body type of [NetworkResponse]
 * @param U the error body type of [NetworkResponse]
 *
 * Example:
 * val usersResponse = executeWithRetry { getUsers() }
 *
 * println(usersResponse() ?: "No users found")
 */
operator fun <T : Any, U : Any> NetworkResponse<T, U>.invoke(): T? {
    return if (this is NetworkResponse.Success) body else null
}

/**
 * Extension to check if response is from cache
 */
fun <T : Any, U : Any> NetworkResponse<T, U>.isFromCache(): Boolean {
    return this is NetworkResponse.Success && this.headers?.get("X-From-Cache") == "true"
}

/**
 * Extension to mark response as from cache
 */
fun <T : Any> NetworkResponse.Success<T>.markAsFromCache(): NetworkResponse.Success<T> {
    val newHeaders = this.headers?.newBuilder()?.add("X-From-Cache", "true")?.build()
        ?: okhttp3.Headers.Builder().add("X-From-Cache", "true").build()
    return this.copy(headers = newHeaders)
}