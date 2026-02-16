# NetworkResponse Adapter 🚀

[![](https://jitpack.io/v/navgurukul/NetworkResponseAdapter.svg)](https://jitpack.io/#navgurukul/NetworkResponseAdapter)
[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)

A powerful Retrofit CallAdapter for elegant and type-safe network response handling in Android applications. Say goodbye to messy try-catch blocks and hello to clean, expressive error handling!

## ✨ Features

- 🎯 **Type-safe error handling** - Separate types for success and error responses
- 🔒 **Sealed class hierarchy** - Clean pattern matching with when expressions
- 💾 **Built-in caching** - Room-based caching with multiple strategies (Cache-First, Network-First, etc.)
- ⚡ **Coroutines support** - Async operations with Kotlin coroutines
- 🔄 **Retry mechanism** - Automatic retry with exponential backoff
- 📦 **Header preservation** - Maintains response headers through the wrapper
- 🌐 **Offline support** - Graceful handling of network unavailability
- 🎨 **Clean API** - Intuitive and easy to use

## 📦 Installation

### Step 1: Add JitPack repository

Add JitPack to your root `build.gradle` or `settings.gradle.kts`:

**Groovy (build.gradle):**
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

**Kotlin DSL (settings.gradle.kts):**
```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Step 2: Add the dependency

**Groovy:**
```gradle
dependencies {
    implementation 'com.github.navgurukul:NetworkResponseAdapter:1.0.0'
}
```

**Kotlin DSL:**
```kotlin
dependencies {
    implementation("com.github.navgurukul:NetworkResponseAdapter:1.0.0")
}
```

## 🚀 Quick Start

### 1. Setup Retrofit

```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .addCallAdapterFactory(NetworkResponseAdapterFactory())
    .addConverterFactory(GsonConverterFactory.create())
    .build()
```

### 2. Define API Interface

```kotlin
data class User(val id: Int, val name: String, val email: String)
data class ErrorResponse(val message: String, val code: Int)

interface ApiService {
    @GET("users")
    suspend fun getUsers(): NetworkResponse<List<User>, ErrorResponse>
    
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): NetworkResponse<User, ErrorResponse>
    
    @POST("users")
    suspend fun createUser(@Body user: User): NetworkResponse<User, ErrorResponse>
}
```

### 3. Handle Responses

The beauty of `NetworkResponse` is in its clean, expressive error handling:

```kotlin
class UserRepository(private val apiService: ApiService) {
    
    suspend fun loadUsers(): Result<List<User>> {
        return when (val response = apiService.getUsers()) {
            is NetworkResponse.Success -> {
                // ✅ Success - you have the data!
                val users = response.body
                val statusCode = response.code
                val headers = response.headers
                Result.success(users)
            }
            
            is NetworkResponse.ServerError -> {
                // ❌ Server returned an error (4xx, 5xx)
                val errorBody = response.body // Your custom error type
                val statusCode = response.code
                Log.e("API", "Server error: ${errorBody?.message}")
                Result.failure(Exception("Server error: $statusCode"))
            }
            
            is NetworkResponse.NetworkError -> {
                // 🌐 Network error (no internet, timeout, etc.)
                Log.e("API", "Network error: ${response.error.message}")
                Result.failure(response.error)
            }
            
            is NetworkResponse.UnknownError -> {
                // ⚠️ Unknown error (parsing error, etc.)
                Log.e("API", "Unknown error: ${response.error.message}")
                Result.failure(response.error)
            }
        }
    }
}
```

### 4. Simplified Error Handling

Use the `Error` interface for cases where you don't need to distinguish error types:

```kotlin
when (val response = apiService.getUsers()) {
    is NetworkResponse.Success -> {
        // Handle success
        updateUI(response.body)
    }
    is NetworkResponse.Error -> {
        // Handle any error
        showError(response.error.message)
    }
}
```

## 🎯 Advanced Features

### Caching

Implement intelligent caching strategies to improve performance and offline support:

```kotlin
// Setup cache manager
val database = Room.databaseBuilder(context, AppDatabase::class.java, "cache-db").build()
val cacheManager = CacheManager(database, Gson())

// Use caching with your API calls
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

#### Cache Strategies

- **`NETWORK_FIRST`** - Try network first, fallback to cache on error
- **`CACHE_FIRST`** - Use cache if available, otherwise fetch from network
- **`NETWORK_ONLY`** - Always fetch from network, update cache
- **`CACHE_ONLY`** - Only use cache, never make network requests
- **`CACHE_WITH_EXPIRY`** - Use cache if not expired, otherwise fetch from network

### Retry with Exponential Backoff

Automatically retry failed requests with configurable backoff:

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

### Combining Retry and Cache

Get the best of both worlds:

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

### OkHttp Interceptors

Add caching at the HTTP level:

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(CacheInterceptor(maxAgeSeconds = 300))
    .addInterceptor(OfflineCacheInterceptor { isNetworkAvailable() })
    .cache(Cache(context.cacheDir, 10 * 1024 * 1024)) // 10 MB
    .build()

val retrofit = Retrofit.Builder()
    .client(okHttpClient)
    .addCallAdapterFactory(NetworkResponseAdapterFactory())
    .build()
```

## 📚 Response Types

### NetworkResponse.Success
```kotlin
data class Success<T : Any>(
    val body: T,              // The response body
    val headers: Headers?,    // Response headers
    val code: Int             // HTTP status code
)
```

### NetworkResponse.ServerError
```kotlin
data class ServerError<U : Any>(
    val body: U?,             // Parsed error body
    val code: Int,            // HTTP status code
    val headers: Headers?     // Response headers
)
```

### NetworkResponse.NetworkError
```kotlin
data class NetworkError(
    val error: IOException    // The network error
)
```

### NetworkResponse.UnknownError
```kotlin
data class UnknownError(
    val error: Throwable,     // The error
    val code: Int?,           // HTTP status code (if available)
    val headers: Headers?     // Response headers (if available)
)
```


## 🛠️ Extension Functions

### Invoke Operator
Get the body or null:
```kotlin
val users: List<User>? = apiService.getUsers()()
```

### Check if from Cache
```kotlin
if (response.isFromCache()) {
    println("This data is from cache")
}
```

## 💡 Best Practices

### 1. Define Custom Error Types
```kotlin
data class ApiError(
    val message: String,
    val errorCode: String,
    val timestamp: Long
)
```

### 2. Use Repository Pattern
```kotlin
class UserRepository(
    private val apiService: ApiService,
    private val cacheManager: CacheManager
) {
    suspend fun getUsers(forceRefresh: Boolean = false): Result<List<User>> {
        val config = CacheConfig(
            strategy = if (forceRefresh) CacheStrategy.NETWORK_ONLY 
                      else CacheStrategy.CACHE_FIRST,
            maxAgeSeconds = 300
        )
        
        return when (val response = executeWithCache(
            cacheManager, "users", 
            object : TypeToken<List<User>>() {}.type, 
            config
        ) { apiService.getUsers() }) {
            is NetworkResponse.Success -> Result.success(response.body)
            is NetworkResponse.Error -> Result.failure(response.error)
        }
    }
}
```

### 3. Handle Loading States
```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

class UserViewModel(private val repository: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<User>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<User>>> = _uiState
    
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _uiState.value = when (val response = repository.getUsers()) {
                is NetworkResponse.Success -> UiState.Success(response.body)
                is NetworkResponse.Error -> UiState.Error(response.error.message ?: "Unknown error")
            }
        }
    }
}
```

## 🔧 Requirements

- Android API 21+
- Kotlin 1.9+
- Retrofit 2.9+
- OkHttp 4.12+
- Coroutines 1.7+

## 📖 Documentation

For more detailed documentation, check out:
- [Publishing Guide](PUBLISHING.md) - How to publish updates
- [Build Status](BUILD_STATUS.md) - Build configuration details

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the AGPL-3.0 License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Inspired by the need for cleaner error handling in Android networking
- Built with ❤️ by [NavGurukul](https://github.com/navgurukul)

## 📞 Support

If you have any questions or run into issues, please open an issue on GitHub.

---

**Made with ❤️ for the Android community**
