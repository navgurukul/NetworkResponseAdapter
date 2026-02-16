---
layout: default
title: API Reference
---

# API Reference

## NetworkResponse

Sealed class representing all possible network response states.

### NetworkResponse.Success

Represents a successful HTTP response (2xx status codes).

```kotlin
data class Success<T : Any>(
    val body: T,              // The response body
    val headers: Headers?,    // Response headers
    val code: Int             // HTTP status code
) : NetworkResponse<T, Nothing>()
```

**Properties:**
- `body: T` - The deserialized response body
- `headers: Headers?` - HTTP response headers
- `code: Int` - HTTP status code (200-299)

### NetworkResponse.ServerError

Represents an HTTP error response from the server (4xx, 5xx status codes).

```kotlin
data class ServerError<U : Any>(
    val body: U?,             // Parsed error body
    val code: Int,            // HTTP status code
    val headers: Headers?     // Response headers
) : NetworkResponse<Nothing, U>()
```

**Properties:**
- `body: U?` - The deserialized error body (null if parsing failed)
- `code: Int` - HTTP status code (400-599)
- `headers: Headers?` - HTTP response headers

### NetworkResponse.NetworkError

Represents a network-level error (no internet, timeout, DNS failure, etc.).

```kotlin
data class NetworkError(
    val error: IOException    // The network error
) : NetworkResponse<Nothing, Nothing>()
```

**Properties:**
- `error: IOException` - The underlying network exception

### NetworkResponse.UnknownError

Represents any other unexpected error (parsing errors, etc.).

```kotlin
data class UnknownError(
    val error: Throwable,     // The error
    val code: Int?,           // HTTP status code (if available)
    val headers: Headers?     // Response headers (if available)
) : NetworkResponse<Nothing, Nothing>()
```

**Properties:**
- `error: Throwable` - The underlying exception
- `code: Int?` - HTTP status code if available
- `headers: Headers?` - HTTP response headers if available

## NetworkResponseAdapterFactory

Factory for creating NetworkResponse call adapters.

```kotlin
class NetworkResponseAdapterFactory : CallAdapter.Factory()
```

**Usage:**

```kotlin
val retrofit = Retrofit.Builder()
    .addCallAdapterFactory(NetworkResponseAdapterFactory())
    .build()
```

## Cache Management

### CacheManager

Manages caching of network responses using Room database.

```kotlin
class CacheManager(
    private val database: AppDatabase,
    private val gson: Gson
)
```

**Constructor Parameters:**
- `database: AppDatabase` - Room database instance
- `gson: Gson` - Gson instance for serialization

**Methods:**

```kotlin
suspend fun <T> get(key: String, type: Type): T?
suspend fun <T> put(key: String, value: T, type: Type)
suspend fun invalidate(key: String)
suspend fun clear()
```

### CacheConfig

Configuration for cache behavior.

```kotlin
data class CacheConfig(
    val strategy: CacheStrategy = CacheStrategy.NETWORK_FIRST,
    val maxAgeSeconds: Long = 300,
    val forceRefresh: Boolean = false
)
```

**Properties:**
- `strategy: CacheStrategy` - The caching strategy to use
- `maxAgeSeconds: Long` - Maximum age of cached data in seconds
- `forceRefresh: Boolean` - Force network fetch even if cache is valid

### CacheStrategy

Enum defining cache strategies.

```kotlin
enum class CacheStrategy {
    NETWORK_FIRST,      // Try network first, fallback to cache
    CACHE_FIRST,        // Use cache if available, otherwise network
    NETWORK_ONLY,       // Always fetch from network
    CACHE_ONLY,         // Only use cache
    CACHE_WITH_EXPIRY   // Use cache if not expired
}
```

## Retry Functions

### executeWithRetry

Execute a network call with automatic retry on failure.

```kotlin
suspend fun <T : Any, U : Any> executeWithRetry(
    times: Int = 3,
    initialDelay: Long = 100,
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    shouldRetry: (NetworkResponse<T, U>) -> Boolean = { it is NetworkResponse.Error },
    block: suspend () -> NetworkResponse<T, U>
): NetworkResponse<T, U>
```

**Parameters:**
- `times: Int` - Maximum number of retry attempts
- `initialDelay: Long` - Initial delay in milliseconds
- `maxDelay: Long` - Maximum delay in milliseconds
- `factor: Double` - Multiplier for exponential backoff
- `shouldRetry: (NetworkResponse<T, U>) -> Boolean` - Predicate to determine if retry should occur
- `block: suspend () -> NetworkResponse<T, U>` - The network call to execute

