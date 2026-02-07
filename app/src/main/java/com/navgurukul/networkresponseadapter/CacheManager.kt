package com.navgurukul.networkresponseadapter


import android.content.Context
import androidx.room.Room
import com.aura.fct.data.data_source.local.AppDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages caching operations for network responses
 */
@Singleton
class CacheManager @Inject constructor(
    private val database: AppDatabase,
    private val gson: Gson
) {

    /**
     * Stores a successful network response in cache
     */
    suspend fun <T : Any> cacheResponse(
        key: String,
        response: NetworkResponse.Success<T>,
        maxAgeSeconds: Long
    ) = withContext(Dispatchers.IO) {
        try {
            val cacheEntry = CacheEntry(
                key = key,
                data = gson.toJson(response.body),
                headers = response.headers?.let { gson.toJson(headersToMap(it)) },
                code = response.code,
                timestamp = System.currentTimeMillis(),
                maxAgeSeconds = maxAgeSeconds
            )
            database.cacheDao().insertOrUpdate(cacheEntry)
        } catch (e: Exception) {
            // Log error but don't throw - caching failures shouldn't break the app
            e.printStackTrace()
        }
    }

    /**
     * Retrieves a cached response if available and valid
     */
    suspend fun <T : Any> getCachedResponse(
        key: String,
        type: Type,
        config: CacheConfig
    ): NetworkResponse.Success<T>? = withContext(Dispatchers.IO) {
        try {
            val cacheEntry = database.cacheDao().get(key) ?: return@withContext null

            if (!isCacheValid(cacheEntry, config)) {
                return@withContext null
            }

            val data = gson.fromJson<T>(cacheEntry.data, type)
            val headers = cacheEntry.headers?.let {
                mapToHeaders(gson.fromJson(it, object : TypeToken<Map<String, List<String>>>() {}.type))
            }

            NetworkResponse.Success(
                body = data,
                headers = headers,
                code = cacheEntry.code
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Checks if cached data is still valid based on cache configuration
     */
    private fun isCacheValid(cacheEntry: CacheEntry, config: CacheConfig): Boolean {
        val currentTime = System.currentTimeMillis()
        val age = (currentTime - cacheEntry.timestamp) / 1000 // age in seconds

        return when (config.strategy) {
            CacheStrategy.CACHE_ONLY -> true
            CacheStrategy.CACHE_WITH_EXPIRY -> age <= config.maxAgeSeconds
            CacheStrategy.CACHE_FIRST -> age <= config.staleWhileRevalidateSeconds
            else -> age <= config.maxAgeSeconds
        }
    }

    /**
     * Clears expired cache entries
     */
    suspend fun clearExpiredCache() = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            database.cacheDao().deleteExpired(currentTime)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Clears all cache entries
     */
    suspend fun clearAllCache() = withContext(Dispatchers.IO) {
        try {
            database.cacheDao().deleteAll()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Generates cache key from request URL and parameters
     */
    fun generateCacheKey(url: String, method: String = "GET", body: String? = null): String {
        val keyBuilder = StringBuilder()
        keyBuilder.append(method).append("_")
        keyBuilder.append(url.hashCode())
        body?.let { keyBuilder.append("_").append(it.hashCode()) }
        return keyBuilder.toString()
    }

    private fun headersToMap(headers: Headers): Map<String, List<String>> {
        val map = mutableMapOf<String, MutableList<String>>()
        for (i in 0 until headers.size) {
            val name = headers.name(i)
            val value = headers.value(i)
            map.getOrPut(name) { mutableListOf() }.add(value)
        }
        return map
    }

    private fun mapToHeaders(map: Map<String, List<String>>): Headers {
        val builder = Headers.Builder()
        map.forEach { (name, values) ->
            values.forEach { value ->
                builder.add(name, value)
            }
        }
        return builder.build()
    }
}