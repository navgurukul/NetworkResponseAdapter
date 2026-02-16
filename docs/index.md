---
layout: default
title: Home
---

# NetworkResponse Adapter 🚀

[![](https://jitpack.io/v/navgurukul/NetworkResponseAdapter.svg)](https://jitpack.io/#navgurukul/NetworkResponseAdapter)
[![License: AGPL v3](https://img.shields.io/badge/License-AGPL_v3-blue.svg)](https://www.gnu.org/licenses/agpl-3.0)

A powerful Retrofit CallAdapter for elegant and type-safe network response handling in Android applications.

## Why NetworkResponse Adapter?

Say goodbye to messy try-catch blocks and hello to clean, expressive error handling!

- 🎯 **Type-safe error handling** - Separate types for success and error responses
- 🔒 **Sealed class hierarchy** - Clean pattern matching with when expressions
- 💾 **Built-in caching** - Room-based caching with multiple strategies
- ⚡ **Coroutines support** - Async operations with Kotlin coroutines
- 🔄 **Retry mechanism** - Automatic retry with exponential backoff
- 🌐 **Offline support** - Graceful handling of network unavailability

## Quick Example

```kotlin
interface ApiService {
    @GET("users")
    suspend fun getUsers(): NetworkResponse<List<User>, ErrorResponse>
}

// Clean error handling
when (val response = apiService.getUsers()) {
    is NetworkResponse.Success -> {
        val users = response.body
        updateUI(users)
    }
    is NetworkResponse.ServerError -> {
        showError(response.body?.message)
    }
    is NetworkResponse.NetworkError -> {
        showOfflineMessage()
    }
    is NetworkResponse.UnknownError -> {
        logError(response.error)
    }
}
```

## Get Started

Check out the [Installation Guide](installation.html) to add NetworkResponse Adapter to your project.

## Documentation

- [Installation](installation.html)
- [Quick Start](quickstart.html)
- [Advanced Features](advanced.html)
- [API Reference](api.html)

## Support

- [GitHub Issues](https://github.com/navgurukul/NetworkResponseAdapter/issues)
- [Contributing Guide](https://github.com/navgurukul/NetworkResponseAdapter#contributing)

---

Made with ❤️ by [NavGurukul](https://github.com/navgurukul)
