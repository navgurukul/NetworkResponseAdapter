---
layout: default
title: Quick Start
---

# Quick Start Guide

## 1. Setup Retrofit

Configure Retrofit with the NetworkResponseAdapterFactory:

```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .addCallAdapterFactory(NetworkResponseAdapterFactory())
    .addConverterFactory(GsonConverterFactory.create())
    .build()
```

## 2. Define Your Models

```kotlin
// Success response model
data class User(
    val id: Int,
    val name: String,
    val email: String
)

// Error response model
data class ErrorResponse(
    val message: String,
    val code: Int
)
```

## 3. Create API Interface

```kotlin
interface ApiService {
    @GET("users")
    suspend fun getUsers(): NetworkResponse<List<User>, ErrorResponse>
    
    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Int): NetworkResponse<User, ErrorResponse>
    
    @POST("users")
    suspend fun createUser(@Body user: User): NetworkResponse<User, ErrorResponse>
}
```

## 4. Handle Responses

### Complete Error Handling

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
                val errorBody = response.body
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

### Simplified Error Handling

For cases where you don't need to distinguish between error types:

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

## 5. Use in ViewModel

```kotlin
class UserViewModel(private val repository: UserRepository) : ViewModel() {
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    fun loadUsers() {
        viewModelScope.launch {
            when (val response = repository.getUsers()) {
                is NetworkResponse.Success -> {
                    _users.value = response.body
                    _error.value = null
                }
                is NetworkResponse.Error -> {
                    _error.value = response.error.message
                }
            }
        }
    }
}
```

## Response Types

### NetworkResponse.Success
Contains the successful response body, headers, and status code.

```kotlin
data class Success<T : Any>(
    val body: T,
    val headers: Headers?,
    val code: Int
)
```

### NetworkResponse.ServerError
Contains the parsed error body from the server (4xx, 5xx responses).

```kotlin
data class ServerError<U : Any>(
    val body: U?,
    val code: Int,
    val headers: Headers?
)
```

### NetworkResponse.NetworkError
Represents network-level errors (no internet, timeout, etc.).

```kotlin
data class NetworkError(
    val error: IOException
)
```

### NetworkResponse.UnknownError
Catches any other unexpected errors.

```kotlin
data class UnknownError(
    val error: Throwable,
    val code: Int?,
    val headers: Headers?
)
```

## Extension Functions

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

## Next Steps

- [Advanced Features](advanced.html) - Learn about caching and retry mechanisms
- [API Reference](api.html) - Complete API documentation

[← Back to Home](index.html)
