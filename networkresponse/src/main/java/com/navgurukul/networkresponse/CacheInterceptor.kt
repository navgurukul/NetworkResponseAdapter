package com.navgurukul.networkresponse


import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

/**
 * OkHttp interceptor for HTTP-level caching
 */
class CacheInterceptor(
    private val maxAgeSeconds: Int = 300, // 5 minutes default
    private val maxStaleSeconds: Int = 3600 // 1 hour default
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Check if request has cache control annotation or header
        val cacheControl = CacheControl.Builder()
            .maxAge(maxAgeSeconds, TimeUnit.SECONDS)
            .maxStale(maxStaleSeconds, TimeUnit.SECONDS)
            .build()

        return response.newBuilder()
            .header("Cache-Control", cacheControl.toString())
            .build()
    }
}

/**
 * Offline cache interceptor - serves cached responses when offline
 */
class OfflineCacheInterceptor(
    private val networkChecker: () -> Boolean
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        if (!networkChecker()) {
            // If offline, force cache usage
            val cacheControl = CacheControl.Builder()
                .onlyIfCached()
                .maxStale(7, TimeUnit.DAYS) // Accept stale cache up to 7 days when offline
                .build()

            request = request.newBuilder()
                .cacheControl(cacheControl)
                .build()
        }

        return chain.proceed(request)
    }
}