package com.navgurukul.networkresponse

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.Type

/**
 * Executes a network call with caching support based on the provided cache configuration
 */
suspend fun <T : Any, E : Any> executeWithCache(
    cacheManager: CacheManager,
    cacheKey: String,
    type: Type,
    config: CacheConfig,
    networkCall: suspend () -> NetworkResponse<T, E>
): NetworkResponse<T, E> = withContext(Dispatchers.IO) {
    when (config.strategy) {
        CacheStrategy.CACHE_FIRST -> {
            // Try cache first, fallback to network
            val cached = cacheManager.getCachedResponse<T>(cacheKey, type, config)
            if (cached != null) {
                return@withContext cached
            }
            val networkResponse = networkCall()
            if (networkResponse is NetworkResponse.Success) {
                cacheManager.cacheResponse(cacheKey, networkResponse, config.maxAgeSeconds)
            }
            networkResponse
        }

        CacheStrategy.NETWORK_FIRST -> {
            // Try network first, fallback to cache
            try {
                val networkResponse = networkCall()
                if (networkResponse is NetworkResponse.Success) {
                    cacheManager.cacheResponse(cacheKey, networkResponse, config.maxAgeSeconds)
                }
                networkResponse
            } catch (e: Exception) {
                cacheManager.getCachedResponse<T>(cacheKey, type, config)
                    ?: throw e
            }
        }

        CacheStrategy.NETWORK_ONLY -> {
            // Always fetch from network, update cache
            val networkResponse = networkCall()
            if (networkResponse is NetworkResponse.Success) {
                cacheManager.cacheResponse(cacheKey, networkResponse, config.maxAgeSeconds)
            }
            networkResponse
        }

        CacheStrategy.CACHE_ONLY -> {
            // Only use cache, never make network requests
            cacheManager.getCachedResponse<T>(cacheKey, type, config)
                ?: NetworkResponse.NetworkError(java.io.IOException("No cached data available"))
        }

        CacheStrategy.CACHE_WITH_EXPIRY -> {
            // Use cache if not expired, otherwise fetch from network
            val cached = cacheManager.getCachedResponse<T>(cacheKey, type, config)
            if (cached != null) {
                return@withContext cached
            }
            val networkResponse = networkCall()
            if (networkResponse is NetworkResponse.Success) {
                cacheManager.cacheResponse(cacheKey, networkResponse, config.maxAgeSeconds)
            }
            networkResponse
        }
    }
}

/**
 * Executes a network call with retry logic and exponential backoff
 */
suspend fun <T : Any, E : Any> executeWithRetry(
    times: Int = 3,
    initialDelay: Long = 100,
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    networkCall: suspend () -> NetworkResponse<T, E>
): NetworkResponse<T, E> {
    var currentDelay = initialDelay
    repeat(times - 1) { attempt ->
        val result = networkCall()
        if (result is NetworkResponse.Success) {
            return result
        }
        kotlinx.coroutines.delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return networkCall() // Last attempt
}

/**
 * Combines retry logic with caching
 */
suspend fun <T : Any, E : Any> executeWithRetryAndCache(
    cacheManager: CacheManager,
    cacheKey: String,
    type: Type,
    config: CacheConfig,
    times: Int = 3,
    initialDelay: Long = 100,
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    networkCall: suspend () -> NetworkResponse<T, E>
): NetworkResponse<T, E> {
    return executeWithCache(cacheManager, cacheKey, type, config) {
        executeWithRetry(times, initialDelay, maxDelay, factor, networkCall)
    }
}
