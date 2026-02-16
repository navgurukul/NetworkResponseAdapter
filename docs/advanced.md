---
layout: default
title: Advanced Features
---

# Advanced Features

## Caching

Implement intelligent caching strategies to improve performance and offline support.

### Setup Cache Manager

```kotlin
// Create Room database
val database = Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "cache-db"
).build()

// Initialize cache manager
val cacheManager = CacheManager(database, Gson())
```

### Use Caching

```kotlin
suspend fun getUsersWithCache(): NetworkResponse<List<User>, ErrorResponse> {
    return executeWithCache(
        cacheManager = cacheManager,
        cacheKey = "users_list",
        type = object : TypeToken<List<User>>() {}.type,
        config = CacheConfig(
            strategy = CacheStrategy.CACHE_FIRST,
            maxAgeSeconds = 300 // 5 minutes
        )
    ) {
        apiService.getUsers()
    }
}
```

### Cache Strategies

#### NETWORK_FIRST
Try network first, fallback to cache on error. Best for data that changes frequently.

```kotlin
CacheConfig(strategy = CacheStrategy.NETWORK_FIRST)
```

#### CACHE_FIRST
Use cache if available, otherwise fetch from network. Best for relatively static data.

```kotlin
CacheConfig(strategy = CacheStrategy.CACHE_FIRST)
```

#### NETWORK_ONLY
Always fetch from network, update cache. Best for real-time data.

```kotlin
CacheConfig(strategy = CacheStrategy.NETWORK_ONLY)
```

#### CACHE_ONLY
Only use cache, never make network requests. Best for offline mode.

```kotlin
CacheConfig(strategy = CacheStrategy.CACHE_ONLY)
```

#### CACHE_WITH_EXPIRY
Use cache if not expired, otherwise fetch from network. Best for time-sensitive data.

```kotlin
CacheConfig(
    strategy = CacheStrategy.CACHE_WITH_EXPIRY,
    maxAgeSeconds = 300
)
```

## Retry Mechanism

Automatically retry failed requests with exponential backoff.

### Basic Retry

```kotlin
suspend fun getUsersWithRetry(): NetworkResponse<List<User>, ErrorResponse> {
    return executeWithRetry(
        times = 3,              // Retry up to 3 times
        initialDelay = 100,     // Start with 100ms delay
        maxDelay = 1000,        // Max 1 second delay
        factor = 2.0            // Double the delay each time
    ) {
        apiService.getUsers()
    }
}
```

### Retry with Custom Logic

```kotlin
suspend fun getUsersWithConditionalRetry(): NetworkResponse<List<User>, ErrorResponse> {
    return executeWithRetry(
        times = 3,
        shouldRetry = { response ->
            // Only retry on network errors or 5xx server errors
            when (response) {
                is NetworkResponse.NetworkError -> true
                is NetworkResponse.ServerError -> response.code >= 500
                else -> false
            }
        }
    ) {
        apiService.getUsers()
    }
}
```

## Combining Retry and Cache

Get the best of both worlds - resilient network calls with caching:

```kotlin
suspend fun getUsersResilient(): NetworkResponse<List<User>, ErrorResponse> {
    return executeWithRetryAndCache(
        cacheManager = cacheManager,
        cacheKey = "users_list",
        type = object : TypeToken<List<User>>() {}.type,
        config = CacheConfig(strategy = CacheStrategy.CACHE_FIRST),
        times = 3,
        initialDelay = 100
    ) {
        apiService.getUsers()
    }
}
```

## OkHttp Interceptors

Add caching at the HTTP level for more control.

### Cache Interceptor

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(CacheInterceptor(maxAgeSeconds = 300))
    .cache(Cache(context.cacheDir, 10 * 1024 * 1024)) // 10 MB
    .build()
```

### Offline Cache Interceptor

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(OfflineCacheInterceptor { 
        isNetworkAvailable() 
    })
    .cache(Cache(context.cacheDir, 10 * 1024 * 1024))
    .build()
```

### Complete Setup

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(CacheInterceptor(maxAgeSeconds = 300))
    .addInterceptor(OfflineCacheInterceptor { isNetworkAvailable() })
    .cache(Cache(context.cacheDir, 10 * 1024 * 1024))
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

val retrofit = Retrofit.Builder()
    .client(okHttpClient)
    .baseUrl("https://api.example.com/")
    .addCallAdapterFactory(NetworkResponseAdapterFactory())
    .addConverterFactory(GsonConverterFactory.create())
    .build()
```

## Best Practices

### 1. Repository Pattern

Encapsulate network logic in repositories:

```kotlin
class UserRepository(
    private val apiService: ApiService,
    private val cacheManager: CacheManager
) {
    suspend fun getUsers(forceRefresh: Boolean = false): Result<List<User>> {
        val config = CacheConfig(
            strategy = if (forceRefresh) {
                CacheStrategy.NETWORK_ONLY
            } else {
                CacheStrategy.CACHE_FIRST
            },
            maxAgeSeconds = 300
        )
        
        return when (val response = executeWithCache(
            cacheManager, 
            "users", 
            object : TypeToken<List<User>>() {}.type, 
            config
        ) { 
            apiService.getUsers() 
        }) {
            is NetworkResponse.Success -> Result.success(response.body)
            is NetworkResponse.Error -> Result.failure(response.error)
        }
    }
}
```

### 2. UI State Management

Use sealed classes for UI states:

```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class UserViewModel(private val repository: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<User>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<User>>> = _uiState
    
    fun loadUsers(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = when (val response = repository.getUsers(forceRefresh)) {
                is NetworkResponse.Success -> UiState.Success(response.body)
                is NetworkResponse.Error -> UiState.Error(
                    response.error.message ?: "Unknown error"
                )
            }
        }
    }
}
```

### 3. Custom Error Types

Define domain-specific error types:

```kotlin
data class ApiError(
    val message: String,
    val errorCode: String,
    val timestamp: Long,
    val details: Map<String, Any>? = null
)

// Use in API interface
interface ApiService {
    @GET("users")
    suspend fun getUsers(): NetworkResponse<List<User>, ApiError>
}
```

### 4. Testing

Mock responses for testing:

```kotlin
class FakeApiService : ApiService {
    override suspend fun getUsers(): NetworkResponse<List<User>, ErrorResponse> {
        return NetworkResponse.Success(
            body = listOf(User(1, "Test User", "test@example.com")),
            code = 200,
            headers = null
        )
    }
}
```

## Performance Tips

1. Use appropriate cache strategies based on data volatility
2. Set reasonable cache expiry times
3. Implement retry only for idempotent operations
4. Use OkHttp's connection pooling for better performance
5. Consider using `CACHE_FIRST` for static data
6. Monitor cache size and implement cleanup strategies

## Next Steps

- [API Reference](api.html) - Complete API documentation
- [GitHub Repository](https://github.com/navgurukul/NetworkResponseAdapter) - Source code and examples

[← Back to Home](index.html)
