# NetworkResponse Adapter 🚀

[![](https://jitpack.io/v/navgurukul/NetworkResponseAdapter.svg)](https://jitpack.io/#navgurukul/NetworkResponseAdapter)
[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)

A plug-and-play Retrofit CallAdapter for type-safe network response handling in Android. Clean error handling with sealed classes — no more messy try-catch blocks.

## Installation

Add JitPack to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency:

```kotlin
dependencies {
    implementation("com.github.navgurukul:NetworkResponseAdapter:1.0.0")
}
```

## Setup

Add the adapter factory to Retrofit:

```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .addCallAdapterFactory(NetworkResponseAdapterFactory())
    .addConverterFactory(GsonConverterFactory.create())
    .build()
```

## Usage

Define your API:

```kotlin
interface ApiService {
    @GET("users")
    suspend fun getUsers(): NetworkResponse<List<User>, ErrorResponse>
}
```

Handle responses:

```kotlin
when (val response = apiService.getUsers()) {
    is NetworkResponse.Success -> response.body       // Your data
    is NetworkResponse.ServerError -> response.code   // 4xx, 5xx
    is NetworkResponse.NetworkError -> response.error // No internet, timeout
    is NetworkResponse.UnknownError -> response.error // Parsing errors, etc.
}
```

Or simplified:

```kotlin
when (val response = apiService.getUsers()) {
    is NetworkResponse.Success -> handleData(response.body)
    is NetworkResponse.Error -> showError(response.error.message)
}
```

## Features

- 🎯 Type-safe error handling with sealed classes
- 💾 Built-in Room caching (Cache-First, Network-First, etc.)
- 🔄 Retry with exponential backoff
- ⚡ Kotlin Coroutines support
- 📦 Header preservation
- 🌐 Offline support

## Documentation

Full docs, advanced usage, caching strategies, and API reference:

👉 **[navgurukul.github.io/NetworkResponseAdapter](https://navgurukul.github.io/NetworkResponseAdapter/)**

## License

[AGPL-3.0](LICENSE)
