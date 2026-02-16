---
layout: default
title: Installation
---

# Installation

## Requirements

- Android API 21+
- Kotlin 1.9+
- Retrofit 2.9+
- OkHttp 4.12+
- Coroutines 1.7+

## Step 1: Add JitPack Repository

Add JitPack to your root `build.gradle` or `settings.gradle.kts`:

### Groovy (build.gradle)

```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

### Kotlin DSL (settings.gradle.kts)

```kotlin
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}
```

## Step 2: Add the Dependency

### Groovy

```gradle
dependencies {
    implementation 'com.github.navgurukul:NetworkResponseAdapter:1.0.0'
}
```

### Kotlin DSL

```kotlin
dependencies {
    implementation("com.github.navgurukul:NetworkResponseAdapter:1.0.0")
}
```

## Optional Dependencies

### For Caching Support

```kotlin
dependencies {
    // Room for caching
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Gson for serialization
    implementation("com.google.code.gson:gson:2.10.1")
}
```

## Verify Installation

After syncing your project, verify the installation by creating a simple Retrofit instance:

```kotlin
val retrofit = Retrofit.Builder()
    .baseUrl("https://api.example.com/")
    .addCallAdapterFactory(NetworkResponseAdapterFactory())
    .addConverterFactory(GsonConverterFactory.create())
    .build()
```

## Next Steps

- [Quick Start Guide](quickstart.html) - Learn the basics
- [Advanced Features](advanced.html) - Explore caching and retry mechanisms

[← Back to Home](index.html)