**Returns:** `NetworkResponse<T, U>` - The final response after retries

### executeWithCache

Execute a network call with caching support.

```kotlin
suspend fun <T : Any, U : Any> executeWithCache(
    cacheManager: CacheManager,
    cacheKey: String,
    type: Type,
    config: CacheConfig = CacheConfig(),
    block: suspend () -> NetworkResponse<T, U>
): NetworkResponse<T, U>
```

**Parameters:**
- `cacheManager: CacheManager` - The cache manager instance
- `cacheKey: String` - Unique key for this cached data
- `type: Type` - Type token for deserialization
- `config: CacheConfig` - Cache configuration
- `block: suspend () -> NetworkResponse<T, U>` - The network call to execute

**Returns:** `NetworkResponse<T, U>` - The response from cache or network

### executeWithRetryAndCache

Execute a network call with both retry and caching support.

```kotlin
suspend fun <T : Any, U : Any> executeWithRetryAndCache(
    cacheManager: CacheManager,
    cacheKey: String,
    type: Type,
    config: CacheConfig = CacheConfig(),
    times: Int = 3,
    initialDelay: Long = 100,
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    block: suspend () -> NetworkResponse<T, U>
): NetworkResponse<T, U>
```

**Parameters:** Combination of `executeWithRetry` and `executeWithCache` parameters

**Returns:** `NetworkResponse<T, U>` - The final response

## Extension Functions

### invoke()

Operator function to extract the body or return null.

```kotlin
operator fun <T : Any, U : Any> NetworkResponse<T, U>.invoke(): T?
```

**Usage:**

```kotlin
val users: List<User>? = apiService.getUsers()()
```

### isFromCache()

Check if the response came from cache.

```kotlin
fun <T : Any, U : Any> NetworkResponse<T, U>.isFromCache(): Boolean
```

**Usage:**

```kotlin
if (response.isFromCache()) {
    println("Data from cache")
}
```

### onSuccess()

Execute a block if the response is successful.

```kotlin
inline fun <T : Any, U : Any> NetworkResponse<T, U>.onSuccess(
    block: (T) -> Unit
): NetworkResponse<T, U>
```

**Usage:**

```kotlin
response.onSuccess { users ->
    updateUI(users)
}
```

### onError()

Execute a block if the response is an error.

```kotlin
inline fun <T : Any, U : Any> NetworkResponse<T, U>.onError(
    block: (Throwable) -> Unit
): NetworkResponse<T, U>
```

**Usage:**

```kotlin
response.onError { error ->
    showError(error.message)
}
```

## OkHttp Interceptors

### CacheInterceptor

Adds cache-control headers to responses.

```kotlin
class CacheInterceptor(
    private val maxAgeSeconds: Int = 300
) : Interceptor
```

**Constructor Parameters:**
- `maxAgeSeconds: Int` - Maximum age for cached responses

### OfflineCacheInterceptor

Enables offline caching when network is unavailable.

```kotlin
class OfflineCacheInterceptor(
    private val isNetworkAvailable: () -> Boolean
) : Interceptor
```

**Constructor Parameters:**
- `isNetworkAvailable: () -> Boolean` - Function to check network availability

## Room Database

### AppDatabase

Room database for caching.

```kotlin
@Database(entities = [CacheEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}
```

### CacheEntry

Entity representing a cached response.

```kotlin
@Entity(tableName = "cache")
data class CacheEntry(
    @PrimaryKey val key: String,
    val data: String,
    val timestamp: Long,
    val headers: String?
)
```

### CacheDao

DAO for cache operations.

```kotlin
@Dao
interface CacheDao {
    @Query("SELECT * FROM cache WHERE key = :key")
    suspend fun get(key: String): CacheEntry?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CacheEntry)
    
    @Query("DELETE FROM cache WHERE key = :key")
    suspend fun delete(key: String)
    
    @Query("DELETE FROM cache")
    suspend fun clear()
}
```

## Type Aliases

Convenient type aliases for common patterns.

```kotlin
typealias GenericResponse<T> = NetworkResponse<T, ErrorResponse>
typealias EmptyResponse = NetworkResponse<Unit, ErrorResponse>
```

**Usage:**

```kotlin
interface ApiService {
    @GET("users")
    suspend fun getUsers(): GenericResponse<List<User>>
    
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): EmptyResponse
}
```

[← Back to Home](index.html)
