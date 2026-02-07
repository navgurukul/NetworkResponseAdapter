package com.navgurukul.networkresponse


/**
 * Defines different caching strategies for network requests
 */
enum class CacheStrategy {
    /**
     * Always fetch from network first, fallback to cache on network error
     */
    NETWORK_FIRST,

    /**
     * Always fetch from cache first, fallback to network if cache miss
     */
    CACHE_FIRST,

    /**
     * Only use network, ignore cache completely
     */
    NETWORK_ONLY,

    /**
     * Only use cache, never make network requests
     */
    CACHE_ONLY,

    /**
     * Use cache if available and not expired, otherwise fetch from network
     */
    CACHE_WITH_EXPIRY
}

/**
 * Cache configuration for network requests
 */
data class CacheConfig(
    val strategy: CacheStrategy = CacheStrategy.NETWORK_FIRST,
    val maxAgeSeconds: Long = 300, // 5 minutes default
    val staleWhileRevalidateSeconds: Long = 3600, // 1 hour default
    val forceRefresh: Boolean = false
)

/**
 * Annotation to mark API methods with caching configuration
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cacheable(
    val strategy: CacheStrategy = CacheStrategy.NETWORK_FIRST,
    val maxAgeSeconds: Long = 300,
    val staleWhileRevalidateSeconds: Long = 3600,
    val key: String = ""
)